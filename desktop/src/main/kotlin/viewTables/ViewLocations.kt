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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.DataBase
import inputs.InputFieldForNumber
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONArray
import org.json.JSONObject


data class Location(
    val _id: ObjectId = ObjectId(),
    val geometry: Geometry
)

data class Geometry(
    val type: String = "Point",
    val coordinates: List<Double>
)


@Composable
fun LocationCard(location: Location, onDelete: (Location) -> Unit, onSave: (Location) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val longitude = remember { mutableStateOf(location.geometry.coordinates[0]) }
    val latitude = remember { mutableStateOf(location.geometry.coordinates[1]) }

    if (isEditing.value) {
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
                Text("Id: ${location._id}")
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForNumber(
                    label = "Longitude",
                    value = longitude.value.toString(),
                    onValueChange = { longitude.value = it.toDouble() },
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForNumber(
                    label = "Latitude",
                    value = latitude.value.toString(),
                    onValueChange = { latitude.value = it.toDouble() },
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if (latitude.value.toString().isEmpty() || longitude.value.toString().isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }

                            val updatedLocation = location.copy(
                                _id = location._id,
                                geometry = Geometry(
                                    type = location.geometry.type,
                                    coordinates = listOf(longitude.value, latitude.value)
                                )
                            )
                            onSave(updatedLocation)
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
                Text(
                    "Id: ${location._id}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Longitude: ${location.geometry.coordinates[0]}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Latitude: ${location.geometry.coordinates[1]}",
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
                            onDelete(location)
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
fun ViewLocation() {
    val locationState = remember { mutableStateOf(listOf<Location>()) }
    val loadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        locationState.value = runBlocking {
            try {
                val url = "$BACKEND_URL/api/location/all"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonObject = JSONObject(responseBody)
                    val jsonArray = jsonObject.getJSONArray("locations")
                    (0 until jsonArray.length()).map { i ->
                        val obj = jsonArray.getJSONObject(i)

                        Location(
                            _id = ObjectId(obj.getString("_id")),
                            geometry = Geometry(
                                type = obj.getJSONObject("geometry").getString("type"),
                                coordinates = obj.getJSONObject("geometry")
                                    .getJSONArray("coordinates")
                                    .let { coords ->
                                        (0 until coords.length()).map { coords.getDouble(it) }
                                    }
                            )
                        )
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                println("Error while fetching locations: ${e.message}")
                emptyList()
            } finally {
                loadingState.value = false
            }
        }
    }
    if (loadingState.value) {
        Modal("Loading locations...")
    } else if (locationState.value.isEmpty()) {
        Modal("No locations found \nPlease generate some locations first.")
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 300.dp)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(locationState.value) { location ->
                    LocationCard(location = location, onDelete = { deletedLocation ->
                        runBlocking {
                            try {
                                val locationId = deletedLocation._id
                                val url = "$BACKEND_URL/api/location/delete/${locationId}"

                                val client = OkHttpClient()
                                val request = Request.Builder()
                                    .url(url)
                                    .delete()
                                    .build()
                                val response = client.newCall(request).execute()
                                if (response.isSuccessful) {
                                    println("Location with ID ${deletedLocation._id} deleted successfully.")
                                    locationState.value = locationState.value.filter { it._id != deletedLocation._id }
                                } else {
                                    println("No location found with ID ${deletedLocation._id}.")

                                }
                            } catch (e: Exception) {
                                println("Error while deleting location: ${e.message}")
                            }
                        }
                    }, onSave = { editedLocation ->
                        runBlocking {
                            try {
                                val locationId = editedLocation._id
                                val url = "$BACKEND_URL/api/location/update/${locationId}"
                                val client = OkHttpClient()
                                val json = JSONObject()
                                    .put("long", editedLocation.geometry.coordinates[0])
                                    .put("lat", editedLocation.geometry.coordinates[1])
                                    .toString()
                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder()
                                    .url(url)
                                    .put(body)
                                    .build()

                                if (client.newCall(request).execute().use { res -> res.isSuccessful }) {
                                    println("Location with ID ${editedLocation._id} updated successfully.")
                                    locationState.value = locationState.value.map {
                                        if (it._id == editedLocation._id) editedLocation else it
                                    }
                                } else {
                                    println("No location found with ID ${editedLocation._id}.")

                                }
                            } catch (e: Exception) {
                                println("Error while updating location: ${e.message}")
                            }
                        }

                    })
                }

            }

        }
    }

}