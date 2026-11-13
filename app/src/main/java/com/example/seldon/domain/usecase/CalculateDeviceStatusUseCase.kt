package com.example.seldon.domain.usecase

import com.example.seldon.domain.model.Device
import com.example.seldon.domain.model.DeviceStatus
import java.util.Locale

data class DeviceEvaluation(
    val device: Device,
    val status: DeviceStatus,
    val relativeDrainPercent: Double,
    val hourlyDrainMah: Double,
    val reason: String
)

class CalculateDeviceStatusUseCase {

    operator fun invoke(device: Device): DeviceEvaluation {
        // 1. Regra do INDETERMINADO (REQ-03): Verifica logo no início
        if (device.batteryCapacityMah <= 0 || device.screenOnMinutes <= 0) {
            return DeviceEvaluation(
                device = device,
                status = DeviceStatus.INDETERMINADO,
                relativeDrainPercent = 0.0,
                hourlyDrainMah = 0.0,
                reason = "Dados insuficientes para determinar o status."
            )
        }

        val hoursOn = device.screenOnMinutes / 60.0
        val hourlyDrainMah = device.energyConsumedMah / hoursOn
        val relativeDrainPercent = (hourlyDrainMah / device.batteryCapacityMah) * 100.0

        // 2. Regra Disjuntiva (OU - ||): Basta uma métrica falhar para agravar o status
        val isCritico = relativeDrainPercent >= 12.0 || device.failures >= 3 ||  device.droppedFramesPercent >= 15.0
        val isAtencao = relativeDrainPercent >= 7.0 || device.failures >= 1 || device.droppedFramesPercent >= 8.0

        val status = when {
            isCritico -> DeviceStatus.CRITICO
            isAtencao -> DeviceStatus.ATENCAO
            else -> DeviceStatus.OK
        }

        // Formatação numérica com vírgula (padrão PT-BR)
        val drainFormatted = String.format(Locale("pt", "BR"), "%.1f", relativeDrainPercent)
        val jankFormatted = String.format(Locale("pt", "BR"), "%.1f", device.droppedFramesPercent)

        val conversionSuffix = if (device.isConvertedFromMwh) {
            " Consumo normalizado a partir de mWh antes da comparação."
        } else {
            ""
        }

        val reason = when (status) {
            DeviceStatus.CRITICO -> {
                "Dreno relativo de $drainFormatted %/h, ${device.failures} falhas e $jankFormatted% de quadros perdidos atingiram os limiares críticos.$conversionSuffix"
            }
            DeviceStatus.ATENCAO -> {
                "Dreno relativo de $drainFormatted %/h, ${device.failures} falhas e $jankFormatted% de quadros perdidos atingiram os limiares de atenção.$conversionSuffix"
            }
            DeviceStatus.OK -> {
                "Dispositivo operando dentro dos parâmetros normais.$conversionSuffix"
            }
            DeviceStatus.INDETERMINADO -> {
                "Dados insuficientes para determinar o status."
            }
        }

        return DeviceEvaluation(
            device = device,
            status = status,
            relativeDrainPercent = relativeDrainPercent,
            hourlyDrainMah = hourlyDrainMah,
            reason = reason
        )
    }
}