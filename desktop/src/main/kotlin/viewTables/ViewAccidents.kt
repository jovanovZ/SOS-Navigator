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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.DataBase
import inputs.InputFieldForText
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId

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
                    onValueChange = { locationIdInput.value = it })
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    label = "Type of accident",
                    value = typeOfAccidentInput.value,
                    onValueChange = { typeOfAccidentInput.value = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if(locationIdInput.value.isEmpty() || typeOfAccidentInput.value.isEmpty()) {
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
                Text("Id: ${accident._id}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Location Id: ${accident.locationId}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Type of accident: ${accident.typeOfAccident}")
                Spacer(modifier = Modifier.height(12.dp))
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


    LaunchedEffect(Unit) {
        accidentsState.value = runBlocking {
            try {
                val db = DataBase.getDatabase()
                val collection = db.getCollection("accidents", Document::class.java)

                val documents = collection.find().asFlow().toList()
                documents.map { doc ->
                    Accident(
                        _id = doc.getObjectId("_id"),
                        locationId = doc.getObjectId("locationId"),
                        typeOfAccident = doc.getString("typeOfAccident")
                    )
                }
            } catch (e: Exception) {
                println("Error while fetching accidents: ${e.message}")
                emptyList()
            }
        }
    }

    if (accidentsState.value.isEmpty()) {
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
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("accidents", Document::class.java)
                                val filter = Document("_id", deletedAccident._id)

                                val result = collection.deleteOne(filter).asFlow().toList()
                                if (result.isNotEmpty() && result[0].deletedCount > 0) {
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
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("accidents", Document::class.java)
                                val filter = Document("_id", editedAccident._id)
                                val update = Document(
                                    "\$set", Document("locationId", editedAccident.locationId)
                                        .append("typeOfAccident", editedAccident.typeOfAccident)
                                )

                                val result = collection.updateOne(filter, update).asFlow().toList()
                                if (result.isNotEmpty() && result[0].modifiedCount > 0) {
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