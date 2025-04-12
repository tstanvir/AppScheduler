package com.example.appscheduler.data.model

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
enum class ScheduleState(private val colorValue: Int): Parcelable {
    NOT_EXECUTED(Color.Gray.value.toInt()),
    SCHEDULED(Color.Blue.value.toInt()),
    EXECUTED(Color.Green.value.toInt()),
    CANCELLED(Color.Red.value.toInt());
    val color: Color
        get() = Color(colorValue)

    companion object {
        fun fromColor(color: Color): ScheduleState? {
            return entries.find { it.color == color }
        }
    }
}

@Parcelize
data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("scheduled_time")
    val scheduledTime: Long,
    @SerializedName("state")
    var state: ScheduleState = ScheduleState.SCHEDULED
) : Parcelable
