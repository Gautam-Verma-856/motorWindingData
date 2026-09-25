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

sealed class EmailVerificationStatus {
    object Idle : EmailVerificationStatus()
    object Checking : EmailVerificationStatus()
    object Verified : EmailVerificationStatus()
    data class NotVerified(val message: String) : EmailVerificationStatus()
    object Resent : EmailVerificationStatus()
    data class Error(val message: String) : EmailVerificationStatus()
}

class AuthViewModel : ViewModel() {
    private val repo = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _emailVerificationStatus = MutableStateFlow<EmailVerificationStatus>(EmailVerificationStatus.Idle)
    val emailVerificationStatus = _emailVerificationStatus.asStateFlow()

    private val _registeredEmail = MutableStateFlow<String?>(null)
    val registeredEmail = _registeredEmail.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _usersList = MutableStateFlow<List<User>>(emptyList())
    val usersList = _usersList.asStateFlow()
    
    private val _isFetchingUsers = MutableStateFlow(false)
    val isFetchingUsers = _isFetchingUsers.asStateFlow()

    private val _isSessionChecking = MutableStateFlow(true)
    val isSessionChecking = _isSessionChecking.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            _isSessionChecking.value = true
            try {
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
                } else {
                    _currentUser.value = null
                    _authState.value = AuthState.Idle
                }
            } catch (_: Exception) {
                _currentUser.value = null
                _authState.value = AuthState.Idle
            } finally {
                _isSessionChecking.value = false
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

    fun formatIndianPhoneNumber(raw: String): String? {
        val trimmed = raw.trim().replace(" ", "").replace("-", "")
        return when {
            trimmed.startsWith("+91") && trimmed.length == 13 -> trimmed
            trimmed.startsWith("91") && trimmed.length == 12 -> "+$trimmed"
            trimmed.startsWith("0") && trimmed.length == 11 -> "+91${trimmed.substring(1)}"
            trimmed.length == 10 && (trimmed.startsWith("6") || trimmed.startsWith("7") || trimmed.startsWith("8") || trimmed.startsWith("9")) -> "+91$trimmed"
            else -> null
        }
    }

    fun register(name: String, email: String, mobile: String, pass: String) {
        val formatted = formatIndianPhoneNumber(mobile) ?: mobile.trim()
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val res = repo.registerPublicUser(name, email, formatted, pass)
            res.onSuccess {
                _registeredEmail.value = it.email
                _emailVerificationStatus.value = EmailVerificationStatus.Idle
                _authState.value = AuthState.Idle
            }
            res.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun checkEmailVerification() {
        _emailVerificationStatus.value = EmailVerificationStatus.Checking
        viewModelScope.launch {
            val res = repo.checkCurrentUserEmailVerification()
            res.onSuccess { verified ->
                if (verified) {
                    _emailVerificationStatus.value = EmailVerificationStatus.Verified
                } else {
                    _emailVerificationStatus.value = EmailVerificationStatus.NotVerified(
                        "Email is not verified yet. Please check your inbox (and spam folder) and click the verification link."
                    )
                }
            }
            res.onFailure {
                _emailVerificationStatus.value = EmailVerificationStatus.Error(
                    it.message ?: "Unable to check verification status."
                )
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            val res = repo.resendVerificationEmail()
            res.onSuccess {
                _emailVerificationStatus.value = EmailVerificationStatus.Resent
            }
            res.onFailure {
                _emailVerificationStatus.value = EmailVerificationStatus.Error(
                    it.message ?: "Failed to resend verification email."
                )
            }
        }
    }

    fun resendVerificationEmailForLogin(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repo.resendVerificationEmailForCredentials(email, pass)
            res.onSuccess {
                onResult(true, "Verification email resent successfully! Please check your inbox.")
            }
            res.onFailure {
                onResult(false, it.message ?: "Failed to resend verification email.")
            }
        }
    }

    fun resetEmailVerificationState() {
        _emailVerificationStatus.value = EmailVerificationStatus.Idle
        _registeredEmail.value = null
    }

    fun logout() {
        repo.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
        resetEmailVerificationState()
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
