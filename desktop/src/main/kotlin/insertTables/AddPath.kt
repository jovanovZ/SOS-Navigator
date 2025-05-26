package insertTables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inputs.InputFieldForText
import org.bson.types.ObjectId

@Composable
@Preview
fun AddPath() {
    val accidentId = remember { mutableStateOf("") }
    val locationPoints = remember { mutableStateOf(mutableListOf<Map<String, Double>>()) }
    val newLat = remember { mutableStateOf("") }
    val newLng = remember { mutableStateOf("") }

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
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Insert into Path table", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Accident id", fontSize = 18.sp)
                InputFieldForText(
                    value = accidentId.value,
                    onValueChange = { accidentId.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Accident id"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Latitude", fontSize = 18.sp)
                InputFieldForText(
                    value = newLat.value,
                    onValueChange = { newLat.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Latitude"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Longitude", fontSize = 18.sp)
                InputFieldForText(
                    value = newLng.value,
                    onValueChange = { newLng.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Longitude"
                )
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(30),
                    onClick = {
                        val lat = newLat.value.toDoubleOrNull()
                        val lng = newLng.value.toDoubleOrNull()
                        if (lat != null && lng != null) {
                            locationPoints.value.add(mapOf("lat" to lat, "lng" to lng))
                            newLat.value = ""
                            newLng.value = ""
                        } else {
                            println("Invalid latitude or longitude")
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Add Location Point")
                }

                Text("Current Location Points: ${locationPoints.value.joinToString(", ") { "(${it["lat"]}, ${it["lng"]})" }}", fontSize = 14.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30),
                        onClick = {
                            if (accidentId.value.isEmpty() || locationPoints.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            if (!ObjectId.isValid(accidentId.value)) {
                                println("Accident ID must be a valid ObjectId")
                                return@Button
                            }
                            println("""
                                Path info:
                                Accident ID: ${accidentId.value}
                                Location Points: ${locationPoints.value.joinToString(", ") { "(${it["lat"]}, ${it["lng"]})" }}
                            """.trimIndent()
                            )
                        }) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}