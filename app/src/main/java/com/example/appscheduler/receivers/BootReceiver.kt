package com.example.appscheduler.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.util.Constants.KEY_PREF_SCHEDULES
import com.example.appscheduler.util.Constants.KEY_SCHEDULE
import com.example.appscheduler.util.Constants.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "onReceive():: intent: $intent")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val sharedPreferences = context.getSharedPreferences(KEY_SCHEDULE, MODE_PRIVATE)
                val gson = Gson()

                val json = sharedPreferences.getString(KEY_PREF_SCHEDULES, "") ?: ""
                val type = object : TypeToken<List<Schedule>>() {}.type

                val schedules = gson.fromJson<List<Schedule>>(json, type) ?: emptyList()

                schedules.forEach { schedule ->
                    println("$TAG -> ${schedule.packageName}       ${schedule.id}")

                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmIntent = Intent(context, AppLauncherReceiver::class.java).apply {
                        putExtra(KEY_SCHEDULE, schedule)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        schedule.id.hashCode(),
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        schedule.scheduledTime,
                        pendingIntent
                    )
                }
            }
        }
    }
}