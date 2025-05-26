package viewTables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import db.DataBase
import inputs.InputFieldForText
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId

data class PathData(
    val _id: ObjectId = ObjectId(),
    val accidentId: ObjectId,
    val locationPoints: List<LocationPoint>
)

data class LocationPoint(
    val lat: Double,
    val lng: Double
)

@Composable
fun PathCard(path: PathData, onDelete: (PathData) -> Unit, onSave: (PathData) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val accidentIdInput = remember { mutableStateOf(path.accidentId.toString()) }
    val locationPointsInput = remember { mutableStateOf(path.locationPoints) }

    if (isEditing.value) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .width(400.dp)
                .height(300.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFFFF)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Id: ${path._id}")
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = accidentIdInput.value,
                    onValueChange = { accidentIdInput.value = it },
                    label = "Accident id",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                locationPointsInput.value.forEachIndexed { index, point ->
                    InputFieldForText(
                        value = point.lat.toString(),
                        onValueChange = { newLat ->
                            locationPointsInput.value = locationPointsInput.value.toMutableList().apply {
                                this[index] = this[index].copy(lat = newLat.toDoubleOrNull() ?: 0.0)
                            }
                        },
                        label = "Latitude ${index + 1}",
                        inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    InputFieldForText(
                        value = point.lng.toString(),
                        onValueChange = { newLng ->
                            locationPointsInput.value = locationPointsInput.value.toMutableList().apply {
                                this[index] = this[index].copy(lng = newLng.toDoubleOrNull() ?: 0.0)
                            }
                        },
                        label = "Longitude ${index + 1}",
                        inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if (accidentIdInput.value.isEmpty() || locationPointsInput.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            val updatedPath = path.copy(
                                accidentId = ObjectId(accidentIdInput.value),
                                locationPoints = locationPointsInput.value
                            )
                            onSave(updatedPath)
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
                .height(300.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFFFF)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Id: ${path._id}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Accident id: ${path.accidentId}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Location points:",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                path.locationPoints.forEach { point ->
                    Text(
                        "Lat: ${point.lat}, Lng: ${point.lng}",
                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                    )
                }
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
                            onDelete(path)
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
fun ViewPaths() {
    val pathState = remember { mutableStateOf(listOf<PathData>()) }
    val loadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        pathState.value = runBlocking {
            try {
                val db = DataBase.getDatabase()
                val collection = db.getCollection("paths", Document::class.java)

                val documents = collection.find().asFlow().toList()
                documents.map { doc ->
                    PathData(
                        _id = doc.getObjectId("_id"),
                        accidentId = doc.getObjectId("accidentId"),
                        locationPoints = doc.getList("locationPoints", Document::class.java).map { point ->
                            LocationPoint(
                                lat = point.getDouble("lat"),
                                lng = point.getDouble("lng")
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                println("Error while fetching paths: ${e.message}")
                emptyList()
            } finally {
                loadingState.value = false
            }
        }
    }
    if (loadingState.value) {
        Modal("Loading paths...")
    } else if (pathState.value.isEmpty()) {
        Modal("No paths found \nPlease generate some paths first.")
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
                items(pathState.value) { path ->
                    PathCard(path = path, onDelete = { deletedPath ->
                        runBlocking {
                            try {
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("paths", Document::class.java)
                                val filter = Document("_id", deletedPath._id)

                                val result = collection.deleteOne(filter).asFlow().toList()
                                if (result.isNotEmpty() && result[0].deletedCount > 0) {
                                    println("Path with ID ${deletedPath._id} deleted successfully.")
                                    pathState.value = pathState.value.filter { it._id != deletedPath._id }
                                } else {
                                    println("No path found with ID ${deletedPath._id}.")
                                }
                            } catch (e: Exception) {
                                println("Error while deleting path: ${e.message}")
                            }
                        }
                    }, onSave = { editedPath ->
                        runBlocking {
                            try {
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("paths", Document::class.java)
                                val filter = Document("_id", editedPath._id)
                                val update = Document(
                                    "\$set", Document(
                                        "accidentId", editedPath.accidentId
                                    ).append(
                                        "locationPoints", editedPath.locationPoints.map { point ->
                                            Document("lat", point.lat).append("lng", point.lng)
                                        }
                                    )
                                )

                                val result = collection.updateOne(filter, update).asFlow().toList()
                                if (result.isNotEmpty() && result[0].modifiedCount > 0) {
                                    println("Path with ID ${editedPath._id} updated successfully.")
                                    pathState.value = pathState.value.map {
                                        if (it._id == editedPath._id) editedPath else it
                                    }
                                } else {
                                    println("No path found with ID ${editedPath._id}.")
                                }
                            } catch (e: Exception) {
                                println("Error while updating path: ${e.message}")
                            }
                        }

                    })
                }

            }

        }
    }


}