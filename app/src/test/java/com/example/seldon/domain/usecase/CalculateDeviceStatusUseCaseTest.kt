package com.example.seldon.domain.usecase

import com.example.seldon.domain.model.Device
import com.example.seldon.domain.model.DeviceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateDeviceStatusUseCaseTest {

    private val useCase = CalculateDeviceStatusUseCase()

    private fun createDevice(
        relativeDrainPercent: Double,
        failures: Int,
        jankPercent: Double
    ): Device {
        // Capacidade 1000 mAh, 60 min de tela => dreno absoluto = energyConsumedMah
        val batteryCapacity = 1000.0
        val screenMinutes = 60.0
        val energyConsumed = (relativeDrainPercent / 100.0) * batteryCapacity

        return Device(
            id = "TEST-01",
            name = "Test Phone",
            soc = "Snapdragon",
            tier = "mid",
            androidApi = 34,
            lastSeenFormatted = "N/A",
            batteryLevelPct = 80,
            batteryCapacityMah = batteryCapacity,
            screenOnMinutes = screenMinutes,
            energyConsumedMah = energyConsumed,
            isConvertedFromMwh = false,
            failures = failures,
            droppedFramesPercent = jankPercent
        )
    }

    @Test
    fun `deve retornar CRITICO quando todos os limiares criticos forem atingidos exatamente`() {
        val device = createDevice(relativeDrainPercent = 12.0, failures = 3, jankPercent = 15.0)
        val result = useCase(device)
        assertEquals(DeviceStatus.CRITICO, result.status)
    }

    @Test
    fun `deve retornar OK quando dreno estiver imediatamente abaixo do limiar de atencao`() {
        val device = createDevice(relativeDrainPercent = 6.9, failures = 1, jankPercent = 8.0)
        val result = useCase(device)
        assertEquals(DeviceStatus.OK, result.status)
    }

    @Test
    fun `deve retornar ATENCAO quando estiver imediatamente no limiar de atencao`() {
        val device = createDevice(relativeDrainPercent = 7.0, failures = 1, jankPercent = 8.0)
        val result = useCase(device)
        assertEquals(DeviceStatus.ATENCAO, result.status)
    }

    @Test
    fun `deve retornar ATENCAO se falhas forem abaixo do limiar critico mesmo com dreno alto`() {
        val device = createDevice(relativeDrainPercent = 12.5, failures = 2, jankPercent = 16.0)
        val result = useCase(device)
        assertEquals(DeviceStatus.ATENCAO, result.status)
    }
}