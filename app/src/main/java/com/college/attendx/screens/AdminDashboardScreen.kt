package com.college.attendx.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.college.attendx.models.AttendanceRecord
import com.college.attendx.models.AttendanceSession
import com.college.attendx.repositories.FirebaseRepository
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ListenerRegistration
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

private val BrutalBg = Color(0xFFF5F5F0)
private val BrutalBlack = Color(0xFF1A1A1A)
private val BrutalOrange = Color(0xFFFF6B35)
private val BrutalGreen = Color(0xFF00C853)
private val BrutalRed = Color(0xFFFF3B30)
private val BrutalGray = Color(0xFF666666)

@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var activeSession by remember { mutableStateOf<AttendanceSession?>(null) }
    var liveCount by remember { mutableStateOf(0) }
    var listenerReg by remember { mutableStateOf<ListenerRegistration?>(null) }

    var subject by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var radiusText by remember { mutableStateOf("40") }
    var durationText by remember { mutableStateOf("60") }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            createSessionWithLocation(
                context, repository, scope,
                subject, division, group, radiusText, durationText,
                onSuccess = { session ->
                    activeSession = session
                    listenerReg = repository.listenToAttendanceCount(session.sessionId) { count ->
                        liveCount = count
                    }
                    isCreating = false
                },
                onError = { msg -> errorMessage = msg; isCreating = false }
            )
        } else {
            errorMessage = "Location permission is required to anchor the classroom."
            isCreating = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { listenerReg?.remove() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalBg)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Dashboard,
                    contentDescription = "Admin",
                    tint = BrutalBlack,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("ADMIN", fontSize = 28.sp, fontWeight = FontWeight.Black, color = BrutalBlack)
            }
            Text(
                text = "← BACK",
                color = BrutalGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onBack() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.width(60.dp).height(4.dp).background(BrutalOrange))
        Spacer(modifier = Modifier.height(24.dp))

        val session = activeSession
        if (session == null) {
            errorMessage?.let {
                Text(it, color = BrutalRed, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
            }

            BrutalField(subject, { subject = it }, "Subject")
            Spacer(modifier = Modifier.height(10.dp))
            BrutalField(division, { division = it.uppercase() }, "Division (e.g. A)")
            Spacer(modifier = Modifier.height(10.dp))
            BrutalField(group, { group = it.uppercase() }, "Group (e.g. G1)")
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    BrutalField(radiusText, { radiusText = it }, "Radius (m)")
                }
                Box(modifier = Modifier.weight(1f)) {
                    BrutalField(durationText, { durationText = it }, "Duration (min)")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (subject.isBlank() || division.isBlank() || group.isBlank()) {
                        errorMessage = "Please fill in subject, division, and group"
                        return@Button
                    }
                    errorMessage = null
                    isCreating = true
                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasLocationPermission) {
                        createSessionWithLocation(
                            context, repository, scope,
                            subject, division, group, radiusText, durationText,
                            onSuccess = { newSession ->
                                activeSession = newSession
                                listenerReg = repository.listenToAttendanceCount(newSession.sessionId) { count ->
                                    liveCount = count
                                }
                                isCreating = false
                            },
                            onError = { msg -> errorMessage = msg; isCreating = false }
                        )
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isCreating,
                colors = ButtonDefaults.buttonColors(containerColor = BrutalOrange),
                shape = RoundedCornerShape(0.dp)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("START SESSION", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        } else {
            ActiveSessionView(
                session = session,
                liveCount = liveCount,
                repository = repository,
                scope = scope,
                context = context,
                onEndSession = {
                    scope.launch {
                        repository.closeSession(session.sessionId)
                        listenerReg?.remove()
                        listenerReg = null
                        activeSession = session.copy(isActive = false)
                    }
                },
                onNewSession = {
                    listenerReg?.remove()
                    listenerReg = null
                    activeSession = null
                    liveCount = 0
                    subject = ""; division = ""; group = ""
                }
            )
        }
    }
}

