package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.HistoryItemDomain
import com.example.data.repository.CreativeAiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val selectedCategory: String = "ALL", // ALL, CHAT, IMAGE, MUSIC, GAME_MIND, GAME_CHESS, GAME_TICTACTOE, GAME_MAZE
    val searchQuery: String = "",
    val sortOrder: String = "newest", // newest, oldest, title
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val items: List<HistoryItemDomain> = emptyList(),
    val totalCount: Int = 0
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val state = _uiState.value
            val res = repository.getHistoryFromBackend(
                category = state.selectedCategory,
                query = state.searchQuery,
                sort = state.sortOrder,
                page = state.currentPage,
                pageSize = state.pageSize
            )
            res.onSuccess { fetchedItems ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = fetchedItems,
                    totalCount = fetchedItems.size,
                    hasMore = fetchedItems.size >= state.pageSize
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to load history items"
                )
            }
        }
    }

    fun setCategory(cat: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = cat, currentPage = 1)
        fetchHistory()
    }

    fun setSearchQuery(q: String) {
        _uiState.value = _uiState.value.copy(searchQuery = q, currentPage = 1)
        fetchHistory()
    }

    fun setSortOrder(sort: String) {
        _uiState.value = _uiState.value.copy(sortOrder = sort, currentPage = 1)
        fetchHistory()
    }

    fun nextPage() {
        if (_uiState.value.hasMore) {
            _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage + 1)
            fetchHistory()
        }
    }

    fun prevPage() {
        if (_uiState.value.currentPage > 1) {
            _uiState.value = _uiState.value.copy(currentPage = _uiState.value.currentPage - 1)
            fetchHistory()
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
            fetchHistory()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _uiState.value = _uiState.value.copy(items = emptyList(), totalCount = 0)
        }
    }
}
