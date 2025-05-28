package insertTables

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import inputs.InputFieldForText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.types.ObjectId
import org.json.JSONObject

@Composable
@Preview
fun AddUser() {
    val username = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val secondPassword = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

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
                modifier = Modifier.padding(40.dp).verticalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Insert into User table", style = MaterialTheme.typography.h5)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Username", fontSize = 18.sp)
                InputFieldForText(
                    value = username.value,
                    onValueChange = { username.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Username"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Email", fontSize = 18.sp)
                InputFieldForText(
                    value = email.value,
                    onValueChange = { email.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Email"
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                //TU SE LAHKO NAREDIM DA BO PIKICE ZA ZDAJ JE DA VIDIMO CE SE JE PRAV VNESLO
                Text("Password", fontSize = 18.sp)
                InputFieldForText(
                    value = password.value,
                    onValueChange = { password.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Password",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )
                Text("Enter password again", fontSize = 18.sp)
                InputFieldForText(
                    value = secondPassword.value,
                    onValueChange = { secondPassword.value = it },
                    inputModifier = Modifier.fillMaxWidth(),
                    label = "Password",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))



                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.BottomEnd) {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30),
                        onClick = {
                            if (username.value.isEmpty() || email.value.isEmpty() || password.value.isEmpty() || secondPassword.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            if( password.value != secondPassword.value) {
                                println("Passwords do not match")
                                return@Button
                            }
                            try{
                                val url = "${BACKEND_URL}/api/user/register"
                                val client = OkHttpClient()
                                val json = JSONObject()
                                    .put("username", username.value)
                                    .put("email", email.value)
                                    .put("password", password.value)
                                    .toString()
                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder().url(url).post(body).build()
                                val response = client.newCall(request).execute()
                                if(response.isSuccessful){
                                    val responseBody = response.body?.string() ?: ""
                                    println("User created: $responseBody")
                                    username.value = ""
                                    email.value = ""
                                    password.value = ""
                                    secondPassword.value = ""
                                } else {
                                    println("Failed to create user: ${response.message}")
                                }
                            }catch(e: Exception) {
                                println("Error inserting user: ${e.message}")
                            }
                        }) {
                        Text("Insert")
                    }
                }
            }
        }
    }
}
