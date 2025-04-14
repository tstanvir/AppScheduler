package com.example.appscheduler.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler.data.model.AppInfo
import com.example.appscheduler.data.repository.AppListRepository
import com.example.appscheduler.util.Constants.TAG
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppListViewModel(private val context: Context): ViewModel() {
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _apps.value = AppListRepository.getInstalledApps(context)
            Log.i(TAG, "Number of installed apps: ${_apps.value.size}")
        }
    }

    fun refresh() {
        loadInstalledApps()
    }
}