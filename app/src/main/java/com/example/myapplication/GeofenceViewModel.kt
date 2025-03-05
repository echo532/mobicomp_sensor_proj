package com.example.myapplication

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GeofenceViewModel : ViewModel() {
    private val _unityHallCount = MutableStateFlow(0)
    val unityHallCount: StateFlow<Int> get() = _unityHallCount

    private val _campusCenterCount = MutableStateFlow(0)
    val campusCenterCount: StateFlow<Int> get() = _campusCenterCount

    fun incrementUnityHallCount() {
        _unityHallCount.value += 1
    }

    fun incrementCampusCenterCount() {
        _campusCenterCount.value += 1
    }
}
