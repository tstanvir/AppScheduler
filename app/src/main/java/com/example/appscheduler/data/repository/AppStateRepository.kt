package com.example.appscheduler.data.repository

import android.content.Context
import com.example.appscheduler.data.model.ScheduleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppStateRepository {
    private val _appStates = MutableStateFlow<Map<String, ScheduleState>>(emptyMap())
    val appStates: StateFlow<Map<String, ScheduleState>> = _appStates.asStateFlow()
    private var isInitialized = false

    @Synchronized
    fun initialize(context: Context) {
        if (isInitialized) return

        val apps = AppListRepository.getInstalledApps(context)

        apps.forEach { app ->
            _appStates.value = _appStates.value.toMutableMap().apply {
                put(app.packageName, ScheduleState.NOT_SCHEDULED)
            }
        }

        isInitialized = true
    }

    @Synchronized
    fun scheduleApp(packageName: String) {
        _appStates.value = _appStates.value.toMutableMap().apply {
            put(packageName, ScheduleState.SCHEDULED)
        }
    }

    @Synchronized
    fun markAsExecuted(packageName: String) {
        _appStates.value = _appStates.value.toMutableMap().apply {
            put(packageName, ScheduleState.EXECUTED)
        }
    }

    @Synchronized
    fun cancelSchedule(packageName: String) {
        _appStates.value = _appStates.value.toMutableMap().apply {
            put(packageName, ScheduleState.CANCELLED)
        }
    }
}