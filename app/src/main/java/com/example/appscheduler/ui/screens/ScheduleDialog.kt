package com.example.appscheduler.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import java.util.Calendar
import java.util.Date

@Composable
fun ScheduleDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
    viewModel: ScheduleViewModel
) {
    var selectedTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            selectedTime = calendar.timeInMillis
        },
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        Calendar.getInstance().get(Calendar.MINUTE),
        false
    )

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
                    onClick = { timePickerDialog.show() },
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
        }
    }
}