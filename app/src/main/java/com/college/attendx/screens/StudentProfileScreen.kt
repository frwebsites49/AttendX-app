package com.college.attendx.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StudentProfileScreen(
    onSave: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var name by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }

    var isNameFocused by remember { mutableStateOf(false) }
    var isRollFocused by remember { mutableStateOf(false) }
    var isDivisionFocused by remember { mutableStateOf(false) }
    var isGroupFocused by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && rollNumber.isNotBlank() &&
            division.isNotBlank() && group.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PROFILE",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1A1A),
                            letterSpacing = (-2).sp
                        )
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(6.dp)
                                .background(Color(0xFFFF6B35))
                        )
                    }
                }
                Text(
                    text = "Complete your profile to continue",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Brutalist input fields
            BrutalistInput(
                value = name,
                onValueChange = { name = it },
                placeholder = "FULL NAME",
                isFocused = isNameFocused,
                onFocusChange = { isNameFocused = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BrutalistInput(
                value = rollNumber,
                onValueChange = { rollNumber = it },
                placeholder = "ROLL NUMBER",
                isFocused = isRollFocused,
                onFocusChange = { isRollFocused = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            BrutalistInput(
                value = division,
                onValueChange = { division = it.uppercase() },
                placeholder = "DIVISION",
                isFocused = isDivisionFocused,
                onFocusChange = { isDivisionFocused = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BrutalistInput(
                value = group,
                onValueChange = { group = it.uppercase() },
                placeholder = "GROUP",
                isFocused = isGroupFocused,
                onFocusChange = { isGroupFocused = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error message with icon
            errorMessage?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFE8E8))
                        .border(2.dp, Color(0xFFFF0000))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = null,
                        tint = Color(0xFFFF0000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = Color(0xFFFF0000),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Save Button with icon
            BrutalistButton(
                onClick = {
                    if (isFormValid) {
                        onSave(name, rollNumber, division, group)
                    }
                },
                text = "SAVE & CONTINUE",
                enabled = isFormValid,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Back link with icon
            Row(
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "BACK",
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BrutalistInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val borderColor = when {
        isFocused -> Color(0xFFFF6B35)
        value.isNotBlank() -> Color(0xFF1A1A1A)
        else -> Color(0xFFCCCCCC)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, borderColor)
            .background(
                if (isFocused) Color(0xFFFFF5F0)
                else Color.White
            )
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .clickable { onFocusChange(true) },
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty() && !isFocused) {
            Text(
                text = placeholder,
                color = Color(0xFF999999),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
fun BrutalistButton(
    onClick: () -> Unit,
    text: String,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val backgroundColor = when {
        !enabled -> Color(0xFFCCCCCC)
        isLoading -> Color(0xFFFF6B35)
        else -> Color(0xFFFF6B35)
    }

    val borderColor = when {
        !enabled -> Color(0xFF999999)
        else -> Color(0xFF1A1A1A)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = if (enabled && !isLoading) 6.dp else 0.dp,
                shape = RoundedCornerShape(0.dp),
                clip = false
            )
            .border(3.dp, borderColor)
            .background(backgroundColor)
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = null,
                    tint = if (enabled) Color.White else Color(0xFF666666),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = if (enabled) Color.White else Color(0xFF666666),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}