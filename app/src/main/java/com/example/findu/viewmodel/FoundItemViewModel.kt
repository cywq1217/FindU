package com.example.findu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findu.model.FieldTemplate
import com.example.findu.model.FormField
import com.example.findu.model.InputType
import com.example.findu.model.ItemCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoundItemViewModel : ViewModel() {
    private val _selectedCategory = MutableStateFlow<ItemCategory?>(null)
    val selectedCategory: StateFlow<ItemCategory?> = _selectedCategory.asStateFlow()

    private val _fieldTemplate = MutableStateFlow<FieldTemplate?>(null)
    val fieldTemplate: StateFlow<FieldTemplate?> = _fieldTemplate.asStateFlow()

    fun selectCategory(category: ItemCategory) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _fieldTemplate.value = createFieldTemplate(category)
        }
    }

    private fun createFieldTemplate(category: ItemCategory): FieldTemplate {
        return when (category) {
            ItemCategory.CAMPUS_CARD -> FieldTemplate(
                category = category,
                fields = listOf(
                    FormField("证件号后四位", "输入后四位数字", true, InputType.NUMBER),
                    FormField("卡套颜色", "如黑色、蓝色", true, InputType.TEXT),
                    FormField("卡面特征", "如贴照片、有划痕", false, InputType.TEXT),
                    FormField("拾得校区", "选择校区", true, InputType.SELECT)
                )
            )
            ItemCategory.KEYS -> FieldTemplate(
                category = category,
                fields = listOf(
                    FormField("钥匙串数量", "输入数量", true, InputType.NUMBER),
                    FormField("钥匙串特征", "如挂绳、挂件", true, InputType.TEXT),
                    FormField("拾得位置", "描述具体位置", true, InputType.TEXT),
                    FormField("钥匙颜色", "银色/黑色/其他", false, InputType.TEXT)
                )
            )
            ItemCategory.HEADPHONES -> FieldTemplate(
                category = category,
                fields = listOf(
                    FormField("耳机品牌", "苹果/华为/小米等", true, InputType.SELECT),
                    FormField("耳机类型", "有线/无线", true, InputType.RADIO),
                    FormField("外观特征", "描述颜色、磨损等", true, InputType.TEXT),
                    FormField("拾得场景", "教室/图书馆等", false, InputType.SELECT)
                )
            )
            else -> FieldTemplate(
                category = category,
                fields = listOf(
                    FormField("物品描述", "请详细描述物品特征", true, InputType.TEXT)
                )
            )
        }
    }
}