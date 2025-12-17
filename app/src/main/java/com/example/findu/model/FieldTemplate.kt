@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package com.example.findu.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

/**
 * 字段模板（绑定类别与表单项）
 */
@Serializable
data class FieldTemplate(
    val category: ItemCategory,
    val fields: List<FormField>
)

/**
 * 单个表单项
 * @param label 字段标签
 * @param hint 输入提示
 * @param isRequired 是否必填
 * @param inputType 输入类型
 * @param privacyNote 隐私说明（仅用于匹配，不显示）
 */
@Serializable
data class FormField(
    val label: String,
    val hint: String,
    val isRequired: Boolean,
    val inputType: InputType = InputType.TEXT,
    val privacyNote: String = "仅用于匹配，不对外展示"
)

@Serializable
enum class InputType {
    TEXT, NUMBER, DATE, SELECT, RADIO
}