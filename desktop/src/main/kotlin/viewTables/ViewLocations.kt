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
import org.bson.Document
import org.bson.types.ObjectId


data class Location(
    val id: ObjectId,
    val geometry: Geometry
)

data class Geometry(
    val type: String = "Point",
    val coordinates: List<Double>
)


@Composable
internal fun LocationCard(location: Location) {
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
            Text("Id: ${location.id}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Longitude: ${location.geometry.coordinates[0]}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Latitude: ${location.geometry.coordinates[1]}")
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}


@Composable
fun ViewLocation() {
    val locations = runBlocking {
        try {
            val db = DataBase.getDatabase()
            val collection = db.getCollection("locations", Document::class.java)
            val documents = collection.find().asFlow().toList()

            documents.map { doc ->
                val geometry = doc.get("geometry", Document::class.java)
                Location(
                    id = doc.getObjectId("_id"),
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
    if (locations.isEmpty()) {
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
                items(locations) { location ->
                    LocationCard(location)
                }
            }

        }
    }

}