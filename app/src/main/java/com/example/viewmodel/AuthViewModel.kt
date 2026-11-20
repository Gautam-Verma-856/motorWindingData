package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.NotificationPreferences
import com.example.model.NotificationSettings
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

sealed class ChangePasswordState {
    object Idle : ChangePasswordState()
    object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

sealed class PasswordResetState {
    object Idle : PasswordResetState()
    object Loading : PasswordResetState()
    data class Success(val message: String) : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

sealed class ProfileUpdateState {
    object Idle : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    data class Success(val message: String) : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
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

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState = _changePasswordState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val passwordResetState = _passwordResetState.asStateFlow()

    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState = _profileUpdateState.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings = _notificationSettings.asStateFlow()

    private val _seenPublicNotificationKeys = MutableStateFlow<Set<String>>(emptySet())
    val seenPublicNotificationKeys = _seenPublicNotificationKeys.asStateFlow()

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

    fun login(email: String, pass: String, expectedRole: String? = null) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val res = repo.login(email, pass, expectedRole)
            res.onSuccess {
                _currentUser.value = it
                _authState.value = AuthState.Success(it)
            }
            res.onFailure {
                _currentUser.value = null
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun loginPublic(email: String, pass: String) = login(email, pass, "public")
    fun loginAdmin(email: String, pass: String) = login(email, pass, "admin")

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
        _usersList.value = emptyList()
        _seenPublicNotificationKeys.value = emptySet()
        resetEmailVerificationState()
        resetChangePasswordState()
        resetPasswordResetState()
        resetProfileUpdateState()
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
        _usersList.value = _usersList.value.map {
            if (it.id == userId) it.copy(accountStatus = status, adminSeen = true, seen = true) else it
        }
        viewModelScope.launch {
            repo.updateUserStatus(userId, status)
            fetchUsers()
        }
    }

    fun markUserAsSeen(userId: String) {
        _usersList.value = _usersList.value.map {
            if (it.id == userId) it.copy(adminSeen = true, seen = true) else it
        }
        viewModelScope.launch {
            repo.markUserAsSeen(userId)
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

    fun validateChangePasswordInput(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        return when {
            currentPassword.isBlank() -> "Current password is required."
            newPassword.isBlank() -> "New password is required."
            confirmPassword.isBlank() -> "Confirm new password is required."
            newPassword.length < 6 -> "New password must be at least 6 characters."
            newPassword == currentPassword -> "New password must be different from current password."
            newPassword != confirmPassword -> "New password and confirm password do not match."
            else -> null
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        expectedRole: String
    ) {
        val activeUser = _currentUser.value
        if (activeUser == null || !activeUser.role.trim().equals(expectedRole.trim(), ignoreCase = true)) {
            _changePasswordState.value = ChangePasswordState.Error(
                "Access denied: Only the signed-in ${expectedRole.lowercase()} account can change its password here."
            )
            return
        }

        val validationError = validateChangePasswordInput(currentPassword, newPassword, confirmPassword)
        if (validationError != null) {
            _changePasswordState.value = ChangePasswordState.Error(validationError)
            return
        }

        _changePasswordState.value = ChangePasswordState.Loading
        viewModelScope.launch {
            val res = repo.changeCurrentUserPassword(currentPassword, newPassword, expectedRole)
            res.onSuccess {
                _changePasswordState.value = ChangePasswordState.Success(
                    "Your password has been updated securely."
                )
            }
            res.onFailure {
                _changePasswordState.value = ChangePasswordState.Error(
                    it.message ?: "Failed to update password."
                )
            }
        }
    }

    fun resetChangePasswordState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }

    fun sendPasswordResetEmail(email: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            _passwordResetState.value = PasswordResetState.Error("Please enter a valid email address.")
            return
        }
        _passwordResetState.value = PasswordResetState.Loading
        viewModelScope.launch {
            val res = repo.sendPasswordResetEmail(trimmed)
            res.onSuccess {
                _passwordResetState.value = PasswordResetState.Success(
                    "Password reset link sent to $trimmed. Please check your inbox and spam folder."
                )
            }
            res.onFailure {
                _passwordResetState.value = PasswordResetState.Error(
                    it.message ?: "Failed to send password reset email."
                )
            }
        }
    }

    fun resetPasswordResetState() {
        _passwordResetState.value = PasswordResetState.Idle
    }

    fun updateUserProfile(name: String, mobile: String, photoUrl: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _profileUpdateState.value = ProfileUpdateState.Error("Full name cannot be empty.")
            return
        }
        val trimmedMobile = mobile.trim()
        val formattedMobile = if (trimmedMobile.isNotEmpty()) {
            formatIndianPhoneNumber(trimmedMobile) ?: trimmedMobile
        } else {
            ""
        }

        _profileUpdateState.value = ProfileUpdateState.Loading
        viewModelScope.launch {
            val res = repo.updateUserProfile(trimmedName, formattedMobile, photoUrl.trim())
            res.onSuccess { updatedUser ->
                _currentUser.value = updatedUser
                _profileUpdateState.value = ProfileUpdateState.Success("Profile updated successfully.")
            }
            res.onFailure {
                _profileUpdateState.value = ProfileUpdateState.Error(
                    it.message ?: "Failed to update profile."
                )
            }
        }
    }

    fun resetProfileUpdateState() {
        _profileUpdateState.value = ProfileUpdateState.Idle
    }

    fun syncNotificationPreferences(context: Context) {
        _notificationSettings.value = NotificationPreferences.loadSettings(context)
        val uid = _currentUser.value?.id.orEmpty()
        if (uid.isNotBlank()) {
            _seenPublicNotificationKeys.value = NotificationPreferences.loadSeenPublicKeys(context, uid)
        }
    }

    fun updateNotificationSettings(context: Context, settings: NotificationSettings) {
        _notificationSettings.value = settings
        NotificationPreferences.saveSettings(context, settings)
    }

    fun markPublicNotificationSeen(context: Context, key: String) {
        val uid = _currentUser.value?.id.orEmpty()
        if (key.isBlank()) return
        val updated = _seenPublicNotificationKeys.value + key
        _seenPublicNotificationKeys.value = updated
        if (uid.isNotBlank()) {
            NotificationPreferences.saveSeenPublicKeys(context, uid, updated)
        }
    }

    fun markAllPublicNotificationsSeen(context: Context, keys: Collection<String>) {
        val uid = _currentUser.value?.id.orEmpty()
        if (keys.isEmpty()) return
        val updated = _seenPublicNotificationKeys.value + keys
        _seenPublicNotificationKeys.value = updated
        if (uid.isNotBlank()) {
            NotificationPreferences.saveSeenPublicKeys(context, uid, updated)
        }
    }
}
