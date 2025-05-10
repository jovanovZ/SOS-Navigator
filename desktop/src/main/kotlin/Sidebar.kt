import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SidebarWithDropdown() {
    var expandedTable by remember { mutableStateOf(false) }
    var expandedScraper by remember { mutableStateOf(false) }
    var expandedGenerator by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(Color(0xFFD0E8FF))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedTable = !expandedTable }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tables", fontSize = 18.sp)
                    Text(if (expandedTable) "˅" else ">", fontSize = 18.sp)
                }

                DropdownMenu(
                    expanded = expandedTable,
                    onDismissRequest = { expandedTable = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(onClick = { expandedTable = false }) { Text("User") }
                    DropdownMenuItem(onClick = { expandedTable = false }) { Text("Simulation") }
                    DropdownMenuItem(onClick = { expandedTable = false }) { Text("Accident") }
                    DropdownMenuItem(onClick = { expandedTable = false }) { Text("Location") }
                    DropdownMenuItem(onClick = { expandedTable = false }) { Text("Station") }
                    DropdownMenuItem(onClick = { expandedTable = false }) { Text("Path") }
                }
            }
            Text("No table selected", color = Color.Gray)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedScraper = !expandedScraper }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Scraper", fontSize = 18.sp)
                    Text(if (expandedScraper) "˅" else ">", fontSize = 18.sp)
                }

                DropdownMenu(
                    expanded = expandedScraper,
                    onDismissRequest = { expandedScraper = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(onClick = { expandedScraper = false }) { Text("Bolnica") }
                    DropdownMenuItem(onClick = { expandedScraper = false }) { Text("Policija") }
                    DropdownMenuItem(onClick = { expandedScraper = false }) { Text("Gasilci") }
                }
            }
            Text("No scraper selected", color = Color.Gray)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedGenerator = !expandedGenerator }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Generate", fontSize = 18.sp)
                    Text(if (expandedGenerator) "˅" else ">", fontSize = 18.sp)
                }

                DropdownMenu(
                    expanded = expandedGenerator,
                    onDismissRequest = { expandedGenerator = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(onClick = { expandedGenerator = false }) { Text("User") }
                    DropdownMenuItem(onClick = { expandedGenerator = false }) { Text("Simulation") }
                    DropdownMenuItem(onClick = { expandedGenerator = false }) { Text("Accident") }
                    DropdownMenuItem(onClick = { expandedGenerator = false }) { Text("Location") }
                    DropdownMenuItem(onClick = { expandedGenerator = false }) { Text("Station") }
                    DropdownMenuItem(onClick = { expandedGenerator = false }) { Text("Path") }
                }
            }
            Text("No generator selected", color = Color.Gray)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFBBDEFB))
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("About", fontSize = 16.sp)
        }
    }
}