package com.example.appscheduler.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Parcelable
import android.util.Log
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.util.Constants.KEY_SCHEDULE
import com.example.appscheduler.util.Constants.TAG

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
            } else {
                Log.e(TAG, "No launch intent available for: $schedule.packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: ${e.message}")
        }
    }
}