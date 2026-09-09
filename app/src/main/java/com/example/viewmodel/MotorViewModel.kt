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
    object Success : UiState()
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

    fun loadData() {
        viewModelScope.launch {
            repo.getSinglePhaseMotors().onSuccess { _singlePhaseMotors.value = it }
            repo.getThreePhaseMotors().onSuccess { _threePhaseMotors.value = it }
        }
    }

    suspend fun uploadPhoto(uri: Uri): String? {
        val result = repo.uploadImage(uri)
        return result.getOrNull()
    }

    fun saveSinglePhase(motor: SinglePhaseMotor) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveSinglePhaseMotor(motor).onSuccess {
                loadData()
                _uiState.value = UiState.Success
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save")
            }
        }
    }

    fun deleteSinglePhase(id: String) {
        viewModelScope.launch {
            repo.deleteSinglePhaseMotor(id).onSuccess {
                loadData()
            }.onFailure {
                // Optionally handle error
            }
        }
    }

    fun saveThreePhase(motor: ThreePhaseMotor) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveThreePhaseMotor(motor).onSuccess {
                loadData()
                _uiState.value = UiState.Success
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save")
            }
        }
    }

    fun deleteThreePhase(id: String) {
        viewModelScope.launch {
            repo.deleteThreePhaseMotor(id).onSuccess {
                loadData()
            }.onFailure {
                // Optionally handle error
            }
        }
    }
    
    fun resetUiState() {
        _uiState.value = UiState.Idle
    }
}
