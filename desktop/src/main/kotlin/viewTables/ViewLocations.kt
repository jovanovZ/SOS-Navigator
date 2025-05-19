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
import org.bson.Document
import org.bson.types.ObjectId


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
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForNumber(
                    label = "Latitude",
                    value = latitude.value.toString(),
                    onValueChange = { latitude.value = it.toDouble() },
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
                Text("Id: ${location._id}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Longitude: ${location.geometry.coordinates[0]}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Latitude: ${location.geometry.coordinates[1]}")
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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


    LaunchedEffect(Unit) {
        locationState.value = runBlocking {
            try {
                val db = DataBase.getDatabase()
                val collection = db.getCollection("locations", Document::class.java)

                val documents = collection.find().asFlow().toList()
                documents.map { doc ->
                    val geometry = doc.get("geometry", Document::class.java)
                    Location(
                        _id = doc.getObjectId("_id"),
                        geometry = Geometry(
                            type = geometry.getString("type"),
                            coordinates = geometry.getList("coordinates", Number::class.java)
                                .map { it.toDouble() }
                        )
                    )
                }
            } catch (e: Exception) {
                println("Error while fetching locations: ${e.message}")
                emptyList()
            }
        }
    }

    if (locationState.value.isEmpty()) {
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
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("locations", Document::class.java)
                                val filter = Document("_id", deletedLocation._id)

                                val result = collection.deleteOne(filter).asFlow().toList()
                                if (result.isNotEmpty() && result[0].deletedCount > 0) {
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
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("locations", Document::class.java)
                                val filter = Document("_id", editedLocation._id)
                                val update = Document(
                                    "\$set", Document(
                                        "geometry", Document("type", editedLocation.geometry.type)
                                            .append("coordinates", editedLocation.geometry.coordinates)
                                    )
                                )

                                val result = collection.updateOne(filter, update).asFlow().toList()
                                if (result.isNotEmpty() && result[0].modifiedCount > 0) {
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