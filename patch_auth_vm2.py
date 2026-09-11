with open("app/src/main/java/com/example/viewmodel/AuthViewModel.kt", "r") as f:
    content = f.read()

target = """    private fun checkSession() {
        viewModelScope.launch {
            val user = repo.fetchCurrentUserProfile()
            if (user != null) {
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            }
        }
    }"""

replacement = """    private fun checkSession() {
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
    }"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/viewmodel/AuthViewModel.kt", "w") as f:
    f.write(content)
