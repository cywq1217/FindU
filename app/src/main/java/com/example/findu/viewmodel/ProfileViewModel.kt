package com.example.findu.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findu.model.FoundItem
import com.example.findu.model.LostItem
import com.example.findu.model.User
import com.example.findu.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val context: Context, private val userId: String) : ViewModel() {

    private val _myFoundItems = MutableStateFlow<List<FoundItem>>(emptyList())
    val myFoundItems: StateFlow<List<FoundItem>> = _myFoundItems.asStateFlow()
    
    private val _myLostItems = MutableStateFlow<List<LostItem>>(emptyList())
    val myLostItems: StateFlow<List<LostItem>> = _myLostItems.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        Log.d("ProfileViewModel", "Init with userId: $userId")
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Loading data for userId: $userId")
                
                // 创建临时用户对象用于显示（因为没有用户表）
                _currentUser.value = User(
                    userId = userId,
                    username = userId.substringBefore('@').take(10),
                    password = "",
                    phone = "",
                    email = null
                )
                
                // 从 Supabase 加载用户的拾得物品
                _myFoundItems.value = SupabaseRepository.getFoundItemsByUser(userId)
                
                // 从 Supabase 加载用户的遗失物品
                _myLostItems.value = SupabaseRepository.getLostItemsByUser(userId)
                
                Log.d("ProfileViewModel", "Data loaded: ${_myFoundItems.value.size} found, ${_myLostItems.value.size} lost")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading user data", e)
            }
        }
    }
    
    fun refreshData() {
        loadUserData()
    }
}

class ProfileViewModelFactory(private val context: Context, private val userId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(context, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}