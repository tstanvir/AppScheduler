package com.example.appscheduler.data.repository

import com.example.appscheduler.data.model.Schedule

object ScheduleRepository {
    private var appScheduleMap = mutableMapOf<String, Schedule>()

    @Synchronized
    fun getLatestSchedule(packageName: String): Schedule? {
        return appScheduleMap[packageName]
    }

    @Synchronized
    fun putLatestSchedule(packageName: String, schedule: Schedule) {
        appScheduleMap[packageName] = schedule
    }
}