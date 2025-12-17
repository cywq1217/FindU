@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.findu.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 遗失物品数据模型
 */
@Serializable
@Entity(tableName = "lost_items")
data class LostItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @SerialName("user_id") val userId: String = "currentUser", // 提交者ID
    val category: ItemCategory, // 物品类别
    val features: Map<String, String>, // 特征字段
    @SerialName("lose_time") val loseTime: Long, // 遗失时间（时间戳）
    val latitude: Double, // 最后出现位置纬度
    val longitude: Double, // 最后出现位置经度
    @SerialName("submit_time") val submitTime: Long = System.currentTimeMillis(), // 提交时间
    val status: ItemStatus = ItemStatus.SEARCHING, // 状态
    @SerialName("matched_found_item_id") val matchedFoundItemId: String? = null // 匹配到的物品ID
)