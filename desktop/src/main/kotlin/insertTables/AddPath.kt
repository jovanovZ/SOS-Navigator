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

@Composable
@Preview
fun AddPath() {
    val accidentId = remember { mutableStateOf("") }
    val arrayOfLocationIds = remember { mutableStateOf(mutableListOf<String>()) }
    val newLocationId = remember { mutableStateOf("") }

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

                Text("Add Location id (array of locationIds)", fontSize = 18.sp)
                InputFieldForText(
                    value = newLocationId.value,
                    onValueChange = { newLocationId.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Simulation id"
                )
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(30),
                    onClick = {
                        if (newLocationId.value.isNotEmpty() && !arrayOfLocationIds.value.contains(newLocationId.value)) {
                            arrayOfLocationIds.value.add(newLocationId.value)
                            newLocationId.value = ""
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Add ID")
                }

                Text("Current Simulation IDs: ${arrayOfLocationIds.value.joinToString(", ")}", fontSize = 14.sp)


                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30),
                        onClick = {
                           if(accidentId.value.isEmpty() || arrayOfLocationIds.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            println("""
                                Path info:
                                Accident ID: ${accidentId.value}
                                Location IDs: ${arrayOfLocationIds.value.joinToString(", ")}
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
