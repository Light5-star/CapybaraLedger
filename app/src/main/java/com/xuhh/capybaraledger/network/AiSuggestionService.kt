package com.xuhh.capybaraledger.network

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import com.xuhh.capybaraledger.data.dao.BillWithCategory
import com.xuhh.capybaraledger.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * AI建议服务，用于生成个性化财务建议
 */
class AiSuggestionService {
    companion object {
        private const val TAG = "AiSuggestionService"
        private const val API_URL = "https://hunyuan.tencentcloudapi.com"
        
        // 这些密钥应该从更安全的地方获取，比如服务器端
        private const val SECRET_ID = "Your SECRET ID"
        private const val SECRET_KEY = "Your SECRET KEY"

        // 时间同步相关常量
        private const val TIME_SYNC_INTERVAL = 5 * 60 * 1000L // 5分钟同步一次
        private const val TIME_OFFSET_THRESHOLD = 30L // 30秒时间差阈值
        private const val TIME_BUFFER = 60L // 60秒缓冲时间
        
        // 时间同步状态
        private var lastSyncTime = 0L
        private var timeOffset = 0L
        private var serverBaseTime = 0L // 初始值为0，将从实际响应中获取
        
        // AI建议缓存相关
        private var cachedAdvice: String? = null
        private var lastAdviceTime: Long = 0
        private var lastLedgerId: String? = null
        private var lastYear: Int = 0
        private var lastMonth: Int = 0
        private var lastBillsHash: Int = 0
    }

    /**
     * 获取AI建议
     * @param bills 账单列表
     * @param categories 分类列表
     * @param ledgerId 账本ID
     * @param year 年份
     * @param month 月份
     * @param forceRefresh 是否强制刷新
     * @return AI生成的财务建议
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getFinancialAdvice(
        bills: List<BillWithCategory>,
        monthlyIncome: Double,
        monthlyExpense: Double,
        topCategories: List<Pair<Category, Double>>,
        ledgerId: String,
        year: Int,
        month: Int,
        forceRefresh: Boolean = false
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // 计算当前账单数据的哈希值
                val currentBillsHash = bills.hashCode()
                
                // 检查是否需要刷新
                if (!forceRefresh && shouldUseCache(ledgerId, year, month, currentBillsHash)) {
                    Log.d(TAG, "Using cached advice")
                    return@withContext cachedAdvice ?: "正在获取AI建议..."
                }
                
                Log.d(TAG, "Refreshing AI advice for ledger: $ledgerId, year: $year, month: $month")
                
                // 准备请求数据
                val requestData = prepareRequestData(bills, monthlyIncome, monthlyExpense, topCategories)
                
                // 发送请求到腾讯混元大模型API
                val response = sendRequest(requestData)
                
                // 解析响应数据
                val advice = parseResponse(response)
                
                // 更新缓存
                updateAdviceCache(advice, ledgerId, year, month, currentBillsHash)
                
                advice
            } catch (e: Exception) {
                Log.e(TAG, "Error getting financial advice", e)
                
                // 如果有缓存，在出错时返回缓存
                if (cachedAdvice != null) {
                    Log.d(TAG, "Returning cached advice due to error")
                    return@withContext cachedAdvice + "\n\n(注意: 获取最新建议时发生错误，显示的是缓存内容)"
                }
                
                "获取AI建议时发生错误: ${e.message}"
            }
        }
    }
    
    /**
     * 检查是否应该使用缓存的建议
     */
    private fun shouldUseCache(ledgerId: String, year: Int, month: Int, currentBillsHash: Int): Boolean {
        // 如果没有缓存，需要刷新
        if (cachedAdvice == null) {
            Log.d(TAG, "No cache available, refresh needed")
            return false
        }
        
        // 如果账本ID变化，需要刷新
        if (lastLedgerId != ledgerId) {
            Log.d(TAG, "Ledger changed from $lastLedgerId to $ledgerId, refresh needed")
            return false
        }
        
        // 如果年份或月份变化，需要刷新
        if (lastYear != year || lastMonth != month) {
            Log.d(TAG, "Date changed from $lastYear-$lastMonth to $year-$month, refresh needed")
            return false
        }
        
        // 如果账单数据变化，需要刷新
        if (lastBillsHash != currentBillsHash) {
            Log.d(TAG, "Bills data changed, refresh needed")
            return false
        }
        
        Log.d(TAG, "No changes detected, using cache")
        return true
    }
    
