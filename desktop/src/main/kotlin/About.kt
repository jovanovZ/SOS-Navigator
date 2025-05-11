import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun About() {
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
            backgroundColor = Color.White,
            modifier = Modifier.width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SOS-Navigator", style = MaterialTheme.typography.h5)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Subject", fontSize = 16.sp, color = Color(0xFF1565C0))
                Text("Principi programskih jezikov", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Authors", fontSize = 16.sp, color = Color(0xFF1565C0))
                Text("• Zdravko Jovanov")
                Text("• Miha Brunec")
                Text("• Martin Kobal")
            }
        }
    }
}