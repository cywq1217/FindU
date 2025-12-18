package com.example.findu.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findu.model.FieldTemplate
import com.example.findu.model.FormField
import com.example.findu.model.FoundItem
import com.example.findu.model.InputType
import com.example.findu.model.ItemCategory
import com.example.findu.model.ItemStatus
import com.example.findu.model.LostItem
import com.example.findu.model.Notification
import com.example.findu.repository.SupabaseRepository
import com.example.findu.utils.MatchingService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MatchResultEvent(val similarity: Double, val matchedItem: FoundItem?)

// 定义提交结果密封类，用于UI状态展示
sealed class SubmitResult {
    data object Initial : SubmitResult()
    data object Pending : SubmitResult() // 待定/等待中
    data class Success(val message: String) : SubmitResult() // 成功（匹配成功或提交成功）
    data class Error(val message: String) : SubmitResult() // 失败
}

class FoundItemFormViewModel(private val context: Context) : ViewModel() {
    private val _currentTemplate = MutableStateFlow<FieldTemplate>(
        FieldTemplate(ItemCategory.OTHERS, emptyList())
    )
    val currentTemplate: StateFlow<FieldTemplate> = _currentTemplate.asStateFlow()

    private val _formData = MutableStateFlow<Map<String, String>>(emptyMap())
    val formData: StateFlow<Map<String, String>> = _formData.asStateFlow()

    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> = _isFormValid.asStateFlow()

    // 提交结果流
    private val _submitResultEvents = MutableSharedFlow<SubmitResult>()
    val submitResultEvents: SharedFlow<SubmitResult> = _submitResultEvents.asSharedFlow()

    private val _matchResult = MutableSharedFlow<MatchResultEvent>()
    val matchResult: SharedFlow<MatchResultEvent> = _matchResult.asSharedFlow()

    // 新增：位置选择状态 (Latitude, Longitude)
    private val _selectedLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val selectedLocation: StateFlow<Pair<Double, Double>?> = _selectedLocation.asStateFlow()

    // 更新选中的位置
    fun updateLocation(lat: Double, lng: Double) {
        _selectedLocation.value = lat to lng
        // 这里可以添加逻辑，比如根据经纬度反查地址描述并填入表单
    }

    fun initForm(category: ItemCategory) {
        val template = createFieldTemplate(category)
        _currentTemplate.value = template
        _formData.value = template.fields.associate { it.label to "" }
        _selectedLocation.value = null // 重置位置
    }

    fun updateFormData(fieldName: String, value: String) {
        _formData.update { currentData ->
            currentData + (fieldName to value)
        }
        val valid = _currentTemplate.value.fields.all { field ->
            !field.isRequired || _formData.value[field.label].isNullOrBlank().not()
        }
        _isFormValid.value = valid
    }

    fun submitFoundItem(foundItem: FoundItem) {
        viewModelScope.launch {
            try {
                Log.d("FoundItemVM", "Submitting found item for userId: ${foundItem.userId}")
                
                // 1. 提交到 Supabase
                SupabaseRepository.insertFoundItem(foundItem)

                // 2. 反向匹配：查找所有 SEARCHING 状态的 LostItem (从云端)
                // 先获取所有遗失物品，在代码中过滤类别（避免数据库字段格式问题）
                val allLostItems = SupabaseRepository.getAllLostItems()
                val searchingLostItems = allLostItems.filter { 
                    it.category == foundItem.category && 
                    (it.status == null || it.status == ItemStatus.SEARCHING)
                }
                Log.d("FoundItemVM", "All lost items: ${allLostItems.size}, Searching lost items for category ${foundItem.category}: ${searchingLostItems.size}")
                
                var hasMatch = false
                for (lostItem in searchingLostItems) {
                    val score = MatchingService.calculateSimilarity(foundItem, lostItem)
                    Log.d("FoundItemVM", "Matching with lostItem ${lostItem.id} (user: ${lostItem.userId}), score: $score")
                    if (score > 0.7) { 
                        hasMatch = true
                        Log.d("FoundItemVM", "Match found! Sending notification to user: ${lostItem.userId}")
                        
                        // 使用 NonCancellable 确保通知和状态更新不会被取消
                        withContext(NonCancellable) {
                            // 1. 给失主创建消息通知 (云端)
                            val notification = Notification(
                                userId = lostItem.userId,
                                title = "发现线索",
                                content = "您丢失的 ${foundItem.category} 可能找到了！",
                                relatedItemId = lostItem.id
                            )
                            SupabaseRepository.insertNotification(notification)

                            // 2. 更新失物状态为 MATCHED，记录匹配到的拾得物 ID (云端)
                            SupabaseRepository.updateLostItemStatus(lostItem.id, ItemStatus.MATCHED.name, foundItem.id)
                            
                            // 3. 更新当前拾得物品状态为 MATCHED (云端)
                            SupabaseRepository.updateFoundItemStatus(foundItem.id, ItemStatus.MATCHED.name)
                        }
                    }
                }

                if (hasMatch) {
                    _submitResultEvents.emit(SubmitResult.Success("提交成功，已匹配到相关失主并发送通知！"))
                } else {
                    _submitResultEvents.emit(SubmitResult.Success("提交成功，感谢您的帮助！"))
                }

            } catch (e: Exception) {
                Log.e("FoundItemVM", "Error inserting found item", e)
                _submitResultEvents.emit(SubmitResult.Error("提交失败: ${e.message}"))
            }
        }
    }

