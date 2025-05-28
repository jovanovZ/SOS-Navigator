package viewTables

import BACKEND_URL
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import db.DataBase
import inputs.InputFieldForNumber
import inputs.InputFieldForText
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.bson.types.ObjectId
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.bson.Document
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date


data class Simulation(
    val _id: ObjectId = ObjectId(),
    val userId: ObjectId,
    val simulationName: String,
    val accidentId: ObjectId,
    val typeOfServices: String,
    val bestStationId: ObjectId,
    val bestPathId: ObjectId,
    val responseTime: Int,
    val created: Date,
    val locationFrom: String,
    val locationTo: String
)


@Composable
fun SimulationCard(simulation: Simulation, onDelete: (Simulation) -> Unit, onSave: (Simulation) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val userIdInput = remember { mutableStateOf(simulation.userId.toString()) }
    val simulationNameInput = remember { mutableStateOf(simulation.simulationName) }
    val accidentIdInput = remember { mutableStateOf(simulation.accidentId.toString()) }
    val typeOfServicesInput = remember { mutableStateOf(simulation.typeOfServices) }
    val bestStationIdInput = remember { mutableStateOf(simulation.bestStationId.toString()) }
    val bestPathIdInput = remember { mutableStateOf(simulation.bestPathId.toString()) }

    val responseTimeInput = remember { mutableStateOf(simulation.responseTime) }


    if (isEditing.value) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .width(500.dp)
                .height(400.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFFFF),

            ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                Text("Id: ${simulation._id}")
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = userIdInput.value,
                    onValueChange = { userIdInput.value = it },
                    label = "User id",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = accidentIdInput.value,
                    onValueChange = { accidentIdInput.value = it },
                    label = "Accident id",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = simulationNameInput.value,
                    onValueChange = { simulationNameInput.value = it },
                    label = "Simulation name",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = typeOfServicesInput.value,
                    onValueChange = { typeOfServicesInput.value = it },
                    label = "Type of services",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = bestStationIdInput.value,
                    onValueChange = { bestStationIdInput.value = it },
                    label = "Best station id",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = bestPathIdInput.value,
                    onValueChange = { bestPathIdInput.value = it },
                    label = "Best path id",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForNumber(
                    value = responseTimeInput.value.toString(),
                    onValueChange = { responseTimeInput.value = it.toIntOrNull() ?: 0 },
                    label = "Response time",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if (userIdInput.value.isEmpty() || accidentIdInput.value.isEmpty() ||
                                typeOfServicesInput.value.isEmpty() || bestStationIdInput.value.isEmpty() ||
                                bestPathIdInput.value.isEmpty() || responseTimeInput.value.toString()
                                    .isEmpty() || simulationNameInput.value.isEmpty()
                            ) {
                                println("Please fill all fields")
                                return@Button
                            }
                            if (!ObjectId.isValid(userIdInput.value) ||
                                !ObjectId.isValid(accidentIdInput.value) ||
                                !ObjectId.isValid(bestStationIdInput.value) ||
                                !ObjectId.isValid(bestPathIdInput.value)
                            ) {
                                println("Invalid ObjectId format")
                                return@Button
                            }
                            if (responseTimeInput.value <= 0) {
                                println("Response time must be greater than 0")
                                return@Button
                            }

                            val updatedSimulation = simulation.copy(
                                userId = ObjectId(userIdInput.value),
                                accidentId = ObjectId(accidentIdInput.value),
                                typeOfServices = typeOfServicesInput.value,
                                bestStationId = ObjectId(bestStationIdInput.value),
                                bestPathId = ObjectId(bestPathIdInput.value),
                                responseTime = responseTimeInput.value,
                                simulationName = simulationNameInput.value
                            )
                            onSave(updatedSimulation)
                            isEditing.value = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = {
                            isEditing.value = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .width(500.dp)
                .height(400.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFFFF),

            ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Id: ${simulation._id}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "User id: ${simulation.userId}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Simulation name: ${simulation.simulationName}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Accident id: ${simulation.accidentId}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Type of services: ${simulation.typeOfServices}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Best station Id: ${simulation.bestStationId}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Best path Id: ${simulation.bestPathId}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Response time: ${simulation.responseTime}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Created: ${simulation.created}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Address from: ${simulation.locationFrom}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Address to: ${simulation.locationTo}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                ) {
                    Button(
                        onClick = {
                            isEditing.value = true
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Edit")
                    }
                    Button(
                        onClick = {
                            onDelete(simulation)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }

}


@Composable
fun ViewSimulation() {
    val simulationState = remember { mutableStateOf(listOf<Simulation>()) }
    val loadingState = remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        simulationState.value = runBlocking {
            try {
                val url = "${BACKEND_URL}/api/simulation/all"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    (0 until jsonArray.length()).map { i ->
                        val obj = jsonArray.getJSONObject(i)
                        val userIdObj = obj.getJSONObject("userId")
                        val accidentIdObj = obj.getJSONObject("accidentId")
                        val bestStationIdObj = obj.getJSONObject("bestStationId")
                        val bestPathIdObj = obj.getJSONObject("bestPathId")
                        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        val createdDate = dateFormat.parse(obj.getString("created"))

                        Simulation(
                            _id = ObjectId(obj.getString("_id")),
                            userId = ObjectId(userIdObj.getString("_id")),
                            simulationName = obj.getString("simulationName"),
                            accidentId = ObjectId(accidentIdObj.getString("_id")),
                            typeOfServices = obj.getString("typeOfServices"),
                            bestStationId = ObjectId(bestStationIdObj.getString("_id")),
                            bestPathId = ObjectId(bestPathIdObj.getString("_id")),
                            responseTime = obj.getInt("responseTime"),
                            created = createdDate,
                            locationFrom = obj.getString("locationFrom"),
                            locationTo = obj.getString("locationTo")
                        )
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                println("Error while fetching simulations: ${e.message}")
                emptyList()
            } finally {
                loadingState.value = false
            }
        }
    }
    if (loadingState.value) {
        Modal("Loading simulations...")
    } else if (simulationState.value.isEmpty()) {
        Modal("No simulations found \nPlease generate some simulations first.")
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 300.dp)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 450.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(simulationState.value) { simulation ->
                    SimulationCard(simulation = simulation, onDelete = { deletedSimulation ->
                        runBlocking {
                            try {
                                val simulationId = deletedSimulation._id
                                val url = "$BACKEND_URL/api/simulation/delete/${simulationId}"

                                val client = OkHttpClient()
                                val request = Request.Builder()
                                    .url(url)
                                    .delete()
                                    .build()

                                if (client.newCall(request).execute().use { res -> res.isSuccessful }) {
                                    println("Simulation with ID ${deletedSimulation._id} deleted successfully.")
                                    simulationState.value =
                                        simulationState.value.filter { it._id != deletedSimulation._id }
                                } else {
                                    println("No simulation found with ID ${deletedSimulation._id}.")

                                }
                            } catch (e: Exception) {
                                println("Error while deleting simulation: ${e.message}")
                            }
                        }
                    }, onSave = { editedSimulation ->
                        runBlocking {
                            try {
                                val simulationId = editedSimulation._id
                                val url = "$BACKEND_URL/api/simulation/update/${simulationId}"
                                val client = OkHttpClient()
                                val json = JSONObject()
                                    .put("userId", editedSimulation.userId.toString())
                                    .put("accidentId", editedSimulation.accidentId.toString())
                                    .put("typeOfServices", editedSimulation.typeOfServices)
                                    .put("bestStationId", editedSimulation.bestStationId.toString())
                                    .put("bestPathId", editedSimulation.bestPathId.toString())
                                    .put("responseTime", editedSimulation.responseTime)
                                    .toString()
                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder()
                                    .url(url)
                                    .put(body)
                                    .build()
                                val response = client.newCall(request).execute()
                                if(response.isSuccessful){
                                    println("Simulation with ID ${editedSimulation._id} updated successfully.")
                                    simulationState.value = simulationState.value.map {
                                        if (it._id == editedSimulation._id) editedSimulation else it
                                    }
                                } else {
                                    println("Failed to update simulation with ID ${editedSimulation._id}.")
                                }
                            } catch (e: Exception) {
                                println("Error while updating simulation: ${e.message}")
                            }
                        }

                    })
                }

            }
        }

    }
}
