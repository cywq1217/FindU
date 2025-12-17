package com.example.findu.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.findu.model.ItemStatus
import com.example.findu.model.LostItem
import kotlinx.coroutines.flow.Flow

@Dao
interface LostItemDao {
    @Insert
    suspend fun insert(lostItem: LostItem): Long // Room returns -1 for String PKs usually, but method signature is valid

    @Update
    suspend fun update(lostItem: LostItem)

    @Query("UPDATE lost_items SET status = :status, matchedFoundItemId = :foundItemId WHERE id = :id")
    suspend fun updateStatus(id: String, status: ItemStatus, foundItemId: String?)

    @Query("SELECT * FROM lost_items ORDER BY submitTime DESC")
    fun getAllLostItems(): Flow<List<LostItem>>

    @Query("SELECT * FROM lost_items WHERE userId = :userId ORDER BY submitTime DESC")
    fun getItemsByUser(userId: String): Flow<List<LostItem>>

    @Query("SELECT * FROM lost_items WHERE category = :category")
    suspend fun getByCategory(category: String): List<LostItem>

    // 获取特定类别且状态为寻找中的物品，用于匹配
    @Query("SELECT * FROM lost_items WHERE category = :category AND status = 'SEARCHING'")
    suspend fun getSearchingItems(category: String): List<LostItem>

    @Query("SELECT * FROM lost_items WHERE id = :id")
    suspend fun getById(id: String): LostItem?
}