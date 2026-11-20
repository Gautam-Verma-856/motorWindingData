package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CustomCategory
import com.example.model.CustomMotor
import com.example.model.MotorSearchEngine
import com.example.model.MotorSearchFilter
import com.example.model.MotorSearchItem
import com.example.model.SearchMotorType
import com.example.model.SearchSortOption
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import com.example.repository.MotorRepository
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Job
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

    private var lastIsAdmin: Boolean = false
    private var lastUserId: String? = null

    // --- Advanced Search State & Paginated Cache ---
    private val _draftSearchFilter = MutableStateFlow(MotorSearchFilter())
    val draftSearchFilter = _draftSearchFilter.asStateFlow()

    private val _appliedSearchFilter = MutableStateFlow(MotorSearchFilter())
    val appliedSearchFilter = _appliedSearchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<MotorSearchItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading = _isSearchLoading.asStateFlow()

    private val _isSearchLoadingMore = MutableStateFlow(false)
    val isSearchLoadingMore = _isSearchLoadingMore.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError = _searchError.asStateFlow()

    private val _hasMoreSearchResults = MutableStateFlow(false)
    val hasMoreSearchResults = _hasMoreSearchResults.asStateFlow()

    private val _loadedSearchCount = MutableStateFlow(0)
    val loadedSearchCount = _loadedSearchCount.asStateFlow()

    private val _hasExecutedSearch = MutableStateFlow(false)
    val hasExecutedSearch = _hasExecutedSearch.asStateFlow()

    private val _searchValidationMessage = MutableStateFlow<String?>(null)
    val searchValidationMessage = _searchValidationMessage.asStateFlow()

    private var _searchSingleCache: List<SinglePhaseMotor> = emptyList()
    private var _searchThreeCache: List<ThreePhaseMotor> = emptyList()
    private var searchSingleCursor: DocumentSnapshot? = null
    private var searchThreeCursor: DocumentSnapshot? = null
    private var hasMoreSearchSingle: Boolean = false
    private var hasMoreSearchThree: Boolean = false
    private var hasLoadedSingleSearchCache: Boolean = false
    private var hasLoadedThreeSearchCache: Boolean = false
    private var hasInitializedSearchCache: Boolean = false
    private var searchCacheIsAdmin: Boolean? = null
    private var activeSearchJob: Job? = null

    fun clearData() {
        activeSearchJob?.cancel()
        activeSearchJob = null
        _singlePhaseMotors.value = emptyList()
        _threePhaseMotors.value = emptyList()
        _customCategories.value = emptyList()
        _customMotors.value = emptyList()
        _uiState.value = UiState.Idle
        _isLoadingData.value = false
        lastIsAdmin = false
        lastUserId = null

        _draftSearchFilter.value = MotorSearchFilter()
        _appliedSearchFilter.value = MotorSearchFilter()
        _searchResults.value = emptyList()
        _isSearchLoading.value = false
        _isSearchLoadingMore.value = false
        _searchError.value = null
        _searchValidationMessage.value = null
        _hasExecutedSearch.value = false
        _hasMoreSearchResults.value = false
        _loadedSearchCount.value = 0
        _searchSingleCache = emptyList()
        _searchThreeCache = emptyList()
        searchSingleCursor = null
        searchThreeCursor = null
        hasMoreSearchSingle = false
        hasMoreSearchThree = false
        hasLoadedSingleSearchCache = false
        hasLoadedThreeSearchCache = false
        hasInitializedSearchCache = false
        searchCacheIsAdmin = null
    }

    private var isTestSeededMotors: Boolean = false

    fun loadData(isAdmin: Boolean = lastIsAdmin, userId: String? = lastUserId) {
        if (isTestSeededMotors) return
        val currentFirebaseUser = runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }.getOrNull()
        if (currentFirebaseUser == null) {
            clearData()
            return
        }
        lastIsAdmin = isAdmin
        val effectiveUid = userId?.ifBlank { currentFirebaseUser.uid } ?: currentFirebaseUser.uid
        lastUserId = effectiveUid

        viewModelScope.launch {
            _isLoadingData.value = true
            repo.getSinglePhaseMotors(isAdmin, effectiveUid).onSuccess { _singlePhaseMotors.value = it }
            repo.getThreePhaseMotors(isAdmin, effectiveUid).onSuccess { _threePhaseMotors.value = it }
            _isLoadingData.value = false
        }
    }
    
    suspend fun uploadPhoto(context: Context, uri: Uri): Result<String> {
        return repo.uploadImage(context, uri)
    }
    
    fun saveSinglePhase(motor: SinglePhaseMotor) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveSinglePhaseMotor(motor).onSuccess { savedMotor ->
                // Optimistically update list so the new/edited motor appears immediately
                val currentList = _singlePhaseMotors.value
                val updatedList = if (currentList.any { it.id == savedMotor.id }) {
                    currentList.map { if (it.id == savedMotor.id) savedMotor else it }
                } else {
                    listOf(savedMotor) + currentList
                }
                _singlePhaseMotors.value = updatedList
                upsertSearchSingleCache(savedMotor)
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success("save")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save")
            }
        }
    }
    
    fun deleteSinglePhase(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _singlePhaseMotors.value = _singlePhaseMotors.value.filter { it.id != id }
            removeFromSearchSingleCache(id)
            repo.deleteSinglePhaseMotor(id).onSuccess {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success("delete")
            }.onFailure {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Error(it.message ?: "Failed to delete")
            }
        }
    }
    
    fun saveThreePhase(motor: ThreePhaseMotor) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveThreePhaseMotor(motor).onSuccess { savedMotor ->
                val currentList = _threePhaseMotors.value
                val updatedList = if (currentList.any { it.id == savedMotor.id }) {
                    currentList.map { if (it.id == savedMotor.id) savedMotor else it }
                } else {
                    listOf(savedMotor) + currentList
                }
                _threePhaseMotors.value = updatedList
                upsertSearchThreeCache(savedMotor)
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success("save")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save")
            }
        }
    }
    
    fun deleteThreePhase(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _threePhaseMotors.value = _threePhaseMotors.value.filter { it.id != id }
            removeFromSearchThreeCache(id)
            repo.deleteThreePhaseMotor(id).onSuccess {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success("delete")
            }.onFailure {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Error(it.message ?: "Failed to delete")
            }
        }
    }
    
    fun updateSinglePhaseStatus(id: String, status: String, rejectionReason: String = "") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Optimistically update local list so UI immediately reflects the new status
            _singlePhaseMotors.value = _singlePhaseMotors.value.map {
                if (it.id == id) it.copy(
                    status = status,
                    rejectionReason = if (status == "rejected") rejectionReason.trim() else "",
                    adminSeen = true,
                    seen = true
                ) else it
            }
            updateSearchSingleStatusCache(id, status, rejectionReason)
            repo.updateSinglePhaseStatus(id, status, rejectionReason).onSuccess {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }
    
    fun updateThreePhaseStatus(id: String, status: String, rejectionReason: String = "") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Optimistically update local list so UI immediately reflects the new status
            _threePhaseMotors.value = _threePhaseMotors.value.map {
                if (it.id == id) it.copy(
                    status = status,
                    rejectionReason = if (status == "rejected") rejectionReason.trim() else "",
                    adminSeen = true,
                    seen = true
                ) else it
            }
            updateSearchThreeStatusCache(id, status, rejectionReason)
            repo.updateThreePhaseStatus(id, status, rejectionReason).onSuccess {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }

    // --- Custom Category Operations ---
    fun saveCustomCategory(category: CustomCategory) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repo.saveCustomCategory(category).onSuccess {
                loadData(isAdmin = true, userId = lastUserId)
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
                loadData(isAdmin = true, userId = lastUserId)
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
            repo.saveCustomMotor(motor).onSuccess { savedMotor ->
                val currentList = _customMotors.value
                val updatedList = if (currentList.any { it.id == savedMotor.id }) {
                    currentList.map { if (it.id == savedMotor.id) savedMotor else it }
                } else {
                    listOf(savedMotor) + currentList
                }
                _customMotors.value = updatedList
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success("save_custom_motor")
            }.onFailure {
                _uiState.value = UiState.Error(it.message ?: "Failed to save motor data")
            }
        }
    }

    fun deleteCustomMotor(id: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _customMotors.value = _customMotors.value.filter { it.id != id }
            repo.deleteCustomMotor(id).onSuccess {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success("delete_custom_motor")
            }.onFailure {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Error(it.message ?: "Failed to delete motor data")
            }
        }
    }

    fun updateCustomMotorStatus(id: String, status: String, rejectionReason: String = "") {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Optimistically update local list so UI immediately reflects the new status
            _customMotors.value = _customMotors.value.map {
                if (it.id == id) it.copy(
                    status = status,
                    rejectionReason = if (status == "rejected") rejectionReason.trim() else "",
                    adminSeen = true,
                    seen = true
                ) else it
            }
            repo.updateCustomMotorStatus(id, status, rejectionReason).onSuccess {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Success(if (status == "approved") "approve" else "reject")
            }.onFailure {
                loadData(lastIsAdmin, lastUserId)
                _uiState.value = UiState.Error(it.message ?: "Failed to update status")
            }
        }
    }

    fun markMotorAsSeen(type: String, id: String) {
        when (type.lowercase()) {
            "single" -> {
                _singlePhaseMotors.value = _singlePhaseMotors.value.map {
                    if (it.id == id) it.copy(adminSeen = true, seen = true) else it
                }
            }
            "three" -> {
                _threePhaseMotors.value = _threePhaseMotors.value.map {
                    if (it.id == id) it.copy(adminSeen = true, seen = true) else it
                }
            }
            "custom" -> {
                _customMotors.value = _customMotors.value.map {
                    if (it.id == id) it.copy(adminSeen = true, seen = true) else it
                }
            }
        }
        viewModelScope.launch {
            repo.markMotorAsSeen(type, id)
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

    // --- Advanced Search Operations ---
    private fun invalidateSearchCacheOnly() {
        _searchSingleCache = emptyList()
        _searchThreeCache = emptyList()
        searchSingleCursor = null
        searchThreeCursor = null
        hasMoreSearchSingle = false
        hasMoreSearchThree = false
        hasLoadedSingleSearchCache = false
        hasLoadedThreeSearchCache = false
        hasInitializedSearchCache = false
        _loadedSearchCount.value = 0
        _hasMoreSearchResults.value = false
    }

    private fun recomputeSearchResults(isAdmin: Boolean = searchCacheIsAdmin ?: lastIsAdmin) {
        if (!_hasExecutedSearch.value) {
            _searchResults.value = emptyList()
            _hasMoreSearchResults.value = false
            return
        }
        val unified = MotorSearchEngine.buildUnifiedList(_searchSingleCache, _searchThreeCache)
        _loadedSearchCount.value = unified.size
        _searchResults.value = MotorSearchEngine.filterAndSort(
            items = unified,
            filter = _appliedSearchFilter.value,
            isAdmin = isAdmin
        )
        _hasMoreSearchResults.value = when (_appliedSearchFilter.value.motorType) {
            SearchMotorType.SINGLE_PHASE -> hasMoreSearchSingle
            SearchMotorType.THREE_PHASE -> hasMoreSearchThree
            SearchMotorType.ALL -> hasMoreSearchSingle || hasMoreSearchThree
        }
    }

    private fun upsertSearchSingleCache(motor: SinglePhaseMotor) {
        if (!hasLoadedSingleSearchCache && !hasInitializedSearchCache) return
        val isAdmin = searchCacheIsAdmin ?: lastIsAdmin
        if (!isAdmin && !motor.isExplicitlyApproved()) return
        _searchSingleCache = if (_searchSingleCache.any { it.id == motor.id }) {
            _searchSingleCache.map { if (it.id == motor.id) motor else it }
        } else {
            listOf(motor) + _searchSingleCache
        }
        recomputeSearchResults(isAdmin)
    }

    private fun upsertSearchThreeCache(motor: ThreePhaseMotor) {
        if (!hasLoadedThreeSearchCache && !hasInitializedSearchCache) return
        val isAdmin = searchCacheIsAdmin ?: lastIsAdmin
        if (!isAdmin && !motor.isExplicitlyApproved()) return
        _searchThreeCache = if (_searchThreeCache.any { it.id == motor.id }) {
            _searchThreeCache.map { if (it.id == motor.id) motor else it }
        } else {
            listOf(motor) + _searchThreeCache
        }
        recomputeSearchResults(isAdmin)
    }

    private fun removeFromSearchSingleCache(id: String) {
        if (!hasLoadedSingleSearchCache && !hasInitializedSearchCache) return
        _searchSingleCache = _searchSingleCache.filter { it.id != id }
        recomputeSearchResults()
    }

    private fun removeFromSearchThreeCache(id: String) {
        if (!hasLoadedThreeSearchCache && !hasInitializedSearchCache) return
        _searchThreeCache = _searchThreeCache.filter { it.id != id }
        recomputeSearchResults()
    }

    private fun updateSearchSingleStatusCache(id: String, status: String, rejectionReason: String) {
        if (!hasLoadedSingleSearchCache && !hasInitializedSearchCache) return
        _searchSingleCache = _searchSingleCache.map {
            if (it.id == id) it.copy(
                status = status,
                rejectionReason = if (status == "rejected") rejectionReason.trim() else "",
                adminSeen = true,
                seen = true
            ) else it
        }
        recomputeSearchResults()
    }

    private fun updateSearchThreeStatusCache(id: String, status: String, rejectionReason: String) {
        if (!hasLoadedThreeSearchCache && !hasInitializedSearchCache) return
        _searchThreeCache = _searchThreeCache.map {
            if (it.id == id) it.copy(
                status = status,
                rejectionReason = if (status == "rejected") rejectionReason.trim() else "",
                adminSeen = true,
                seen = true
            ) else it
        }
        recomputeSearchResults()
    }

    private fun currentAuthUserOrNull(): com.google.firebase.auth.FirebaseUser? {
        return try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }
    }

    fun resetSearchSession() {
        activeSearchJob?.cancel()
        activeSearchJob = null
        _draftSearchFilter.value = MotorSearchFilter()
        _appliedSearchFilter.value = MotorSearchFilter()
        _hasExecutedSearch.value = false
        _searchResults.value = emptyList()
        _searchValidationMessage.value = null
        _searchError.value = null
        _isSearchLoading.value = false
        _isSearchLoadingMore.value = false
    }

    fun initSearch(isAdmin: Boolean) {
        if (searchCacheIsAdmin != null && searchCacheIsAdmin != isAdmin) {
            resetSearchSession()
            invalidateSearchCacheOnly()
        }
        searchCacheIsAdmin = isAdmin
        if (_hasExecutedSearch.value && hasInitializedSearchCache) {
            recomputeSearchResults(isAdmin)
        } else if (!_hasExecutedSearch.value) {
            _searchResults.value = emptyList()
        }
    }

    fun executeSearch(isAdmin: Boolean = searchCacheIsAdmin ?: lastIsAdmin) {
        if (searchCacheIsAdmin != null && searchCacheIsAdmin != isAdmin) {
            invalidateSearchCacheOnly()
        }
        searchCacheIsAdmin = isAdmin

        val draft = _draftSearchFilter.value
        val sanitized = if (isAdmin) {
            draft
        } else {
            draft.copy(approvalStatus = com.example.model.SearchApprovalStatus.ALL)
        }

        if (!sanitized.hasAnyActiveFilter(isAdmin)) {
            activeSearchJob?.cancel()
            activeSearchJob = null
            _isSearchLoading.value = false
            _hasExecutedSearch.value = false
            _searchResults.value = emptyList()
            _searchError.value = null
            _searchValidationMessage.value = "Please enter a search keyword or select at least one filter."
            return
        }

        _searchValidationMessage.value = null
        _searchError.value = null
        _draftSearchFilter.value = sanitized
        _appliedSearchFilter.value = sanitized
        _hasExecutedSearch.value = true

        val needSingleFetch = !hasLoadedSingleSearchCache && sanitized.motorType != SearchMotorType.THREE_PHASE
        val needThreeFetch = !hasLoadedThreeSearchCache && sanitized.motorType != SearchMotorType.SINGLE_PHASE

        if (!needSingleFetch && !needThreeFetch) {
            recomputeSearchResults(isAdmin)
            return
        }

        // Prevent duplicate concurrent Firestore queries
        if (activeSearchJob?.isActive == true) {
            return
        }

        val currentFirebaseUser = currentAuthUserOrNull()
        if (currentFirebaseUser == null || currentFirebaseUser.uid.isBlank()) {
            _searchError.value = "Authentication required to search motors."
            return
        }

        activeSearchJob = viewModelScope.launch {
            _isSearchLoading.value = true
            _searchError.value = null
            repo.fetchSearchMotorPage(
                isAdmin = isAdmin,
                motorType = sanitized.motorType,
                fetchSingle = needSingleFetch,
                fetchThree = needThreeFetch,
                singleCursor = if (hasLoadedSingleSearchCache) searchSingleCursor else null,
                threeCursor = if (hasLoadedThreeSearchCache) searchThreeCursor else null,
                pageSize = 25L
            ).onSuccess { page ->
                if (needSingleFetch) {
                    _searchSingleCache = page.singlePhaseMotors.distinctBy { it.id }
                    searchSingleCursor = page.nextSingleCursor
                    hasMoreSearchSingle = page.hasMoreSingle
                    hasLoadedSingleSearchCache = true
                }
                if (needThreeFetch) {
                    _searchThreeCache = page.threePhaseMotors.distinctBy { it.id }
                    searchThreeCursor = page.nextThreeCursor
                    hasMoreSearchThree = page.hasMoreThree
                    hasLoadedThreeSearchCache = true
                }
                hasInitializedSearchCache = hasLoadedSingleSearchCache || hasLoadedThreeSearchCache
                recomputeSearchResults(isAdmin)
            }.onFailure { err ->
                _searchError.value = err.message ?: "Failed to load motors from cloud."
            }
            _isSearchLoading.value = false
        }
    }

    fun refreshSearch(isAdmin: Boolean = searchCacheIsAdmin ?: lastIsAdmin) {
        if (!_hasExecutedSearch.value) {
            executeSearch(isAdmin)
            return
        }
        val activeFilter = _appliedSearchFilter.value
        if (!activeFilter.hasAnyActiveFilter(isAdmin)) {
            executeSearch(isAdmin)
            return
        }
        val currentFirebaseUser = currentAuthUserOrNull()
        if (currentFirebaseUser == null || currentFirebaseUser.uid.isBlank()) {
            _searchError.value = "Authentication required to search motors."
            return
        }
        activeSearchJob?.cancel()
        searchCacheIsAdmin = isAdmin
        invalidateSearchCacheOnly()
        val fetchSingle = activeFilter.motorType != SearchMotorType.THREE_PHASE
        val fetchThree = activeFilter.motorType != SearchMotorType.SINGLE_PHASE

        activeSearchJob = viewModelScope.launch {
            _isSearchLoading.value = true
            _searchError.value = null
            repo.fetchSearchMotorPage(
                isAdmin = isAdmin,
                motorType = activeFilter.motorType,
                fetchSingle = fetchSingle,
                fetchThree = fetchThree,
                singleCursor = null,
                threeCursor = null,
                pageSize = 25L
            ).onSuccess { page ->
                if (fetchSingle) {
                    _searchSingleCache = page.singlePhaseMotors.distinctBy { it.id }
                    searchSingleCursor = page.nextSingleCursor
                    hasMoreSearchSingle = page.hasMoreSingle
                    hasLoadedSingleSearchCache = true
                }
                if (fetchThree) {
                    _searchThreeCache = page.threePhaseMotors.distinctBy { it.id }
                    searchThreeCursor = page.nextThreeCursor
                    hasMoreSearchThree = page.hasMoreThree
                    hasLoadedThreeSearchCache = true
                }
                hasInitializedSearchCache = hasLoadedSingleSearchCache || hasLoadedThreeSearchCache
                recomputeSearchResults(isAdmin)
            }.onFailure { err ->
                _searchError.value = err.message ?: "Failed to load motors from cloud."
            }
            _isSearchLoading.value = false
        }
    }

    fun loadMoreSearchResults() {
        val isAdmin = searchCacheIsAdmin ?: lastIsAdmin
        if (!_hasExecutedSearch.value || _isSearchLoading.value || _isSearchLoadingMore.value) return
        val needSingle = _appliedSearchFilter.value.motorType != SearchMotorType.THREE_PHASE && hasMoreSearchSingle
        val needThree = _appliedSearchFilter.value.motorType != SearchMotorType.SINGLE_PHASE && hasMoreSearchThree
        if (!needSingle && !needThree) return

        viewModelScope.launch {
            _isSearchLoadingMore.value = true
            _searchError.value = null
            repo.fetchSearchMotorPage(
                isAdmin = isAdmin,
                motorType = _appliedSearchFilter.value.motorType,
                fetchSingle = needSingle,
                fetchThree = needThree,
                singleCursor = searchSingleCursor,
                threeCursor = searchThreeCursor,
                pageSize = 25L
            ).onSuccess { page ->
                if (needSingle) {
                    _searchSingleCache = (_searchSingleCache + page.singlePhaseMotors).distinctBy { it.id }
                    searchSingleCursor = page.nextSingleCursor
                    hasMoreSearchSingle = page.hasMoreSingle
                    hasLoadedSingleSearchCache = true
                }
                if (needThree) {
                    _searchThreeCache = (_searchThreeCache + page.threePhaseMotors).distinctBy { it.id }
                    searchThreeCursor = page.nextThreeCursor
                    hasMoreSearchThree = page.hasMoreThree
                    hasLoadedThreeSearchCache = true
                }
                hasInitializedSearchCache = hasLoadedSingleSearchCache || hasLoadedThreeSearchCache
                recomputeSearchResults(isAdmin)
            }.onFailure { err ->
                _searchError.value = err.message ?: "Failed to load more results."
            }
            _isSearchLoadingMore.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _draftSearchFilter.value = _draftSearchFilter.value.copy(query = query)
        if (query.isNotBlank() && _searchValidationMessage.value != null) {
            _searchValidationMessage.value = null
        }
    }

    fun updateDraftFilter(transform: (MotorSearchFilter) -> MotorSearchFilter) {
        val updated = transform(_draftSearchFilter.value)
        val validCategories = MotorSearchEngine.categoriesForMotorType(updated.motorType)
        val sanitizedCategory = if (validCategories.any { it.equals(updated.category, ignoreCase = true) }) {
            updated.category
        } else {
            "All"
        }
        val finalFilter = updated.copy(category = sanitizedCategory)
        _draftSearchFilter.value = finalFilter
        val isAdmin = searchCacheIsAdmin ?: lastIsAdmin
        if (finalFilter.hasAnyActiveFilter(isAdmin) && _searchValidationMessage.value != null) {
            _searchValidationMessage.value = null
        }
    }

    fun applyFilters() {
        executeSearch(searchCacheIsAdmin ?: lastIsAdmin)
    }

    fun clearSearch() {
        resetSearchSession()
    }

    fun clearAllFilters() {
        resetSearchSession()
    }

    fun updateSortOption(sortOption: SearchSortOption) {
        _draftSearchFilter.value = _draftSearchFilter.value.copy(sortBy = sortOption)
        _appliedSearchFilter.value = _appliedSearchFilter.value.copy(sortBy = sortOption)
        if (_hasExecutedSearch.value && hasInitializedSearchCache) {
            recomputeSearchResults()
        }
    }

    fun seedMotorsForTest(
        singlePhaseMotors: List<SinglePhaseMotor>,
        threePhaseMotors: List<ThreePhaseMotor>
    ) {
        isTestSeededMotors = true
        _singlePhaseMotors.value = singlePhaseMotors
        _threePhaseMotors.value = threePhaseMotors
        _isLoadingData.value = false
    }

    fun seedSearchDataForTest(
        singlePhaseMotors: List<SinglePhaseMotor>,
        threePhaseMotors: List<ThreePhaseMotor>,
        isAdmin: Boolean,
        hasMore: Boolean = false
    ) {
        searchCacheIsAdmin = isAdmin
        _searchSingleCache = if (isAdmin) singlePhaseMotors else singlePhaseMotors.filter { it.isExplicitlyApproved() }
        _searchThreeCache = if (isAdmin) threePhaseMotors else threePhaseMotors.filter { it.isExplicitlyApproved() }
        hasMoreSearchSingle = hasMore
        hasMoreSearchThree = false
        hasLoadedSingleSearchCache = true
        hasLoadedThreeSearchCache = true
        hasInitializedSearchCache = true
        _hasExecutedSearch.value = false
        _searchResults.value = emptyList()
        _searchValidationMessage.value = null
        _isSearchLoading.value = false
        _isSearchLoadingMore.value = false
        _searchError.value = null
    }

    fun setSearchErrorForTest(
        message: String?,
        isAdmin: Boolean = false,
        executedSearch: Boolean = true
    ) {
        searchCacheIsAdmin = isAdmin
        hasLoadedSingleSearchCache = true
        hasLoadedThreeSearchCache = true
        hasInitializedSearchCache = true
        _hasExecutedSearch.value = executedSearch
        _searchResults.value = emptyList()
        _isSearchLoading.value = false
        _isSearchLoadingMore.value = false
        _searchError.value = message
    }

    fun findSinglePhaseMotor(id: String): SinglePhaseMotor? {
        return _singlePhaseMotors.value.find { it.id == id }
            ?: _searchSingleCache.find { it.id == id }
    }

    fun findThreePhaseMotor(id: String): ThreePhaseMotor? {
        return _threePhaseMotors.value.find { it.id == id }
            ?: _searchThreeCache.find { it.id == id }
    }

    fun ensureMotorLoaded(type: String, id: String, isAdmin: Boolean) {
        if (id.isBlank()) return
        if (type == "single" && findSinglePhaseMotor(id) != null) return
        if (type == "three" && findThreePhaseMotor(id) != null) return
        val currentFirebaseUser = currentAuthUserOrNull() ?: return
        if (currentFirebaseUser.uid.isBlank()) return

        viewModelScope.launch {
            if (type == "single") {
                repo.getSinglePhaseMotorById(id, isAdmin).onSuccess { motor ->
                    if (motor != null) {
                        _singlePhaseMotors.value = (_singlePhaseMotors.value + motor).distinctBy { it.id }
                        upsertSearchSingleCache(motor)
                    }
                }
            } else if (type == "three") {
                repo.getThreePhaseMotorById(id, isAdmin).onSuccess { motor ->
                    if (motor != null) {
                        _threePhaseMotors.value = (_threePhaseMotors.value + motor).distinctBy { it.id }
                        upsertSearchThreeCache(motor)
                    }
                }
            }
        }
    }
}
