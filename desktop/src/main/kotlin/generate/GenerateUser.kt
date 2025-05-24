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

@Composable
@Preview
fun GenerateUser() {
    val instanceCount = remember { mutableStateOf("") }
    val faker = Faker()
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
                    Text("Generate User", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Number of Instances", fontSize = 18.sp)
                InputFieldForNumber(
                    value = instanceCount.value,
                    onValueChange = { instanceCount.value = it },
                    inputModifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            if(instanceCount.value.isEmpty() || instanceCount.value.toInt() <= 0) {
                                return@Button
                            }
                            var name:String
                            var email:String
                            var password:String
                            val imageUrl:String = "https://picsum.photos/200/300"
                            var historySimulations:List<String>
                            // z data clasi si pripravi reqbody tak kot je v path
                            for (i in 0 until instanceCount.value.toInt()) {
                                name = faker.name.name()
                                email = faker.internet.email()
                                password = faker.barcode.ean8()
                                // manjka se samo historySimulations naredi funkcijo na backendu ki ti vrne random
                                // veliki array simulacij
                                println(name)
                                println(email)
                                println(password)
                                println(imageUrl)
                                println("\n")
                            }

                        }) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}