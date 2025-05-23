package com.example.appscheduler.data.repository

import android.content.Context
import com.example.appscheduler.data.model.ScheduleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppStateRepository {
    private val _appStates = MutableStateFlow<MutableMap<String, ScheduleState>>(mutableMapOf())
    val appStates: StateFlow<MutableMap<String, ScheduleState>> = _appStates.asStateFlow()
    var appStatesChanged = MutableStateFlow(0)
        private set

    @Synchronized
    fun initialize(context: Context) {
        val apps = AppListRepository.getInstalledApps(context)
        apps.forEach { app ->
            _appStates.value[app.packageName] = ScheduleState.NOT_SCHEDULED
        }
        appStatesChanged.value = appStatesChanged.value xor 1
    }

    @Synchronized
    fun scheduleApp(packageName: String) {
        _appStates.value[packageName] = ScheduleState.SCHEDULED
        appStatesChanged.value = appStatesChanged.value xor 1
    }

    @Synchronized
    fun markAsExecuted(packageName: String) {
        _appStates.value[packageName] = ScheduleState.EXECUTED
        appStatesChanged.value = appStatesChanged.value xor 1
    }

    @Synchronized
    fun cancelSchedule(packageName: String) {
        _appStates.value[packageName] = ScheduleState.CANCELLED
        appStatesChanged.value = appStatesChanged.value xor 1
    }
}