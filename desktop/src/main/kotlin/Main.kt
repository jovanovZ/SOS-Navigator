import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
enum class Mode {
    TABLE,
    SCRAPER,
    GENERATOR,
    ABOUT
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



@Composable
@Preview
fun App(
    state: MutableState<Mode> = mutableStateOf(Mode.ABOUT),
    tablesState: MutableState<Tables> = mutableStateOf(Tables.NONE),
    scraperState: MutableState<Scraper> = mutableStateOf(Scraper.NONE),
    generatorState: MutableState<Generator> = mutableStateOf(Generator.NONE)
) {
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().background(Color(0xFFFFFFFF))) {
            SidebarWithDropdown(state, tablesState, scraperState, generatorState)
        }
        when(state.value){
            Mode.ABOUT -> About()
            Mode.TABLE -> println("TODO")
            Mode.SCRAPER -> ScrapePrompt(scraperState)
            Mode.GENERATOR -> println("TODO")

        }
    }
}

fun main() = application {
    val stateMode = mutableStateOf(Mode.ABOUT)
    val stateForTable: MutableState<Tables> = remember { mutableStateOf(Tables.NONE) }
    val stateForScraper: MutableState<Scraper> = remember {   mutableStateOf(Scraper.NONE)}
    val stateForGenerator: MutableState<Generator> = remember {   mutableStateOf(Generator.NONE)}

    Window(onCloseRequest = ::exitApplication) {
        App(stateMode, stateForTable, stateForScraper, stateForGenerator)
    }
}