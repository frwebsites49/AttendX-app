package com.college.attendx.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class AttendanceRecord(
    val roll: String,
    val name: String,
    val date: String,
    val time: String,
    val distance: Int,
    val status: String
)

@Composable
fun ExportScreen(
    subject: String,
    division: String,
    group: String,
    records: List<AttendanceRecord>,
    onExportPDF: () -> Unit,
    onExportCSV: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(DarkBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ATTENDANCE LIST",
            style = MaterialTheme.typography.displayMedium,
            color = Yellow
        )

        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .background(Yellow)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CyberCard {
            Text(
                text = subject,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(
                text = "Division $division · Group $group",
                color = Gray,
                fontSize = 13.sp
            )
            Text(
                text = "Total present: ${records.size}",
                color = Green,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Attendance Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(BorderColor)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Roll",
                color = Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Name",
                color = Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(2f)
            )
            Text(
                text = "Date",
                color = Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Time",
                color = Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Dist",
                color = Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Status",
                color = Yellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // Attendance Table Body
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(records) { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = record.roll,
                        color = White,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = record.name,
                        color = White,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        text = record.date,
                        color = White,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = record.time,
                        color = White,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${record.distance}m",
                        color = White,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = record.status,
                        color = if (record.status == "Present") Green else Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CyberButton(
            onClick = onExportPDF,
            text = "EXPORT AS PDF"
        )

        CyberButton(
            onClick = onExportCSV,
            text = "EXPORT AS CSV",
            isSecondary = true
        )

        CyberLink(
            onClick = onBack,
            text = "← Back to session"
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}