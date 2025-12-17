package com.example.findu.model

import androidx.room.TypeConverter

/**
 * 转换器类
 * 功能：
 * 1. Map<String, String> <-> String
 * 2. ItemStatus <-> String
 * 3. ItemCategory <-> String
 */
class MapTypeConverters {

    // Map → String（存储时）
    @TypeConverter
    fun mapToString(map: Map<String, String>?): String? {
        if (map == null) return null
        return map.entries.joinToString("|") { "${it.key}=${it.value}" }
    }

    // String → Map（读取时）
    @TypeConverter
    fun stringToMap(value: String?): Map<String, String>? {
        if (value == null) return null
        if (value.isBlank()) return emptyMap()
        return value.split("|").associate { entry ->
            val parts = entry.split("=", limit = 2)
            val key = parts.getOrElse(0) { "" }
            val value = parts.getOrElse(1) { "" }
            key to value
        }
    }

    // ItemStatus → String
    @TypeConverter
    fun fromItemStatus(status: ItemStatus): String {
        return status.name
    }

    // String → ItemStatus
    @TypeConverter
    fun toItemStatus(value: String): ItemStatus {
        return try {
            ItemStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ItemStatus.SEARCHING // 默认值
        }
    }

    // ItemCategory → String
    @TypeConverter
    fun fromItemCategory(category: ItemCategory): String {
        return category.name
    }

    // String → ItemCategory
    @TypeConverter
    fun toItemCategory(value: String): ItemCategory {
        return try {
            ItemCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ItemCategory.OTHERS // 默认值
        }
    }
}