package com.example.findu.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FoundItemFormViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoundItemFormViewModel::class.java)) {
            return FoundItemFormViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}