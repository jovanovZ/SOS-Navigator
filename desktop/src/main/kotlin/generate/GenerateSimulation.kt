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

fun generateRandomServices(): String {
    val services = listOf("policija", "rešilci", "gasilci")
    val numberOfServices = Random.nextInt(1, 4)
    return services.shuffled().take(numberOfServices).joinToString(", ")
}

@Composable
@Preview
fun GenerateSimulation() {
    val instanceCount = remember { mutableStateOf("") }

    val responseTimeMin = remember { mutableStateOf("") }
    val responseTimeMax = remember { mutableStateOf("") }
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
                text = "Simulation Generation Complete\nAll simulations have been\n generated successfully.",
                onClose = {
                    finished.value = false
                    instanceCount.value = ""
                    responseTimeMin.value = ""
                    responseTimeMax.value = ""
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
                        Text("Generate Simulation", style = MaterialTheme.typography.h5)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Number of Instances", fontSize = 18.sp)
                    InputFieldForNumber(
                        value = instanceCount.value,
                        onValueChange = { instanceCount.value = it },
                        inputModifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Response time range (in sec)", fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        InputFieldForNumber(
                            label = "Min",
                            value = responseTimeMin.value,
                            onValueChange = { responseTimeMin.value = it },
                            inputModifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        InputFieldForNumber(
                            label = "Max",
                            value = responseTimeMax.value,
                            onValueChange = { responseTimeMax.value = it },
                            inputModifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }


                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(50),
                            onClick = {
                                if (instanceCount.value.isEmpty() || responseTimeMin.value.isEmpty() || responseTimeMax.value.isEmpty()) {
                                    println("Please fill in all fields")
                                    return@Button
                                }
                                if (instanceCount.value.toInt() <= 0 || responseTimeMin.value.toInt() <= 0 || responseTimeMax.value.toInt() <= 0) {
                                    println("Please enter valid numbers")
                                    return@Button
                                }
                                if (responseTimeMin.value.toInt() > responseTimeMax.value.toInt()) {
                                    println("Minimum response time cannot be greater than maximum")
                                    return@Button
                                }
                                var simulationName: String
                                var typeOfServices: String
                                var responseTime: Int
                                val client = OkHttpClient()
                                for (i in 1..instanceCount.value.toInt()) {
                                    try {
                                        //pridobi random userId
                                        val userIdRequest = Request.Builder()
                                            .url("${BACKEND_URL}/api/user/randomId")
                                            .get()
                                            .build()
                                        val userIdResponse = client.newCall(userIdRequest).execute()
                                        val userIdJson = JSONObject(userIdResponse.body?.string() ?: "")
                                        val userId = userIdJson.getString("id")

                                        //pridobi random accidentId
                                        val accidentIdRequest = Request.Builder()
                                            .url("${BACKEND_URL}/api/accident/randomId")
                                            .get()
                                            .build()
                                        val accidentIdResponse = client.newCall(accidentIdRequest).execute()
                                        val accidentIdJson = JSONObject(accidentIdResponse.body?.string() ?: "")
                                        val accidentId = accidentIdJson.getString("id")


                                        //pridobi random stationId
                                        val stationIdRequest = Request.Builder()
                                            .url("${BACKEND_URL}/api/station/randomId")
                                            .get()
                                            .build()
                                        val stationIdResponse = client.newCall(stationIdRequest).execute()
                                        val stationIdJson = JSONObject(stationIdResponse.body?.string() ?: "")
                                        val stationId = stationIdJson.getString("id")

                                        //pridobi random pathId
                                        val pathIdRequest = Request.Builder()
                                            .url("${BACKEND_URL}/api/path/randomId")
                                            .get()
                                            .build()
                                        val pathIdResponse = client.newCall(pathIdRequest).execute()
                                        val pathIdJson = JSONObject(pathIdResponse.body?.string() ?: "")
                                        val pathId = pathIdJson.getString("id")

                                        simulationName = "Generirana simulacija $i"

                                        typeOfServices = generateRandomServices()

                                        responseTime =
                                            Random.nextInt(responseTimeMin.value.toInt(), responseTimeMax.value.toInt())
                                        responseTime *= 1000

                                        val url = "${BACKEND_URL}/api/simulation/create"
                                        val json = JSONObject()
                                            .put("simulationName", simulationName)
                                            .put("userId", userId)
                                            .put("accidentId", accidentId)
                                            .put("bestStationId", stationId)
                                            .put("bestPathId", pathId)
                                            .put("typeOfServices", typeOfServices)
                                            .put("responseTime", responseTime)
                                            .put("locationFrom", stationId)
                                            .put("locationTo", accidentId)
                                            .toString()
                                        val body = json.toRequestBody(("application/json").toMediaTypeOrNull())
                                        val request = Request.Builder().url(url).post(body).build()
                                        val response = client.newCall(request).execute()
                                        if (response.isSuccessful) {
                                            val responseString = response.body?.string() ?: ""
                                            val responseJson = JSONObject(responseString)
                                            val newSimulation = responseJson.getJSONObject("newSimulation")
                                            println("Simulation created successfully: $newSimulation")
                                        } else {
                                            println("Failed to create simulation: ${response.message}")
                                        }
                                    } catch (e: Exception) {
                                        errorMessage.value = "Error generating simulation: ${e.message}"
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