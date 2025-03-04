package com.example.myapplication

import StepCounter
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
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



class MainActivity : ComponentActivity() {
    val STEP_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    fun hasStepPermission(): Boolean {
        //gpt
        return ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       if(!hasStepPermission()){
           ActivityCompat.requestPermissions(
               this, STEP_PERMISSIONS, 0
           )
       }

        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }


}


@Composable
fun MainScreen() {
    val context = LocalContext.current
    val stepCounter = remember{ StepCounter(context.applicationContext) }

    var campusCenterCount by remember { mutableIntStateOf(0) }

    var unityHallCount by remember {mutableStateOf(0)}


    //Stores the movement type of the user, starts at Still
    var movementType by remember {mutableStateOf("Still")}

    //used to show time taken during activity
    var previousMovementType by remember {mutableStateOf(movementType)}

    // current context of app, used for toast

    var prevTime = remember { //cannot use .now(), invalid for API 24
        val calendar = Calendar.getInstance()
        calendar.timeInMillis
    }

    //TODO: movement type tracker code

    //TODO: geofencing tracking code

    //TODO: step count tracking code
    LaunchedEffect(Unit) {
        //only called when screen loads
        stepCounter.startListen()
    }

    val stepCount by stepCounter.stepCount.collectAsState()



    //used to add toast messages when movementType changes
    LaunchedEffect(movementType) { // Runs every time 'counter' changes

        //get time between last activity start
        var nextTime = Calendar.getInstance().timeInMillis
        val differenceInMillis = nextTime - prevTime
        val minutes = (differenceInMillis / 1000) / 60
        val seconds = (differenceInMillis / 1000) % 60
        prevTime = nextTime // reassign last time

        // makes text appear grammatically correct
        var properType = ""
        when (previousMovementType){
            "Still" -> properType = "stood still"
            "Walking" -> properType = "walked"
            "Running" -> properType = "ran"
            "Driving" -> properType = "drove"
        }
        val formattedTime = "$minutes minutes, $seconds seconds."

        Toast.makeText(context, "You have just $properType for $formattedTime", Toast.LENGTH_SHORT).show()
        previousMovementType = movementType // track current movement type

    }


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

        Button(onClick = {
            movementType = "Walking"
        }) {
            Text("Remove this button")
        }

        // Text Placeholders
        Text(text = "Visits to Unity Hall geoFence: $campusCenterCount", fontSize = 18.sp, color = Color.White)

        Text(text = "Visits to Unity Hall geoFence: $unityHallCount", fontSize = 18.sp, color = Color.White)

        Text(text = "Steps taken since app started: $stepCount", fontSize = 18.sp, color = Color.White)

        Spacer(modifier = Modifier.height(20.dp))

        MapImage()

        Spacer(modifier = Modifier.height(20.dp))

        ImageHolder(movementType)

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom Text Placeholder
        Text(text = "you are $movementType", fontSize = 18.sp, color = Color.White)
    }
}


@Composable
fun MapImage() {
    //TODO: make map appear
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Gray, shape = RoundedCornerShape(8.dp))
    ) {}
}

@Composable
fun ImageHolder(movementType: String) {
    var resourceId: Int
    when (movementType) {
        "Still" -> resourceId = R.drawable.man_still
        "Walking" -> resourceId = R.drawable.man_walking
        "Running" -> resourceId = R.drawable.man_running
        "Driving" -> resourceId = R.drawable.man_in_car
        else -> resourceId = R.drawable.man_still // Default just in case
    }
    Image(
        painter = painterResource(id = resourceId),
        contentDescription = "Placeholder Image",
        modifier = Modifier
            .size(200.dp)
    )
}

