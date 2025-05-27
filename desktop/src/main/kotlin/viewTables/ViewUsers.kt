package viewTables

import BACKEND_URL
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.DataBase
import inputs.InputFieldForText
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bson.Document
import org.bson.types.ObjectId
import org.json.JSONArray


data class User(
    val _id: ObjectId = ObjectId(),
    val name: String,
    val email: String,
    val password: String,
    val imageUrl: String,
    val historySimulations: List<ObjectId>
)


@Composable
fun UserCard(user: User, onDelete: (User) -> Unit, onSave: (User) -> Unit) {
    val isEditing = remember { mutableStateOf(false) }
    val nameInput = remember { mutableStateOf(user.name) }
    val emailInput = remember { mutableStateOf(user.email) }
    val imageUrlInput = remember { mutableStateOf(user.imageUrl) }
    if (isEditing.value) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .width(600.dp)
                .height(320.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFFFF),

            ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Id: ${user._id}")
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = nameInput.value,
                    onValueChange = { nameInput.value = it },
                    label = "Username",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = emailInput.value,
                    onValueChange = { emailInput.value = it },
                    label = "Email",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                InputFieldForText(
                    value = imageUrlInput.value,
                    onValueChange = { imageUrlInput.value = it },
                    label = "Image url",
                    inputModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            if (emailInput.value.isEmpty() || imageUrlInput.value.isEmpty() || nameInput.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            val updatedUser = user.copy(
                                _id = user._id,
                                name = nameInput.value,
                                email = emailInput.value,
                                password = user.password,
                                imageUrl = imageUrlInput.value,
                                historySimulations = user.historySimulations
                            )
                            onSave(updatedUser)
                            isEditing.value = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = {
                            isEditing.value = false
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .width(600.dp)
                .height(250.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFFFFF),

            ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Id: ${user._id}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Username: ${user.name}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Email: ${user.email}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Image url: ${user.imageUrl}",
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp)
                )
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                ) {
                    Button(
                        onClick = {
                            isEditing.value = true
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Edit")
                    }
                    Button(
                        onClick = {
                            onDelete(user)
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(30)
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }

}


@Composable
fun ViewUsers() {
    val userState = remember { mutableStateOf(listOf<User>()) }
    val loadingState = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userState.value = runBlocking {
            try {
                val url = "${BACKEND_URL}/api/user/all"
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    (0 until jsonArray.length()).map { i ->
                        val obj = jsonArray.getJSONObject(i)
                        val historySimulationsJson = obj.getJSONArray("historySimulations")
                        val historySimulations = (0 until historySimulationsJson.length()).map { j ->
                            ObjectId(historySimulationsJson.getString(j))
                        }
                        User(
                            _id = ObjectId(obj.getString("_id")),
                            name = obj.getString("name"),
                            email = obj.getString("email"),
                            password = obj.getString("password"),
                            imageUrl = obj.getString("imageUrl"),
                            historySimulations = historySimulations
                        )
                    }
                } else {
                    emptyList<User>()
                }
            } catch (e: Exception) {
                println("Error while fetching users: ${e.message}")
                emptyList()
            } finally {
                loadingState.value = false
            }
        }
    }

    if (loadingState.value) {
        Modal("Loading users...")
    } else if (userState.value.isEmpty()) {
        Modal("No users found \nPlease register some users first.")
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 300.dp)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 550.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(userState.value) { user ->
                    UserCard(user = user, onDelete = { deletedUser ->
                        runBlocking {
                            try {
                                val userId = deletedUser._id.toString()
                                val url = "$BACKEND_URL/api/user/delete/$userId"
                                val client = OkHttpClient()
                                val request = Request.Builder()
                                    .url(url)
                                    .delete()
                                    .build()
                                val response = client.newCall(request).execute()
                                if (response.isSuccessful) {
                                    println("User with ID $userId deleted successfully.")
                                    userState.value = userState.value.filter { it._id != deletedUser._id }
                                } else {
                                    println("No user found with ID $userId.")
                                }
                            } catch (e: Exception) {
                                println("Error while deleting user: ${e.message}")
                            }
                        }
                    }, onSave = { editedUser ->
                        runBlocking {
                            try {
                                val userId = editedUser._id.toString()
                                val url = "$BACKEND_URL/api/user/update/$userId"
                                val client = OkHttpClient()
                                val json = org.json.JSONObject()
                                    .put("username", editedUser.name)
                                    .put("email", editedUser.email)
                                    .put("imageUrl", editedUser.imageUrl)
                                    .toString()
                                val body = json.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder()
                                    .url(url)
                                    .put(body)
                                    .build()
                                val response = client.newCall(request).execute()
                                if (response.isSuccessful) {
                                    println("User with ID $userId updated successfully.")
                                    userState.value = userState.value.map {
                                        if (it._id == editedUser._id) editedUser else it
                                    }
                                } else {
                                    println("Failed to update user: ${response.message}")
                                }
                            } catch (e: Exception) {
                                println("Error while updating user: ${e.message}")
                            }
                        }
                    })
                }
            }
        }
    }
}