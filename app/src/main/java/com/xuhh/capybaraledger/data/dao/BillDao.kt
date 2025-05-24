package com.xuhh.capybaraledger.data.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.xuhh.capybaraledger.data.model.Bill
import com.xuhh.capybaraledger.data.model.Categories
import com.xuhh.capybaraledger.data.model.Category

data class BillWithCategory(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "category_id",
        entityColumn = "id",
        entity = Category::class
    )
    val category: Category
)

@Dao
interface BillDao {
    @Insert
    suspend fun insert(bill: Bill): Long

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getBillById(id: Long): Bill?

    @Query("SELECT * FROM bills WHERE id = :billId")
    suspend fun getBillWithLogging(billId: Long): BillWithCategory? {
        Log.d("BillDao", "开始查询账单，ID: $billId")
        val bill = getBillById(billId)
        
        if (bill != null) {
            Log.d("BillDao", """
                查询结果: 账单存在
                账单ID: ${bill.id}
                金额: ${bill.amount}
                分类ID: ${bill.categoryId}
                账本ID: ${bill.ledgerId}
            """.trimIndent())
            
            // 获取分类信息
            val category = Categories.getCategoryById(bill.categoryId)
            if (category != null) {
                Log.d("BillDao", """
                    分类信息:
                    分类ID: ${category.id}
                    分类名称: ${category.name}
                    分类类型: ${category.type}
                """.trimIndent())
                return BillWithCategory(bill, category)
            } else {
                Log.e("BillDao", "分类ID ${bill.categoryId} 不存在，使用默认分类")
                // 如果找不到分类，使用默认分类
                val defaultCategory = if (bill.type == Bill.TYPE_EXPENSE) {
                    Categories.EXPENSE_CATEGORIES.firstOrNull()
                } else {
                    Categories.INCOME_CATEGORIES.firstOrNull()
                }
                
                if (defaultCategory != null) {
                    Log.d("BillDao", """
                        使用默认分类:
                        分类ID: ${defaultCategory.id}
                        分类名称: ${defaultCategory.name}
                        分类类型: ${defaultCategory.type}
                    """.trimIndent())
                    return BillWithCategory(bill, defaultCategory)
                } else {
                    Log.e("BillDao", "无法找到默认分类")
                    return null
                }
            }
        } else {
            Log.e("BillDao", """
                查询结果: 账单不存在
                请求ID: $billId
                请检查:
                1. 账单ID是否正确
                2. 数据库连接是否正常
                3. 账单是否已被删除
            """.trimIndent())
            return null
        }
    }

    @Transaction
    @Query("""
        SELECT * FROM bills 
        WHERE date = :date AND ledger_id = :ledgerId
    """)
    suspend fun getBillsByDate(date: Long, ledgerId: Long?): List<BillWithCategory>

    @Transaction
    @Query("""
        SELECT * FROM bills 
        WHERE ledger_id = :ledgerId AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getBillsByLedgerAndMonth(ledgerId: Long, startDate: Long, endDate: Long): List<BillWithCategory>

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBill(id: Long)

    @Transaction
    suspend fun deleteBillWithTransaction(id: Long): Boolean {
        val bill = getBillById(id) ?: return false

        deleteBill(id)
        
        return getBillById(id) == null
    }

    @Update
    suspend fun updateBill(bill: Bill)

    @Transaction
    suspend fun updateBillWithTransaction(bill: Bill): Boolean {
        try {
            val existingBill = getBillById(bill.id)
            if (existingBill == null) {
                Log.e("BillDao", "更新失败：账单ID ${bill.id} 不存在")
                return false
            }
            
            // 验证数据完整性
            if (bill.amount <= 0) {
                Log.e("BillDao", "更新失败：金额必须大于0")
                return false
            }
            
            if (bill.categoryId <= 0) {
                Log.e("BillDao", "更新失败：分类ID无效")
                return false
            }
            
            if (bill.ledgerId <= 0) {
                Log.e("BillDao", "更新失败：账本ID无效")
                return false
            }
            
            // 执行更新
            updateBill(bill)
            
            // 验证更新是否成功
            val updatedBill = getBillById(bill.id)
            if (updatedBill == null) {
                Log.e("BillDao", "更新失败：更新后无法找到账单")
                return false
            }
            
            // 验证更新后的数据是否正确
            if (updatedBill.amount != bill.amount ||
                updatedBill.type != bill.type ||
                updatedBill.categoryId != bill.categoryId ||
                updatedBill.ledgerId != bill.ledgerId ||
                updatedBill.date != bill.date ||
                updatedBill.time != bill.time) {
                Log.e("BillDao", "更新失败：更新后的数据与预期不符")
                return false
            }
            
            Log.d("BillDao", "账单更新成功：ID=${bill.id}, 金额=${bill.amount}, 类型=${bill.type}")
            return true
        } catch (e: Exception) {
            Log.e("BillDao", "更新账单时发生异常: ${e.message}", e)
            return false
        }
    }

    @Query("SELECT SUM(amount) FROM bills WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getExpenseAmount(type: Int, startDate: Long, endDate: Long): Double

    @Transaction
    @Query("SELECT * FROM bills WHERE ledger_id = :ledgerId")
    suspend fun getBillsByLedger(ledgerId: Long): List<BillWithCategory>

    @Transaction
    @Query("""
        SELECT * FROM bills 
        WHERE date BETWEEN :startDate AND :endDate 
        AND ledger_id = :ledgerId
        ORDER BY date DESC
    """)
    suspend fun getDailyBills(
        startDate: Long,
        endDate: Long,
        ledgerId: Long
    ): List<BillWithCategory>

    @Query("SELECT * FROM bills WHERE ledger_id = :ledgerId AND date >= :startTime AND date <= :endTime ORDER BY date DESC")
    suspend fun getBillsByLedgerIdAndTimeRange(ledgerId: Long, startTime: Long, endTime: Long): List<Bill>

    @Query("SELECT * FROM bills WHERE time >= :startTime AND time < :endTime")
    suspend fun getBillsByTimeRange(startTime: Long, endTime: Long): List<BillWithCategory>

    @Transaction
    @Query(
        """
        SELECT b.*, c.* FROM bills b 
        INNER JOIN categories c ON b.category_id = c.id 
        WHERE b.ledger_id = :ledgerId AND b.date BETWEEN :startTime AND :endTime
    """
    )
    suspend fun getBillsWithCategoryByTimeRange(ledgerId: Long, startTime: Long, endTime: Long): List<BillWithCategory>

    @Transaction
    @Query(
        """
        SELECT b.*, c.* FROM bills b 
        INNER JOIN categories c ON b.category_id = c.id 
        WHERE b.ledger_id = :ledgerId 
        AND b.date >= :startDate 
        AND b.date < :endDate 
        ORDER BY b.date DESC, b.time DESC, b.id DESC
    """
    )
    suspend fun getBillsByDateRange(
        ledgerId: Long,
        startDate: Long,
        endDate: Long
    ): List<BillWithCategory>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM bills 
        WHERE type = :type 
        AND ledger_id = :ledgerId
        AND date >= :startDate 
        AND date < :endDate
    """)
    suspend fun getExpenseAmount(
        type: Int,
        ledgerId: Long,
        startDate: Long,
        endDate: Long
    ): Double
    
    // 获取记账的不同日期总数
    @Query("SELECT COUNT(DISTINCT date) FROM bills")
    suspend fun getDistinctBillDatesCount(): Int
    
    // 获取按日期排序的所有账单日期（不重复）
    @Query("SELECT DISTINCT date FROM bills ORDER BY date DESC")
    suspend fun getAllDistinctBillDates(): List<Long>
}