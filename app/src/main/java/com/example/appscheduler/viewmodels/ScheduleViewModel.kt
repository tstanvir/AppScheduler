package com.example.appscheduler.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.data.model.ScheduleState
import com.example.appscheduler.data.repository.AppStateRepository
import com.example.appscheduler.data.repository.ScheduleRepository
import com.example.appscheduler.receivers.AppLauncherReceiver
import com.example.appscheduler.util.Constants.KEY_PACKAGE_NAME
import com.example.appscheduler.util.Constants.KEY_PREF_SCHEDULES
import com.example.appscheduler.util.Constants.KEY_SCHEDULE_ID
import com.example.appscheduler.util.Constants.NAME_PREF_SCHEDULE
import com.example.appscheduler.util.Constants.ONE_SECOND
import com.example.appscheduler.util.Constants.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class ScheduleViewModel(private val context: Context): ViewModel() {
    private val sharedPreferences = context.getSharedPreferences(NAME_PREF_SCHEDULE, MODE_PRIVATE)
    private val gson = Gson()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private var schedules = emptyList<Schedule>()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AppStateRepository.initialize(context)
            loadSchedules()
        }
    }

    private fun loadSchedules() {
        val json = sharedPreferences.getString(KEY_PREF_SCHEDULES, "") ?: ""
        val type = object : TypeToken<List<Schedule>>() {}.type
        schedules = gson.fromJson(json, type) ?: emptyList()

        schedules.forEach { schedule ->
            Log.i(TAG, "$schedule at ${Date(schedule.scheduledTime)}")
            ScheduleRepository.addSchedule(schedule.packageName, schedule)

            when (schedule.state) {
                ScheduleState.SCHEDULED -> AppStateRepository.scheduleApp(schedule.packageName)
                ScheduleState.EXECUTED -> AppStateRepository.markAsExecuted(schedule.packageName)
                ScheduleState.CANCELLED -> AppStateRepository.cancelSchedule(schedule.packageName)
                ScheduleState.NOT_SCHEDULED -> {}
            }
        }
    }

    private fun saveSchedules() {
        val json = gson.toJson(schedules)
        sharedPreferences.edit().putString(KEY_PREF_SCHEDULES, json).apply()
    }

    fun scheduleApp(schedule: Schedule): Boolean {
        if (isConflict(schedule)) {
            var offsetSlot = 0
            var slotFound = false

            val maxOffset = 55
            val step = 5

            val usedOffsets = schedules
                .filter { it.state == ScheduleState.SCHEDULED && it.scheduledTime == schedule.scheduledTime }
                .map { it.scheduledTimeOffset }
                .sorted()

            val availableOffset = (0..maxOffset step step)
                .firstOrNull { it !in usedOffsets }

            if (availableOffset != null) {
                offsetSlot = availableOffset
                slotFound = true
            }

            Log.i(TAG, "offsetSlot: $offsetSlot")
            if (!slotFound) {
                Toast.makeText(context, "${Date(schedule.scheduledTime)} has no slot available to schedule", Toast.LENGTH_LONG).show()
                return false
            }

            schedule.scheduledTimeOffset = offsetSlot
        }

        if (AppStateRepository.appStates.value[schedule.packageName] == ScheduleState.SCHEDULED) {
            cancelSchedule(ScheduleRepository.getLatestSchedule(schedule.packageName))
        }

        val pendingIntent = getPendingIntent(schedule, true)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            schedule.scheduledTime + schedule.scheduledTimeOffset * ONE_SECOND,
            pendingIntent!!
        )

        schedules += schedule
        saveSchedules()
        AppStateRepository.scheduleApp(schedule.packageName)
        ScheduleRepository.addSchedule(schedule.packageName, schedule)
        Log.i(TAG, "${schedule.packageName} is scheduled at ${Date(schedule.scheduledTime)}")
        return true
    }

    private fun isConflict(schedule: Schedule): Boolean {
        return schedules.any {
            it.scheduledTime == schedule.scheduledTime && AppStateRepository.appStates.value[it.packageName] == ScheduleState.SCHEDULED
        }
    }

    fun cancelSchedule(schedule: Schedule?) {
        val pendingIntent = getPendingIntent(schedule, false)
        alarmManager.cancel(pendingIntent!!)

        schedules = schedules.map {
            if (it.packageName == schedule?.packageName) it.copy(state = ScheduleState.CANCELLED) else it
        }

        AppStateRepository.cancelSchedule(schedule?.packageName!!)
        ScheduleRepository.updateSchedule(schedule.packageName, schedule.id, ScheduleState.CANCELLED)
        saveSchedules()
    }

    private fun getPendingIntent(
        schedule: Schedule?,
        toSchedule: Boolean
    ): PendingIntent? {
        val intent = Intent(context, AppLauncherReceiver::class.java)
        var flags = PendingIntent.FLAG_IMMUTABLE

        if (toSchedule) {
            intent.putExtra(KEY_PACKAGE_NAME, schedule?.packageName)
            intent.putExtra(KEY_SCHEDULE_ID, schedule?.id)
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