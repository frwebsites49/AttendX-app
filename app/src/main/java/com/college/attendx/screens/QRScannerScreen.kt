package com.college.attendx.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.college.attendx.repositories.FirebaseRepository
import com.college.attendx.ui.components.CameraQrScanner
import com.college.attendx.ui.components.ScannerOverlay
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    return r * c
}

@Composable
fun QRScannerScreen(
    onBack: () -> Unit,
    activeSessionId: String? = null // Add this parameter
) {
    val context = LocalContext.current
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    var isProcessing by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var resultSuccess by remember { mutableStateOf(false) }
    var scannerSessionKey by remember { mutableStateOf(0) }
    var isScanningActive by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F0))) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ ICON CHANGE: Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SCAN QR", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1A1A))
                }
                Row(
                    modifier = Modifier.clickable {
                        isScanningActive = false
                        onBack()
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(20.dp)
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

            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.width(60.dp).height(4.dp).background(Color(0xFFFF6B35)))
            Spacer(modifier = Modifier.height(24.dp))

            resultMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (resultSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (resultSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            contentDescription = null,
                            tint = if (resultSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (resultSuccess) "Attendance Marked" else "Rejected",
                                color = if (resultSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = message,
                                color = Color(0xFF1A1A1A),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isProcessing -> {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFFFF6B35))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Verifying attendance…", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                    !hasCameraPermission -> {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Filled.Camera,
                                    contentDescription = "Camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Camera permission required", color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                    isScanningActive -> {
                        key(scannerSessionKey) {
                            CameraQrScanner(
                                modifier = Modifier.fillMaxSize(),
                                onQrCodeScanned = { sessionId ->
                                    isScanningActive = false
                                    handleScan(context, repository, scope, sessionId,
                                        onStart = { isProcessing = true; resultMessage = null },
                                        onResult = { success, message ->
                                            isProcessing = false
                                            resultSuccess = success
                                            resultMessage = message
                                        }
                                    )
                                }
                            )
                        }
                        ScannerOverlay(modifier = Modifier.fillMaxSize())
                        Text(
                            text = "Pinch to zoom",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.QrCode,
                                contentDescription = "QR Code",
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (hasCameraPermission) {
                Button(
                    onClick = {
                        if (!isScanningActive) {
                            resultMessage = null
                            scannerSessionKey++
                            isScanningActive = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProcessing) "PROCESSING…" else if (isScanningActive) "SCANNING…" else "START SCAN",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
            } else {
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCCCCCC))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Camera,
                            contentDescription = null,
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GRANT CAMERA PERMISSION",
                            color = Color(0xFF1A1A1A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Point camera at the QR code shown by your teacher",
                color = Color(0xFF666666),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun handleScan(
    context: android.content.Context,
    repository: FirebaseRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    sessionId: String,
    onStart: () -> Unit,
    onResult: (success: Boolean, message: String) -> Unit
) {
    onStart()

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasLocationPermission) {
        onResult(false, "Location permission is required to verify you are in class.")
        return
    }

    scope.launch {
        val profileResult = repository.getUserProfile()
        val profile = profileResult.getOrNull()

        if (profile == null || !profile.isProfileComplete) {
            onResult(false, "Please complete your profile before scanning.")
            return@launch
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            onResult(false, "Not logged in.")
            return@launch
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                onResult(false, "Could not get your location. Make sure GPS is turned on and try again.")
                return@addOnSuccessListener
            }

            scope.launch {
                val sessionResult = repository.getSession(sessionId)
                val session = sessionResult.getOrNull()
                if (session == null) {
                    onResult(false, "This QR code does not match any active session.")
                    return@launch
                }

                val distance = haversineMeters(
                    location.latitude, location.longitude,
                    session.latitude, session.longitude
                )

                val markResult = repository.markAttendance(
                    sessionId = sessionId,
                    studentId = userId,
                    studentName = profile.name,
                    rollNumber = profile.rollNumber,
                    division = profile.division,
                    group = profile.group,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    distance = distance
                )

                markResult.onSuccess {
                    onResult(true, "Attendance marked for ${profile.name} (Roll No. ${profile.rollNumber}). You are ${distance.toInt()}m from class.")
                }.onFailure { e ->
                    onResult(false, e.message ?: "Attendance rejected")
                }
            }
        }.addOnFailureListener {
            onResult(false, "Could not get your location: ${it.message}")
        }
    }
}