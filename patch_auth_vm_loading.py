import re

with open("app/src/main/java/com/example/viewmodel/AuthViewModel.kt", "r") as f:
    content = f.read()

new_users_list = """    private val _usersList = MutableStateFlow<List<User>>(emptyList())
    val usersList = _usersList.asStateFlow()
    
    private val _isFetchingUsers = MutableStateFlow(false)
    val isFetchingUsers = _isFetchingUsers.asStateFlow()"""
    
content = content.replace("    private val _usersList = MutableStateFlow<List<User>>(emptyList())\n    val usersList = _usersList.asStateFlow()", new_users_list)

old_fetch = """    fun fetchUsers() {
        viewModelScope.launch {
            val res = repo.getAllUsers()
            res.onSuccess { _usersList.value = it }
        }
    }"""
    
new_fetch = """    fun fetchUsers() {
        viewModelScope.launch {
            _isFetchingUsers.value = true
            val res = repo.getAllUsers()
            res.onSuccess { _usersList.value = it }
            _isFetchingUsers.value = false
        }
    }"""
content = content.replace(old_fetch, new_fetch)

with open("app/src/main/java/com/example/viewmodel/AuthViewModel.kt", "w") as f:
    f.write(content)
