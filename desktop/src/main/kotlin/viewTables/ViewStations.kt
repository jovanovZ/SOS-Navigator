package viewTables

import InputFieldForBoolean
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

internal data class Station(
    val _id: ObjectId = ObjectId(),
    val locationId: ObjectId,
    val typeOfStation: String,
    val isPermanent: Boolean,
    val region: String
)


@Composable
internal fun StationCard(station: Station, onDelete: (Station) -> Unit, onSave: (Station) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val locationIdInput = remember { mutableStateOf(station.locationId.toString()) }
    val typeOfStationInput = remember { mutableStateOf(station.typeOfStation) }
    val isPermanentInput = remember { mutableStateOf(station.isPermanent) }
    val regionInput = remember { mutableStateOf(station.region) }

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
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Id: ${station._id}")
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = locationIdInput.value,
                    onValueChange = { locationIdInput.value = it },
                    label = "Location id",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = typeOfStationInput.value,
                    onValueChange = { typeOfStationInput.value = it },
                    label = "Type of station",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForBoolean(
                    value = isPermanentInput.value,
                    onValueChange = { isPermanentInput.value = it },
                    label = "Is permanent",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = regionInput.value,
                    onValueChange = { regionInput.value = it },
                    label = "Region",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if (locationIdInput.value
                                    .isEmpty() || typeOfStationInput.value.isEmpty() || regionInput.value.isEmpty()
                            ) {
                                println("Please fill all fields")
                                return@Button
                            }
                            if (!ObjectId.isValid(locationIdInput.value)) {
                                println("Invalid ObjectId")
                                return@Button
                            }
                            val updatedStation = station.copy(
                                _id = station._id,
                                locationId = ObjectId(locationIdInput.value),
                                typeOfStation = typeOfStationInput.value,
                                isPermanent = isPermanentInput.value,
                            )
                            onSave(updatedStation)
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
                .height(270.dp)
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
                    "Id: ${station._id}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Location id: ${station.locationId}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Type of station: ${station.typeOfStation}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Is permanent: ${station.isPermanent}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Region: ${station.region}",
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
                            onDelete(station)
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
fun ViewStations() {
    val stationState = remember { mutableStateOf(listOf<Station>()) }
    val loadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        stationState.value = runBlocking {
            try {
                //api klic za getall
                val db = DataBase.getDatabase()
                val collection = db.getCollection("stations")
                val stations = collection.find().asFlow().toList()
                stations.map { doc ->
                    Station(
                        _id = doc.getObjectId("_id"),
                        locationId = doc.getObjectId("locationId"),
                        typeOfStation = doc.getString("typeOfStation"),
                        isPermanent = doc.getBoolean("isPermanent"),
                        region = doc.getString("region")
                    )
                }
            } catch (e: Exception) {
                println("Error while fetching stations: ${e.message}")
                emptyList()
            } finally {
                loadingState.value = false
            }
        }
    }
    if (loadingState.value) {
        Modal("Loading stations...")

    } else if (stationState.value.isEmpty()) {
        Modal("No stations found \nPlease generate some stations first.")
    } else {

        rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 300.dp)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 350.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(stationState.value) { station ->
                    StationCard(station = station, onDelete = { deletedStation ->
                        runBlocking {
                            try {
                                //api klic za delete
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("stations", Document::class.java)
                                val filter = Document("_id", deletedStation._id)

                                val result = collection.deleteOne(filter).asFlow().toList()
                                if (result.isNotEmpty() && result[0].deletedCount > 0) {
                                    println("Station with ID ${deletedStation._id} deleted successfully.")
                                    stationState.value =
                                        stationState.value.filter { it._id != deletedStation._id }
                                } else {
                                    println("No station found with ID ${deletedStation._id}.")
                                }
                            } catch (e: Exception) {
                                println("Error while deleting station: ${e.message}")
                            }
                        }
                    }, onSave = { editedStation ->
                        runBlocking {
                            try {
                                //api klic za update
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("stations", Document::class.java)
                                val filter = Document("_id", editedStation._id)
                                val update = Document(
                                    "\$set", Document(
                                        "locationId", editedStation.locationId
                                    ).append(
                                        "typeOfStation", editedStation.typeOfStation
                                    ).append(
                                        "isPermanent", editedStation.isPermanent
                                    ).append(
                                        "region", editedStation.region
                                    )

                                )

                                val result = collection.updateOne(filter, update).asFlow().toList()
                                if (result.isNotEmpty() && result[0].modifiedCount > 0) {
                                    println("Station with ID ${editedStation._id} updated successfully.")
                                    stationState.value = stationState.value.map {
                                        if (it._id == editedStation._id) editedStation else it
                                    }
                                } else {
                                    println("No station found with ID ${editedStation._id}.")
                                }
                            } catch (e: Exception) {
                                println("Error while updating station: ${e.message}")
                            }
                        }

                    })
                }
            }

        }
    }
}