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


data class Simulation(
    val _id: ObjectId = ObjectId(),
    val userId: ObjectId,
    val accidentId: ObjectId,
    val typeOfServices: List<String>,
    val bestStationId: ObjectId,
    val bestPathId: ObjectId,
    val responseTime: Double
)


@Composable
fun SimulationCard(simulation: Simulation, onDelete: (Simulation) -> Unit, onSave: (Simulation) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val userIdInput = remember { mutableStateOf(simulation.userId) }
    val accidentIdInput = remember { mutableStateOf(simulation.accidentId) }
    val typeOfServicesInput = remember { mutableStateOf(simulation.typeOfServices) }
    val bestStationIdInput = remember { mutableStateOf(simulation.bestStationId) }
    val bestPathIdInput = remember { mutableStateOf(simulation.bestPathId) }
    val responseTimeInput = remember { mutableStateOf(simulation.responseTime) }


if (isEditing.value ) {
    Surface(
        modifier = Modifier
            .padding(24.dp)
            .width(500.dp)
            .height(320.dp)
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
                value = userIdInput.value.toString(),
                onValueChange = { userIdInput.value = ObjectId(it) },
                label = "User id"
            )
            Spacer(modifier = Modifier.height(4.dp))
            InputFieldForText(
                value = accidentIdInput.value.toString(),
                onValueChange = { accidentIdInput.value = ObjectId(it) },
                label = "Accident id"
            )
            Spacer(modifier = Modifier.height(4.dp))
            InputFieldForText(
                value = typeOfServicesInput.value.joinToString(", "),
                onValueChange = { typeOfServicesInput.value = it.split(",").map { it.trim() } },
                label = "Type of services"
            )
            Spacer(modifier = Modifier.height(4.dp))
            InputFieldForText(
                value = bestStationIdInput.value.toString(),
                onValueChange = { bestStationIdInput.value = ObjectId(it) },
                label = "Best station id"
            )
            Spacer(modifier = Modifier.height(4.dp))
            InputFieldForText(
                value = bestPathIdInput.value.toString(),
                onValueChange = { bestPathIdInput.value = ObjectId(it) },
                label = "Best path id"
            )
            Spacer(modifier = Modifier.height(4.dp))
            InputFieldForNumber(
                value = responseTimeInput.value.toString(),
                onValueChange = { responseTimeInput.value = it.toDouble() },
                label = "Response time"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        if (userIdInput.value.toString().isEmpty() || accidentIdInput.value.toString().isEmpty() ||
                            typeOfServicesInput.value.isEmpty() || bestStationIdInput.value.toString().isEmpty() ||
                            bestPathIdInput.value.toString().isEmpty() || responseTimeInput.value.toString()
                                .isEmpty()
                        ) {
                            println("Please fill all fields")
                            return@Button
                        }
                        if (!ObjectId.isValid(userIdInput.value.toString()) ||
                            !ObjectId.isValid(accidentIdInput.value.toString()) ||
                            !ObjectId.isValid(bestStationIdInput.value.toString()) ||
                            !ObjectId.isValid(bestPathIdInput.value.toString())
                        ) {
                            println("Invalid ObjectId format")
                            return@Button
                        }
                        if (responseTimeInput.value <= 0) {
                            println("Response time must be greater than 0")
                            return@Button
                        }

                        val updatedSimulation = simulation.copy(
                            userId = userIdInput.value,
                            accidentId = accidentIdInput.value,
                            typeOfServices = typeOfServicesInput.value,
                            bestStationId = bestStationIdInput.value,
                            bestPathId = bestPathIdInput.value,
                            responseTime = responseTimeInput.value
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
            .height(320.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFFFFF),

        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Id: ${simulation._id}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("User id: ${simulation.userId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Accident id: ${simulation.accidentId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Type of services: ${simulation.typeOfServices.joinToString(", ")}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Best station Id: ${simulation.bestStationId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Best path Id: ${simulation.bestPathId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Response time: ${simulation.responseTime}")
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

    LaunchedEffect(Unit) {
        simulationState.value = runBlocking {
            try {
                val db = DataBase.getDatabase()
                val collection = db.getCollection("simulations")
                val documents = collection.find().asFlow().toList()
                documents.map { doc ->
                    Simulation(
                        _id = doc.getObjectId("_id"),
                        userId = doc.getObjectId("userId"),
                        accidentId = doc.getObjectId("accidentId"),
                        typeOfServices = doc.getList("typeOfServices", String::class.java),
                        bestStationId = doc.getObjectId("bestStationId"),
                        bestPathId = doc.getObjectId("bestPathId"),
                        responseTime = doc.getDouble("responseTime")
                    )
                }
            } catch (e: Exception) {
                println("Error while fetching simulations: ${e.message}")
                emptyList()
            }
        }
    }

    if (simulationState.value.isEmpty()) {
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
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("simulations", Document::class.java)
                                val filter = Document("_id", deletedSimulation._id)

                                val result = collection.deleteOne(filter).asFlow().toList()
                                if (result.isNotEmpty() && result[0].deletedCount > 0) {
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
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("simulations", Document::class.java)
                                val filter = Document("_id", editedSimulation._id)
                                val update = Document(
                                    "\$set", Document(
                                        "userId", editedSimulation.userId
                                    ).append(
                                        "accidentId", editedSimulation.accidentId
                                    ).append("typeOfServices", editedSimulation.typeOfServices)
                                        .append("bestStationId", editedSimulation.bestStationId)
                                        .append("bestPathId", editedSimulation.bestPathId)
                                        .append("responseTime", editedSimulation.responseTime)

                                )

                                val result = collection.updateOne(filter, update).asFlow().toList()
                                if (result.isNotEmpty() && result[0].modifiedCount > 0) {
                                    println("Simulation with ID ${editedSimulation._id} updated successfully.")
                                    simulationState.value = simulationState.value.map {
                                        if (it._id == editedSimulation._id) editedSimulation else it
                                    }
                                } else {
                                    println("No simulation found with ID ${editedSimulation._id}.")
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
