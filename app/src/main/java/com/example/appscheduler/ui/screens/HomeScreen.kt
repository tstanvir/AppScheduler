package com.example.appscheduler.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.appscheduler.data.model.AppInfo
import com.example.appscheduler.data.model.ScheduleState
import com.example.appscheduler.data.repository.AppStateRepository
import com.example.appscheduler.data.repository.ScheduleRepository
import com.example.appscheduler.util.Constants.TAG
import com.example.appscheduler.viewmodels.AppListViewModel
import com.example.appscheduler.viewmodels.ScheduleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appListViewModel: AppListViewModel,
    scheduleViewModel: ScheduleViewModel
) {
    val context = LocalContext.current
    val installedApps = appListViewModel.apps.collectAsState().value
    var showDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

    if (showDialog && selectedApp != null) {
        ScheduleDialog(
            app = selectedApp!!,
            onDismiss = { showDialog = false },
            viewModel = scheduleViewModel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Scheduler") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 8.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = installedApps,
                key = { app -> app.packageName }
            ) { app ->
                AppCellItem(
                    app = app,
                    onScheduleClick = {
                        selectedApp = app
                        showDialog = true
                    },
                    scheduleViewModel,
                    context
                )
            }
        }
    }
}

@Composable
fun AppCellItem(
    app: AppInfo,
    onScheduleClick: () -> Unit,
    scheduleViewModel: ScheduleViewModel,
    context: Context,
    cornerAlignment: Alignment = Alignment.TopEnd,
    indicatorPadding: Dp = 4.dp
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (app.icon != null) {
            Box(modifier = Modifier) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = app.name,
                    modifier = Modifier.size(48.dp)
                        .clickable { onAppClicked(app.packageName, context) }
                )

                Box(
                    modifier = Modifier
                        .align(cornerAlignment)
                        .padding(indicatorPadding)
                ) {
                    AppStateIndicator(packageName = app.packageName, context, scheduleViewModel)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.name,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onScheduleClick) {
                Text(text = "Schedule")
            }
        }
    }
}

@Composable
fun AppStateIndicator(packageName: String, context: Context, scheduleViewModel: ScheduleViewModel) {
    val appStates = AppStateRepository.appStates.collectAsState().value
    val appState = appStates[packageName]!!
    Log.i(TAG, "state: $appState for package: $packageName with color: ${appState.color}")

    Box(
        modifier = Modifier
            .size(16.dp)
            .background(
                color = appState.color,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable {
                onStateButtonClicked(appState, packageName, context, scheduleViewModel)
            }
    )
}

private fun onStateButtonClicked(appState: ScheduleState, packageName: String, context: Context, scheduleViewModel: ScheduleViewModel) {
    if (appState != ScheduleState.SCHEDULED) {
        Toast.makeText(context, "You can't cancel if app is not scheduled.", Toast.LENGTH_SHORT).show()
        return
    }
    Toast.makeText(context, "Schedule canceled", Toast.LENGTH_SHORT).show()
    scheduleViewModel.cancelSchedule(ScheduleRepository.getLatestSchedule(packageName))
}

private fun onAppClicked(packageName: String, context: Context) {
    val pm = context.packageManager
    val launchIntent = pm.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    }
}
