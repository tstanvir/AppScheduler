package com.example.appscheduler.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appscheduler.data.repository.AppListRepository

class ViewModelFactory() : ViewModelProvider.Factory {
    private lateinit var repository: AppListRepository
    private lateinit var context: Context
    constructor(repository: AppListRepository) : this() {
        this.repository = repository
    }
    constructor(context: Context): this() {
        this.context = context
    }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppListViewModel(repository) as T
        } else if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}