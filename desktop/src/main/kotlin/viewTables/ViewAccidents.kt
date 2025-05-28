package viewTables

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
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.DataBase
import inputs.InputFieldForText
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject
import BACKEND_URL

data class Accident(
    val _id: ObjectId = ObjectId(),
    var locationId: ObjectId,
    var typeOfAccident: String
)

@Composable
fun AccidentCard(accident: Accident, onDelete: (Accident) -> Unit, onSave: (Accident) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val locationIdInput = remember { mutableStateOf(accident.locationId.toString()) }
    val typeOfAccidentInput = remember { mutableStateOf(accident.typeOfAccident) }

    Surface(
        modifier = Modifier
            .padding(24.dp)
            .width(400.dp)
            .height(220.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFFFFF),

        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (isEditing.value) {
                Text("Id: ${accident._id}")
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    label = "Location Id",
                    value = locationIdInput.value,
                    onValueChange = { locationIdInput.value = it },
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    label = "Type of accident",
                    value = typeOfAccidentInput.value,
                    onValueChange = { typeOfAccidentInput.value = it },
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if (locationIdInput.value.isEmpty() || typeOfAccidentInput.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            if (!ObjectId.isValid(locationIdInput.value)) {
                                println("Invalid ObjectId format")
                                return@Button
                            }
                            val updatedAccident = accident.copy(
                                _id = accident._id,
                                locationId = ObjectId(locationIdInput.value),
                                typeOfAccident = typeOfAccidentInput.value
                            )
                            onSave(updatedAccident)
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

            } else {
                Text(
                    "Id: ${accident._id}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Location Id: ${accident.locationId}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Type of accident: ${accident.typeOfAccident}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp)
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
                            onDelete(accident)
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
fun ViewAccidents() {
    val accidentsState = remember { mutableStateOf(listOf<Accident>()) }
    val loadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        accidentsState.value = runBlocking {
            try {
                val url = "$BACKEND_URL/api/accident/all"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    (0 until jsonArray.length()).map { i ->
                        val obj = jsonArray.getJSONObject(i)
                        val locationIdObj = obj.getJSONObject("locationId")
                        Accident(
                            _id = ObjectId(obj.getString("_id")),
                            locationId = ObjectId(locationIdObj.getString("_id")),
                            typeOfAccident = obj.getString("typeOfAccident")
                        )
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                println("Error while fetching accidents: ${e.message}")
                emptyList()
            } finally {
                loadingState.value = false
            }
        }
    }
    if (loadingState.value) {
        Modal("Loading accidents...")
    } else if (accidentsState.value.isEmpty()) {
        Modal("No accidents found \nPlease generate some accidents first.")
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 300.dp)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 550.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(accidentsState.value) { accident ->
                    AccidentCard(accident = accident, onDelete = { deletedAccident ->
                        runBlocking {
                            try {
                                val accidentId = deletedAccident._id
                                val url = "$BACKEND_URL/api/accident/delete/${accidentId}"

                                val client = OkHttpClient()
                                val request = Request.Builder()
                                    .url(url)
                                    .delete()
                                    .build()

                                if(client.newCall(request).execute().use { res -> res.isSuccessful }){
                                    println("Accident with ID ${deletedAccident._id} deleted successfully.")
                                    accidentsState.value = accidentsState.value.filter { it._id != deletedAccident._id }
                                } else {
                                    println("No accident found with ID ${deletedAccident._id}.")

                                }

                            } catch (e: Exception) {
                                println("Error while deleting accident: ${e.message}")
                            }
                        }
                    }, onSave = { editedAccident ->
                        runBlocking {
                            try {
                                val accidentId = editedAccident._id
                                val url = "$BACKEND_URL/api/accident/update/${accidentId}"
                                val client = OkHttpClient()
                                val json = JSONObject()
                                    .put("locationId", editedAccident.locationId.toString())
                                    .put("typeOfAccident", editedAccident.typeOfAccident)
                                    .toString()
                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder()
                                    .url(url)
                                    .put(body)
                                    .build()
                                val response = client.newCall(request).execute()
                                if(response.isSuccessful) {
                                    println("Accident with ID ${editedAccident._id} updated successfully.")
                                    accidentsState.value = accidentsState.value.map {
                                        if (it._id == editedAccident._id) editedAccident else it
                                    }
                                } else {
                                    println("No accident found with ID ${editedAccident._id}.")

                                }
                            } catch (e: Exception) {
                                println("Error while updating accident: ${e.message}")
                            }
                        }

                    })
                }
            }
        }
    }
}