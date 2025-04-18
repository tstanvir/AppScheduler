package com.example.appscheduler.data.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import java.util.UUID

enum class ScheduleState(private val colorLong: ULong) {
    NOT_SCHEDULED(Color.Gray.value),
    SCHEDULED(Color.Blue.value),
    EXECUTED(Color.Green.value),
    CANCELLED(Color.Red.value);

    val color: Color
        get() = Color(colorLong)

    companion object {
        fun fromColor(color: Color): ScheduleState? {
            return entries.find { it.color.value == color.value }
        }
    }
}

data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("scheduled_time")
    val scheduledTime: Long,
    @SerializedName("state")
    var state: ScheduleState = ScheduleState.SCHEDULED
)
