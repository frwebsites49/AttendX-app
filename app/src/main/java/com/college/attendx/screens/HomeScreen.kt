package com.college.attendx.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.college.attendx.R
import com.college.attendx.UserProfileData
import com.college.attendx.utils.AdminConfig
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun HomeScreen(
    user: FirebaseUser?,
    userProfile: UserProfileData?,
    onNavigateToProfile: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    activeSessionId: String? = null
) {
    val displayName = userProfile?.name?.ifEmpty { user?.displayName ?: user?.email ?: "User" }
        ?: user?.displayName ?: user?.email ?: "User"
    val isProfileComplete = userProfile?.isComplete == true
    val isAdminUser = user?.email?.let { AdminConfig.isAdmin(it) } ?: false

    var showInfoDialog by remember { mutableStateOf(false) }

    android.util.Log.d("HomeScreen", "========== HOME SCREEN ==========")
    android.util.Log.d("HomeScreen", "isAdminUser: $isAdminUser")
    android.util.Log.d("HomeScreen", "User Email: ${user?.email}")
    android.util.Log.d("HomeScreen", "===================================")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Info Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ATTENDX",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1A1A),
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (isProfileComplete) "Welcome, $displayName" else "Complete your profile",
                        fontSize = 14.sp,
                        color = if (isProfileComplete) Color(0xFF1A1A1A) else Color(0xFFFF6B35)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = if (isProfileComplete) Color(0xFF00C853) else Color(0xFFFF6B35),
                                shape = RoundedCornerShape(50)
                            )
                    )
                    // Info Icon
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF999999),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showInfoDialog = true }
                    )
                }
            }

            // ADMIN MODE CARD
            if (isAdminUser) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = "Admin",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ADMIN MODE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = user?.email ?: "Unknown",
                            fontSize = 11.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ ICON CHANGES: Replaced emojis with icons
            QuickActionCard(
                title = "SCAN QR",
                subtitle = "Mark your attendance",
                icon = Icons.Filled.QrCodeScanner,
                iconColor = Color.White,
                onClick = {
                    android.util.Log.d("HomeScreen", "SCAN QR clicked")
                    onNavigateToScan()
                },
                color = Color(0xFFFF6B35)
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickActionCard(
                title = "MY PROFILE",
                subtitle = if (isProfileComplete) "View your details" else "Complete your profile",
                icon = Icons.Filled.Person,
                iconColor = Color.White,
                onClick = {
                    android.util.Log.d("HomeScreen", "MY PROFILE clicked")
                    onNavigateToProfile()
                },
                color = Color(0xFF1A1A1A)
            )

            // ADMIN DASHBOARD CARD
            if (isAdminUser) {
                Spacer(modifier = Modifier.height(12.dp))
                QuickActionCard(
                    title = "ADMIN DASHBOARD",
                    subtitle = "Manage attendance sessions & generate QR",
                    icon = Icons.Filled.Dashboard,
                    iconColor = Color.White,
                    onClick = {
                        android.util.Log.d("HomeScreen", "🔴🔴🔴 ADMIN DASHBOARD CARD CLICKED!")
                        onNavigateToAdmin()
                    },
                    color = Color(0xFFFF6B35)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout button with icon
            Row(
                modifier = Modifier
                    .clickable {
                        android.util.Log.d("HomeScreen", "SIGN OUT clicked")
                        onLogout()
                    }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = "Sign Out",
                    tint = Color(0xFF999999),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SIGN OUT",
                    color = Color(0xFF999999),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Brutalist Info Dialog
    if (showInfoDialog) {
        Dialog(
            onDismissRequest = { showInfoDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color(0xFF121212))
                    .border(3.dp, Color(0xFFFF6B35))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon with Brutalist border
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(3.dp, Color(0xFFFF6B35))
                        .background(Color(0xFFFFE4C4)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Name - Brutalist style
                Text(
                    text = "ATTENDX",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF6B35),
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Version
                Text(
                    text = "v1.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9E9E9E),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(2.dp)
                        .background(Color(0xFFFF6B35))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "AttendX app created for the student's of SASCMA",
                    fontSize = 14.sp,
                    color = Color(0xFFE0E0E0),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(1.dp)
                        .background(Color(0xFF333333))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Watermark
                Text(
                    text = "Created by F/R Websites",
                    fontSize = 11.sp,
                    color = Color(0xFF009688),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Brutalist Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFFFD500))
                        .border(2.dp, Color(0xFF000000))
                        .clickable { showInfoDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GOT IT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(4.dp, RoundedCornerShape(0.dp))
            .clickable {
                android.util.Log.d("QuickActionCard", "Clicked: $title")
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color, RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}