package viewTables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// za teste dokler ni povezave na bazo
internal data class Simulation(
    val userId: String,
    val accidentId: String,
    val typeOfServices: List<String>,
    val bestStationId: String,
    val bestPathId: String,
    val responseTime: Double
)

internal val simulations = listOf(
    Simulation(
        userId = "64b7f3c2e4b0f5a1d2c3e4f1",
        accidentId = "64b7f3c2e4b0f5a1d2c3e4f2",
        typeOfServices = listOf("Policijska", "Bolnica"),
        bestStationId = "64b7f3c2e4b0f5a1d2c3e4f3",
        bestPathId = "64b7f3c2e4b0f5a1d2c3e4f4",
        responseTime = 1200.213
    ),
    Simulation(
        userId = "64b7f3c2e4b0f5a1d2c3e4f5",
        accidentId = "64b7f3c2e4b0f5a1d2c3e4f6",
        typeOfServices = listOf("Policijska", "Bolnica, Gasilci"),
        bestStationId = "64b7f3c2e4b0f5a1d2c3e4f7",
        bestPathId = "64b7f3c2e4b0f5a1d2c3e4f8",
        responseTime = 1500.3123
    ),
    Simulation(
        userId = "64b7f3c2e4b0f5a1d2c3e4f9",
        accidentId = "64b7f3c2e4b0f5a1d2c3e4fa",
        typeOfServices = listOf("Policijska"),
        bestStationId = "64b7f3c2e4b0f5a1d2c3e4fb",
        bestPathId = "64b7f3c2e4b0f5a1d2c3e4fc",
        responseTime = 1800.01023
    )
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

            Text("User id: ${simulation.userId}" )
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