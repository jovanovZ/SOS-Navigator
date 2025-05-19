package viewTables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

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
    val locationPoints: List<ObjectId>
)


@Composable
fun PathCard(path: PathData, onDelete: (PathData) -> Unit, onSave: (PathData) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val accidentIdInput = remember { mutableStateOf(path.accidentId) }
    val locationPointsInput = remember { mutableStateOf(path.locationPoints) }
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
                Text("Id: ${path._id}")
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = accidentIdInput.value.toString(),
                    onValueChange = { accidentIdInput.value = ObjectId(it) },
                    label = "Accident id"
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = locationPointsInput.value.joinToString(", "),
                    onValueChange = { input ->
                        val newPoints = input.split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .map {
                                if (ObjectId.isValid(it)) {
                                    ObjectId(it)
                                } else {
                                    println("Invalid ObjectId: $it")
                                    return@map ObjectId()
                                }
                            }
                        if (newPoints.all { it.toHexString().length == 24 }) {
                            locationPointsInput.value = newPoints
                        } else {
                            println("All ObjectIds must be 24-character hex strings")
                        }
                    },
                    label = "Location points (comma separated)"
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if (accidentIdInput.value.toString().isEmpty() || locationPointsInput.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            if (!ObjectId.isValid(accidentIdInput.value.toString())) {
                                println("Invalid ObjectId format")
                                return@Button
                            }
                            if (locationPointsInput.value.any { !ObjectId.isValid(it.toString()) }) {
                                println("Invalid ObjectId format in location points")
                                return@Button
                            }
                            val updatedPath = path.copy(
                                _id = path._id,
                                accidentId = accidentIdInput.value,
                                locationPoints = locationPointsInput.value.map { ObjectId(it.toString()) }
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
                Text("Id: ${path._id}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Accident id: ${path.accidentId}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Location points:")
                Text(path.locationPoints.joinToString(", "))
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
                        locationPoints = doc.getList("locationPoints", ObjectId::class.java)
                    )
                }
            } catch (e: Exception) {
                println("Error while fetching locations: ${e.message}")
                emptyList()
            }
        }
    }

    if (pathState.value.isEmpty()) {
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
                                        "locationPoints", editedPath.locationPoints
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