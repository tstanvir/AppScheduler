package com.example.appscheduler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Parcelable
import android.util.Log
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.data.model.ScheduleState
import com.example.appscheduler.data.repository.AppStateRepository
import com.example.appscheduler.util.Constants.KEY_PREF_SCHEDULES
import com.example.appscheduler.util.Constants.KEY_SCHEDULE
import com.example.appscheduler.util.Constants.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppLauncherReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val schedule = intent?.parcelable<Schedule>(KEY_SCHEDULE)
        Log.i(TAG, "onReceive():: with package: ${schedule?.packageName}")

        launchTargetApp(context!!, schedule!!)
    }

    private inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= TIRAMISU -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    private fun launchTargetApp(context: Context, schedule: Schedule) {
        try {
            Log.i(TAG, "Attempting to launch app: ${schedule.packageName}")
            val launchIntent = context.packageManager.getLaunchIntentForPackage(schedule.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }

            if (launchIntent != null) {
                context.startActivity(launchIntent)
                updateSharedPref(context, schedule.id)
                updateAppState(context, schedule.packageName)
            } else {
                Log.e(TAG, "No launch intent available for: $schedule.packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}")
        }
    }

    private fun updateAppState(context: Context, packageName: String) {
        AppStateRepository.initialize(context)
        AppStateRepository.markAsExecuted(packageName)
    }

    private fun updateSharedPref(context: Context, scheduleId: String) {
        val sharedPreferences = context.getSharedPreferences(KEY_SCHEDULE, MODE_PRIVATE)
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