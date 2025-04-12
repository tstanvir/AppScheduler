package com.example.appscheduler.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.receivers.AppLauncherReceiver
import com.example.appscheduler.util.Constants.KEY_PREF_SCHEDULES
import com.example.appscheduler.util.Constants.KEY_SCHEDULE
import com.example.appscheduler.util.Constants.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ScheduleViewModel(private val context: Context): ViewModel() {
    private val sharedPreferences = context.getSharedPreferences(KEY_SCHEDULE, MODE_PRIVATE)
    private val gson = Gson()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadSchedules()
        }
    }

    private fun loadSchedules() {
        val json = sharedPreferences.getString(KEY_PREF_SCHEDULES, "") ?: ""
        val type = object : TypeToken<List<Schedule>>() {}.type
        _schedules.value = gson.fromJson(json, type) ?: emptyList()

        _schedules.value.forEach { schedule ->
            println("$TAG -> ${schedule.packageName}       ${schedule.id}")
        }
    }

    private fun saveSchedules() {
        val json = gson.toJson(_schedules.value)
        sharedPreferences.edit().putString("schedules", json).apply()
    }

    fun scheduleApp(schedule: Schedule) {
        Log.i(TAG, "${schedule.packageName} is scheduled at ${Date(schedule.scheduledTime)}")

        _schedules.value += schedule
        saveSchedules()

        val pendingIntent = getPendingIntent(schedule, true)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            schedule.scheduledTime,
            pendingIntent!!
        )
    }

    private fun getPendingIntent(
        schedule: Schedule?,
        toSchedule: Boolean
    ): PendingIntent? {
        val intent = Intent(context, AppLauncherReceiver::class.java)
        var flags = PendingIntent.FLAG_IMMUTABLE

        if (toSchedule) {
            intent.putExtra("schedule", schedule)
            flags = flags or PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            schedule?.id.hashCode(),
            intent,
            flags
        )
    }
}