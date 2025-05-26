package insertTables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inputs.InputFieldForNumber
import inputs.InputFieldForText
import org.bson.types.ObjectId

@Composable
@Preview
fun AddSimulation() {
    val userId = remember { mutableStateOf("") }
    val accidentId = remember { mutableStateOf("") }
    val typeOfServices = remember { mutableStateOf("") }
    val simulationName = remember { mutableStateOf("") }


    val bestStationId = remember { mutableStateOf("") }
    val bestPathId = remember { mutableStateOf("") }
    val responseTime = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()


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
                modifier = Modifier.padding(16.dp).verticalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Insert into Simulation table", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("User id", fontSize = 18.sp)
                InputFieldForText(
                    value = userId.value,
                    onValueChange = { userId.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "User id"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Simulation name", fontSize = 18.sp)
                InputFieldForText(
                    value = simulationName.value,
                    onValueChange = { simulationName.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Simulation name"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Accident id", fontSize = 18.sp)
                InputFieldForText(
                    value = accidentId.value,
                    onValueChange = { accidentId.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Accident id"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Type of service", fontSize = 18.sp)
                InputFieldForText(
                    value = typeOfServices.value,
                    onValueChange = { typeOfServices.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Type of service"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Best station id", fontSize = 18.sp)
                InputFieldForText(
                    value = bestStationId.value,
                    onValueChange = { bestStationId.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Best station id"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Best path id", fontSize = 18.sp)
                InputFieldForText(
                    value = bestPathId.value,
                    onValueChange = { bestPathId.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Best path id"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Response time (in milliseconds)", fontSize = 18.sp)
                InputFieldForNumber(
                    value = responseTime.value,
                    onValueChange = { responseTime.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Response time"
                )

                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30),
                        onClick = {
                            if (userId.value.isEmpty() || accidentId.value.isEmpty() || bestStationId.value.isEmpty() || bestPathId.value.isEmpty() || responseTime.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            if (!ObjectId.isValid(userId.value) || !ObjectId.isValid(accidentId.value) || !ObjectId.isValid(
                                    bestStationId.value
                                ) || !ObjectId.isValid(bestPathId.value)
                            ) {
                                println("ID must be a valid ObjectId")
                                return@Button
                            }
                            println(
                                """
                                        Simulation info:
                                        User ID: ${userId.value}
                                        Accident ID: ${accidentId.value}
                                        Type of service: ${typeOfServices.value}
                                        Best Station ID: ${bestStationId.value}
                                        Best Path ID: ${bestPathId.value}
                                        Response Time: ${responseTime.value} ms
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
