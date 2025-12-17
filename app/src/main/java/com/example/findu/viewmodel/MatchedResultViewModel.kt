package com.example.findu.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findu.model.FoundItem
import com.example.findu.repository.SupabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchedResultViewModel(private val context: Context, private val foundItemId: String) : ViewModel() {

    private val _foundItem = MutableStateFlow<FoundItem?>(null)
    val foundItem: StateFlow<FoundItem?> = _foundItem.asStateFlow()

    init {
        loadFoundItem()
    }

    private fun loadFoundItem() {
        viewModelScope.launch {
            // 从 Supabase 获取拾得物品
            val item = SupabaseRepository.getFoundItemById(foundItemId)
            _foundItem.value = item
        }
    }
}

class MatchedResultViewModelFactory(private val context: Context, private val foundItemId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchedResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MatchedResultViewModel(context, foundItemId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}