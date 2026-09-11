package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
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
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val _isLoadingData = MutableStateFlow(false)
    val isLoadingData = _isLoadingData.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            _isLoadingData.value = true
            repo.getSinglePhaseMotors().onSuccess { _singlePhaseMotors.value = it }
            repo.getThreePhaseMotors().onSuccess { _threePhaseMotors.value = it }
            _isLoadingData.value = false
        }
    }
    
    suspend fun uploadPhoto(uri: Uri): String? {
        return repo.uploadImage(uri).getOrNull()
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
            repo.updateSinglePhaseStatus(id, status).onSuccess {
                loadData()
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }
    
    fun updateThreePhaseStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.updateThreePhaseStatus(id, status).onSuccess {
                loadData()
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }
    
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}
