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
        val hoursOn = device.screenOnMinutes / 60.0
        val hourlyDrainMah = if (hoursOn > 0) device.energyConsumedMah / hoursOn else 0.0
        val relativeDrainPercent = if (device.batteryCapacityMah > 0) {
            (hourlyDrainMah / device.batteryCapacityMah) * 100.0
        } else {
            0.0
        }

        // Regra conjuntiva (AND) de limiares
        val isCritico = relativeDrainPercent >= 12.0 && device.failures >= 3 && device.droppedFramesPercent >= 15.0
        val isAtencao = relativeDrainPercent >= 7.0 && device.failures >= 1 && device.droppedFramesPercent >= 8.0

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

        // Correção de interpolação: uso de ${device.failures} em vez de $device.failures
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