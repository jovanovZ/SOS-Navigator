package generate

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
@Preview
fun GenerateUser() {
    val instanceCount = remember { mutableStateOf("") }

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
            Column(
                modifier = Modifier.padding(16.dp),
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
                            /*
                            val baseUsernames = listOf(
                                "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Hannah", "Ivy", "Jack",
                                "Kathy", "Leo", "Mona", "Nina", "Oscar", "Paul", "Quinn", "Rachel", "Steve", "Tina",
                                "Uma", "Victor", "Wendy", "Xander", "Yara", "Zane", "Liam", "Noah", "Emma", "Olivia"
                            )
                            val username = "${baseUsernames[Random.nextInt(0, 30)]}${Random.nextInt(1000, 9999)}"
                            val email = "$username@generated.com"
                            val password = "pass${Random.nextInt(1000, 9999)}${username}"
                            // val imageUrl = deafult photo
                            // rabimo se neki array  za historySimulations
                            */
                            }) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}
