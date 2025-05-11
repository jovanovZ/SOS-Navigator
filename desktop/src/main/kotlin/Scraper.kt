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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun ScrapePrompt(scraperState: MutableState<Scraper>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 200.dp)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = 8.dp,
            backgroundColor = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (scraperState.value == Scraper.NONE) "First select scraper" else "Do you want to scrape ${
                        scraperState.value.toString().lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                    }?",
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if(scraperState.value != Scraper.NONE){
                        Button(
                            onClick = { println("Cancelled") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("No", color = Color.White)
                        }

                        Button(
                            onClick = { println("Scraping gasilci...") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Scrape", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}