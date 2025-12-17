@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.findu.model

import androidx.annotation.DrawableRes
import com.example.findu.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

/**
 * 物品类别枚举（包含图标、显示名称和字段数量）
 */
@Serializable
enum class ItemCategory(
    val displayName: String,
    @DrawableRes val iconRes: Int,
    val fieldCount: Int // 该类别对应的字段数量
) {
    CAMPUS_CARD("校园卡", R.drawable.ic_campus_card, 4),
    KEYS("钥匙", R.drawable.ic_keys, 3),
    HEADPHONES("耳机", R.drawable.ic_headphones, 2),
    WALLET("钱包", R.drawable.ic_wallet, 3),
    CLOTHES("衣物", R.drawable.ic_clothes, 2),
    BACKPACK("背包", R.drawable.ic_backpack, 3),
    ELECTRONICS("电子产品", R.drawable.ic_electronics, 4),
    OTHERS("其他", R.drawable.ic_others, 1)
}