    fun submitLostItem(
        category: ItemCategory,
        features: Map<String, String>,
        location: Pair<Double, Double>? = null,
        userId: String // 新增 userId 参数
    ) {
        val finalLocation = location ?: _selectedLocation.value ?: (0.0 to 0.0)

        viewModelScope.launch {
            try {
                Log.d("FoundItemVM", "Submitting lost item for userId: $userId")
                
                // 1. 先保存失物信息，状态默认为 SEARCHING
                val lostItem = LostItem(
                    userId = userId, // 使用传入的 userId
                    category = category,
                    features = features,
                    loseTime = System.currentTimeMillis(),
                    latitude = finalLocation.first,
                    longitude = finalLocation.second,
                    status = ItemStatus.SEARCHING
                )
                // 提交到 Supabase
                SupabaseRepository.insertLostItem(lostItem)

                // 2. 尝试匹配现有的拾得物 (从云端获取同类别物品)
                val allFoundItems = SupabaseRepository.getFoundItemsByCategory(category.name)

                var bestMatch: FoundItem? = null
                var maxScore = 0.0

                for (foundItem in allFoundItems) {
                    val score = MatchingService.calculateSimilarity(foundItem, lostItem)
                    if (score > maxScore) {
                        maxScore = score
                        bestMatch = foundItem
                    }
                }

                if (bestMatch != null && maxScore > 0.6) {
                    // 使用 NonCancellable 确保通知和状态更新不会被取消
                    withContext(NonCancellable) {
                        // 更新当前 LostItem 状态 (云端)
                        SupabaseRepository.updateLostItemStatus(lostItem.id, ItemStatus.MATCHED.name, bestMatch.id)
                        
                        // 更新匹配到的 FoundItem 状态为 MATCHED (云端)
                        SupabaseRepository.updateFoundItemStatus(bestMatch.id, ItemStatus.MATCHED.name)
                        
                        // 创建通知给自己
                        val notification = Notification(
                            userId = lostItem.userId,
                            title = "匹配成功",
                            content = "刚刚提交的失物已匹配到疑似物品！",
                            relatedItemId = lostItem.id
                        )
                        SupabaseRepository.insertNotification(notification)
                        
                        // 如果拾得者不是自己，也给拾得者发送通知
                        if (bestMatch.userId != lostItem.userId) {
                            val notificationToFinder = Notification(
                                userId = bestMatch.userId,
                                title = "物品已匹配",
                                content = "您拾得的 ${bestMatch.category} 已匹配到失主！",
                                relatedItemId = bestMatch.id
                            )
                            SupabaseRepository.insertNotification(notificationToFinder)
                        }
                    }

                    _matchResult.emit(MatchResultEvent(maxScore, bestMatch))
                    _submitResultEvents.emit(SubmitResult.Success("匹配成功！"))
                } else {
                    // 3. 暂时没匹配到
                    _matchResult.emit(MatchResultEvent(0.0, null))
                    _submitResultEvents.emit(SubmitResult.Pending)
                }
            } catch (e: Exception) {
                Log.e("FoundItemVM", "Error submitting lost item", e)
                _submitResultEvents.emit(SubmitResult.Error("提交失败: ${e.message}"))
            }
        }
    }

    fun submitFoundItemWithMatch(
        category: ItemCategory,
        features: Map<String, String>,
        imagePath: String,
        location: Pair<Double, Double>? = null,
        userId: String // 新增 userId 参数
    ) {
        val finalLocation = location ?: _selectedLocation.value ?: (0.0 to 0.0)

        val foundItem = FoundItem(
            userId = userId, // 传入 userId
            category = category,
            features = features,
            imagePath = imagePath,
            latitude = finalLocation.first,
            longitude = finalLocation.second,
            pickUpTime = System.currentTimeMillis(),
            status = ItemStatus.SEARCHING
        )
        submitFoundItem(foundItem)
    }


    private fun createFieldTemplate(category: ItemCategory): FieldTemplate {
        return when (category) {
            ItemCategory.CAMPUS_CARD -> FieldTemplate(
                category = category,
                fields = listOf(
                    FormField("证件号后四位", "输入后四位数字", true, InputType.NUMBER),
                    FormField("卡套颜色", "如黑色、蓝色", true, InputType.TEXT),
                    FormField("卡面特征", "如贴照片、有划痕", false, InputType.TEXT),
                    FormField("校区", "选择校区", true, InputType.SELECT)
                )
            )
            ItemCategory.KEYS -> FieldTemplate(
                category = category,
                fields = listOf(
                    FormField("钥匙串数量", "输入数量", true, InputType.NUMBER),
                    FormField("钥匙串特征", "如挂绳、挂件", true, InputType.TEXT),
                    FormField("位置描述", "描述具体位置", true, InputType.TEXT),
                    FormField("钥匙颜色", "银色/黑色/其他", false, InputType.TEXT)
                )
            )
            ItemCategory.HEADPHONES -> FieldTemplate(
                category = category,
                fields = listOf(
                    FormField("耳机品牌", "苹果/华为/小米等", true, InputType.SELECT),
                    FormField("耳机类型", "有线/无线", true, InputType.RADIO),
                    FormField("外观特征", "描述颜色、磨损等", true, InputType.TEXT),
                    FormField("场景", "教室/图书馆等", false, InputType.SELECT)
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