    /**
     * 更新建议缓存
     */
    private fun updateAdviceCache(advice: String, ledgerId: String, year: Int, month: Int, billsHash: Int) {
        cachedAdvice = advice
        lastAdviceTime = System.currentTimeMillis()
        lastLedgerId = ledgerId
        lastYear = year
        lastMonth = month
        lastBillsHash = billsHash
        Log.d(TAG, "Updated advice cache for ledger: $ledgerId, year: $year, month: $month")
    }

    /**
     * 准备请求数据
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareRequestData(
        bills: List<BillWithCategory>,
        monthlyIncome: Double,
        monthlyExpense: Double,
        topCategories: List<Pair<Category, Double>>
    ): String {
        // 获取当月日期范围
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        
        // 格式化账单数据
        val billsSummary = formatBillsData(bills)
        
        // 格式化顶级消费类别
        val topCategoriesText = topCategories.mapIndexed { index, (category, amount) ->
            "${index + 1}. ${category.name}: ¥${String.format("%.2f", amount)}元"
        }.joinToString("\n")
        
        // 生成提示词
        val prompt = """
        作为一名专业的财务顾问，请根据以下用户的财务数据，提供个性化的理财建议：

        当前日期：${year}年${month}月
        月收入：¥${String.format("%.2f", monthlyIncome)}
        月支出：¥${String.format("%.2f", monthlyExpense)}
        月结余：¥${String.format("%.2f", monthlyIncome - monthlyExpense)}
        
        消费前五类别：
        $topCategoriesText
        
        账单摘要：
        $billsSummary
        
        请基于上述信息，提供以下方面的建议：
        1. 支出分析：分析用户的消费模式和习惯
        2. 节约建议：针对高支出类别，提供3-5条具体的省钱建议
        3. 收入提升：如果收入低于支出，提供增加收入的可行性建议
        4. 储蓄规划：提供合理的储蓄目标
        
        请使用简洁、友好的语言，给出实用的建议，不超过300字。
        """.trimIndent()

        // 构建消息数组
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("Role", "user")
                put("Content", prompt)
            })
        }

        // 构建完整请求体 - 按照API文档规范构建
        return JSONObject().apply {
            put("Model", "hunyuan-lite")
            put("Messages", messages)
            put("TopP", 0.8)
            put("Temperature", 0.6)
            put("Stream", false)
        }.toString()
    }

    /**
     * 格式化账单数据
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatBillsData(bills: List<BillWithCategory>): String {
        if (bills.isEmpty()) return "无账单数据"

        // 按日期分组
        val billsByDate = bills.groupBy { bill ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = bill.bill.date
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            calendar.time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        }

        // 生成每天的消费摘要
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return billsByDate.entries.sortedByDescending { it.key }.take(10).joinToString("\n") { (date, dailyBills) ->
            val dailyExpense = dailyBills.filter { it.bill.type == 0 }.sumOf { it.bill.amount }
            val dailyIncome = dailyBills.filter { it.bill.type == 1 }.sumOf { it.bill.amount }
            val categorySummary = dailyBills.groupBy { it.category.name }
                .map { (category, categoryBills) -> 
                    val sum = categoryBills.sumOf { it.bill.amount }
                    "$category: ¥${String.format("%.2f", sum)}"
                }
                .joinToString(", ")
            
            "$date - 支出:¥${String.format("%.2f", dailyExpense)}, 收入:¥${String.format("%.2f", dailyIncome)}\n主要类别: $categorySummary"
        }
    }

    /**
     * 获取与服务器同步的时间戳
     */
    private fun getServerSyncedTimestamp(): Long {
        val currentTime = System.currentTimeMillis() / 1000
        
        // 检查是否需要更新时间同步
        if (shouldUpdateTimeSync()) {
            updateTimeSync(currentTime)
        }
        
        // 计算最终时间戳
        val finalTimestamp = currentTime + timeOffset + TIME_BUFFER
        
        // 调试信息
        Log.d(TAG, "Local timestamp: $currentTime")
        Log.d(TAG, "Time offset: $timeOffset")
        Log.d(TAG, "Final timestamp: $finalTimestamp")
        
        return finalTimestamp
    }
    
