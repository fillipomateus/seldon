package com.example.seldon.domain.repository

import com.example.seldon.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    suspend fun getDevices(): List<Device>
    fun getFavoriteDeviceIds(): Flow<Set<String>>
    suspend fun toggleFavorite(deviceId: String)
}