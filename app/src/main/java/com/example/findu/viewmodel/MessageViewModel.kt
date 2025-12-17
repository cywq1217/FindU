package com.example.findu.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findu.model.Notification
import com.example.findu.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageViewModel(private val context: Context, private val userId: String) : ViewModel() {
    
    // 使用 StateFlow 来存储来自云端的数据
    private val _messages = MutableStateFlow<List<Notification>>(emptyList())
    val messages: StateFlow<List<Notification>> = _messages.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            // 从 Supabase 获取消息
            val notificationList = SupabaseRepository.getUserNotifications(userId)
            _messages.value = notificationList
            
            // 计算未读数
            // 注意：因为 Supabase 是远程请求，我们这里简单地通过过滤 list 来计算，
            // 或者再次调用 getUnreadCount。为了性能，直接从 list 过滤即可。
            _unreadCount.value = notificationList.count { !it.isRead }.toLong()
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            // 更新云端状态
            SupabaseRepository.markNotificationAsRead(notificationId)
            
            // 更新本地 UI 状态
            _messages.value = _messages.value.map { 
                if (it.id == notificationId) it.copy(isRead = true) else it 
            }
            _unreadCount.value = _messages.value.count { !it.isRead }.toLong()
        }
    }
}

class MessageViewModelFactory(private val context: Context, private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MessageViewModel(context, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}