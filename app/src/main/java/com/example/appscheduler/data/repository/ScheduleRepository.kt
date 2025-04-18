package com.example.appscheduler.data.repository

import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.data.model.ScheduleState

object ScheduleRepository {
    private var appScheduleMap = mutableMapOf<String, MutableList<Schedule>>()

    @Synchronized
    fun getLatestSchedule(packageName: String): Schedule? {
        return appScheduleMap[packageName]?.last()
    }

    @Synchronized
    fun addSchedule(packageName: String, schedule: Schedule) {
        if (appScheduleMap[packageName] == null) {
            appScheduleMap[packageName] = mutableListOf()
        }
        appScheduleMap[packageName]?.add(schedule)
    }

    @Synchronized
    fun updateSchedule(packageName: String, scheduleId: String, state: ScheduleState) {
        var schedules = appScheduleMap[packageName]

        schedules = schedules?.map {
            if (it.id == scheduleId) it.copy(state = state) else it
        }?.toMutableList()

        appScheduleMap[packageName] = schedules!!
    }

    @Synchronized
    fun getAllExecutedScheduleOf(packageName: String): MutableList<Long> {
        val schedules = appScheduleMap[packageName]
        val executionTimeStamps = mutableListOf<Long>()
        schedules?.filter {
            it.state == ScheduleState.EXECUTED
        }?.forEach {
            executionTimeStamps.add(it.scheduledTime)
        }

        return executionTimeStamps
    }
}