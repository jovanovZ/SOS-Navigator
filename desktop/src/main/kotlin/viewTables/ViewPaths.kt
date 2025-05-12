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
internal data class Path(
    val accidentId: String,
    val locationPoints: List<String>
)

internal val paths = listOf(
    Path(
        accidentId = "64b7f3c2e4b0f5a1d2c3e4f1",
        locationPoints = listOf("LocationID1", "LocationID1", "LocationID1")
    ),
    Path(
        accidentId = "64b7f3c2e4b0f5a1d2c3e4f2",
        locationPoints = listOf("LocationID1", "LocationID1", "LocationID1")
    ),
    Path(
        accidentId = "64b7f3c2e4b0f5a1d2c3e4f3",
        locationPoints = listOf("LocationID1", "LocationID1", "LocationID1")
    )
)


@Composable
internal fun PathCard(path: Path) {
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

            Text("Accident id: ${path.accidentId}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Location points: ${path.locationPoints.joinToString(", ")}")
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}


@Composable
fun ViewPaths() {
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