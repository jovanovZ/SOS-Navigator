import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InputFieldForBoolean(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    label: String = "Select",
    inputModifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.body1
        )
        Checkbox(
            checked = value,
            onCheckedChange = { onValueChange(it) }
        )
    }
}