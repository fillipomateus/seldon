package com.example.seldon.data.model

import com.google.gson.annotations.SerializedName

data class DeviceDto(
    val id: String?,
    val name: String?,
    val batteryCapacityMah: Double?,
    val screenOnMinutes: Double?,
    val energyConsumedMah: Double?,
    val energyConsumedMwh: Double?,
    val nominalVoltage: Double?,
    val failures: Int?,
    val droppedFramesPercent: Double?
)