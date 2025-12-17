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
    val title: String, // 标题
    val content: String, // 内容
    @SerialName("related_item_id") val relatedItemId: String, // 关联的物品ID
    @SerialName("is_read") val isRead: Boolean = false, // 是否已读
    val timestamp: Long = System.currentTimeMillis() // 接收时间
)