    /**
     * 检查是否需要更新时间同步
     */
    private fun shouldUpdateTimeSync(): Boolean {
        val now = System.currentTimeMillis()
        return now - lastSyncTime > TIME_SYNC_INTERVAL || timeOffset == 0L || serverBaseTime == 0L
    }
    
    /**
     * 更新时间同步状态
     */
    private fun updateTimeSync(currentTime: Long) {
        // 如果没有服务器时间基准值，使用当前时间+预估偏移量
        if (serverBaseTime == 0L) {
            // 首次调用时，我们没有服务器时间信息，使用当前时间并设置一个较小的偏移量
            // 这样即使有些不准确，也会通过错误响应自动调整
            timeOffset = 60L // 初始假设服务器时间比本地快60秒
            Log.d(TAG, "No server time available yet, using estimated offset: $timeOffset")
        } else {
            // 计算时间偏移量
            timeOffset = serverBaseTime - currentTime
            
            Log.d(TAG, "Using server base time: $serverBaseTime, calculated offset: $timeOffset")
        }
        
        // 更新最后同步时间
        lastSyncTime = System.currentTimeMillis()
    }

    /**
     * 发送请求到腾讯混元大模型API
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendRequest(requestData: String): String {
        val url = URL(API_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        
        // 设置请求头部
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.setRequestProperty("Host", "hunyuan.tencentcloudapi.com")
        
        // 使用与服务器同步的时间戳
        val timestamp = getServerSyncedTimestamp()
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(timestamp * 1000)
        
        Log.d(TAG, "Using timestamp: $timestamp for API request")
        
        // 按照腾讯云API文档要求，所有公共参数放在请求头中
        connection.setRequestProperty("X-TC-Action", "ChatCompletions")
        connection.setRequestProperty("X-TC-Timestamp", timestamp.toString())
        connection.setRequestProperty("X-TC-Version", "2023-09-01")
        connection.setRequestProperty("X-TC-Region", "ap-guangzhou")
        
        // 计算签名
        val auth = calculateSignatureV3(requestData, timestamp, date)
        connection.setRequestProperty("Authorization", auth)
        
        connection.doOutput = true
        connection.doInput = true
        
        // 发送请求
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestData)
            writer.flush()
        }
        
        // 获取响应
        val responseCode = connection.responseCode
        return if (responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val errorStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            Log.e(TAG, "API request failed with code $responseCode: $errorStream")
            // 尝试从错误响应中提取服务器时间
            extractServerTimeFromError(errorStream)
            
            throw Exception("API request failed with code $responseCode: $errorStream")
        }
    }

    /**
     * 使用TC3-HMAC-SHA256算法计算签名 - 按照API文档规范实现
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateSignatureV3(requestData: String, timestamp: Long, date: String): String {
        val algorithm = "TC3-HMAC-SHA256"
        val service = "hunyuan"
        val host = "hunyuan.tencentcloudapi.com"
        
        // 步骤1：创建规范请求字符串
        val httpRequestMethod = "POST"
        val canonicalUri = "/"
        val canonicalQueryString = ""
        val canonicalHeaders = "content-type:application/json; charset=utf-8\n" +
                              "host:$host\n" +
                              "x-tc-action:chatcompletions\n"
        val signedHeaders = "content-type;host;x-tc-action"
        val hashedRequestPayload = sha256Hex(requestData)
        val canonicalRequest = "$httpRequestMethod\n$canonicalUri\n$canonicalQueryString\n$canonicalHeaders\n$signedHeaders\n$hashedRequestPayload"
        
        // 步骤2：创建待签名字符串
        val credentialScope = "$date/$service/tc3_request"
        val hashedCanonicalRequest = sha256Hex(canonicalRequest)
        val stringToSign = "$algorithm\n$timestamp\n$credentialScope\n$hashedCanonicalRequest"
        
        // 步骤3：计算签名
        val secretDate = hmacSha256(("TC3$SECRET_KEY").toByteArray(), date)
        val secretService = hmacSha256(secretDate, service)
        val secretSigning = hmacSha256(secretService, "tc3_request")
        val signature = bytesToHex(hmacSha256(secretSigning, stringToSign))
        
        // 步骤4：构造Authorization
        return "$algorithm Credential=$SECRET_ID/$credentialScope, SignedHeaders=$signedHeaders, Signature=$signature"
    }

    /**
     * 解析API响应
     */
    private fun parseResponse(response: String): String {
        try {
            Log.d(TAG, "Raw API response: $response")
            
            // 先尝试直接作为JSON对象解析
            val jsonObject = JSONObject(response)
            
            // 检查是否有错误信息
            if (jsonObject.has("Response") && jsonObject.getJSONObject("Response").has("Error")) {
                val error = jsonObject.getJSONObject("Response").getJSONObject("Error")
                val errorCode = error.optString("Code", "Unknown")
                val errorMessage = error.optString("Message", "未知错误")
                Log.e(TAG, "API error: $errorCode - $errorMessage")
                
                // 尝试从错误消息中提取服务器时间
                extractServerTimeFromError(errorMessage)
                
                return "AI服务返回错误: $errorMessage (错误码: $errorCode)"
            }
            
            // 如果有Choices字段，获取第一个选择的内容
            if (jsonObject.has("Response") && jsonObject.getJSONObject("Response").has("Choices")) {
                val choices = jsonObject.getJSONObject("Response").getJSONArray("Choices")
                if (choices.length() > 0) {
                    val choice = choices.getJSONObject(0)
                    if (choice.has("Message") && choice.getJSONObject("Message").has("Content")) {
                        val content = choice.getJSONObject("Message").getString("Content")
                        // 处理Markdown格式
                        return formatMarkdownContent(content)
                    }
                }
            }
            
            return "未能从AI响应中提取有效内容，请查看日志了解详情"
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing API response", e)
            return "解析API响应时出错: ${e.message}\n原始响应: $response"
        }
    }

