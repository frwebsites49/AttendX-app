package com.college.attendx.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.ui.components.CyberButton
import com.college.attendx.ui.components.CyberLink
import com.college.attendx.ui.theme.*

@Composable
fun RoleSelectionScreen(
    onAdminClick: () -> Unit,
    onStudentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(DarkBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // ✅ ICON CHANGE: Added icon to header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.QrCode,
                contentDescription = "Attendance",
                tint = Yellow,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ATTENDANCE",
                style = MaterialTheme.typography.displayLarge,
                color = Yellow
            )
        }

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .background(Yellow)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Smart Attendance System",
            color = Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        CyberButton(
            onClick = onAdminClick,
            text = "CONTINUE AS ADMIN / TEACHER"
        )

        CyberButton(
            onClick = onStudentClick,
            text = "CONTINUE AS STUDENT",
            isSecondary = true
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}