package com.example.appscheduler.ui.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.data.model.ScheduleState
import com.example.appscheduler.receivers.AppLauncherReceiver
import com.example.appscheduler.util.Constants.KEY_PREF_SCHEDULES
import com.example.appscheduler.util.Constants.KEY_SCHEDULE
import com.example.appscheduler.util.Constants.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ScheduleViewModel(private val context: Context): ViewModel() {
    private val sharedPreferences = context.getSharedPreferences(KEY_SCHEDULE, MODE_PRIVATE)
    private val gson = Gson()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadSchedules()
        }
    }

    private fun loadSchedules() {
        val json = sharedPreferences.getString(KEY_PREF_SCHEDULES, "") ?: ""
        val type = object : TypeToken<List<Schedule>>() {}.type
        _schedules.value = gson.fromJson(json, type) ?: emptyList()
        for (value in _schedules.value) {
            println("$TAG -> ${value.packageName}")
        }
    }

    private fun saveSchedules() {
        val json = gson.toJson(_schedules.value)
        sharedPreferences.edit().putString("schedules", json).apply()
    }

    fun scheduleApp(schedule: Schedule) {
        Log.i(TAG, "${schedule.packageName} is scheduled at ${Date(schedule.scheduledTime)}")

        val newSchedule = Schedule(
            packageName = schedule.packageName,
            scheduledTime = schedule.scheduledTime,
            state = ScheduleState.SCHEDULED
        )

        _schedules.value = _schedules.value.filter { it.packageName != schedule.packageName } + newSchedule
        saveSchedules()

        val intent = Intent(context, AppLauncherReceiver::class.java).apply {
            putExtra("schedule", schedule)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.packageName.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            schedule.scheduledTime,
            pendingIntent
        )
    }
}