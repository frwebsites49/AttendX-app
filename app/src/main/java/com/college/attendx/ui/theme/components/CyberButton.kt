package com.college.attendx.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.ui.theme.*

@Composable
fun CyberButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    isSecondary: Boolean = false
) {
    val backgroundColor = when {
        isDanger -> Red
        isSecondary -> Color.White
        else -> Yellow
    }

    val textColor = when {
        isDanger -> Color.White
        isSecondary -> Black
        else -> Black
    }

    val borderColor = when {
        isDanger -> Red
        else -> BorderColor
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(0.dp),
                clip = false
            ),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.4f)
        ),
        border = BorderStroke(3.dp, borderColor)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun CyberSmallButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false
) {
    CyberButton(
        onClick = onClick,
        text = text,
        modifier = modifier.height(44.dp),
        isDanger = isDanger
    )
}

@Composable
fun CyberLink(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Gray,
            fontSize = 13.sp,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
        )
    }
}