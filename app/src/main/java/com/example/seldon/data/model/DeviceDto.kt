package com.example.seldon.data.model

import com.google.gson.annotations.SerializedName

data class DeviceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("soc") val soc: String?,
    @SerializedName("tier") val tier: String?,
    @SerializedName("androidApi") val androidApi: Int?,
    @SerializedName("lastSeen") val lastSeen: String?,
    @SerializedName("batteryLevelPct") val batteryLevelPct: Int?,
    @SerializedName("batteryCapacityMah") val batteryCapacityMah: Double?,
    @SerializedName("screenOnMinutes") val screenOnMinutes: Double?,
    @SerializedName("energyConsumed") val energyConsumed: Double?,
    @SerializedName("energyUnit") val energyUnit: String?,
    @SerializedName("nominalVoltage") val nominalVoltage: Double?,
    @SerializedName("crashCount7d") val crashCount7d: Int?,
    @SerializedName("jankFramesPct") val jankFramesPct: Double?
)