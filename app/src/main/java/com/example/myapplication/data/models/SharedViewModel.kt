package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val _entries = MutableLiveData<List<WeeklyEntry>>()
    val entries: LiveData<List<WeeklyEntry>> = _entries

    init {
        _entries.value = listOf(
            WeeklyEntry("Apr 29", 66f, 28f, 44f, 70f, 62f, 1.8f, 52, 31, 17),
            WeeklyEntry("May 06", 70f, 30f, 45f, 72f, 68f, 2.0f, 51, 32, 17),
            WeeklyEntry("May 13", 74f, 32f, 47f, 74f, 76f, 2.2f, 50, 33, 17),
            WeeklyEntry("May 20", 78f, 33f, 48f, 75f, 82f, 2.4f, 50, 33, 17)
        )
    }

    fun addEntry(entry: WeeklyEntry) {
        val list = _entries.value?.toMutableList() ?: mutableListOf()
        list.add(entry)
        _entries.value = list
    }
}
