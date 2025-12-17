@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.findu.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi
import java.util.UUID

/**
 * 拾得物品数据模型
 */
@Serializable
@Entity(tableName = "found_items")
data class FoundItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @SerialName("user_id") val userId: String = "currentUser", // 提交者ID
    val category: ItemCategory, // 物品类别
    val features: Map<String, String>, // 特征字段（key:字段名，value:值）
    @SerialName("image_path") val imagePath: String, // 图片本地路径
    val latitude: Double, // 纬度（保留5位小数）
    val longitude: Double, // 经度（保留5位小数）
    @SerialName("pick_up_time") val pickUpTime: Long, // 拾得时间（时间戳）
    @SerialName("submit_time") val submitTime: Long = System.currentTimeMillis(), // 提交时间
    val status: ItemStatus = ItemStatus.SEARCHING, // 状态
    @SerialName("is_synced") val isSynced: Boolean = false // 是否已同步到服务器
)