package com.example.findu.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.findu.model.FoundItem
import com.example.findu.model.ItemStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface FoundItemDao {
    @Insert
    suspend fun insert(foundItem: FoundItem): Long // Return row ID, but since PK is String (UUID), Room ignores this for non-integer PKs usually or returns -1. But insert is suspend.

    @Update
    suspend fun update(foundItem: FoundItem)

    @Query("UPDATE found_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: ItemStatus)

    @Query("SELECT * FROM found_items ORDER BY submitTime DESC")
    fun getAllFoundItems(): Flow<List<FoundItem>>

    @Query("SELECT * FROM found_items WHERE userId = :userId ORDER BY submitTime DESC")
    fun getItemsByUser(userId: String): Flow<List<FoundItem>>

    @Query("SELECT * FROM found_items WHERE category = :category")
    suspend fun getByCategory(category: String): List<FoundItem>
    
    @Query("SELECT * FROM found_items ORDER BY submitTime DESC")
    suspend fun getAllItems(): List<FoundItem>

    @Query("SELECT * FROM found_items WHERE id = :id")
    suspend fun getById(id: String): FoundItem?
}