    /**
     * 从错误消息中提取服务器时间
     */
    private fun extractServerTimeFromError(errorMessage: String?) {
        if (errorMessage.isNullOrEmpty()) return
        
        try {
            // 正则表达式匹配服务器时间
            val timePattern = "server time (\\d+)".toRegex()
            val matchResult = timePattern.find(errorMessage)
            
            if (matchResult != null && matchResult.groupValues.size > 1) {
                val newServerTime = matchResult.groupValues[1].toLongOrNull()
                if (newServerTime != null && newServerTime > 0) {
                    Log.d(TAG, "Extracted new server time from error: $newServerTime")
                    serverBaseTime = newServerTime
                    
                    // 立即更新时间偏移
                    val currentTime = System.currentTimeMillis() / 1000
                    timeOffset = serverBaseTime - currentTime
                    lastSyncTime = System.currentTimeMillis()
                    
                    Log.d(TAG, "Updated time offset to $timeOffset based on error message")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting server time from error message", e)
        }
    }

    /**
     * 格式化Markdown内容，使其在应用中正确显示
     */
    private fun formatMarkdownContent(content: String): String {
        // 1. 处理标题 - 使用更友好的格式
        var formattedContent = content.replace(Regex("^#+\\s+(.+)$", RegexOption.MULTILINE)) { matchResult ->
            val level = matchResult.value.indexOf(" ")
            val text = matchResult.groupValues[1]
            when (level) {
                1 -> "\n【$text】\n" // 一级标题使用【】包裹
                2 -> "\n■ $text\n"  // 二级标题使用■标记
                3 -> "\n● $text\n"  // 三级标题使用●标记
                else -> "\n• $text\n" // 其他级别使用•标记
            }
        }

        // 2. 处理列表项
        formattedContent = formattedContent.replace(Regex("^[*-]\\s+(.+)$", RegexOption.MULTILINE)) { matchResult ->
            val text = matchResult.groupValues[1]
            "• $text"
        }

        // 3. 处理数字列表
        formattedContent = formattedContent.replace(Regex("^\\d+\\.\\s+(.+)$", RegexOption.MULTILINE)) { matchResult ->
            val text = matchResult.groupValues[1]
            "• $text"
        }

        // 4. 处理加粗和斜体
        formattedContent = formattedContent.replace("**", "")
            .replace("*", "")
            .replace("__", "")
            .replace("_", "")

        // 5. 处理代码块
        formattedContent = formattedContent.replace(Regex("```[\\s\\S]*?```")) { matchResult ->
            val code = matchResult.value.replace("```", "").trim()
            "\n$code\n"
        }

        // 6. 处理行内代码
        formattedContent = formattedContent.replace(Regex("`([^`]+)`")) { matchResult ->
            matchResult.groupValues[1]
        }

        // 7. 处理引用
        formattedContent = formattedContent.replace(Regex("^>\\s+(.+)$", RegexOption.MULTILINE)) { matchResult ->
            val text = matchResult.groupValues[1]
            "「$text」"
        }

        // 8. 处理水平分割线
        formattedContent = formattedContent.replace(Regex("^[-*_]{3,}$", RegexOption.MULTILINE), "\n---\n")

        // 9. 处理链接
        formattedContent = formattedContent.replace(Regex("\\[(.+?)\\]\\((.+?)\\)")) { matchResult ->
            matchResult.groupValues[1]
        }

        // 10. 处理图片
        formattedContent = formattedContent.replace(Regex("!\\[(.+?)\\]\\((.+?)\\)")) { matchResult ->
            "[图片]"
        }

        // 11. 处理表格（简化处理，只保留内容）
        formattedContent = formattedContent.replace(Regex("\\|[^\\n]+\\|")) { matchResult ->
            matchResult.value.replace("|", " ").trim()
        }

        // 12. 处理换行，确保段落之间有适当的间距
        formattedContent = formattedContent.replace("\n\n+", "\n\n")
            .trim()

        return formattedContent
    }

    /**
     * 计算SHA256哈希
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sha256Hex(data: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return bytesToHex(hash)
    }

    /**
     * 计算HMAC-SHA256
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray())
    }

    /**
     * 字节数组转十六进制字符串
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val i = byte.toInt() and 0xff
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }
    
    /**
     * API响应数据类
     */
    data class ChatCompletionResponse(
        @SerializedName("Response") val response: Response?
    ) {
        data class Response(
            @SerializedName("Error") val error: Error?,
            @SerializedName("RequestId") val requestId: String?,
            @SerializedName("Choices") val choices: List<Choice>?
        ) {
            data class Error(
                @SerializedName("Code") val code: String?,
                @SerializedName("Message") val message: String?
            )
            
            data class Choice(
                @SerializedName("Message") val message: Message?,
                @SerializedName("FinishReason") val finishReason: String?
            ) {
                data class Message(
                    @SerializedName("Role") val role: String?,
                    @SerializedName("Content") val content: String?
                )
            }
        }
    }
} 