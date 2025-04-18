package com.example.appscheduler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.data.model.ScheduleState
import com.example.appscheduler.data.repository.AppStateRepository
import com.example.appscheduler.data.repository.ScheduleRepository
import com.example.appscheduler.util.Constants.KEY_PACKAGE_NAME
import com.example.appscheduler.util.Constants.KEY_PREF_SCHEDULES
import com.example.appscheduler.util.Constants.KEY_SCHEDULE_ID
import com.example.appscheduler.util.Constants.NAME_PREF_SCHEDULE
import com.example.appscheduler.util.Constants.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppLauncherReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val packageName = intent?.getStringExtra(KEY_PACKAGE_NAME)
        val scheduleId = intent?.getStringExtra(KEY_SCHEDULE_ID)
        Log.i(TAG, "onReceive():: with package: $packageName")

        launchTargetApp(context!!, packageName!!, scheduleId!!)
    }

    private fun launchTargetApp(context: Context, packageName: String, scheduleId: String) {
        try {
            Log.i(TAG, "Attempting to launch app: $packageName")
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }

            if (launchIntent != null) {
                context.startActivity(launchIntent)
            } else {
                Log.e(TAG, "No launch intent available for: $packageName")
            }

            updateAppState(context, packageName, scheduleId)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}")
        }
    }

    private fun updateAppState(context: Context, packageName: String, scheduleId: String) {
        updateSharedPref(context, scheduleId)
        AppStateRepository.markAsExecuted(packageName)
        ScheduleRepository.updateSchedule(packageName, scheduleId, ScheduleState.EXECUTED)
    }

    private fun updateSharedPref(context: Context, scheduleId: String) {
        val sharedPreferences = context.getSharedPreferences(NAME_PREF_SCHEDULE, MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(KEY_PREF_SCHEDULES, "") ?: ""
        val type = object : TypeToken<List<Schedule>>() {}.type

        var schedules = gson.fromJson<List<Schedule>>(json, type) ?: emptyList()
        schedules = schedules.map {
            if (it.id == scheduleId) it.copy(state = ScheduleState.EXECUTED) else it
        }

        saveSharedPref(schedules, gson, sharedPreferences)
    }

    private fun saveSharedPref(schedules: List<Schedule>, gson: Gson, sharedPreferences: SharedPreferences) {
        val json = gson.toJson(schedules)
        sharedPreferences.edit().putString(KEY_PREF_SCHEDULES, json).apply()
    }
}