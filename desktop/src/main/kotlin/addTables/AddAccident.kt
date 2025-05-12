package addTables

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
@Preview
fun AddAccident() {
    val accidentId = remember { mutableStateOf("") }
    var expanded = remember { mutableStateOf(false) }
    var selectedAccident = remember { mutableStateOf("") }
    val typeOfAccident = remember { mutableStateListOf(
        "Prometna",
        "Požar",
        "Naravna nesreča",
        "Onesnaženje",
        "Zdravstveni nujni primer",
        "Eksplozija",
        "Napad",
        "Drugo",
    ) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 300.dp)
            .padding(32.dp),
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
                    Text("Insert into Accident table", style = MaterialTheme.typography.h5)
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

                Text("Type of accident", fontSize = 18.sp)
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clickable { expanded.value = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedAccident.value.isEmpty()) "Select Region" else selectedAccident.value,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        typeOfAccident.forEach { region ->
                            DropdownMenuItem(onClick = {
                                selectedAccident.value = region
                                expanded.value = false
                            }) {
                                Text(region)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(50),
                        onClick = {
                           if(accidentId.value.isEmpty() || selectedAccident.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            println("""
                                Accident info:
                                Accident ID: ${accidentId.value}
                                Type of accident: ${selectedAccident.value}
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
