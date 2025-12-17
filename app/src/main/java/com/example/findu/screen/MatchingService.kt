package com.example.findu.service

import com.example.findu.model.FoundItem
import com.example.findu.model.LostItem
import kotlin.math.min

object MatchingService {

    // 不同类别的匹配阈值
    private val thresholds = mapOf(
        "CAMPUS_CARD" to 0.8,    // 校园卡需要80%相似度
        "KEYS" to 0.7,           // 钥匙需要70%相似度
        "HEADPHONES" to 0.6,     // 耳机需要60%相似度
        "WALLET" to 0.75,        // 钱包需要75%相似度
        "CLOTHES" to 0.5,        // 衣物需要50%相似度
        "BACKPACK" to 0.65,      // 背包需要65%相似度
        "ELECTRONICS" to 0.7,    // 电子产品需要70%相似度
        "OTHERS" to 0.4          // 其他物品需要40%相似度
    )

    /**
     * 匹配拾得物品和遗失物品
     * @return 匹配度（0-1之间），如果低于阈值则返回null
     */
    fun match(foundItem: FoundItem, lostItem: LostItem): Double? {
        // 类别不同直接返回null
        if (foundItem.category != lostItem.category) {
            return null
        }

        val threshold = thresholds[foundItem.category.name] ?: 0.5
        val similarity = calculateSimilarity(foundItem, lostItem)

        return if (similarity >= threshold) similarity else null
    }

    /**
     * 计算两个物品的相似度
     */
    private fun calculateSimilarity(foundItem: FoundItem, lostItem: LostItem): Double {
        var totalScore = 0.0
        var maxScore = 0.0

        // 对比特征字段
        foundItem.features.forEach { (key, foundValue) ->
            val lostValue = lostItem.features[key]
            if (lostValue != null) {
                val fieldScore = calculateFieldSimilarity(foundValue, lostValue)
                totalScore += fieldScore
                maxScore += 1.0
            }
        }

        // 如果没有任何共同字段，返回0
        if (maxScore == 0.0) return 0.0

        // 位置相似度（如果位置接近，增加匹配度）
        val locationSimilarity = calculateLocationSimilarity(
            foundItem.latitude, foundItem.longitude,
            lostItem.latitude, lostItem.longitude
        )

        // 综合相似度：特征相似度占70%，位置相似度占30%
        val featureSimilarity = totalScore / maxScore
        return 0.7 * featureSimilarity + 0.3 * locationSimilarity
    }

    /**
     * 计算字段相似度
     */
    private fun calculateFieldSimilarity(value1: String, value2: String): Double {
        if (value1 == value2) return 1.0

        // 简单字符串相似度（实际可以使用更复杂的算法如编辑距离）
        val longerLength = maxOf(value1.length, value2.length)
        val shorterLength = minOf(value1.length, value2.length)

        if (longerLength == 0) return 1.0

        return shorterLength.toDouble() / longerLength
    }

    /**
     * 计算位置相似度（距离越近，相似度越高）
     */
    private fun calculateLocationSimilarity(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val distance = calculateDistance(lat1, lon1, lat2, lon2)

        // 如果距离在1公里内，相似度为1；距离越远相似度越低，超过10公里为0
        return when {
            distance <= 1.0 -> 1.0
            distance >= 10.0 -> 0.0
            else -> 1.0 - (distance - 1.0) / 9.0
        }
    }

    /**
     * 计算两个坐标点的距离（公里）
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // 地球半径，单位公里

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}