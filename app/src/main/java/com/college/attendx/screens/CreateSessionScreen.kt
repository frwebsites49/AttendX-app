package com.college.attendx.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.college.attendx.ActiveSessionHolder
import com.college.attendx.models.AttendanceRecord
import com.college.attendx.models.AttendanceSession
import com.college.attendx.repositories.FirebaseRepository
import com.college.attendx.utils.QRCodeGenerator
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

@Composable
fun CreateSessionScreen(
    activeSessionHolder: ActiveSessionHolder,
    onBack: () -> Unit,
    onSessionCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FirebaseRepository() }

    var subject by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("40") }
    var duration by remember { mutableStateOf("60") }
    var isLoading by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attendanceRecords by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }

    val showQR = activeSessionHolder.session != null
    val sessionId = activeSessionHolder.session?.sessionId ?: ""

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            createSession(
                context, repository, scope,
                subject, division, group, radius, duration,
                onLoading = { isLoading = it },
                onError = { errorMessage = it },
                onCreated = { id, session ->
                    activeSessionHolder.session = session
                    activeSessionHolder.qrBitmap = QRCodeGenerator.generateQRCode(id)
                    Toast.makeText(context, "Session created successfully!", Toast.LENGTH_SHORT).show()
                    onSessionCreated(id)
                }
            )
        } else {
            errorMessage = "Location permission is required - the classroom's GPS position anchors the radius check that prevents off-site attendance."
        }
    }

    fun updateLiveCount() {
        scope.launch {
            try {
                if (sessionId.isNotEmpty()) {
                    val count = repository.getLiveCount(sessionId)
                    if (count.isSuccess) {
                        activeSessionHolder.liveCount = count.getOrNull() ?: 0
                    }
                    val records = repository.getAttendanceBySession(sessionId)
                    if (records.isSuccess) {
                        attendanceRecords = records.getOrNull() ?: emptyList()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    LaunchedEffect(sessionId, showQR) {
        if (showQR && sessionId.isNotEmpty()) {
            while (showQR) {
                updateLiveCount()
                delay(3000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (showQR) Icons.Filled.CheckCircle else Icons.Filled.Add,
                    contentDescription = null,
                    tint = if (showQR) Color(0xFF00C853) else Color(0xFF1A1A1A),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showQR) "QR CODE READY" else "CREATE SESSION",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1A1A1A)
                )
            }
            if (!activeSessionHolder.isSessionActive) {
                Text(
                    text = "\u2190 BACK",
                    color = Color(0xFF666666),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        if (showQR) {
                            activeSessionHolder.clear()
                        } else {
                            onBack()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.width(60.dp).height(4.dp).background(Color(0xFFFF6B35)))
        Spacer(modifier = Modifier.height(24.dp))

        if (showQR && activeSessionHolder.qrBitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SESSION CREATED",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00C853)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan this QR code to mark attendance",
                        color = Color(0xFF666666),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    activeSessionHolder.qrBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(220.dp)
                                .background(Color.White)
                                .padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF6B35).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.People,
                                        contentDescription = null,
                                        tint = Color(0xFF1A1A1A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Students Present",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1A1A1A)
                                    )
                                }
                                Text(
                                    text = "Live counter updates automatically",
                                    fontSize = 11.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                            Text(
                                text = activeSessionHolder.liveCount.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF6B35)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (activeSessionHolder.isSessionActive) {
                        Text(
                            text = "\uD83D\uDD12 Navigation is locked while this session is live",
                            fontSize = 11.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val session = activeSessionHolder.session
                            if (session == null) {
                                Toast.makeText(context, "Session info not available yet", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isExporting = true
                            scope.launch {
                                val result = repository.getAttendanceBySession(sessionId)
                                result.onSuccess { records ->
                                    exportAsPdf(context, session, records)
                                    isExporting = false
                                }.onFailure {
                                    Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    isExporting = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORT AS PDF", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val session = activeSessionHolder.session
                            if (session == null) {
                                Toast.makeText(context, "Session info not available yet", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isExporting = true
                            scope.launch {
                                val result = repository.getAttendanceBySession(sessionId)
                                result.onSuccess { records ->
                                    exportAsCsv(context, session, records)
                                    isExporting = false
                                }.onFailure {
                                    Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                    isExporting = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TableChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORT AS CSV", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.closeSession(sessionId)
                                    activeSessionHolder.session = activeSessionHolder.session?.copy(isActive = false)
                                    Toast.makeText(context, "Session ended", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("END", color = Color.White)
                        }

                        Button(
                            onClick = {
                                activeSessionHolder.clear()
                                subject = ""
                                division = ""
                                group = ""
                                radius = "40"
                                duration = "60"
                                attendanceRecords = emptyList()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !activeSessionHolder.isSessionActive,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                            shape = RoundedCornerShape(0.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NEW", color = Color.White)
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Session Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject *") },
                placeholder = { Text("e.g., Computer Science") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B35),
                    unfocusedBorderColor = Color(0xFFCCCCCC)
                ),
                shape = RoundedCornerShape(0.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = division,
                onValueChange = { division = it.uppercase() },
                label = { Text("Division *") },
                placeholder = { Text("e.g., A") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B35),
                    unfocusedBorderColor = Color(0xFFCCCCCC)
                ),
                shape = RoundedCornerShape(0.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = group,
                onValueChange = { group = it.uppercase() },
                label = { Text("Group *") },
                placeholder = { Text("e.g., G1") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B35),
                    unfocusedBorderColor = Color(0xFFCCCCCC)
                ),
                shape = RoundedCornerShape(0.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = radius,
                    onValueChange = { radius = it },
                    label = { Text("Radius (m)") },
                    placeholder = { Text("40") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B35),
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    ),
                    shape = RoundedCornerShape(0.dp)
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (min)") },
                    placeholder = { Text("60") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B35),
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    ),
                    shape = RoundedCornerShape(0.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\u26A0\uFE0F Location permission is required to mark attendance",
                color = Color(0xFFFF6B35),
                fontSize = 12.sp
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color(0xFFFF0000), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (subject.isBlank() || division.isBlank() || group.isBlank()) {
                        errorMessage = "Please fill all required fields"
                        return@Button
                    }
                    errorMessage = null

                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasLocationPermission) {
                        createSession(
                            context, repository, scope,
                            subject, division, group, radius, duration,
                            onLoading = { isLoading = it },
                            onError = { errorMessage = it },
                            onCreated = { id, session ->
                                activeSessionHolder.session = session
                                activeSessionHolder.qrBitmap = QRCodeGenerator.generateQRCode(id)
                                Toast.makeText(context, "Session created successfully!", Toast.LENGTH_SHORT).show()
                                onSessionCreated(id)
                            }
                        )
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                shape = RoundedCornerShape(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GENERATE QR CODE", color = Color.White)
                    }
                }
            }
        }
    }
}

private fun createSession(
    context: Context,
    repository: FirebaseRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    subject: String,
    division: String,
    group: String,
    radius: String,
    duration: String,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onCreated: (String, AttendanceSession) -> Unit
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        onError("Location permission not granted")
        return
    }

    onLoading(true)

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    fusedClient.lastLocation.addOnSuccessListener { location ->
        if (location == null) {
            onLoading(false)
            onError("Could not get current location. Make sure GPS is turned on and try again.")
            return@addOnSuccessListener
        }

        scope.launch {
            try {
                val session = AttendanceSession(
                    subject = subject,
                    division = division,
                    group = group,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = radius.toIntOrNull() ?: 40,
                    expiryTime = System.currentTimeMillis() + (duration.toLongOrNull() ?: 60L) * 60 * 1000,
                    isActive = true
                )

                val result = repository.createSession(session)
                if (result.isSuccess) {
                    val id = result.getOrThrow()
                    onCreated(id, session.copy(sessionId = id))
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to create session")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            } finally {
                onLoading(false)
            }
        }
    }.addOnFailureListener {
        onLoading(false)
        onError("Could not get current location: ${it.message}")
    }
}

private fun exportAsPdf(context: Context, session: AttendanceSession, records: List<AttendanceRecord>) {
    val pdfDocument = android.graphics.pdf.PdfDocument()
    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    var y = 40f
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText("ATTENDANCE SHEET", 40f, y, paint)

    y += 24f
    paint.textSize = 11f
    paint.isFakeBoldText = false
    canvas.drawText("Subject: ${session.subject}", 40f, y, paint)
    y += 16f
    canvas.drawText("Division: ${session.division}   Group: ${session.group}", 40f, y, paint)
    y += 16f
    canvas.drawText("Total Present: ${records.size}", 40f, y, paint)

    y += 30f
    paint.isFakeBoldText = true
    canvas.drawText("Roll", 40f, y, paint)
    canvas.drawText("Name", 110f, y, paint)
    canvas.drawText("Date", 280f, y, paint)
    canvas.drawText("Time", 360f, y, paint)
    canvas.drawText("Dist(m)", 460f, y, paint)
    paint.isFakeBoldText = false
    y += 8f
    canvas.drawLine(40f, y, 555f, y, paint)
    y += 18f

    for (r in records) {
        if (y > 800f) break
        canvas.drawText(r.rollNumber, 40f, y, paint)
        canvas.drawText(r.studentName.take(20), 110f, y, paint)
        canvas.drawText(r.date, 280f, y, paint)
        canvas.drawText(r.time, 360f, y, paint)
        canvas.drawText(r.distance.toInt().toString(), 460f, y, paint)
        y += 18f
    }

    pdfDocument.finishPage(page)

    val exportsDir = File(context.cacheDir, "exports")
    if (!exportsDir.exists()) exportsDir.mkdirs()
    val safeSubject = session.subject.replace(Regex("[^A-Za-z0-9]"), "_")
    val file = File(exportsDir, "attendance_${safeSubject}_${System.currentTimeMillis()}.pdf")
    pdfDocument.writeTo(file.outputStream())
    pdfDocument.close()

    shareFile(context, file, "application/pdf")
}

private fun exportAsCsv(context: Context, session: AttendanceSession, records: List<AttendanceRecord>) {
    val exportsDir = File(context.cacheDir, "exports")
    if (!exportsDir.exists()) exportsDir.mkdirs()
    val safeSubject = session.subject.replace(Regex("[^A-Za-z0-9]"), "_")
    val file = File(exportsDir, "attendance_${safeSubject}_${System.currentTimeMillis()}.csv")

    FileWriter(file).use { writer ->
        writer.append("Roll No,Name,Division,Group,Date,Time,Distance (m),Verified,Status\n")
        for (r in records) {
            writer.append(escapeCsv(r.rollNumber)).append(',')
            writer.append(escapeCsv(r.studentName)).append(',')
            writer.append(escapeCsv(r.division)).append(',')
            writer.append(escapeCsv(r.group)).append(',')
            writer.append(escapeCsv(r.date)).append(',')
            writer.append(escapeCsv(r.time)).append(',')
            writer.append(r.distance.toInt().toString()).append(',')
            writer.append(if (r.isVerified) "YES" else "NO").append(',')
            writer.append("Present").append('\n')
        }
    }

    shareFile(context, file, "text/csv")
}

private fun escapeCsv(value: String): String {
    return if (value.contains(",") || value.contains("\"")) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
}

private fun shareFile(context: Context, file: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share attendance sheet"))
}