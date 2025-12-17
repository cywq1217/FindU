package com.example.findu.utils

import com.example.findu.model.FoundItem
import com.example.findu.model.ItemCategory
import com.example.findu.model.LostItem
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object MatchingService {

    // 匹配阈值配置
    private val THRESHOLDS = mapOf(
        ItemCategory.CAMPUS_CARD to 0.9, // 校园卡匹配要求高（证件号）
        ItemCategory.KEYS to 0.7,        // 钥匙匹配主要靠地点和描述
        ItemCategory.HEADPHONES to 0.8,  // 耳机匹配
        ItemCategory.OTHERS to 0.6       // 其他物品默认
    )

    // 计算两个物品的相似度
    fun calculateSimilarity(foundItem: FoundItem, lostItem: LostItem): Double {
        // 1. 类别必须相同
        if (foundItem.category != lostItem.category) return 0.0

        var totalScore = 0.0
        var totalWeight = 0.0

        // 2. 计算地理位置相似度 (权重 0.3)
        val distance = calculateDistance(
            foundItem.latitude, foundItem.longitude,
            lostItem.latitude, lostItem.longitude
        )
        // 假设距离在500米以内算匹配度高，超过2公里算0分
        val locationScore = when {
            distance < 500 -> 1.0
            distance > 2000 -> 0.0
            else -> (2000 - distance) / 1500
        }
        totalScore += locationScore * 0.3
        totalWeight += 0.3

        // 3. 计算时间相似度 (权重 0.2)
        // 拾得时间应该晚于遗失时间
        val timeDiff = foundItem.pickUpTime - lostItem.loseTime
        val timeScore = if (timeDiff >= 0) {
            // 时间差越小越匹配，假设在3天内匹配度高
            val daysDiff = timeDiff / (1000 * 60 * 60 * 24)
            if (daysDiff < 3) 1.0 else maxOf(0.0, 1.0 - (daysDiff - 3) * 0.1)
        } else {
            0.0 // 捡到时间早于丢失时间，逻辑上不可能（或者是旧数据），这里给0分
        }
        totalScore += timeScore * 0.2
        totalWeight += 0.2

        // 4. 特征字段匹配 (权重 0.5)
        val featureScore = calculateFeatureSimilarity(
            foundItem.category,
            foundItem.features,
            lostItem.features
        )
        totalScore += featureScore * 0.5
        totalWeight += 0.5

        return if (totalWeight > 0) totalScore / totalWeight else 0.0
    }

    // 具体的特征匹配逻辑
    private fun calculateFeatureSimilarity(
        category: ItemCategory,
        foundFeatures: Map<String, String>,
        lostFeatures: Map<String, String>
    ): Double {
        var matchCount = 0
        var totalFields = 0

        // 根据不同类别定义关键字段
        val keyFields = when (category) {
            ItemCategory.CAMPUS_CARD -> listOf("证件号后四位", "卡套颜色")
            ItemCategory.KEYS -> listOf("钥匙串数量", "钥匙颜色")
            ItemCategory.HEADPHONES -> listOf("耳机品牌", "耳机类型")
            else -> foundFeatures.keys.toList()
        }

        keyFields.forEach { key ->
            val foundValue = foundFeatures[key]
            val lostValue = lostFeatures[key]
            
            if (!foundValue.isNullOrBlank() && !lostValue.isNullOrBlank()) {
                totalFields++
                // 简单的文本包含或相等匹配
                if (foundValue.trim().equals(lostValue.trim(), ignoreCase = true) ||
                    foundValue.contains(lostValue) || lostValue.contains(foundValue)) {
                    matchCount++
                }
            }
        }

        return if (totalFields > 0) matchCount.toDouble() / totalFields else 0.0
    }

    // 检查是否匹配成功
    fun isMatch(foundItem: FoundItem, lostItem: LostItem): Boolean {
        val similarity = calculateSimilarity(foundItem, lostItem)
        val threshold = THRESHOLDS[foundItem.category] ?: 0.6
        return similarity >= threshold
    }

    // 计算两点距离（米）
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // 地球半径，单位米
        val phi1 = lat1 * Math.PI / 180
        val phi2 = lat2 * Math.PI / 180
        val deltaPhi = (lat2 - lat1) * Math.PI / 180
        val deltaLambda = (lon2 - lon1) * Math.PI / 180

        val a = sin(deltaPhi / 2).pow(2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }
}
