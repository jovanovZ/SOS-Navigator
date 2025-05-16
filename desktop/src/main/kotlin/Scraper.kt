import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import it.skrape.core.*
import it.skrape.fetcher.*
import it.skrape.selects.*
import it.skrape.selects.html5.*

data class PoliceStation(
    val name: String,
    val location: String,
    val city: String
)

data class FireFighterStation(
    val name: String,
    val location: String,
    val city: String
)


data class Hospital(
    val name: String,
    val location: String,
    val city: String
)

fun getPoliceStations(): List<PoliceStation> {
    val policeStations = mutableListOf<PoliceStation>()
    val cityCodePairs = listOf(
        "celje" to "ce",
        "koper" to "kp",
        "kranj" to "kr",
        "ljubljana" to "lj",
        "maribor" to "mb",
        "murska-sobota" to "ms",
        "nova-gorica" to "ng",
        "novo-mesto" to "nm"
    )

    for ((city, code) in cityCodePairs) {
        try {
            skrape(HttpFetcher) {
                request {
                    url =
                        "https://www.policija.si/o-slovenski-policiji/organiziranost/policijske-uprave/pu-$city/policijske-enote-pu-$code"
                }

                response {
                    htmlDocument {
                        val tables = findAll("table#padd3")
                        tables.forEach { table ->
                            val rows = table.findAll("tr").take(2)
                            if (rows.size >= 2) {
                                val name = rows[0].findFirst("td").text.trim()
                                val location = rows[1].findAll("td")[1].text.trim()
                                policeStations.add(PoliceStation(name, location, city))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Failed to scrape $city: ${e.message}")
        }
    }

    return policeStations
}


fun getFireStations(): List<FireFighterStation> {
    val fireStations = mutableListOf<FireFighterStation>()

    skrape(HttpFetcher) {
        request {
            url = "http://zspg112.si/gasilske-enote/"
        }

        response {
            htmlDocument {
                findAll("a.ult_price_action_button").forEach { anchor ->
                    val name = anchor.findFirst("h3.ult-responsive").text.trim()

                    val fullText = anchor.findFirst("div.ult_price_features").text.trim()
                    val lines = fullText.lines()
                    val addressLine = lines.getOrNull(0) ?: ""
                    val location = addressLine.substringBefore(" Tel.: ").trim()
                    val city = location
                        .split(",")
                        .getOrNull(1)
                        ?.trim()
                        ?.replace(Regex("^\\d{3,5}\\s*"), "")
                        ?: "Unknown"

                    fireStations.add(FireFighterStation(name, location, city))
                }
            }
        }
    }

    return fireStations
}


fun getHospitals(): List<Hospital> {
    val hospitals = mutableListOf<Hospital>()

    skrape(HttpFetcher) {
        request {
            url =
                "https://www.gov.uk/government/publications/slovenia-list-of-medical-facilities/list-of-medical-facilities-and-practitioners-in-slovenia"
        }

        response {
            htmlDocument {
                val table = findFirst("table")
                val rows = table.findAll("tr").drop(1)

                rows.forEach { row ->
                    val columns = row.findAll("td")
                    if (columns.size >= 4) {
                        val name = columns[0].findFirst("a")?.text?.trim() ?: ""
                        val address = columns[3].text.trim()

                        val city = run {
                            val partAfterComma = address.substringAfterLast(",").trim()
                            Regex("""\d{4}""").find(partAfterComma)?.let { match ->
                                partAfterComma.substring(match.range.last + 1).trim()
                            } ?: address.split(" ").takeLast(2).joinToString(" ")
                        }

                        hospitals.add(Hospital(name, address, city))
                    }
                }
            }
        }
    }

    return hospitals
}

@Composable
fun ScrapePrompt(scraperState: MutableState<Scraper>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 300.dp)
            .background(color = Color(0xFFE3F2FD)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = 8.dp,
            backgroundColor = Color.White,

            ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (scraperState.value == Scraper.NONE) "First select scraper" else "Do you want to scrape ${
                        scraperState.value.toString().lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                    }?",
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (scraperState.value != Scraper.NONE) {
                        Button(
                            onClick = { println("Cancelled") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(30)
                        ) {
                            Text("No", color = Color.White)
                        }

                        Button(
                            onClick = {
                                when (scraperState.value) {
                                    Scraper.POLICE -> {
                                        val policeStations = getPoliceStations()
                                        println("\nFinal List of Police stations:")
                                        policeStations.forEach { station ->
                                            println("${station.name}: ${station.location} (City: ${station.city})")
                                        }
                                    }

                                    Scraper.AMBULANCE -> {
                                        val hospitals = getHospitals()
                                        println("\nFinal List of Hospitals:")
                                        hospitals.forEach { station ->
                                            println("${station.name}: ${station.location} (City: ${station.city})")
                                        }
                                    }

                                    Scraper.FIRE_DEPARTMENT -> {
                                        val fireStations = getFireStations()
                                        println("\nFinal List of Fire Fighter Stations:")
                                        fireStations.forEach { station ->
                                            println("${station.name}: ${station.location} (City: ${station.city})")
                                        }
                                    }

                                    Scraper.NONE -> {
                                            println("Printing None")
                                    }
                                }
                                println("Scraping ${scraperState.value}")
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(30)
                        ) {
                            Text("Scrape", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}