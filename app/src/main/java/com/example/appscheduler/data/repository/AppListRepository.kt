package com.example.appscheduler.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.appscheduler.data.model.AppInfo
import com.example.appscheduler.util.Constants.ANDROID_SPECIFIC_APP
import com.example.appscheduler.util.Constants.OWN_PACKAGE_NAME
import java.util.Locale
import kotlin.random.Random

class AppListRepository(private val context: Context) {
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                val isSystemApp = app.flags and ApplicationInfo.FLAG_SYSTEM != 0
                val isCurrentApp = app.packageName.startsWith(OWN_PACKAGE_NAME)
                !isSystemApp && !isCurrentApp
            }
            .map { app ->
                AppInfo(
                    name = getName(app, packageManager),
                    packageName = app.packageName,
                    icon = app.loadIcon(packageManager)
                )
            }
    }

    private fun getName(
        app: ApplicationInfo,
        packageManager: PackageManager
    ): String {
        val label = app.loadLabel(packageManager).toString()
        val name = when {
            label.contains(ANDROID_SPECIFIC_APP) -> ANDROID_SPECIFIC_APP.plus(Random.nextInt(1000))
            label.containsMultipleWord() -> label.shortenedLabel()
            label.length > 9 -> label.take(9).plus("...")
            else -> label
        }
        return name
    }

    private fun String.shortenedLabel(): String {
        return this.split(" ").joinToString(separator = " ") {
            it.first().toString().uppercase(Locale.US)
        }
    }

    private fun String.containsMultipleWord(): Boolean {
        return this.split(" ").size > 1
    }
}