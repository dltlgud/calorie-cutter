package com.example.main.calendarui2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutViewModel : ViewModel() {

    private val _selectedDate = MutableLiveData(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: LiveData<String> = _selectedDate

    private val _workoutRecords = MutableLiveData<List<WorkoutRecord>>()
    val workoutRecords: LiveData<List<WorkoutRecord>> = _workoutRecords

    fun selectDate(date: String) {
        _selectedDate.value = date
        _workoutRecords.value = emptyList()
    }
}
