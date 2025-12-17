package com.example.findu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.findu.model.FoundItem
import com.example.findu.model.LostItem
import com.example.findu.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    
    private val _foundItems = MutableStateFlow<List<FoundItem>>(emptyList())
    val foundItems: StateFlow<List<FoundItem>> = _foundItems.asStateFlow()
    
    private val _lostItems = MutableStateFlow<List<LostItem>>(emptyList())
    val lostItems: StateFlow<List<LostItem>> = _lostItems.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData(userId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _foundItems.value = SupabaseRepository.getAllFoundItems()
                _lostItems.value = SupabaseRepository.getAllLostItems()
                
                if (userId != null) {
                    _unreadCount.value = SupabaseRepository.getUnreadCount(userId)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshUnreadCount(userId: String) {
        viewModelScope.launch {
            try {
                _unreadCount.value = SupabaseRepository.getUnreadCount(userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
