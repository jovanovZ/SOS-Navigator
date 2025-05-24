package generate

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inputs.InputFieldForNumber
import io.github.serpro69.kfaker.Faker
import org.bson.types.ObjectId
import kotlin.random.Random

fun generateRandomServices(): String {
    val services = listOf("policija", "reĹˇilci", "gasilci")
    val numberOfServices = Random.nextInt(1, 4)
    return services.shuffled().take(numberOfServices).joinToString(", ")
}

@Composable
@Preview
fun GenerateSimulation() {
    val faker = Faker()
    val instanceCount = remember { mutableStateOf("") }

    val responseTimeMin = remember { mutableStateOf("") }
    val responseTimeMax = remember { mutableStateOf("") }


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
            backgroundColor = Color(0xFFFFFFFF),
            modifier = Modifier.width(600.dp)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Generate Simulation", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Number of Instances", fontSize = 18.sp)
                InputFieldForNumber(
                    value = instanceCount.value,
                    onValueChange = { instanceCount.value = it },
                    inputModifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Response time range (in sec)", fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    InputFieldForNumber(
                        label = "Min",
                        value = responseTimeMin.value,
                        onValueChange = { responseTimeMin.value = it },
                        inputModifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    InputFieldForNumber(
                        label = "Max",
                        value = responseTimeMax.value,
                        onValueChange = { responseTimeMax.value = it },
                        inputModifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }


                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            if (instanceCount.value.isEmpty() || responseTimeMin.value.isEmpty() || responseTimeMax.value.isEmpty()) {
                                println("Please fill in all fields")
                                return@Button
                            }
                            if (instanceCount.value.toInt() <= 0 || responseTimeMin.value.toInt() <= 0 || responseTimeMax.value.toInt() <= 0) {
                                println("Please enter valid numbers")
                                return@Button
                            }
                            if (responseTimeMin.value.toInt() > responseTimeMax.value.toInt()) {
                                println("Minimum response time cannot be greater than maximum")
                                return@Button
                            }
                            var userId:ObjectId
                            var simulationName:String
                            var accidentId: ObjectId
                            var typeOfServices:String
                            var stationId: ObjectId
                            var pathId: ObjectId
                            var responseTime: Int
                            // z data clasi si pripravi reqbody tak kot je v path
                            for (i in 1..instanceCount.value.toInt()) {
                                //pridobi random userId
                                simulationName = "Generirana simulacija $i"
                                //pridobi random accidentId
                                typeOfServices = generateRandomServices()
                                //pridobi random stationId
                                //pridobi random pathId
                                responseTime = Random.nextInt(responseTimeMin.value.toInt(), responseTimeMax.value.toInt())
                                responseTime *= 1000 //response time to milliseconds

                                println(simulationName)
                                println(typeOfServices)
                                println(responseTime)
                            }

                        }) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}
