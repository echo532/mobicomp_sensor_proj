package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.location.ActivityTransitionResult

class ActivityTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)

            result?.transitionEvents?.forEach { event ->
                val activityType = when (event.activityType) {
                    com.google.android.gms.location.DetectedActivity.STILL -> "Still"
                    com.google.android.gms.location.DetectedActivity.WALKING -> "Walking"
                    com.google.android.gms.location.DetectedActivity.RUNNING -> "Running"
                    com.google.android.gms.location.DetectedActivity.IN_VEHICLE -> "Driving"
                    else -> "Unknown"
                }

                globalActivityState = activityType
            }
        }
    }
}