private fun createSessionWithLocation(
    context: Context,
    repository: FirebaseRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    subject: String,
    division: String,
    group: String,
    radiusText: String,
    durationText: String,
    onSuccess: (AttendanceSession) -> Unit,
    onError: (String) -> Unit
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        onError("Location permission not granted")
        return
    }

    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    fusedClient.lastLocation.addOnSuccessListener { location ->
        if (location == null) {
            onError("Could not get current location. Make sure GPS is on and try again.")
            return@addOnSuccessListener
        }
        val radius = radiusText.toIntOrNull() ?: 40
        val duration = durationText.toIntOrNull() ?: 60
        val now = System.currentTimeMillis()

        val newSession = AttendanceSession(
            subject = subject,
            division = division,
            group = group,
            latitude = location.latitude,
            longitude = location.longitude,
            radius = radius,
            createdAt = now,
            expiryTime = now + duration * 60_000L,
            isActive = true
        )

        scope.launch {
            val result = repository.createSession(newSession)
            result.onSuccess { sessionId ->
                onSuccess(newSession.copy(sessionId = sessionId))
            }.onFailure { e ->
                onError(e.message ?: "Failed to create session")
            }
        }
    }.addOnFailureListener {
        onError("Could not get current location: ${it.message}")
    }
}

@Composable
private fun ActiveSessionView(
    session: AttendanceSession,
    liveCount: Int,
    repository: FirebaseRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    context: Context,
    onEndSession: () -> Unit,
    onNewSession: () -> Unit
) {
    var records by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var isExporting by remember { mutableStateOf(false) }

    Text(
        text = "${session.subject} · Div ${session.division} · ${session.group}",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = BrutalBlack
    )
    Spacer(modifier = Modifier.height(16.dp))

    val qrBitmap = remember(session.sessionId) { generateQrBitmap(session.sessionId, 512) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color.White)
                .border(3.dp, BrutalBlack)
                .padding(12.dp)
        ) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Session QR Code",
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "$liveCount students present",
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        color = BrutalGreen,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = if (session.isActive) "Live · radius ${session.radius}m" else "SESSION ENDED",
        fontSize = 12.sp,
        color = if (session.isActive) BrutalGray else BrutalRed,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (session.isActive) {
        Button(
            onClick = onEndSession,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrutalRed),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("END SESSION", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Export buttons with icons
    Button(
        onClick = {
            isExporting = true
            scope.launch {
                val result = repository.getAttendanceBySession(session.sessionId)
                result.onSuccess { fetched ->
                    records = fetched
                    exportAsPdf(context, session, fetched)
                    isExporting = false
                }.onFailure {
                    isExporting = false
                }
            }
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !isExporting,
        colors = ButtonDefaults.buttonColors(containerColor = BrutalOrange),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPORT AS PDF", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = {
            isExporting = true
            scope.launch {
                val result = repository.getAttendanceBySession(session.sessionId)
                result.onSuccess { fetched ->
                    records = fetched
                    exportAsCsv(context, session, fetched)
                    isExporting = false
                }.onFailure {
                    isExporting = false
                }
            }
        },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !isExporting,
        colors = ButtonDefaults.buttonColors(containerColor = BrutalBlack),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TableChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("EXPORT AS CSV", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }

    if (!session.isActive) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "+ NEW SESSION",
            color = BrutalGray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNewSession() }
                .padding(12.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun BrutalField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = BrutalGray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(0.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrutalOrange,
            unfocusedBorderColor = BrutalBlack,
            focusedTextColor = BrutalBlack,
            unfocusedTextColor = BrutalBlack
        )
    )
}

/** Generates a black/white QR bitmap encoding just the sessionId. */
private fun generateQrBitmap(content: String, size: Int): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bitmap
}

/**
 * Builds a simple text-based PDF (no external PDF library dependency
 * needed beyond Android's built-in android.graphics.pdf.PdfDocument)
 * and opens the share sheet via FileProvider.
 */
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