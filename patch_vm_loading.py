import re

with open("app/src/main/java/com/example/viewmodel/MotorViewModel.kt", "r") as f:
    content = f.read()

new_state = """    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val _isLoadingData = MutableStateFlow(false)
    val isLoadingData = _isLoadingData.asStateFlow()"""
    
content = content.replace("    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)\n    val uiState = _uiState.asStateFlow()", new_state)

old_load = """    fun loadData() {
        viewModelScope.launch {
            repo.getSinglePhaseMotors().onSuccess { _singlePhaseMotors.value = it }
            repo.getThreePhaseMotors().onSuccess { _threePhaseMotors.value = it }
        }
    }"""
new_load = """    fun loadData() {
        viewModelScope.launch {
            _isLoadingData.value = true
            repo.getSinglePhaseMotors().onSuccess { _singlePhaseMotors.value = it }
            repo.getThreePhaseMotors().onSuccess { _threePhaseMotors.value = it }
            _isLoadingData.value = false
        }
    }"""
content = content.replace(old_load, new_load)

with open("app/src/main/java/com/example/viewmodel/MotorViewModel.kt", "w") as f:
    f.write(content)
