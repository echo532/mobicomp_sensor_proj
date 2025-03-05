package com.example.myapplication

import StepCounter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import android.app.PendingIntent
import android.content.Intent
import com.example.myapplication.MainActivity.Companion.TAG
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var geofencingClient: GeofencingClient
    val STEP_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    val LOCATION_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION // Add this for Android 10+
    )

    fun hasStepPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    private val sharedPreferences by lazy {
        getSharedPreferences("GeofenceCounts", Context.MODE_PRIVATE)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasStepPermission()) {
            ActivityCompat.requestPermissions(this, STEP_PERMISSIONS, 0)
        }
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
        }
        resetGeofenceCounts()
        geofencingClient = LocationServices.getGeofencingClient(this)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }

        // Register geofences
        addGeofences()
    }

    private fun resetGeofenceCounts() {
        sharedPreferences.edit()
            .putInt("UNITY_HALL_COUNT", 0)
            .putInt("CAMPUS_CENTER_COUNT", 0)
            .apply()
        Log.d(TAG, "Geofence counts reset on app start")
    }

    private fun createGeofence(id: String, lat: Double, lng: Double, radius: Float): Geofence {
        return Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(lat, lng, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    private val unityHallGeofence = createGeofence("UNITY_HALL", 42.2681091, -71.8095893, 100f)
    private val campusCenterGeofence = createGeofence("CAMPUS_CENTER", 42.2687881, -71.8112722, 100f)

    private fun createGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(unityHallGeofence)
            .addGeofence(campusCenterGeofence)
            .build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    private fun addGeofences() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                LOCATION_PERMISSIONS,
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        geofencingClient.addGeofences(createGeofencingRequest(), geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d(TAG, "Geofences added successfully")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Failed to add geofences: ${exception.message}")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, add geofences
                    Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                    addGeofences()
                } else {
                    // Permission denied, show a message or handle the error
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
                return
            }

            val transitionType = geofencingEvent.geofenceTransition
            when (transitionType) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    val geofenceId = geofencingEvent.triggeringGeofences?.get(0)?.requestId
                    val sharedPreferences = context.getSharedPreferences("GeofenceCounts", Context.MODE_PRIVATE)

                    // Get the user's location
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                val latitude = location.latitude
                                val longitude = location.longitude
                                Log.d(TAG, "User location: Lat = $latitude, Lng = $longitude")
                                Toast.makeText(
                                    context,
                                    "Entered geofence. Location: Lat = $latitude, Lng = $longitude",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Log.e(TAG, "Location is null")
                            }
                        }
                    }

                    when (geofenceId) {
                        "UNITY_HALL" -> {
                            val currentCount = sharedPreferences.getInt("UNITY_HALL_COUNT", 0)
                            sharedPreferences.edit().putInt("UNITY_HALL_COUNT", currentCount + 1).commit()
                            Toast.makeText(context, "Entered Unity Hall geofence", Toast.LENGTH_LONG).show()
                            Log.d(TAG, "Entered Unity Hall geofence")
                        }
                        "CAMPUS_CENTER" -> {
                            val currentCount = sharedPreferences.getInt("CAMPUS_CENTER_COUNT", 0)
                            sharedPreferences.edit().putInt("CAMPUS_CENTER_COUNT", currentCount + 1).commit()
                            Toast.makeText(context, "Entered Campus Center geofence", Toast.LENGTH_LONG).show()
                            Log.d(TAG, "Entered Campus Center geofence")
                        }
                    }
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    // Handle exit if needed
                }
            }
        } else {
            Toast.makeText(context, "geofence is null error", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val stepCounter = remember { StepCounter(context.applicationContext) }
    val sharedPreferences = remember {
        context.getSharedPreferences("GeofenceCounts", Context.MODE_PRIVATE)
    }
    // Retrieve counts from SharedPreferences
    /*var unityHallCount by remember { mutableIntStateOf(sharedPreferences.getInt("UNITY_HALL_COUNT", 0)) }
    var campusCenterCount by remember { mutableIntStateOf(sharedPreferences.getInt("CAMPUS_CENTER_COUNT", 0)) }
    var locationCoordinates by remember { mutableStateOf("Location: Unknown") }
*/
    var unityHallCount by remember { mutableStateOf(0) }
    var campusCenterCount by remember { mutableStateOf(0) }
    // Movement type tracking
    var movementType by remember { mutableStateOf("Still") }
    var previousMovementType by remember { mutableStateOf(movementType) }

    var prevTime = remember {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis
    }

    // Step count tracking
    LaunchedEffect(Unit) {
        stepCounter.startListen()
    }
    val stepCount by stepCounter.stepCount.collectAsState()

    // Movement type change handler
    LaunchedEffect(movementType) {
        val nextTime = Calendar.getInstance().timeInMillis
        val differenceInMillis = nextTime - prevTime
        val minutes = (differenceInMillis / 1000) / 60
        val seconds = (differenceInMillis / 1000) % 60
        prevTime = nextTime

        val properType = when (previousMovementType) {
            "Still" -> "stood still"
            "Walking" -> "walked"
            "Running" -> "ran"
            "Driving" -> "drove"
            else -> "unknown"
        }
        val formattedTime = "$minutes minutes, $seconds seconds."
        Toast.makeText(context, "You have just $properType for $formattedTime", Toast.LENGTH_SHORT).show()
        previousMovementType = movementType
    }
    LaunchedEffect(Unit) {
        while(true) {
            unityHallCount = sharedPreferences.getInt("UNITY_HALL_COUNT", 0)
            campusCenterCount = sharedPreferences.getInt("CAMPUS_CENTER_COUNT", 0)
            delay(1000) // Check every second
        }
    }
    /*
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // Wait for 10 seconds (10,000 milliseconds)
            fetchUserLocation(context) { lat, lng ->
                locationCoordinates = "Location: Lat = $lat, Lng = $lng"
                //Toast.makeText(context, "Fetched location after 10 seconds: Lat = $lat, Lng = $lng", Toast.LENGTH_SHORT).show()
            }
        }
    }*/
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Blue Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Color.Blue)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { movementType = "Walking" }) {
            Text("Remove this button")
        }

        // Text Placeholders
        Text(text = "Visits to Unity Hall geoFence: $unityHallCount", fontSize = 18.sp, color = Color.White)
        Text(text = "Visits to Campus Center geoFence: $campusCenterCount", fontSize = 18.sp, color = Color.White)
        Text(text = "Steps taken since app started: $stepCount", fontSize = 18.sp, color = Color.White)
        // Display location coordinates
        //Text(text = locationCoordinates, fontSize = 18.sp, color = Color.White)

        Spacer(modifier = Modifier.height(20.dp))
        MapImage()
        Spacer(modifier = Modifier.height(20.dp))
        ImageHolder(movementType)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "You are $movementType", fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(20.dp))
        MapImage()
        Spacer(modifier = Modifier.height(20.dp))
        ImageHolder(movementType)
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "You are $movementType", fontSize = 18.sp, color = Color.White)
    }
}

private fun fetchUserLocation(context: Context, onLocationFetched: (lat: Double, lng: Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                onLocationFetched(latitude, longitude)
            } else {
                Log.e("Location", "Location is null")
            }
        }
    }
}

@Composable
fun MapImage() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Gray, shape = RoundedCornerShape(8.dp))
    ) {}
}

@Composable
fun ImageHolder(movementType: String) {
    val resourceId = when (movementType) {
        "Still" -> R.drawable.man_still
        "Walking" -> R.drawable.man_walking
        "Running" -> R.drawable.man_running
        "Driving" -> R.drawable.man_in_car
        else -> R.drawable.man_still
    }
    Image(
        painter = painterResource(id = resourceId),
        contentDescription = "Placeholder Image",
        modifier = Modifier.size(200.dp)
    )
}