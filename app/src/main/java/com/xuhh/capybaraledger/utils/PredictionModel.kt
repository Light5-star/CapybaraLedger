package com.xuhh.capybaraledger.utils

import kotlin.math.max

/**
 * 预测模型类，使用简单线性回归进行预测
 */
class PredictionModel {
    /**
     * 使用简单线性回归进行预测
     * @param data 历史数据
     * @param predictDays 预测天数
     * @return 预测结果
     */
    fun predict(data: List<Float>, predictDays: Int): List<Float> {
        if (data.isEmpty()) return emptyList()
        if (data.size == 1) return List(predictDays) { data[0] }
        
        // 处理数据，移除极端值
        val processedData = removeOutliers(data)
        if (processedData.size < 2) return List(predictDays) { processedData.firstOrNull() ?: 0f }
        
        // 计算斜率和截距
        val n = processedData.size
        var sumX = 0f
        var sumY = 0f
        var sumXY = 0f
        var sumXX = 0f

        for (i in processedData.indices) {
            sumX += i.toFloat()
            sumY += processedData[i]
            sumXY += i * processedData[i]
            sumXX += i * i
        }

        val denominator = n * sumXX - sumX * sumX
        // 如果分母为0，说明所有x值都相同，无法进行线性回归
        if (denominator == 0f) {
            return List(predictDays) { processedData.average().toFloat() }
        }
        
        val slope = (n * sumXY - sumX * sumY) / denominator
        val intercept = (sumY - slope * sumX) / n

        // 生成预测数据，确保预测结果不小于0
        val predictions = mutableListOf<Float>()
        for (i in 0 until predictDays) {
            val prediction = max(0f, slope * (n + i) + intercept)
            predictions.add(prediction)
        }

        return predictions
    }
    
    /**
     * 移除异常值，使用IQR方法
     */
    private fun removeOutliers(data: List<Float>): List<Float> {
        if (data.size < 4) return data
        
        val sortedData = data.sorted()
        val q1Index = (sortedData.size * 0.25).toInt()
        val q3Index = (sortedData.size * 0.75).toInt()
        
        val q1 = sortedData[q1Index]
        val q3 = sortedData[q3Index]
        
        val iqr = q3 - q1
        val lowerBound = q1 - 1.5f * iqr
        val upperBound = q3 + 1.5f * iqr
        
        return data.filter { it in lowerBound..upperBound }
    }
    
    /**
     * 使用指数平滑预测
     * @param data 历史数据
     * @param predictDays 预测天数
     * @param alpha 平滑系数，范围为[0, 1]
     * @return 预测结果
     */
    fun predictWithExponentialSmoothing(data: List<Float>, predictDays: Int, alpha: Float = 0.3f): List<Float> {
        if (data.isEmpty()) return emptyList()
        if (data.size == 1) return List(predictDays) { data[0] }
        
        val smoothedData = mutableListOf<Float>()
        smoothedData.add(data[0])
        
        // 计算平滑值
        for (i in 1 until data.size) {
            val newValue = alpha * data[i] + (1 - alpha) * smoothedData[i - 1]
            smoothedData.add(newValue)
        }
        
        // 预测未来值
        val predictions = mutableListOf<Float>()
        var lastValue = smoothedData.last()
        
        for (i in 0 until predictDays) {
            predictions.add(lastValue)
        }
        
        return predictions
    }
    
    /**
     * 结合线性回归和指数平滑的预测方法
     */
    fun hybridPredict(data: List<Float>, predictDays: Int): List<Float> {
        if (data.size < 3) return predict(data, predictDays)
        
        val linearPredictions = predict(data, predictDays)
        val exponentialPredictions = predictWithExponentialSmoothing(data, predictDays)
        
        // 组合两种预测结果
        return linearPredictions.zip(exponentialPredictions) { linear, exp ->
            (linear * 0.7f + exp * 0.3f)
        }
    }
} 