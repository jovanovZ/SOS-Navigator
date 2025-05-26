package viewTables

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
import org.bson.Document
import org.bson.types.ObjectId


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
                            if (emailInput.value.isEmpty() || imageUrlInput.value.isEmpty()) {
                                println("Please fill all fields")
                                return@Button
                            }
                            val updatedUser = user.copy(
                                _id = user._id,
                                name = user.name,
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
            //api klic za getall
            try {
                val db = DataBase.getDatabase()
                val collection = db.getCollection("users")
                val users = collection.find().asFlow().toList()
                users.map {
                    User(
                        _id = it.getObjectId("_id"),
                        name = it.getString("username"),
                        email = it.getString("email"),
                        password = it.getString("password"),
                        imageUrl = it.getString("imageUrl"),
                        historySimulations = it.getList("historySimulations", ObjectId::class.java)
                    )
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
                                //api klic za delete
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("users", Document::class.java)
                                val filter = Document("_id", deletedUser._id)

                                val result = collection.deleteOne(filter).asFlow().toList()
                                if (result.isNotEmpty() && result[0].deletedCount > 0) {
                                    println("User with ID ${deletedUser._id} deleted successfully.")
                                    userState.value =
                                        userState.value.filter { it._id != deletedUser._id }
                                } else {
                                    println("No user found with ID ${deletedUser._id}.")
                                }
                            } catch (e: Exception) {
                                println("Error while deleting user: ${e.message}")
                            }
                        }
                    }, onSave = { editedUser ->
                        runBlocking {
                            //api klic za update
                            try {
                                val db = DataBase.getDatabase()
                                val collection = db.getCollection("users", Document::class.java)
                                val filter = Document("_id", editedUser._id)
                                val update = Document(
                                    "\$set", Document(
                                        "username", editedUser.name
                                    ).append(
                                        "email", editedUser.email
                                    ).append(
                                        "password", editedUser.password
                                    ).append(
                                        "imageUrl", editedUser.imageUrl
                                    ).append(
                                        "historySimulations", editedUser.historySimulations
                                    )
                                )

                                val result = collection.updateOne(filter, update).asFlow().toList()
                                if (result.isNotEmpty() && result[0].modifiedCount > 0) {
                                    println("User with ID ${editedUser._id} updated successfully.")
                                    userState.value = userState.value.map {
                                        if (it._id == editedUser._id) editedUser else it
                                    }
                                } else {
                                    println("No user found with ID ${editedUser._id}.")
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