package insertTables

import BACKEND_URL
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
import inputs.InputFieldForNumber
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
@Preview
fun AddLocation() {
    val longitude = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf("") }

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
                    Text("Insert into Location table", style = MaterialTheme.typography.h5)
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

                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30),
                        onClick = {
                            if (longitude.value.isEmpty() || latitude.value.isEmpty()) {
                                return@Button
                            }
                            try {
                                val url = "${BACKEND_URL}/api/location/create"
                                val client = OkHttpClient()
                                val json = JSONObject()
                                    .put("long", longitude.value.toDouble())
                                    .put("lat", latitude.value.toDouble())
                                    .toString()
                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder()
                                    .url(url)
                                    .post(body)
                                    .build()
                                client.newCall(request).execute().use { response ->
                                    if (response.isSuccessful) {
                                        val responseBody = response.body?.string() ?: ""
                                        println("Location created: $responseBody")
                                        longitude.value = ""
                                        latitude.value = ""
                                    } else {
                                        println("Failed to create location: ${response.message}")
                                    }
                                }
                            } catch (e: Exception) {
                                println("Error creating location: ${e.message}")
                            }
                        }) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}
