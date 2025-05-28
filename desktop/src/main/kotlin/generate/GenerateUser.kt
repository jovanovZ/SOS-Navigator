package generate

import BACKEND_URL
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
@Preview
fun GenerateUser() {
    val instanceCount = remember { mutableStateOf("") }
    val faker = Faker()
    val finished = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 300.dp)
            .background(color = Color(0xFFE3F2FD)),
        contentAlignment = Alignment.Center
    ) {
        if (errorMessage.value != null) {
            GenerateModal(
                text = errorMessage.value!!,
                onClose = {
                    errorMessage.value = null
                    finished.value = false
                }
            )
        } else if (finished.value) {
            GenerateModal(
                text = "User Generation Complete\nAll users have been\n generated successfully.",
                onClose = {
                    finished.value = false
                    instanceCount.value = ""
                }
            )
        } else {
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



                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(50),
                            onClick = {
                                if (instanceCount.value.isEmpty() || instanceCount.value.toInt() <= 0) {
                                    return@Button
                                }
                                var name: String
                                var email: String
                                var password: String
                                val client = OkHttpClient()
                                for (i in 0 until instanceCount.value.toInt()) {
                                    try {
                                        name = faker.name.name()
                                        email = faker.internet.email()
                                        password = faker.barcode.ean8()

                                        val url = "${BACKEND_URL}/api/user/register"
                                        val json = JSONObject().put("username", name)
                                            .put("email", email)
                                            .put("password", password)
                                            .toString()
                                        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                        val request = Request.Builder().url(url).post(body).build()
                                        val response = client.newCall(request).execute()
                                        if (response.isSuccessful) {
                                            val responseString = response.body?.string() ?: ""
                                            val responseJson = JSONObject(responseString)
                                            val user = responseJson.getJSONObject("user")
                                            println("User generated successfully: $user")
                                        } else {
                                            println("Failed to generate user : ${response.message}")
                                        }
                                    } catch (e: Exception) {
                                        errorMessage.value = "Error generating user: ${e.message}"
                                    }
                                }
                                finished.value = true

                            }) {
                            Text("Generate")
                        }
                    }
                }
            }
        }
    }
}