package generate

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inputs.InputFieldForNumber
import org.bson.types.ObjectId
import kotlin.random.Random

data class LocationPoint(val lat: Double, val lng: Double)

data class RequestBodyPath(
    val accidentId: ObjectId,
    val points: List<LocationPoint>,
)

@Composable
@Preview
fun GeneratePath() {
    val instanceCount = remember { mutableStateOf("") }
    val numberOfPoints = remember { mutableStateOf("") }
    val longitudeMin = remember { mutableStateOf("") }
    val longitudeMax = remember { mutableStateOf("") }

    val latitudeMin = remember { mutableStateOf("") }
    val latitudeMax = remember { mutableStateOf("") }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 300.dp)
            .background(color = Color(0xFFE3F2FD)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = 8.dp,
            backgroundColor = Color(0xFFFFFFFF),
            modifier = Modifier.width(600.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Generate Path", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Number of Instances", fontSize = 18.sp)
                InputFieldForNumber(
                    value = instanceCount.value,
                    onValueChange = { instanceCount.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Number of Instances"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Number of points in path", fontSize = 18.sp)
                InputFieldForNumber(
                    value = numberOfPoints.value,
                    onValueChange = { numberOfPoints.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Number of Points"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Location Range", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Longitude
                Text("Longitude Range", fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InputFieldForNumber(
                        label = "Min",
                        value = longitudeMin.value,
                        onValueChange = { longitudeMin.value = it },
                        inputModifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    InputFieldForNumber(
                        label = "Max",
                        value = longitudeMax.value,
                        onValueChange = { longitudeMax.value = it },
                        inputModifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Latitude
                Text("Latitude Range", fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InputFieldForNumber(
                        label = "Min",
                        value = latitudeMin.value,
                        onValueChange = { latitudeMin.value = it },
                        inputModifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    InputFieldForNumber(
                        label = "Max",
                        value = latitudeMax.value,
                        onValueChange = { latitudeMax.value = it },
                        inputModifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            if (
                                instanceCount.value.isEmpty()
                                || numberOfPoints.value.isEmpty()
                                || longitudeMin.value.isEmpty()
                                || longitudeMax.value.isEmpty()
                                || latitudeMin.value.isEmpty()
                                || latitudeMax.value.isEmpty()
                            ) {
                                println("Please fill in all fields")
                                return@Button
                            }
                            if (
                                instanceCount.value.toInt() <= 0
                                || numberOfPoints.value.toInt() <= 0
                                || longitudeMin.value.toDouble() >= longitudeMax.value.toDouble()
                                || latitudeMin.value.toDouble() >= latitudeMax.value.toDouble()
                            ) {
                                println("Invalid input values")
                                return@Button
                            }
                            if (
                                longitudeMin.value.toDouble() < -180
                                || longitudeMax.value.toDouble() > 180
                                || latitudeMin.value.toDouble() < -90
                                || latitudeMax.value.toDouble() > 90
                            ) {
                                println("Invalid longitude or latitude values")
                                return@Button
                            }

                            var accidentId: ObjectId
                            var latitude: Double
                            var longitude: Double
                            val points = mutableListOf<LocationPoint>()
                            for (i in 0 until instanceCount.value.toInt()) {
                                //pridobi random accidentId

                                for (j in 0 until numberOfPoints.value.toInt()) {
                                    latitude = Random.nextDouble(
                                        latitudeMin.value.toDouble(),
                                        latitudeMax.value.toDouble()
                                    )
                                    longitude = Random.nextDouble(
                                        longitudeMin.value.toDouble(),
                                        longitudeMax.value.toDouble()
                                    )

                                    points.add(
                                        LocationPoint(
                                            lat = latitude,
                                            lng = longitude
                                        )
                                    )
                                }
                                //poklici create api
                                println(points)
                                // za novi loop clearni location points
                                points.clear()
                            }
                        }) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}
