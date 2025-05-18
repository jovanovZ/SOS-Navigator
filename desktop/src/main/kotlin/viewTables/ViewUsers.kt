package viewTables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import db.DataBase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId


// za teste dokler ni povezave na bazo
internal data class User(
    val id: ObjectId,
    val name: String,
    val email: String,
    val password: String,
    val imageUrl: String ,
    val historySimulations: List<ObjectId>
)


@Composable
internal fun UserCard(user: User) {

    Surface(
        modifier = Modifier
            .padding(24.dp)
            .width(600.dp)
            .height(220.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFFFFF),

        ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Id: ${user.id}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Email: ${user.email}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("Password (hashed): ")
            Text(user.password)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Image url: ${user.imageUrl}")
            Spacer(modifier = Modifier.height(4.dp))
            Text("History simulations: ")
            Text(user.historySimulations.joinToString(", "))
        }
    }
}


@Composable
fun ViewUsers() {
    val users = runBlocking {
        try {
            val db = DataBase.getDatabase()
            val collection = db.getCollection("users")
            val users = collection.find().asFlow().toList()
            users.map {
                User(
                    id = it.getObjectId("_id"),
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
        }

    }
    if (users.isEmpty()) {
        BasicText("No users found \nPlease register some users first.")
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
                items(users) { user ->
                    UserCard(user)
                }
            }

        }

    }
}