package viewTables

import androidx.compose.desktop.ui.tooling.preview.Preview
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


// za teste dokler ni povezave na bazo
internal data class User(val name: String, val email: String, val historySimulations: List<String>)

internal val users = listOf(
    User("Miha Brunec", "miha.brunec@gmail.com", listOf("1", "2", "3")),
    User("Miha Brunec", "miha.brunec@gmail.com", listOf("1", "3")),
    User("Miha Brunec", "miha.brunec@gmail.com", listOf("3")),
    User("Miha Brunec", "miha.brunec@gmail.com", listOf( "3")),
    User("Miha Brunec", "miha.brunec@gmail.com", listOf("1", "2", "3","3","3","3")),
    User("Miha Brunec", "miha.brunec@gmail.com", listOf("1", "2", "3"))
)


@Composable
internal fun UserCard(user: User) {

    Surface(
        modifier = Modifier
            .padding(24.dp)
            .width(400.dp)
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
            Text(
                "👤",
                fontSize = 30.sp, textAlign = TextAlign.Center
            ) //tu je v resnici slika
            Spacer(modifier = Modifier.height(8.dp))
            Text(user.name, fontWeight = FontWeight.Bold )
            Spacer(modifier = Modifier.height(4.dp))
            Text(user.email, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(user.historySimulations.joinToString(", "))
        }
    }
}


@Composable
fun ViewUsers() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 300.dp)
            .background(Color(0xFFE3F2FD)),
        contentAlignment = Alignment.Center
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 350.dp),
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