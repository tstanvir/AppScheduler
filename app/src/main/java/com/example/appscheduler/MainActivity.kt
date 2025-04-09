package com.example.appscheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.appscheduler.data.repository.AppListRepository
import com.example.appscheduler.ui.screens.HomeScreen
import com.example.appscheduler.ui.theme.AppSchedulerTheme
import com.example.appscheduler.ui.viewmodels.AppListViewModel
import com.example.appscheduler.ui.viewmodels.AppListViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppSchedulerTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val factory = AppListViewModelFactory(AppListRepository(applicationContext))
                    HomeScreen(ViewModelProvider(this, factory)[AppListViewModel::class.java])
                }
            }
        }
    }
}