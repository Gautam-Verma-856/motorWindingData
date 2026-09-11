package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.User
import com.example.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _usersList = MutableStateFlow<List<User>>(emptyList())
    val usersList = _usersList.asStateFlow()
    
    private val _isFetchingUsers = MutableStateFlow(false)
    val isFetchingUsers = _isFetchingUsers.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val user = repo.fetchCurrentUserProfile()
            if (user != null) {
                if (user.role == "public" && user.accountStatus != "active") {
                    repo.logout()
                    _currentUser.value = null
                    _authState.value = AuthState.Idle
                } else {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val res = repo.login(email, pass)
            res.onSuccess {
                _currentUser.value = it
                _authState.value = AuthState.Success(it)
            }
            res.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, mobile: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val res = repo.registerPublicUser(name, email, mobile, pass)
            res.onSuccess {
                _currentUser.value = it
                _authState.value = AuthState.Success(it)
            }
            res.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        repo.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
    
    fun fetchUsers() {
        viewModelScope.launch {
            _isFetchingUsers.value = true
            val res = repo.getAllUsers()
            res.onSuccess { _usersList.value = it }
            _isFetchingUsers.value = false
        }
    }
    
    fun updateUserStatus(userId: String, status: String) {
        viewModelScope.launch {
            repo.updateUserStatus(userId, status)
            fetchUsers()
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repo.deleteUser(userId)
            fetchUsers()
        }
    }
    
    fun resetState() {
        if (_currentUser.value == null) {
            _authState.value = AuthState.Idle
        }
    }
}
