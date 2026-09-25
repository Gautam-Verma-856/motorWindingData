package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.model.CustomCategory
import com.example.model.CustomMotor
import com.example.repository.MotorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val message: String = "") : UiState()
    data class Error(val message: String) : UiState()
}

class MotorViewModel : ViewModel() {
    private val repo = MotorRepository()
    private val _singlePhaseMotors = MutableStateFlow<List<SinglePhaseMotor>>(emptyList())
    val singlePhaseMotors = _singlePhaseMotors.asStateFlow()
    private val _threePhaseMotors = MutableStateFlow<List<ThreePhaseMotor>>(emptyList())
    val threePhaseMotors = _threePhaseMotors.asStateFlow()
    
    private val _customCategories = MutableStateFlow<List<CustomCategory>>(emptyList())
    val customCategories = _customCategories.asStateFlow()
    
    private val _customMotors = MutableStateFlow<List<CustomMotor>>(emptyList())
    val customMotors = _customMotors.asStateFlow()
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val _isLoadingData = MutableStateFlow(false)
    val isLoadingData = _isLoadingData.asStateFlow()

    fun clearData() {
        _singlePhaseMotors.value = emptyList()
        _threePhaseMotors.value = emptyList()
        _customCategories.value = emptyList()
        _customMotors.value = emptyList()
        _uiState.value = UiState.Idle
        _isLoadingData.value = false
    }

    fun loadData(isAdmin: Boolean = false) {
        val currentFirebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentFirebaseUser == null) {
            clearData()
            return
        }
        viewModelScope.launch {
            _isLoadingData.value = true
            repo.getSinglePhaseMotors().onSuccess { _singlePhaseMotors.value = it }
            repo.getThreePhaseMotors().onSuccess { _threePhaseMotors.value = it }
            repo.getCustomCategories(isAdmin).onSuccess { _customCategories.value = it }
            repo.getCustomMotors().onSuccess { _customMotors.value = it }
            _isLoadingData.value = false
        }
    }
    
    suspend fun uploadPhoto(context: Context, uri: Uri): Result<String> {
        return repo.uploadImage(context, uri)
    }
    
    fun saveSinglePhase(motor: SinglePhaseMotor) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveSinglePhaseMotor(motor).onSuccess {
                loadData()
                _uiState.value = UiState.Success("save")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save")
            }
        }
    }
    
    fun deleteSinglePhase(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.deleteSinglePhaseMotor(id).onSuccess {
                loadData()
                _uiState.value = UiState.Success("delete")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete")
            }
        }
    }
    
    fun saveThreePhase(motor: ThreePhaseMotor) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveThreePhaseMotor(motor).onSuccess {
                loadData()
                _uiState.value = UiState.Success("save")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save")
            }
        }
    }
    
    fun deleteThreePhase(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.deleteThreePhaseMotor(id).onSuccess {
                loadData()
                _uiState.value = UiState.Success("delete")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete")
            }
        }
    }
    
    fun updateSinglePhaseStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Optimistically update local list so UI immediately reflects the new status
            _singlePhaseMotors.value = _singlePhaseMotors.value.map {
                if (it.id == id) it.copy(status = status) else it
            }
            repo.updateSinglePhaseStatus(id, status).onSuccess {
                loadData()
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                loadData()
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }
    
    fun updateThreePhaseStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Optimistically update local list so UI immediately reflects the new status
            _threePhaseMotors.value = _threePhaseMotors.value.map {
                if (it.id == id) it.copy(status = status) else it
            }
            repo.updateThreePhaseStatus(id, status).onSuccess {
                loadData()
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                loadData()
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }

    // --- Custom Category Operations ---
    fun saveCustomCategory(category: CustomCategory) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveCustomCategory(category).onSuccess {
                loadData(isAdmin = true)
                _uiState.value = UiState.Success("save_category")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save category")
            }
        }
    }

    fun deleteCustomCategory(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.deleteCustomCategory(id).onSuccess {
                loadData(isAdmin = true)
                _uiState.value = UiState.Success("delete_category")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete category")
            }
        }
    }

    // --- Custom Motor Operations ---
    fun saveCustomMotor(motor: CustomMotor) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveCustomMotor(motor).onSuccess {
                loadData()
                _uiState.value = UiState.Success("save_custom_motor")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save motor data")
            }
        }
    }

    fun deleteCustomMotor(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.deleteCustomMotor(id).onSuccess {
                loadData()
                _uiState.value = UiState.Success("delete_custom_motor")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to delete motor data")
            }
        }
    }

    fun updateCustomMotorStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Optimistically update local list so UI immediately reflects the new status
            _customMotors.value = _customMotors.value.map {
                if (it.id == id) it.copy(status = status) else it
            }
            repo.updateCustomMotorStatus(id, status).onSuccess {
                loadData()
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                loadData()
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }

    // Explicitly approved getters for public browsing
    fun getApprovedSinglePhaseMotors(): List<SinglePhaseMotor> {
        return _singlePhaseMotors.value.filter { it.isExplicitlyApproved() }
    }

    fun getApprovedThreePhaseMotors(): List<ThreePhaseMotor> {
        return _threePhaseMotors.value.filter { it.isExplicitlyApproved() }
    }

    fun getApprovedCustomMotors(categoryId: String? = null): List<CustomMotor> {
        return _customMotors.value.filter {
            it.isExplicitlyApproved() && (categoryId == null || it.categoryId == categoryId)
        }
    }
    
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    fun getImgBBConfigDiagnostic(): MotorRepository.ImgBBDiagnostic {
        return repo.getImgBBConfigDiagnostic()
    }

    fun getSafeDiagnostic(): MotorRepository.SafeDiagnostic {
        return repo.getSafeDiagnostic()
    }
}
