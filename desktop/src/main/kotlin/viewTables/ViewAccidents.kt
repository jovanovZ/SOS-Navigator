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

internal data class Accident(
    val id : ObjectId,
    val locationId: ObjectId,
    val typeOfAccident: String
)

@Composable
internal fun AccidentCard(accident: Accident) {
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
            Text("Id: ${accident.id}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Location Id: ${accident.locationId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Type of accident: ${accident.typeOfAccident}")
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}


@Composable
fun ViewAccidents() {
    val accidents = runBlocking {
        try {
            val db = DataBase.getDatabase()
            val collection = db.getCollection("accidents", Document::class.java)

            val documents = collection.find().asFlow().toList()
            documents.map { doc ->
                Accident(
                    id = doc.getObjectId("_id"),
                    locationId = doc.getObjectId("locationId"),
                    typeOfAccident = doc.getString("typeOfAccident")
                )
            }
        } catch (e: Exception) {
            println("Error while fetching accidents: ${e.message}")
            emptyList()
        }
    }

    if (accidents.isEmpty()) {
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
                items(accidents) { accident ->
                    AccidentCard(accident)
                }
            }
        }
    }
}