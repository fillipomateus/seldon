package com.example.seldon.domain.model

enum class DeviceStatus {
    INDETERMINADO,
    CRITICO,
    ATENCAO,
    OK
}

data class Device(
    val id: String,
    val name: String,
    val soc: String,
    val tier: String,
    val androidApi: Int,
    val lastSeenFormatted: String,
    val batteryLevelPct: Int,
    val batteryCapacityMah: Double,
    val screenOnMinutes: Double,
    val energyConsumedMah: Double,
    val isConvertedFromMwh: Boolean,
    val failures: Int,
    val droppedFramesPercent: Double
) {
    val absoluteDrainMahH: Double
        get() = if (screenOnMinutes > 0.0) energyConsumedMah / (screenOnMinutes / 60.0) else 0.0
}