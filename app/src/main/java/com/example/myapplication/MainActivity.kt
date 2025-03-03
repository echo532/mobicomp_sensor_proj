package com.example.myapplication

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {

    //Stores the movement type of the user, starts at Still
    var movementType by remember {mutableStateOf("Still")}

    //used to show time taken during activity
    var previousMovementType by remember {mutableStateOf(movementType)}

    // current context of app, used for toast
    val context = LocalContext.current
    var prevTime = remember { //cannot use .now(), invalid for API 24
        val calendar = Calendar.getInstance()
        calendar.timeInMillis
    }


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
        GeoFenceText("Campus Center")
        GeoFenceText("Unity Hall")
        StepCountText()

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
fun TextPlaceholder(text: String) {
    Text(text = text, fontSize = 18.sp, color = Color.White)
}
@Composable
fun GeoFenceText(location: String) {
    //TODO: Geofence code call
    /*
     if location = Campus Center then....
     else location == Unity Hall then....
     */
    var number = 0
    val output = "Visits to $location geoFence: $number"
    Text(text = output, fontSize = 18.sp, color = Color.White)
}

@Composable
fun StepCountText() {
    //TODO: Step Count code call
    var number = 0
    val output = "Steps taken since app started: $number"
    Text(text = output, fontSize = 18.sp, color = Color.White)
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

fun getBottomText(value: String) = "You are $value"

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    MainScreen()
}
