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
internal data class Station(
    val locationId: String,
    val typeOfStation: String,
    val isPermanent: Boolean,
    val region: String
)

internal val stations = listOf(
    Station("64b7f3c2e4b0f5a1d2c3e4f1", "Policijska", true, "Osrednjeslovenska"),
    Station("64b7f3c2e4b0f5a1d2c3e4f1", "Bolnica", false, "Gorenjska"),
    Station("64b7f3c2e4b0f5a1d2c3e4f1", "Gasilci", true, "Podravska"),
    Station("64b7f3c2e4b0f5a1d2c3e4f1", "Policijska", false, "Pomurska"),
    Station("64b7f3c2e4b0f5a1d2c3e4f1", "Bolnica", true, "Obalno-kraška")
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

            Text("Location id: ${station.locationId}" )
            Spacer(modifier = Modifier.height(4.dp))
            Text(station.typeOfStation)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Is permanent: ${station.isPermanent}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Region: ${station.region}")
        }
    }
}


@Composable
fun ViewStations() {
    val scrollState = rememberScrollState()
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