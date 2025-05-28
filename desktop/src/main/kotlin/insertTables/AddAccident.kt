package insertTables

import BACKEND_URL
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
fun AddAccident() {
    var expanded = remember { mutableStateOf(false) }
    val longitude = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf("") }
    var selectedAccident = remember { mutableStateOf("") }
    val typeOfAccident = remember {
        mutableStateListOf(
            "prometna",
            "naravna nesreča",
            "zdravstveni primer",
            "kriminal"
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
                    Text("Insert into Accident table", style = MaterialTheme.typography.h5)
                }
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
                            text = if (selectedAccident.value.isEmpty()) "Select type of accident" else selectedAccident.value,
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
                                Text(region.replaceFirstChar { it.uppercase() })
                            }
                        }
                    }
                }
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

                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30),
                        onClick = {
                            if (selectedAccident.value.isEmpty() || longitude.value.isEmpty() || latitude.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }

                            try{
                                val url = "${BACKEND_URL}/api/accident/create"
                                val client = OkHttpClient()
                                val json = JSONObject()
                                    .put("latitude", latitude.value.toDouble())
                                    .put("longitude", longitude.value.toDouble())
                                    .put("type", selectedAccident.value)
                                    .toString()
                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder()
                                    .url(url)
                                    .post(body)
                                    .build()
                                val response = client.newCall(request).execute()
                                if (response.isSuccessful) {
                                    val responseBody = response.body?.string() ?: ""
                                    println("Accident created: $responseBody")
                                    selectedAccident.value = ""
                                    longitude.value = ""
                                    latitude.value = ""
                                } else {
                                    println("Failed to create accident: ${response.message}")
                                }
                            }catch (e : Exception){
                                println("Error creating accident: ${e.message}")
                            }
                        }) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}
