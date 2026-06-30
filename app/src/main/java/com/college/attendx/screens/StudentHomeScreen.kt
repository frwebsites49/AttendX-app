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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.ui.components.CyberButton
import com.college.attendx.ui.components.CyberCard
import com.college.attendx.ui.components.CyberLink
import com.college.attendx.ui.theme.*

@Composable
fun StudentHomeScreen(
    name: String,
    rollNumber: String,
    division: String,
    group: String,
    onScan: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(DarkBg)
    ) {
        // Header with icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.QrCode,
                    contentDescription = "Attendance",
                    tint = Yellow,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ATTENDANCE",
                    style = MaterialTheme.typography.displayMedium,
                    color = Yellow
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Card
        CyberCard {
            Text(
                text = name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(
                text = "Roll No. $rollNumber · Division $division · Group $group",
                color = Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        CyberButton(
            onClick = onScan,
            text = "SCAN QR CODE",
            modifier = Modifier.height(80.dp)
        )

        CyberLink(
            onClick = onLogout,
            text = "Sign out"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}