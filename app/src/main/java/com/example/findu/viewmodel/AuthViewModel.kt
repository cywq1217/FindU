package com.example.findu.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.findu.model.User
import com.example.findu.network.SupabaseClient
import com.example.findu.repository.SupabaseRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(context: Context) : ViewModel() {
    private val TAG = "AuthViewModel"

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    init {
        // 检查是否有已登录的会话
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        viewModelScope.launch {
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session != null) {
                    val supabaseUser = session.user
                    if (supabaseUser != null) {
                        _currentUser.value = User(
                            userId = supabaseUser.id,
                            username = supabaseUser.email?.substringBefore('@') ?: "user",
                            password = "",
                            phone = supabaseUser.phone ?: "",
                            email = supabaseUser.email
                        )
                        Log.d(TAG, "Found existing session for user: ${supabaseUser.email}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking session", e)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                // 使用邮箱格式登录 Supabase Auth
                val email = if (username.contains('@')) username else "$username@findu.app"
                
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val supabaseUser = session?.user
                
                if (supabaseUser != null) {
                    _currentUser.value = User(
                        userId = supabaseUser.id,
                        username = supabaseUser.email?.substringBefore('@') ?: username,
                        password = "",
                        phone = supabaseUser.phone ?: "",
                        email = supabaseUser.email
                    )
                    _loginState.value = AuthState.Success
                    Log.d(TAG, "Login successful for: ${supabaseUser.email}")
                } else {
                    _loginState.value = AuthState.Error("登录失败")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                val errorMessage = when {
                    e.message?.contains("Password should be at least 6 characters") == true -> "密码至少需要6个字符"
                    e.message?.contains("Invalid email or password") == true -> "邮箱或密码不正确"
                    else -> "登录失败: ${e.message}"
                }
                _loginState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun register(username: String, password: String, phone: String, email: String?) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            try {
                // 验证密码长度
                if (password.length < 6) {
                    _loginState.value = AuthState.Error("密码至少需要6个字符")
                    return@launch
                }
                
                // 使用真实邮箱或生成虚拟邮箱
                val userEmail = if (!email.isNullOrBlank()) email else "$username@findu.app"
                
                Log.d(TAG, "Attempting registration for: $userEmail")
                
                // 注册用户
                val result = SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = userEmail
                    this.password = password
                }
                
                // 注册成功后尝试自动登录
                try {
                    SupabaseClient.client.auth.signInWith(Email) {
                        this.email = userEmail
                        this.password = password
                    }
                } catch (loginError: Exception) {
                    Log.w(TAG, "Auto-login after registration failed, may need email verification", loginError)
                }
                
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                val supabaseUser = session?.user
                
                if (supabaseUser != null) {
                    // 在 users 表中创建用户资料
                    try {
                        SupabaseRepository.createUserProfile(
                            userId = supabaseUser.id,
                            username = username,
                            phone = phone,
                            email = userEmail
                        )
                        Log.d(TAG, "User profile created in database")
                    } catch (profileError: Exception) {
                        Log.w(TAG, "Failed to create user profile, but auth succeeded", profileError)
                    }
                    
                    _currentUser.value = User(
                        userId = supabaseUser.id,
                        username = username,
                        password = "",
                        phone = phone,
                        email = userEmail
                    )
                    _loginState.value = AuthState.Success
                    Log.d(TAG, "Registration and login successful for: $userEmail")
                } else {
                    // 注册可能成功但需要邮箱验证，创建临时用户让用户进入应用
                    _currentUser.value = User(
                        userId = java.util.UUID.randomUUID().toString(),
                        username = username,
                        password = "",
                        phone = phone,
                        email = userEmail
                    )
                    _loginState.value = AuthState.Success
                    Log.d(TAG, "Registration successful (may need email verification): $userEmail")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                val errorMessage = when {
                    e.message?.contains("Password should be at least 6 characters") == true -> "密码至少需要6个字符"
                    e.message?.contains("already registered") == true -> "该邮箱已被注册，请直接登录"
                    e.message?.contains("User already registered") == true -> "该用户已注册，请直接登录"
                    e.message?.contains("Invalid email") == true -> "邮箱格式不正确"
                    e.message?.contains("you can only request this after") == true -> "请求过于频繁，请稍后再试"
                    e.message?.contains("rate limit") == true -> "请求过于频繁，请稍后再试"
                    else -> "注册失败: ${e.message}"
                }
                _loginState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                _currentUser.value = null
                _loginState.value = AuthState.Idle
                Log.d(TAG, "Logout successful")
            } catch (e: Exception) {
                Log.e(TAG, "Logout error", e)
            }
        }
    }

    fun updateUser(user: User) {
        _currentUser.value = user
    }
}

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}