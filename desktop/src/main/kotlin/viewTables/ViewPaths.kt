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
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId

// za teste dokler ni povezave na bazo
internal data class PathData(
    val id: ObjectId ,
    val accidentId: ObjectId,
    val locationPoints: List<ObjectId>
)



@Composable
internal fun PathCard(path: PathData) {
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
            Text("Id: ${path.id}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Accident id: ${path.accidentId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Location points: ${path.locationPoints.joinToString(", ")}")
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}


@Composable
fun ViewPaths() {
    val paths = runBlocking {
        try{
            val db = DataBase.getDatabase()
            val collection = db.getCollection("paths", Document::class.java)
            val documents = collection.find().asFlow().toList()
            documents.map { doc ->
                PathData(
                    id = doc.getObjectId("_id"),
                    accidentId = doc.getObjectId("accidentId"),
                    locationPoints = doc.getList("locationPoints", ObjectId::class.java)
                )
            }
        }catch (e: Exception){
            println("Error while fetching paths: ${e.message}")
            emptyList()
        }
    }
    if(paths.isEmpty()){
        Modal("No paths found \nPlease generate some paths first.")
    }
    else{
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
                items(paths) { path ->
                    PathCard(path)
                }
            }

        }
    }


}