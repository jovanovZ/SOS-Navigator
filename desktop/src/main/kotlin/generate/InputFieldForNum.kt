package generate

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun InputFieldForNumber(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Enter number",
    inputModifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        modifier = inputModifier,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        onValueChange = { input ->
            if (input.all { it.isDigit() || it == '.' || it == '-' }) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
    )
}

