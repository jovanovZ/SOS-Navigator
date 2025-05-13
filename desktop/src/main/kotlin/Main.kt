import insertTables.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import generate.*
import viewTables.*

enum class Mode {
    TABLE,
    SCRAPER,
    GENERATOR,
    ABOUT,
    ADD
}
enum class Tables {
    USER,
    SIMULATION,
    ACCIDENT,
    LOCATION,
    STATION,
    PATH,
    NONE
}
enum class Scraper {
    POLICE,
    AMBULANCE,
    FIRE_DEPARTMENT,
    NONE
}
enum class Generator {
    USER,
    SIMULATION,
    ACCIDENT,
    LOCATION,
    STATION,
    PATH,
    NONE
}
enum class Add {
    USER,
    SIMULATION,
    ACCIDENT,
    LOCATION,
    STATION,
    PATH,
    NONE
}



@Composable
@Preview
fun App(
    state: MutableState<Mode> = mutableStateOf(Mode.ABOUT),
    tablesState: MutableState<Tables> = mutableStateOf(Tables.NONE),
    scraperState: MutableState<Scraper> = mutableStateOf(Scraper.NONE),
    generatorState: MutableState<Generator> = mutableStateOf(Generator.NONE),
    addTableState: MutableState<Add> = mutableStateOf(Add.NONE)
) {
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().background(Color(0xF9FAFB))) {
            SidebarWithDropdown(state, tablesState, scraperState, generatorState,addTableState)
        }
        when(state.value){
            Mode.ABOUT -> About()
            Mode.TABLE -> when(tablesState.value){
                Tables.USER -> ViewUsers()
                Tables.SIMULATION -> ViewSimulation()
                Tables.ACCIDENT -> ViewAccidents()
                Tables.LOCATION -> ViewLocation()
                Tables.STATION -> ViewStations()
                Tables.PATH -> ViewPaths()
                else -> println("TODO")
            }
            Mode.SCRAPER -> ScrapePrompt(scraperState)
            Mode.GENERATOR -> when(generatorState.value){
                Generator.USER -> GenerateUser()
                Generator.SIMULATION -> GenerateSimulation()
                Generator.ACCIDENT -> GenerateAccident()
                Generator.LOCATION -> GenerateLocation()
                Generator.STATION -> GenerateStation()
                Generator.PATH -> GeneratePath()
                else -> println("TODO")
            }
            Mode.ADD -> when(addTableState.value){
                Add.USER -> AddUser()
                Add.SIMULATION -> AddSimulation()
                Add.ACCIDENT -> AddAccident()
                Add.LOCATION -> AddLocation()
                Add.STATION -> AddStation()
                Add.PATH -> AddPath()
                else -> println("TODO")
            }
        }
    }
}

//POPRAVI KO JE ABOUUT SE NE RESETA STATE V SIDEBARU

fun main() = application {
    val stateMode = mutableStateOf(Mode.ABOUT)
    val stateForTable: MutableState<Tables> = remember { mutableStateOf(Tables.NONE) }
    val stateForScraper: MutableState<Scraper> = remember {   mutableStateOf(Scraper.NONE)}
    val stateForGenerator: MutableState<Generator> = remember {   mutableStateOf(Generator.NONE)}
    val stateForAddTables: MutableState<Add> = remember {   mutableStateOf(Add.NONE)}


    Window(onCloseRequest = ::exitApplication) {
        App(stateMode, stateForTable, stateForScraper, stateForGenerator, stateForAddTables)
    }
}