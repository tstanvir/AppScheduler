package com.example.appscheduler.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.appscheduler.data.model.AppInfo
import com.example.appscheduler.data.model.Schedule
import com.example.appscheduler.data.model.ScheduleState
import com.example.appscheduler.data.repository.AppStateRepository
import com.example.appscheduler.viewmodels.ScheduleViewModel
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    viewModel: ScheduleViewModel
) {
    var timePickerState by remember {
        mutableStateOf(
            TimePickerState(
                initialHour = LocalTime.now().hour,
                initialMinute = LocalTime.now().minute,
                is24Hour = true
            )
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Schedule ${app.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Select Time")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val schedule = Schedule(
                            packageName = app.packageName,
                            scheduledTime = selectedTime,
                            state = ScheduleState.SCHEDULED
                        )
                        val prevState = AppStateRepository.appStates.value[app.packageName]
                        val scheduleDone = viewModel.scheduleApp(schedule)
                        if (prevState == ScheduleState.SCHEDULED && scheduleDone) {
                            Toast.makeText(context, "Schedule updated at ${Date(selectedTime)}", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Schedule")
                }
            }

            if (showTimePicker) {
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    title = { Text("Schedule ${app.name}") },
                    text = {
                        Column {
                            TimePicker(state = timePickerState)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedTime = timePickerState.getEpochMilli()
                                showTimePicker = false
                            }
                        ) {
                            Text("Schedule")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showTimePicker = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.getEpochMilli(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, this.hour)
    calendar.set(Calendar.MINUTE, this.minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}