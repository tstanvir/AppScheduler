package com.example.appscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appscheduler.data.repository.AppListRepository
import com.example.appscheduler.ui.screens.HomeScreen
import com.example.appscheduler.ui.theme.AppSchedulerTheme
import com.example.appscheduler.ui.viewmodels.AppListViewModel
import com.example.appscheduler.ui.viewmodels.ScheduleViewModel
import com.example.appscheduler.ui.viewmodels.ViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppSchedulerTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val appListViewModel = viewModel<AppListViewModel>(factory = ViewModelFactory(AppListRepository(this)))
                    val scheduleViewModel = viewModel<ScheduleViewModel>(factory = ViewModelFactory(this))
                    HomeScreen(appListViewModel, scheduleViewModel)
                }
            }
        }
    }
}