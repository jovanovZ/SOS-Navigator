import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().background(Color(0xFFE3F2FD))) {
            SidebarWithDropdown()
        }
        //About()
        }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}