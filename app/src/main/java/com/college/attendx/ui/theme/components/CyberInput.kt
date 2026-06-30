package com.college.attendx.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.ui.theme.*

@Composable
fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textTransform: (String) -> String = { it }
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(textTransform(it)) },
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray,
                fontSize = 13.sp
            )
        },
        shape = RoundedCornerShape(0.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Yellow,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = Yellow,
            unfocusedLabelColor = BorderColor,
            cursorColor = Yellow,
            focusedTextColor = White,
            unfocusedTextColor = White,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray
        ),
        visualTransformation = visualTransformation,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = White
        )
    )
}