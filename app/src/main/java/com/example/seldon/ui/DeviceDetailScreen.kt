package com.example.seldon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seldon.domain.model.DeviceStatus
import com.example.seldon.domain.usecase.DeviceEvaluation

private val DarkBackground = Color(0xFF0B0E14)
private val CardBackground = Color(0xFF161B22)
private val CardBorder = Color(0xFF21262D)
private val TextPrimary = Color(0xFFF0F6FC)
private val TextSecondary = Color(0xFF8B949E)

private val StatusOkColor = Color(0xFF2EA043)
private val StatusAtencaoColor = Color(0xFFD29922)
private val StatusCriticoColor = Color(0xFFF85149)
private val StatusIndeterminadoColor = Color(0xFF6E7681)

@Composable
fun DeviceDetailScreen(
    evaluation: DeviceEvaluation,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onBackClick: () -> Unit
) {
    val device = evaluation.device
    val statusColor = when (evaluation.status) {
        DeviceStatus.OK -> StatusOkColor
        DeviceStatus.ATENCAO -> StatusAtencaoColor
        DeviceStatus.CRITICO -> StatusCriticoColor
        DeviceStatus.INDETERMINADO -> StatusIndeterminadoColor
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Cabeçalho de Navegação
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color(0xFF58A6FF),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Frota",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF58A6FF)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ID e Subtítulo completo (incluindo tier)
        Text(
            text = device.id,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "${device.name} · ${device.soc} · tier ${device.tier}",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Cartão do Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "▲ ${evaluation.status.name}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = evaluation.reason,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grelha Integrada de Métricas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(title = "CONSUMO", value = "${device.absoluteDrainMahH.toInt()}", unit = "mAh/h", modifier = Modifier.weight(1f))
                MetricCell(title = "CAPACIDADE", value = "${device.batteryCapacityMah.toInt()}", unit = "mAh", modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(title = "TELA LIGADA", value = "${device.screenOnMinutes.toInt()}", unit = "min", modifier = Modifier.weight(1f))
                MetricCell(title = "QUADROS PERDIDOS", value = "%.1f".format(device.droppedFramesPercent).replace('.', ','), unit = "%", modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = CardBorder, thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(title = "FALHAS (7D)", value = "${device.failures}", unit = "", modifier = Modifier.weight(1f))
                MetricCell(title = "NÍVEL DE API", value = "${device.androidApi}", unit = "", modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botão de Favorito Persistido
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardBackground)
                .border(1.dp, if (isFavorite) Color(0xFFD29922).copy(alpha = 0.5f) else CardBorder, RoundedCornerShape(8.dp))
                .clickable { onFavoriteToggle() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFavorite) "★ FAVORITO · PERSISTISTIDO" else "☆ MARCAR COMO FAVORITO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFavorite) Color(0xFFFFD700) else TextSecondary,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rodapé de Último Contato
        Text(
            text = "ÚLTIMO CONTATO · ${device.lastSeenFormatted}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun MetricCell(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(14.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}