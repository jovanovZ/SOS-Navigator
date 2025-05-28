package insertTables

import BACKEND_URL
import InputFieldForBoolean
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.types.ObjectId
import org.json.JSONObject

@Composable
@Preview
fun AddStation() {
    val longitude = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf("") }
    var isPermanent = remember { mutableStateOf(false) }
    var expanded = remember { mutableStateOf(false) }
    var expandedType = remember { mutableStateOf(false) }
    var selectedType = remember { mutableStateOf("") }
    val allTypes = remember {
        mutableStateListOf(
            "Policijska", "Bolnica", "Gasilci"
        )
    }
    var selectedRegion = remember { mutableStateOf("") }
    val regions = remember {
        mutableStateListOf(
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
        )
    }


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
                    Text("Insert into Station table", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Type of station ", fontSize = 18.sp)
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .clickable { expandedType.value = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedType.value.isEmpty()) "Select Station" else selectedType.value,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedType.value,
                        onDismissRequest = { expandedType.value = false }
                    ) {
                        allTypes.forEach { type ->
                            DropdownMenuItem(onClick = {
                                selectedType.value = type
                                expandedType.value = false
                            }) {
                                Text(type)
                            }
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Longitude", fontSize = 18.sp)
                InputFieldForNumber(
                    value = longitude.value,
                    onValueChange = { longitude.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Longitude"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Latitude", fontSize = 18.sp)
                InputFieldForNumber(
                    value = latitude.value,
                    onValueChange = { latitude.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Latitude"
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
                        shape = RoundedCornerShape(30),
                        onClick = {
                            if (longitude.value.isEmpty() || latitude.value.isEmpty() || selectedRegion.value.isEmpty() || selectedType.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            try {
                                val url = "${BACKEND_URL}/api/station/create"
                                val client = OkHttpClient()
                                val json = JSONObject()
                                    .put("latitude", latitude.value)
                                    .put("longitude", longitude.value)
                                    .put("typeOfStation", selectedType.value)
                                    .put("isPermanent", isPermanent.value)
                                    .put("region", selectedRegion.value)
                                    .toString()

                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder().url(url).post(body).build()
                                val response = client.newCall(request).execute()

                                if (response.isSuccessful) {
                                    val responseBody = response.body?.string() ?: ""
                                    println("Station created: $responseBody")
                                    longitude.value = ""
                                    latitude.value = ""
                                    selectedType.value = ""
                                    isPermanent.value = false
                                    selectedRegion.value = ""
                                } else {
                                    println("Failed to create station: ${response.message}")
                                }
                            } catch (e: Exception) {
                                println("Error inserting station: ${e.message}")
                            }

                        }) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}
