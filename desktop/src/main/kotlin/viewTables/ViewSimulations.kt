package viewTables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import db.DataBase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.bson.types.ObjectId
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.bson.Document


// za teste dokler ni povezave na bazo
internal data class Simulation(
    val id: ObjectId,
    val userId: ObjectId,
    val accidentId: ObjectId,
    val typeOfServices: List<String>,
    val bestStationId: ObjectId,
    val bestPathId: ObjectId,
    val responseTime: Double
)


@Composable
internal fun SimulationCard(simulation: Simulation) {
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
            Text("Id: ${simulation.id}")
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
        }
    }
}


@Composable
fun ViewSimulation() {
    val simulations = runBlocking {
        try {
            val db = DataBase.getDatabase()
            val collection = db.getCollection("simulations")
            val documents = collection.find().asFlow().toList()
            documents.map { doc ->
                Simulation(
                    id = doc.getObjectId("_id"),
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
    if (simulations.isEmpty()) {
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
                columns = GridCells.Adaptive(minSize = 350.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(simulations) { simulation ->
                    SimulationCard(simulation)
                }
            }

        }
    }
}