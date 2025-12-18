package com.example.findu.repository

import android.util.Log
import com.example.findu.model.FoundItem
import com.example.findu.model.ItemStatus
import com.example.findu.model.LostItem
import com.example.findu.model.Notification
import com.example.findu.network.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase 数据仓库
 * 负责所有与云端数据库的交互
 */
object SupabaseRepository {
    
    private const val TAG = "SupabaseRepo"

    // ---------------------- 拾得物品 (Found Items) ----------------------

    // 提交拾得物品
    suspend fun insertFoundItem(item: FoundItem) = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("found_items").insert(item)
            Log.d(TAG, "Insert found item success: ${item.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Insert found item failed", e)
            throw e
        }
    }

    // 获取所有拾得物品 (按提交时间倒序)
    suspend fun getAllFoundItems(): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("found_items")
                .select(columns = Columns.ALL) {
                    order("submit_time", order = Order.DESCENDING)
                }
                .decodeList<FoundItem>()
        } catch (e: Exception) {
            Log.e(TAG, "Get all found items failed", e)
            emptyList()
        }
    }

    // 查找特定类别的拾得物品 (用于匹配算法)
    suspend fun getFoundItemsByCategory(category: String): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("found_items")
                .select {
                    filter {
                        eq("category", category)
                    }
                }
                .decodeList<FoundItem>()
        } catch (e: Exception) {
            Log.e(TAG, "Get found items by category failed", e)
            emptyList()
        }
    }
    
    // 获取特定用户的拾得物品
    suspend fun getFoundItemsByUser(userId: String): List<FoundItem> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("found_items")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("submit_time", order = Order.DESCENDING)
                }
                .decodeList<FoundItem>()
        } catch (e: Exception) {
            Log.e(TAG, "Get found items by user failed", e)
            emptyList()
        }
    }
    
    // 根据 ID 获取单个拾得物品
    suspend fun getFoundItemById(id: String): FoundItem? = withContext(Dispatchers.IO) {
        try {
            val items = SupabaseClient.client.from("found_items")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeList<FoundItem>()
            items.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Get found item by id failed", e)
            null
        }
    }

    // ---------------------- 遗失物品 (Lost Items) ----------------------

    // 提交遗失物品
    suspend fun insertLostItem(item: LostItem) = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("lost_items").insert(item)
            Log.d(TAG, "Insert lost item success: ${item.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Insert lost item failed", e)
            throw e
        }
    }

    // 获取所有遗失物品 (按提交时间倒序)
    suspend fun getAllLostItems(): List<LostItem> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("lost_items")
                .select(columns = Columns.ALL) {
                    order("submit_time", order = Order.DESCENDING)
                }
                .decodeList<LostItem>()
        } catch (e: Exception) {
            Log.e(TAG, "Get all lost items failed", e)
            emptyList()
        }
    }

    // 查找特定类别且正在寻找中的遗失物品 (用于反向匹配)
    // 注意：status 可能是 "SEARCHING" 或 null（数据库默认值），都视为正在寻找
    suspend fun getSearchingLostItems(category: String): List<LostItem> = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client.from("lost_items")
                .select {
                    filter {
                        eq("category", category)
                    }
                }
                .decodeList<LostItem>()
            // 过滤：status 为 SEARCHING 或 null 都算是正在寻找中
            result.filter { it.status == null || it.status == ItemStatus.SEARCHING }
        } catch (e: Exception) {
            Log.e(TAG, "Get searching lost items failed", e)
            emptyList()
        }
    }

    // 获取特定用户的遗失物品
    suspend fun getLostItemsByUser(userId: String): List<LostItem> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("lost_items")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("submit_time", order = Order.DESCENDING)
                }
                .decodeList<LostItem>()
        } catch (e: Exception) {
            Log.e(TAG, "Get lost items by user failed", e)
            emptyList()
        }
    }

    // 更新遗失物品状态 (匹配成功后)
    suspend fun updateLostItemStatus(id: String, status: String, foundItemId: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("lost_items").update(
                {
                    set("status", status)
                    set("matched_found_item_id", foundItemId)
                }
            ) {
                filter {
                    eq("id", id)
                }
            }
            Log.d(TAG, "Update lost item status success: $id -> $status")
        } catch (e: Exception) {
            Log.e(TAG, "Update lost item status failed", e)
        }
    }

    // 更新拾得物品状态 (匹配成功后)
    suspend fun updateFoundItemStatus(id: String, status: String) = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("found_items").update(
                {
                    set("status", status)
                }
            ) {
                filter {
                    eq("id", id)
                }
            }
            Log.d(TAG, "Update found item status success: $id -> $status")
        } catch (e: Exception) {
            Log.e(TAG, "Update found item status failed", e)
        }
    }

    // ---------------------- 通知 (Notifications) ----------------------

    // 发送通知
    suspend fun insertNotification(notification: Notification) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inserting notification for user: ${notification.userId}, title: ${notification.title}")
            // 使用 JsonObject 构建要插入的数据，排除 ID，让数据库自增
            val data = buildJsonObject {
                put("user_id", notification.userId)
                put("title", notification.title ?: "")
                put("content", notification.content ?: "")
                put("related_item_id", notification.relatedItemId ?: "")
                put("is_read", notification.isRead ?: false)
                put("timestamp", notification.timestamp ?: System.currentTimeMillis())
            }
            SupabaseClient.client.from("notifications").insert(data)
            Log.d(TAG, "Insert notification success for user: ${notification.userId}")
        } catch (e: Exception) {
            Log.e(TAG, "Insert notification failed: ${e.message}", e)
            e.printStackTrace()
        }
    }

    // 获取特定用户的通知
    suspend fun getUserNotifications(userId: String): List<Notification> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.client.from("notifications")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("timestamp", order = Order.DESCENDING)
                }
                .decodeList<Notification>()
        } catch (e: Exception) {
            Log.e(TAG, "Get user notifications failed", e)
            emptyList()
        }
    }
    
    // 获取未读通知数量
    // 注意：is_read 可能是 false 或 null，都视为未读
    suspend fun getUnreadCount(userId: String): Long = withContext(Dispatchers.IO) {
        try {
            val allNotifications = SupabaseClient.client.from("notifications")
                .select { 
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Notification>()
            // 过滤：is_read 为 false 或 null 都算未读
            allNotifications.count { it.isRead != true }.toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Get unread count failed", e)
            0L
        }
    }
    
    // 标记通知为已读
    suspend fun markNotificationAsRead(notificationId: Long) = withContext(Dispatchers.IO) {
        try {
             SupabaseClient.client.from("notifications").update(
                {
                    set("is_read", true)
                }
            ) {
                filter {
                    eq("id", notificationId)
                }
            }
        } catch (e: Exception) {
             Log.e(TAG, "Mark notification read failed", e)
        }
    }

    // ---------------------- 用户资料 (User Profile) ----------------------

    // 创建用户资料
    suspend fun createUserProfile(userId: String, username: String, phone: String, email: String?) = withContext(Dispatchers.IO) {
        try {
            val userProfile = mapOf(
                "id" to userId,
                "username" to username,
                "phone" to phone,
                "email" to email
            )
            SupabaseClient.client.from("users").insert(userProfile)
            Log.d(TAG, "Create user profile success: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Create user profile failed", e)
            throw e
        }
    }

    // 获取用户资料
    suspend fun getUserProfile(userId: String): Map<String, String?>? = withContext(Dispatchers.IO) {
        try {
            val result = SupabaseClient.client.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<kotlinx.serialization.json.JsonObject>()
            
            if (result != null) {
                mapOf(
                    "id" to result["id"]?.toString()?.trim('"'),
                    "username" to result["username"]?.toString()?.trim('"'),
                    "phone" to result["phone"]?.toString()?.trim('"'),
                    "email" to result["email"]?.toString()?.trim('"')
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Get user profile failed", e)
            null
        }
    }
}