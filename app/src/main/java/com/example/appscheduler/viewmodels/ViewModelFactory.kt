package com.example.appscheduler.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory() : ViewModelProvider.Factory {
    private lateinit var context: Context

    constructor(context: Context): this() {
        this.context = context
    }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppListViewModel(context) as T
        } else if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}