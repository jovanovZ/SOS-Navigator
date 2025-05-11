import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun resetOtherStates(
    currentState: MutableState<*>,
    tablesState: MutableState<Tables>,
    scraperState: MutableState<Scraper>,
    generatorState: MutableState<Generator>
) {
    if (currentState != tablesState) tablesState.value = Tables.NONE
    if (currentState != scraperState) scraperState.value = Scraper.NONE
    if (currentState != generatorState) generatorState.value = Generator.NONE
}


@Composable
@Preview
fun SidebarWithDropdown(
    stateMode: MutableState<Mode>,
    tablesState: MutableState<Tables> = mutableStateOf(Tables.NONE),
    scraperState: MutableState<Scraper> = mutableStateOf(Scraper.NONE),
    generatorState: MutableState<Generator> = mutableStateOf(Generator.NONE)
) {
    var expandedTable by remember { mutableStateOf(false) }
    var expandedScraper by remember { mutableStateOf(false) }
    var expandedGenerator by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(Color(0xFFE3F2FD)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            //TABLE DEL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (stateMode.value == Mode.TABLE) Color(0xFFBBDEFB) else Color.Transparent)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tables", fontSize = 18.sp)
                    IconButton(onClick = { expandedTable = !expandedTable }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedTable,
                    onDismissRequest = { expandedTable = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(onClick = {
                        expandedTable = false
                        tablesState.value = Tables.USER
                        stateMode.value = Mode.TABLE
                        resetOtherStates(tablesState, tablesState, scraperState, generatorState)
                    }) { Text("User") }
                    DropdownMenuItem(onClick = {
                        expandedTable = false
                        tablesState.value = Tables.SIMULATION
                        stateMode.value = Mode.TABLE
                        resetOtherStates(tablesState, tablesState, scraperState, generatorState)
                    }) { Text("Simulation") }
                    DropdownMenuItem(onClick = {
                        expandedTable = false
                        tablesState.value = Tables.ACCIDENT
                        stateMode.value = Mode.TABLE
                        resetOtherStates(tablesState, tablesState, scraperState, generatorState)
                    }) { Text("Accident") }
                    DropdownMenuItem(onClick = {
                        expandedTable = false
                        tablesState.value = Tables.LOCATION
                        stateMode.value = Mode.TABLE
                        resetOtherStates(tablesState, tablesState, scraperState, generatorState)
                    }) { Text("Location") }
                    DropdownMenuItem(onClick = {
                        expandedTable = false
                        tablesState.value = Tables.STATION
                        stateMode.value = Mode.TABLE
                        resetOtherStates(tablesState, tablesState, scraperState, generatorState)
                    }) { Text("Station") }
                    DropdownMenuItem(onClick = {
                        expandedTable = false
                        tablesState.value = Tables.PATH
                        stateMode.value = Mode.TABLE
                        resetOtherStates(tablesState, tablesState, scraperState, generatorState)
                    }) { Text("Path") }
                }
            }
            Text(
                text = if (tablesState.value == Tables.NONE) "No table selected" else "${
                    tablesState.value.toString().lowercase().replaceFirstChar { it.uppercase() }
                } table selected",
                color = if (tablesState.value == Tables.NONE) Color.Gray else Color(0xFF1E88E5),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            //SCRAPER DEL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (stateMode.value == Mode.SCRAPER) Color(0xFFBBDEFB) else Color.Transparent)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Scraper", fontSize = 18.sp)
                    IconButton(onClick = { expandedScraper = !expandedScraper }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedScraper,
                    onDismissRequest = { expandedScraper = false },
                    modifier = Modifier.background(Color.White)

                ) {
                    DropdownMenuItem(onClick = {
                        expandedScraper = false
                        scraperState.value = Scraper.AMBULANCE
                        stateMode.value = Mode.SCRAPER
                        resetOtherStates(scraperState, tablesState, scraperState, generatorState)
                    }) { Text("Ambulance") }
                    DropdownMenuItem(onClick = {
                        expandedScraper = false
                        scraperState.value = Scraper.POLICE
                        stateMode.value = Mode.SCRAPER
                        resetOtherStates(scraperState, tablesState, scraperState, generatorState)
                    }) { Text("Police") }
                    DropdownMenuItem(onClick = {
                        expandedScraper = false
                        scraperState.value = Scraper.FIRE_DEPARTMENT
                        stateMode.value = Mode.SCRAPER
                        resetOtherStates(scraperState, tablesState, scraperState, generatorState)
                    }) { Text("Fire Department") }
                }
            }
            Text(
                text = if (scraperState.value == Scraper.NONE) "No scraper selected" else "${
                    scraperState.value.toString().lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                } scraper selected",
                color = if (scraperState.value == Scraper.NONE) Color.Gray else Color(0xFF1E88E5),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            //GENERATOR DEL
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (stateMode.value == Mode.GENERATOR) Color(0xFFBBDEFB) else Color.Transparent)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Generate", fontSize = 18.sp)
                    IconButton(onClick = { expandedGenerator = !expandedGenerator }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expandedGenerator,
                    onDismissRequest = { expandedGenerator = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(onClick = {
                        expandedGenerator = false
                        generatorState.value = Generator.USER
                        stateMode.value = Mode.GENERATOR
                        resetOtherStates(generatorState, tablesState, scraperState, generatorState)
                    }) { Text("User") }
                    DropdownMenuItem(onClick = {
                        expandedGenerator = false
                        generatorState.value = Generator.SIMULATION
                        stateMode.value = Mode.GENERATOR
                        resetOtherStates(generatorState, tablesState, scraperState, generatorState)
                    }) { Text("Simulation") }
                    DropdownMenuItem(onClick = {
                        expandedGenerator = false
                        generatorState.value = Generator.ACCIDENT
                        stateMode.value = Mode.GENERATOR
                        resetOtherStates(generatorState, tablesState, scraperState, generatorState)
                    }) { Text("Accident") }
                    DropdownMenuItem(onClick = {
                        expandedGenerator = false
                        generatorState.value = Generator.LOCATION
                        stateMode.value = Mode.GENERATOR
                        resetOtherStates(generatorState, tablesState, scraperState, generatorState)
                    }) { Text("Location") }
                    DropdownMenuItem(onClick = {
                        expandedGenerator = false
                        generatorState.value = Generator.STATION
                        stateMode.value = Mode.GENERATOR
                        resetOtherStates(generatorState, tablesState, scraperState, generatorState)
                    }) { Text("Station") }
                    DropdownMenuItem(onClick = {
                        expandedGenerator = false
                        generatorState.value = Generator.PATH
                        stateMode.value = Mode.GENERATOR
                        resetOtherStates(generatorState, tablesState, scraperState, generatorState)
                    }) { Text("Path") }
                }
            }
            Text(
                text = if (generatorState.value == Generator.NONE) "No generator selected" else "${
                    generatorState.value.toString().lowercase().replaceFirstChar { it.uppercase() }
                } generator selected",
                color = if (generatorState.value == Generator.NONE) Color.Gray else Color(0xFF1E88E5),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        // ABOUT DEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (stateMode.value == Mode.ABOUT) Color(0xFFBBDEFB) else Color.Transparent)//0xFFBBDEFB
                .clickable {
                    stateMode.value = Mode.ABOUT
                },
            horizontalArrangement = Arrangement.Start
        ) {
            Text("About", fontSize = 16.sp, modifier = Modifier.padding(16.dp))
        }
    }
}