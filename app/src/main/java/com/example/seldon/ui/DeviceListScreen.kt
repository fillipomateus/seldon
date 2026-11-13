package com.example.seldon.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun DeviceListScreen(
    viewModel: DeviceListViewModel,
    onDeviceClick: (DeviceEvaluation) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Frota",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "${uiState.totalCount} DISPOSITIVOS · LIDOS DE ASSETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilterChipsRow(uiState = uiState, onFilterSelected = { viewModel.setFilter(it) })

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.filteredList) { evaluation ->
                val isFavorite = uiState.favoriteIds.contains(evaluation.device.id)
                DeviceCardItem(
                    evaluation = evaluation,
                    isFavorite = isFavorite,
                    onFavoriteToggle = { viewModel.toggleFavorite(evaluation.device.id) },
                    onClick = { onDeviceClick(evaluation) }
                )
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    uiState: DeviceListUiState,
    onFilterSelected: (DeviceStatus?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChipItem(
                label = "TODOS ${uiState.totalCount}",
                isSelected = uiState.selectedFilter == null,
                onClick = { onFilterSelected(null) }
            )
        }
        item {
            FilterChipItem(
                label = "OK ${uiState.okCount}",
                isSelected = uiState.selectedFilter == DeviceStatus.OK,
                onClick = { onFilterSelected(DeviceStatus.OK) }
            )
        }
        item {
            FilterChipItem(
                label = "ATENÇÃO ${uiState.atencaoCount}",
                isSelected = uiState.selectedFilter == DeviceStatus.ATENCAO,
                onClick = { onFilterSelected(DeviceStatus.ATENCAO) }
            )
        }
        item {
            FilterChipItem(
                label = "CRÍTICO ${uiState.criticoCount}",
                isSelected = uiState.selectedFilter == DeviceStatus.CRITICO,
                onClick = { onFilterSelected(DeviceStatus.CRITICO) }
            )
        }
        item {
            FilterChipItem(
                label = "INDET. ${uiState.indeterminadoCount}",
                isSelected = uiState.selectedFilter == DeviceStatus.INDETERMINADO,
                onClick = { onFilterSelected(DeviceStatus.INDETERMINADO) }
            )
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) TextPrimary else CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) DarkBackground else TextSecondary
        )
    }
}

@Composable
fun DeviceCardItem(
    evaluation: DeviceEvaluation,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    val device = evaluation.device
    val statusColor = when (evaluation.status) {
        DeviceStatus.OK -> StatusOkColor
        DeviceStatus.ATENCAO -> StatusAtencaoColor
        DeviceStatus.CRITICO -> StatusCriticoColor
        DeviceStatus.INDETERMINADO -> StatusIndeterminadoColor
    }

    val statusLabel = when (evaluation.status) {
        DeviceStatus.OK -> "OK"
        DeviceStatus.ATENCAO -> "ATENÇÃO"
        DeviceStatus.CRITICO -> "CRÍTICO"
        DeviceStatus.INDETERMINADO -> "SEM DADOS"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = device.id,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${device.batteryLevelPct}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${device.name} · ${device.soc}",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFFFD700) else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }
    }
}