package generate

import BACKEND_URL
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
import io.github.serpro69.kfaker.Faker
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.types.ObjectId
import org.json.JSONObject
import kotlin.random.Random

@Composable
@Preview
fun GenerateStation() {
    val instanceCount = remember { mutableStateOf("") }
    val allRegions = listOf(
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
        "Obalno-kraška",
    )
    val stationType = listOf("Policijska", "Bolnica", "Gasilci")
    val longitudeMin = remember { mutableStateOf("") }
    val longitudeMax = remember { mutableStateOf("") }

    val latitudeMin = remember { mutableStateOf("") }
    val latitudeMax = remember { mutableStateOf("") }
    val finished = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 300.dp)
            .background(color = Color(0xFFE3F2FD)),
        contentAlignment = Alignment.Center
    ) {
        if (errorMessage.value != null) {
            GenerateModal(
                text = errorMessage.value!!,
                onClose = {
                    errorMessage.value = null
                    finished.value = false
                }
            )
        } else if (finished.value) {
            GenerateModal(
                text = "Station Generation Complete\nAll stations have been\n generated successfully.",
                onClose = {
                    finished.value = false
                    instanceCount.value = ""
                    longitudeMin.value = ""
                    longitudeMax.value = ""
                    latitudeMin.value = ""
                    latitudeMax.value = ""
                }
            )
        } else {
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
                        Text("Generate Station", style = MaterialTheme.typography.h5)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Number of Instances", fontSize = 18.sp)
                    InputFieldForNumber(
                        value = instanceCount.value,
                        onValueChange = { instanceCount.value = it },
                        inputModifier = Modifier.fillMaxWidth()
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

                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(50),
                            onClick = {
                                if (instanceCount.value.isEmpty() || instanceCount.value.toInt() <= 0) {
                                    println("Instance count is empty or less than 0")
                                    return@Button
                                }
                                if (longitudeMin.value.isEmpty() || longitudeMax.value.isEmpty() || latitudeMin.value.isEmpty() || latitudeMax.value.isEmpty()) {
                                    println("Longitude or Latitude is empty")
                                    return@Button
                                }
                                if (longitudeMin.value.toDouble() >= longitudeMax.value.toDouble()
                                    || latitudeMin.value.toDouble() >= latitudeMax.value.toDouble()
                                ) {
                                    println("Min and Max values are not valid")
                                    return@Button
                                }
                                if (longitudeMin.value.toDouble() < -180 || longitudeMax.value.toDouble() > 180) {
                                    println("Needs to be between -180 and 180")
                                    return@Button
                                }
                                if (latitudeMin.value.toDouble() < -90 || latitudeMax.value.toDouble() > 90) {
                                    println("Needs to be between -90 and 90")
                                    return@Button
                                }

                                var typeOfStation: String
                                var region: String
                                var isPermanent: Boolean
                                var latitude: Double
                                var longitude: Double
                                val client = OkHttpClient()
                                for (i in 0 until instanceCount.value.toInt()) {
                                    try {
                                        latitude = Random.nextDouble(
                                            latitudeMin.value.toDouble(),
                                            latitudeMax.value.toDouble()
                                        )
                                        longitude = Random.nextDouble(
                                            longitudeMin.value.toDouble(),
                                            longitudeMax.value.toDouble()
                                        )

                                        typeOfStation = stationType.random()
                                        isPermanent = Random.nextBoolean()
                                        region = allRegions.random()

                                        val url = "${BACKEND_URL}/api/station/create"
                                        val json = JSONObject().put("longitude", longitude)
                                            .put("latitude", latitude)
                                            .put("typeOfStation", typeOfStation)
                                            .put("region", region)
                                            .put("isPermanent", isPermanent)
                                            .toString()
                                        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                        val request = Request.Builder().url(url).post(body).build()
                                        client.newCall(request).execute().use { response ->
                                            if (response.isSuccessful) {
                                                val responseString = response.body?.string() ?: ""
                                                val responseJson = JSONObject(responseString)
                                                val station = responseJson.getJSONObject("station")
                                                println("Station created successfully: $station")
                                            } else {
                                                println("Failed to create station: ${response.message}")
                                            }
                                        }

                                    } catch (e: Exception) {
                                        errorMessage.value = "Error generating station: ${e.message}"
                                    }

                                }
                                finished.value = true
                            }) {
                            Text("Generate")
                        }
                    }
                }
            }
        }
    }
}