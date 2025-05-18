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
import db.DataBase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId

// za teste dokler ni povezave na bazo
internal data class Station(
    val id: ObjectId,
    val locationId: ObjectId,
    val typeOfStation: String,
    val isPermanent: Boolean,
    val region: String
)


@Composable
internal fun StationCard(station: Station) {
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
            Text("Id: ${station.id}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Location id: ${station.locationId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Type of station: ${station.typeOfStation}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Is permanent: ${station.isPermanent}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Region: ${station.region}")
        }
    }
}


@Composable
fun ViewStations() {
    val stations = runBlocking {
        try {
            val db = DataBase.getDatabase()
            val collection = db.getCollection("stations")
            val stations = collection.find().asFlow().toList()
            stations.map { doc ->
                Station(
                    id = doc.getObjectId("_id"),
                    locationId = doc.getObjectId("locationId"),
                    typeOfStation = doc.getString("typeOfStation"),
                    isPermanent = doc.getBoolean("isPermanent"),
                    region = doc.getString("region")
                )
            }
        } catch (e: Exception) {
            println("Error while fetching stations: ${e.message}")
            emptyList()
        }
    }
    if (stations.isEmpty()) {
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
                items(stations) { station ->
                    StationCard(station)
                }
            }

        }
    }
}