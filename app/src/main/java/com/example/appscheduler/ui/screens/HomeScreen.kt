package com.example.appscheduler.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.appscheduler.data.model.AppInfo
import com.example.appscheduler.ui.viewmodels.AppListViewModel
import com.example.appscheduler.ui.viewmodels.ScheduleViewModel

@Composable
fun HomeScreen(
    appListViewModel: AppListViewModel,
    scheduleViewModel: ScheduleViewModel
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    val installedApps = appListViewModel.apps.collectAsState().value

    if (showDialog && selectedApp != null) {
        ScheduleDialog(
            app = selectedApp!!,
            onDismiss = { showDialog = false },
            viewModel = scheduleViewModel
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
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
                context
            )
        }
    }
}

@Composable
fun AppCellItem(
    app: AppInfo,
    onScheduleClick: () -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (app.icon != null) {
            Image(
                bitmap = app.icon.toBitmap().asImageBitmap(),
                contentDescription = app.name,
                modifier = Modifier.size(48.dp)
                    .clickable { onAppClicked(app.packageName, context) }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = app.name)
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = onScheduleClick) {
            Text(text = "Schedule")
        }
    }
}

fun onAppClicked(packageName: String, context: Context) {
    val pm = context.packageManager
    val launchIntent = pm.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    }
}
