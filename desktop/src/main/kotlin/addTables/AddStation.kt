package addTables

import InputFieldForBoolean
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
fun AddStation() {
    val locationId = remember { mutableStateOf("") }
    var isPermanent =  remember { mutableStateOf(false) }
    var expanded = remember { mutableStateOf(false) }
    var selectedRegion = remember { mutableStateOf("") }
    val regions = remember { mutableStateListOf(
        "Pomurska",
        "Podravska",
        "Koroška",
        "Savinjska",
        "Zasavska",
        "Posavska",
        "Jugovzhodna Slovenija",
        "Osrednjeslovenska",
        "Gorenjska",
        "Primorsko-notranjska",
        "Goriška",
        "Obalno-kraška"
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
                    Text("Insert into Station table", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Location id", fontSize = 18.sp)
                InputFieldForText(
                    value = locationId.value,
                    onValueChange = { locationId.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Location id"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                InputFieldForBoolean(
                    value = isPermanent.value,
                    onValueChange = { isPermanent.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Is this permanent station?"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Region", fontSize = 18.sp)
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clickable { expanded.value = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedRegion.value.isEmpty()) "Select Region" else selectedRegion.value,
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
                        regions.forEach { region ->
                            DropdownMenuItem(onClick = {
                                selectedRegion.value = region
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
                            if(locationId.value.isEmpty() || selectedRegion.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            println("""
                                Station info:
                                LocationId:${locationId.value}
                                Is permanent: ${isPermanent.value}
                                Region: ${selectedRegion.value}"
                            """.trimIndent())
                        }) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}
