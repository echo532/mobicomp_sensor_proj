import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

import kotlinx.coroutines.flow.MutableStateFlow


private const val TAG = "STEP_COUNT_LISTENER"

//https://developer.android.com/health-and-fitness/guides/basic-fitness-app/read-step-count-data#periodically-retrieving

class StepCounter(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    public val stepCount = MutableStateFlow(0L)
    private var initialStepCount: Long = -1L


    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                var totalSteps = event.values[0].toLong()

                if(initialStepCount == -1L){initialStepCount = totalSteps}

                stepCount.value = totalSteps - initialStepCount // steps since beginning
                Log.d(TAG, "Step count updated: $stepCount")
            }

        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startListen(){
        Log.d(TAG, "Step count started: $stepCount")
            sensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }
    }




