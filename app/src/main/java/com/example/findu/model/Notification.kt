@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.findu.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

/**
 * 消息通知数据模型
 */
@Serializable
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // 修改为 Long 以匹配 Supabase bigint
    @SerialName("user_id") val userId: String, // 接收用户ID
    val title: String? = null, // 标题（可空，兼容 Supabase null 值）
    val content: String? = null, // 内容（可空，兼容 Supabase null 值）
    @SerialName("related_item_id") val relatedItemId: String? = null, // 关联的物品ID（可空）
    @SerialName("is_read") val isRead: Boolean? = false, // 是否已读（可空，兼容 Supabase null 值）
    val timestamp: Long? = System.currentTimeMillis() // 接收时间（可空）
)