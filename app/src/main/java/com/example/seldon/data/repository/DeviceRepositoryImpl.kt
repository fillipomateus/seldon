package com.example.seldon.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.seldon.data.model.DeviceDto
import com.example.seldon.domain.model.Device
import com.example.seldon.domain.repository.DeviceRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStreamReader

private val Context.dataStore by preferencesDataStore(name = "favorites")

private data class SeldonDataWrapper(
    val devices: List<DeviceDto>?
)

class DeviceRepositoryImpl(private val context: Context) : DeviceRepository {
    private val FAVORITES_KEY = stringSetPreferencesKey("favorite_devices")

    override suspend fun getDevices(): List<Device> {
        // Tenta abrir frota.json (exigido) com fallback para seldon.json
        val inputStream = try {
            context.assets.open("frota.json")
        } catch (e: Exception) {
            context.assets.open("seldon.json")
        }

        val reader = InputStreamReader(inputStream)
        val dataWrapper = Gson().fromJson(reader, SeldonDataWrapper::class.java)
        reader.close()

        val dtos = dataWrapper?.devices ?: emptyList()

        return dtos.mapNotNull { dto ->
            val capacity = dto.batteryCapacityMah ?: 0.0
            val screenMinutes = dto.screenOnMinutes ?: 0.0

            // REGRA DO EDITAL: Excluir registros sem capacidade ou com tela ligada zero
            if (capacity <= 0.0 || screenMinutes <= 0.0) {
                return@mapNotNull null
            }

            val unit = dto.energyUnit?.trim()
            val isMwh = unit.equals("mWh", ignoreCase = true)
            val nominalVoltage = dto.nominalVoltage ?: 0.0

            // Se for mWh sem tensão válida, também exclui para evitar divisão por zero
            if (isMwh && nominalVoltage <= 0.0) {
                return@mapNotNull null
            }

            val finalConsumedMah = if (isMwh) {
                (dto.energyConsumed ?: 0.0) / nominalVoltage
            } else {
                dto.energyConsumed ?: 0.0
            }

            val formattedDate = dto.lastSeen?.let { raw ->
                if (raw.length >= 16) {
                    val parts = raw.split("T")
                    val dateParts = parts[0].split("-")
                    val timeParts = parts[1].split(":")
                    "${dateParts.getOrNull(2)}/${dateParts.getOrNull(1)} ${timeParts.getOrNull(0)}:${timeParts.getOrNull(1)} UTC"
                } else raw
            } ?: "N/A"

            Device(
                id = dto.id ?: "N/A",
                name = dto.model ?: "Desconhecido",
                soc = dto.soc ?: "Desconhecido",
                tier = dto.tier ?: "low",
                androidApi = dto.androidApi ?: 0,
                lastSeenFormatted = formattedDate,
                batteryLevelPct = dto.batteryLevelPct ?: 0,
                batteryCapacityMah = capacity,
                screenOnMinutes = screenMinutes,
                energyConsumedMah = finalConsumedMah,
                isConvertedFromMwh = isMwh,
                failures = dto.crashCount7d ?: 0,
                droppedFramesPercent = dto.jankFramesPct ?: 0.0
            )
        }
    }

    override fun getFavoriteDeviceIds(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            preferences[FAVORITES_KEY] ?: emptySet()
        }
    }

    override suspend fun toggleFavorite(deviceId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITES_KEY] ?: emptySet()
            if (current.contains(deviceId)) {
                preferences[FAVORITES_KEY] = current - deviceId
            } else {
                preferences[FAVORITES_KEY] = current + deviceId
            }
        }
    }
}