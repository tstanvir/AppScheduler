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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    context: Context
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
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onAppClicked(app.packageName, context) }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                app.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            AppStateIndicator(
                packageName = app.packageName,
                context,
                scheduleViewModel,
                onScheduleClick
            )
        }
    }
}

@Composable
fun TextCompose(text: String) {
    Text(
        color = Color.Black,
        text = text,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun AppStateIndicator(packageName: String, context: Context, scheduleViewModel: ScheduleViewModel, onScheduleClick: () -> Unit) {
    val appStateChangeListener = AppStateRepository.appStatesChanged.collectAsState().value

    val appStates = AppStateRepository.appStates.collectAsState().value
    val appState = appStates[packageName]!!
    Log.i(TAG, "state: $appState for package: $packageName with color: ${appState.color}")

    Card (
        shape = RoundedCornerShape(50.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp, 40.dp)
                .background(
                    color = appState.color
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                .clickable {
                    onStateButtonClicked(appState,
                        packageName,
                        context,
                        scheduleViewModel,
                        onScheduleClick)
                },
            contentAlignment = Alignment.Center
        ) {
            val text = getStatusText(appState)
            TextCompose(text)
        }
    }
}

fun getStatusText(appState: ScheduleState): String {
    return when (appState) {
        ScheduleState.SCHEDULED -> "Cancel"
        ScheduleState.NOT_SCHEDULED -> "Schedule"
        else -> "ReSchedule"
    }
}

private fun onStateButtonClicked(appState: ScheduleState, packageName: String, context: Context, scheduleViewModel: ScheduleViewModel, onScheduleClick: () -> Unit) {
    if (appState != ScheduleState.SCHEDULED) {
        onScheduleClick()
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
