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
import androidx.compose.runtime.*
import io.github.cdimascio.dotenv.dotenv
import it.skrape.core.*
import it.skrape.fetcher.*
import okhttp3.OkHttpClient

import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import viewTables.Modal


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

fun getLatLngFromAddress(address: String): Pair<Double, Double>? {
    val dotenv = dotenv()
    val apiKey = dotenv["LOCATION_API_KEY"] ?: throw IllegalStateException("API key not found in .env file")

    val client = OkHttpClient()
    val url = "https://api.opencagedata.com/geocode/v1/json?q=${address.replace(" ", "+")}&key=$apiKey"

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            println("Failed to fetch coordinates: ${response.message}")
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val json = JSONObject(responseBody)
        val results = json.getJSONArray("results")
        if (results.length() > 0) {
            val geometry = results.getJSONObject(0).getJSONObject("geometry")
            val lat = geometry.getDouble("lat")
            val lng = geometry.getDouble("lng")
            return Pair(lat, lng)
        }
    }
    return null
}

fun sendRequestForCreating(long: Double, lat: Double, typeOfStation: String) {
    val client = OkHttpClient()

    try {
        val url = "${BACKEND_URL}/api/station/create"
        val json = JSONObject().put("longitude", long)
            .put("latitude", lat)
            .put("typeOfStation", typeOfStation)
            .put("isPermanent", true)
            .put("region", "Podravska")
            .toString()
        val body =
            json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val responseJson = JSONObject(responseString)
                val stationResponse = responseJson.getJSONObject("station")
                println("Station created successfully: $stationResponse")
            } else {
                println("Failed to add station: ${response.message}")
            }
        }
    } catch (e: Exception) {
        println("Error createing station: ${e.message}")
    }
}

@Composable
fun ScrapePrompt(scraperState: MutableState<Scraper>) {
    val policeStations = remember { mutableStateOf(emptyList<PoliceStation>()) }
    val fireStations = remember { mutableStateOf(emptyList<FireFighterStation>()) }
    val hospitals = remember { mutableStateOf(emptyList<Hospital>()) }
    val completedScrape = remember { mutableStateOf(false) }
    val loadingState = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(scraperState.value) {
        completedScrape.value = false
        loadingState.value = false
    }

    if (completedScrape.value) {
        Modal("Scraping completed\n Stations were added successfully")
    } else if (loadingState.value) {
        Modal("Scraping in progress\nPlease wait...")

    } else {
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
                            scraperState.value.toString().lowercase().replace("_", " ")
                                .replaceFirstChar { it.uppercase() }
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
                                    loadingState.value = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        when (scraperState.value) {
                                            Scraper.POLICE -> {
                                                policeStations.value = getPoliceStations()
                                                policeStations.value.forEach { station ->
                                                    val coords = getLatLngFromAddress(station.location)?.toList()
                                                    if (coords == null) {
                                                        return@forEach

                                                    }
                                                    sendRequestForCreating(coords[0], coords[1], "Policijska")

                                                }
                                                completedScrape.value = true


                                            }

                                            Scraper.AMBULANCE -> {
                                                hospitals.value = getHospitals()

                                                hospitals.value.forEach { station ->
                                                    val coords = getLatLngFromAddress(station.location)?.toList()
                                                    if (coords == null) {
                                                        return@forEach

                                                    }
                                                    sendRequestForCreating(coords[0], coords[1], "Bolnica")
                                                }
                                                completedScrape.value = true

                                            }

                                            Scraper.FIRE_DEPARTMENT -> {
                                                fireStations.value = getFireStations()

                                                fireStations.value.forEach { station ->
                                                    val coords = getLatLngFromAddress(station.location)?.toList()
                                                    if (coords == null) {
                                                        return@forEach

                                                    }
                                                    sendRequestForCreating(coords[0], coords[1], "Gasilci")
                                                }
                                                completedScrape.value = true

                                            }

                                            Scraper.NONE -> {
                                                println("Printing None")
                                            }
                                        }
                                    }
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
}