package com.example.appscheduler

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.appscheduler.ui.screens.HomeScreen
import com.example.appscheduler.ui.theme.AppSchedulerTheme
import com.example.appscheduler.viewmodels.AppListViewModel
import com.example.appscheduler.viewmodels.ScheduleViewModel
import com.example.appscheduler.viewmodels.ViewModelFactory


class MainActivity : ComponentActivity() {
    private lateinit var appListViewModel: AppListViewModel
    private lateinit var scheduleViewModel: ScheduleViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkOverlayPermission()
        appListViewModel = ViewModelProvider(this, ViewModelFactory(applicationContext))[AppListViewModel::class.java]
        scheduleViewModel = ViewModelProvider(this, ViewModelFactory(applicationContext))[ScheduleViewModel::class.java]

        setContent {
            AppSchedulerTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(appListViewModel, scheduleViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appListViewModel.refresh()
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
    }
}