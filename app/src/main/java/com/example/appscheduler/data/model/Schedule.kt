package com.example.appscheduler.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ScheduleState: Parcelable {
    SCHEDULED,
    EXECUTED,
    CANCELLED
}

@Parcelize
data class Schedule(
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("scheduled_time")
    val scheduledTime: Long,
    @SerializedName("state")
    var state: ScheduleState = ScheduleState.SCHEDULED
) : Parcelable
