package com.example.seldon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.seldon.data.repository.DeviceRepositoryImpl
import com.example.seldon.data.worker.FleetRefreshWorker
import com.example.seldon.domain.usecase.DeviceEvaluation
import com.example.seldon.ui.DeviceDetailScreen
import com.example.seldon.ui.DeviceListScreen
import com.example.seldon.ui.DeviceListViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Agendamento do WorkManager com restrições energéticas (Trilha C)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        val refreshWorkRequest = PeriodicWorkRequestBuilder<FleetRefreshWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "FleetRefreshWork",
            ExistingPeriodicWorkPolicy.KEEP,
            refreshWorkRequest
        )

        val repository = DeviceRepositoryImpl(applicationContext)
        val viewModel = DeviceListViewModel(repository)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            var selectedEvaluation by remember { mutableStateOf<DeviceEvaluation?>(null) }

            if (selectedEvaluation == null) {
                DeviceListScreen(
                    viewModel = viewModel,
                    onDeviceClick = { evaluation ->
                        selectedEvaluation = evaluation
                    }
                )
            } else {
                val currentEval = selectedEvaluation!!
                val isFav = uiState.favoriteIds.contains(currentEval.device.id)

                DeviceDetailScreen(
                    evaluation = currentEval,
                    isFavorite = isFav,
                    onFavoriteToggle = { viewModel.toggleFavorite(currentEval.device.id) },
                    onBackClick = { selectedEvaluation = null }
                )
            }
        }
    }
}