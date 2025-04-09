package com.example.appscheduler.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler.data.model.AppInfo
import com.example.appscheduler.data.repository.AppListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppListViewModel(private val repository: AppListRepository): ViewModel() {
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _apps.value = repository.getInstalledApps()
        }
    }
}