package com.example.seldon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seldon.domain.model.DeviceStatus
import com.example.seldon.domain.repository.DeviceRepository
import com.example.seldon.domain.usecase.CalculateDeviceStatusUseCase
import com.example.seldon.domain.usecase.DeviceEvaluation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DeviceListUiState(
    val evaluations: List<DeviceEvaluation> = emptyList(),
    val selectedFilter: DeviceStatus? = null,
    val favoriteIds: Set<String> = emptySet(),
    val totalCount: Int = 0,
    val okCount: Int = 0,
    val atencaoCount: Int = 0,
    val criticoCount: Int = 0,
    val indeterminadoCount: Int = 0
) {
    val filteredList: List<DeviceEvaluation>
        get() = if (selectedFilter == null) {
            evaluations
        } else {
            evaluations.filter { it.status == selectedFilter }
        }
}

class DeviceListViewModel(
    private val repository: DeviceRepository,
    private val calculateStatusUseCase: CalculateDeviceStatusUseCase = CalculateDeviceStatusUseCase()
) : ViewModel() {

    private val _rawEvaluations = MutableStateFlow<List<DeviceEvaluation>>(emptyList())
    private val _selectedFilter = MutableStateFlow<DeviceStatus?>(null)

    val uiState: StateFlow<DeviceListUiState> = combine(
        _rawEvaluations,
        _selectedFilter,
        repository.getFavoriteDeviceIds()
    ) { evaluations, filter, favorites ->
        DeviceListUiState(
            evaluations = evaluations,
            selectedFilter = filter,
            favoriteIds = favorites,
            totalCount = evaluations.size,
            okCount = evaluations.count { it.status == DeviceStatus.OK },
            atencaoCount = evaluations.count { it.status == DeviceStatus.ATENCAO },
            criticoCount = evaluations.count { it.status == DeviceStatus.CRITICO },
            indeterminadoCount = evaluations.count { it.status == DeviceStatus.INDETERMINADO }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DeviceListUiState())

    init {
        loadDevices()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            val devices = repository.getDevices()
            val evaluations = devices.map { calculateStatusUseCase(it) }
            _rawEvaluations.value = evaluations
        }
    }

    fun setFilter(status: DeviceStatus?) {
        _selectedFilter.value = status
    }

    fun toggleFavorite(deviceId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(deviceId)
        }
    }
}