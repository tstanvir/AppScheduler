package com.example.appscheduler.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.data.model.ScheduleState
import com.example.appscheduler.util.Constants.KEY_PACKAGE_NAME
import com.example.appscheduler.util.Constants.KEY_PREF_SCHEDULES
import com.example.appscheduler.util.Constants.KEY_SCHEDULE_ID
import com.example.appscheduler.util.Constants.NAME_PREF_SCHEDULE
import com.example.appscheduler.util.Constants.ONE_SECOND
import com.example.appscheduler.util.Constants.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "onReceive():: intent: $intent")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val sharedPreferences = context.getSharedPreferences(NAME_PREF_SCHEDULE, MODE_PRIVATE)
                val gson = Gson()

                val json = sharedPreferences.getString(KEY_PREF_SCHEDULES, "") ?: ""
                val type = object : TypeToken<List<Schedule>>() {}.type

                val schedules = gson.fromJson<List<Schedule>>(json, type) ?: emptyList()

                schedules
                    .filter { it.state == ScheduleState.SCHEDULED }
                    .forEach { schedule ->
                        Log.i(TAG, "$schedule at ${Date(schedule.scheduledTime)}")

                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val alarmIntent = Intent(context, AppLauncherReceiver::class.java).apply {
                            putExtra(KEY_PACKAGE_NAME, schedule.packageName)
                            putExtra(KEY_SCHEDULE_ID, schedule.id)
                        }
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            schedule.id.hashCode(),
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        schedule.scheduledTime + schedule.scheduledTimeOffset * ONE_SECOND,
                        pendingIntent
                    )
                }
            }
        }
    }
}