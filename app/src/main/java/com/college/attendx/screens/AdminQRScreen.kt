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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.ui.components.CyberButton
import com.college.attendx.ui.components.CyberCard
import com.college.attendx.ui.components.CyberLink
import com.college.attendx.ui.theme.*

@Composable
fun AdminQRScreen(
    subject: String,
    division: String,
    group: String,
    presentCount: Int,
    expiryTime: String,
    onEndSession: () -> Unit,
    onExport: () -> Unit,
    onBack: () -> Unit,
    qrBitmap: Any? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(DarkBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$subject · Div $division · $group",
            style = MaterialTheme.typography.headlineMedium,
            color = Yellow
        )

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .background(Yellow)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // QR Code Display
        CyberCard(
            modifier = Modifier
                .width(220.dp)
                .height(220.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.QrCode,
                    contentDescription = "QR Code",
                    tint = Color.Black,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$presentCount students present",
            color = Green,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Expires at $expiryTime",
            color = Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        CyberButton(
            onClick = onEndSession,
            text = "END SESSION",
            isDanger = true
        )

        CyberButton(
            onClick = onExport,
            text = "EXPORT / VIEW LIST",
            isSecondary = true
        )

        CyberLink(
            onClick = onBack,
            text = "← New session"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}