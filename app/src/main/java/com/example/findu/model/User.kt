@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.example.findu.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val username: String,
    val password: String, // 实际项目中应该加密存储
    val phone: String,
    val email: String? = null,
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)