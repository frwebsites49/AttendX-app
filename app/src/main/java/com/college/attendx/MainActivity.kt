package com.college.attendx

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.college.attendx.models.AttendanceSession
import com.college.attendx.models.UserRole
import com.college.attendx.repositories.FirebaseRepository
import com.college.attendx.screens.*
import com.college.attendx.ui.theme.AttendXTheme
import com.college.attendx.viewmodels.AuthState
import com.college.attendx.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.shadow

data class UserProfileData(
    val name: String = "",
    val rollNumber: String = "",
    val division: String = "",
    val group: String = "",
    val email: String = "",
    val isComplete: Boolean = false
)

enum class Screen {
    Home,
    Profile,
    Scan,
    Admin,
    CreateSession
}

class ActiveSessionHolder {
    var session by mutableStateOf<AttendanceSession?>(null)
    var qrBitmap by mutableStateOf<android.graphics.Bitmap?>(null)
    var liveCount by mutableStateOf(0)

    val isSessionActive: Boolean
        get() = session != null && session?.isActive == true

    fun clear() {
        session = null
        qrBitmap = null
        liveCount = 0
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val TAG = "MainActivity"

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        installSplashScreen()
        auth = Firebase.auth

        val firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        com.google.firebase.firestore.FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        enableEdgeToEdge()
        setContent {
            AttendXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        onGoogleSignIn = { startGoogleSignIn() },
                        onGoogleSignOut = {
                            googleSignInClient.signOut().addOnCompleteListener {
                            }
                        }
                    )
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        googleSignInClient.revokeAccess().addOnCompleteListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign in failed", e)
                val errorMessage = when (e.statusCode) {
                    10 -> "SHA-1 fingerprint mismatch. Please check Firebase configuration."
                    12500 -> "Google Sign-In failed. Please try again."
                    12501 -> "User cancelled sign-in."
                    12502 -> "Google account not found."
                    else -> "Sign in failed: ${e.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        if (account == null) {
            Toast.makeText(this, "Google account is null", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(
                        this,
                        "Welcome ${user?.displayName ?: "User"}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    authViewModel.forceRefreshRole()
                    authViewModel.checkAuthState()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    @Composable
    fun AppNavigation(
        authViewModel: AuthViewModel,
        onGoogleSignIn: () -> Unit,
        onGoogleSignOut: () -> Unit
    ) {
        val authState by authViewModel.authState.collectAsState()

        var showSplash by remember { mutableStateOf(true) }
        var currentScreen by remember { mutableStateOf(Screen.Home) }
        var userProfile by remember { mutableStateOf<UserProfileData?>(null) }
        var isSigningOut by remember { mutableStateOf(false) }

        val activeSessionHolder = remember { ActiveSessionHolder() }

        LaunchedEffect(authState) {
            if (authState is AuthState.Authenticated || authState is AuthState.NeedsProfile) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val repository = FirebaseRepository()
                    val result = repository.getUserProfile()
                    val savedProfile = result.getOrNull()

                    userProfile = if (savedProfile != null) {
                        UserProfileData(
                            name = savedProfile.name,
                            rollNumber = savedProfile.rollNumber,
                            division = savedProfile.division,
                            group = savedProfile.group,
                            email = currentUser.email ?: savedProfile.email,
                            isComplete = savedProfile.isProfileComplete
                        )
                    } else {
                        UserProfileData(
                            name = currentUser.displayName ?: currentUser.email?.split("@")?.firstOrNull() ?: "User",
                            email = currentUser.email ?: "",
                            isComplete = false
                        )
                    }
                }
            }
        }

        when {
            isSigningOut -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            showSplash -> {
                SplashScreenContent(
                    onAnimationComplete = {
                        showSplash = false
                    }
                )
            }
            authState is AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            authState is AuthState.Unauthenticated -> {
                LoginScreen(
                    onLoginSuccess = {
                        authViewModel.checkAuthState()
                    },
                    onGoogleSignIn = onGoogleSignIn
                )
            }
            authState is AuthState.NeedsProfile -> {
                MainAppScreen(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        currentScreen = screen
                    },
                    user = FirebaseAuth.getInstance().currentUser,
                    userProfile = userProfile,
                    onUpdateProfile = { profile ->
                        userProfile = profile
                        if (profile.isComplete) {
                            authViewModel.checkAuthState()
                        }
                    },
                    onLogout = {
                        isSigningOut = true
                        FirebaseAuth.getInstance().signOut()
                        onGoogleSignOut()
                        isSigningOut = false
                        userProfile = null
                        authViewModel.checkAuthState()
                    },
                    userRole = UserRole.STUDENT,
                    activeSessionHolder = activeSessionHolder
                )
            }
            authState is AuthState.Authenticated -> {
                val authenticatedState = authState as AuthState.Authenticated
                val role = authenticatedState.role
                MainAppScreen(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        currentScreen = screen
                    },
                    user = FirebaseAuth.getInstance().currentUser,
                    userProfile = userProfile,
                    onUpdateProfile = { profile ->
                        userProfile = profile
                    },
                    onLogout = {
                        isSigningOut = true
                        FirebaseAuth.getInstance().signOut()
                        onGoogleSignOut()
                        isSigningOut = false
                        userProfile = null
                        authViewModel.checkAuthState()
                    },
                    userRole = role,
                    activeSessionHolder = activeSessionHolder
                )
            }
            else -> {
                LoginScreen(
                    onLoginSuccess = {
                        authViewModel.checkAuthState()
                    },
                    onGoogleSignIn = onGoogleSignIn
                )
            }
        }
    }

    @Composable
    fun MainAppScreen(
        currentScreen: Screen,
        onNavigate: (Screen) -> Unit,
        user: FirebaseUser?,
        userProfile: UserProfileData?,
        onUpdateProfile: (UserProfileData) -> Unit,
        onLogout: () -> Unit,
        userRole: UserRole = UserRole.STUDENT,
        activeSessionHolder: ActiveSessionHolder
    ) {
        val context = LocalContext.current

        val userEmail = user?.email ?: ""
        val isAdmin = userEmail == "frcollection3025@gmail.com" ||
                userEmail == "lmnop@gmail.com" ||
                userRole == UserRole.ADMIN

        android.util.Log.d("MainAppScreen", "========== ADMIN CHECK ==========")
        android.util.Log.d("MainAppScreen", "User Email: $userEmail")
        android.util.Log.d("MainAppScreen", "userRole from param: $userRole")
        android.util.Log.d("MainAppScreen", "isAdmin (direct check): $isAdmin")
        android.util.Log.d("MainAppScreen", "===================================")

        val sessionLocked = activeSessionHolder.isSessionActive

        fun guardedNavigate(target: Screen) {
            if (sessionLocked && target != Screen.Admin && target != Screen.CreateSession) {
                Toast.makeText(
                    context,
                    "End the active session before leaving this screen.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            onNavigate(target)
        }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF1A1A1A),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .height(90.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .background(
                            Color(0xFF1A1A1A),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                ) {
                    val navItems = listOf(
                        NavItem("Home", R.drawable.ic_home, Screen.Home),
                        NavItem("Profile", R.drawable.ic_profile, Screen.Profile),
                        NavItem("Scan", R.drawable.ic_scan, Screen.Scan)
                    ) + if (isAdmin) {
                        listOf(NavItem("Admin", R.drawable.ic_admin, Screen.Admin))
                    } else {
                        emptyList()
                    }

                    navItems.forEach { item ->
                        val isSelected = when (item.screen) {
                            Screen.Home -> currentScreen == Screen.Home
                            Screen.Profile -> currentScreen == Screen.Profile
                            Screen.Scan -> currentScreen == Screen.Scan
                            Screen.Admin -> currentScreen == Screen.Admin || currentScreen == Screen.CreateSession
                            else -> false
                        }

                        val itemEnabled = !sessionLocked || item.screen == Screen.Admin

                        NavigationBarItem(
                            icon = {
                                AnimatedIcon(
                                    isSelected = isSelected,
                                    iconRes = item.iconRes,
                                    isScan = item.screen == Screen.Scan
                                )
                            },
                            label = {
                                AnimatedLabel(
                                    isSelected = isSelected,
                                    text = item.label
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (item.screen == Screen.Admin) {
                                    onNavigate(if (sessionLocked) Screen.CreateSession else Screen.Admin)
                                } else {
                                    guardedNavigate(item.screen)
                                }
                            },
                            enabled = itemEnabled,
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    Screen.Home -> {
                        HomeScreen(
                            user = user,
                            userProfile = userProfile,
                            onNavigateToProfile = { guardedNavigate(Screen.Profile) },
                            onNavigateToScan = { guardedNavigate(Screen.Scan) },
                            onNavigateToAdmin = {
                                android.util.Log.d("MainAppScreen", "Navigate to Admin clicked")
                                guardedNavigate(Screen.Admin)
                            },
                            onLogout = onLogout
                        )
                    }
                    Screen.Profile -> {
                        ProfileManagementScreen(
                            user = user,
                            userProfile = userProfile,
                            onUpdateProfile = onUpdateProfile,
                            onBack = { guardedNavigate(Screen.Home) }
                        )
                    }
                    Screen.Scan -> {
                        QRScannerScreen(
                            onBack = { guardedNavigate(Screen.Home) }
                        )
                    }
                    Screen.Admin -> {
                        android.util.Log.d("MainAppScreen", "RENDERING ADMIN SCREEN")
                        android.util.Log.d("MainAppScreen", "isAdmin value: $isAdmin")

                        if (isAdmin) {
                            AdminDashboardScreen(
                                onBack = {
                                    android.util.Log.d("MainAppScreen", "Admin - Back clicked")
                                    guardedNavigate(Screen.Home)
                                }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Not Admin", color = Color.White)
                            }
                        }
                    }
                    Screen.CreateSession -> {
                        android.util.Log.d("MainAppScreen", "RENDERING CREATE SESSION")
                        CreateSessionScreen(
                            activeSessionHolder = activeSessionHolder,
                            onBack = {
                                android.util.Log.d("MainAppScreen", "CreateSession - Back clicked")
                                onNavigate(Screen.Admin)
                            },
                            onSessionCreated = { sessionId ->
                                android.util.Log.d("MainAppScreen", "Session created: $sessionId")
                                Toast.makeText(context, "Session Created: $sessionId", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    data class NavItem(
        val label: String,
        val iconRes: Int,
        val screen: Screen
    )

    @Composable
    fun AnimatedIcon(
        isSelected: Boolean,
        iconRes: Int,
        isScan: Boolean = false
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.1f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        if (isScan) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35),
                                    Color(0xFFFF8A65)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF6B35).copy(alpha = 0.15f),
                                    Color(0xFFFF8A65).copy(alpha = 0.15f)
                                )
                            )
                        },
                        RoundedCornerShape(32.dp)
                    )
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color(0xFFFF6B35),
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFFF6B35) else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    @Composable
    fun AnimatedLabel(
        isSelected: Boolean,
        text: String
    ) {
        val alpha by animateFloatAsState(
            targetValue = if (isSelected) 1.0f else 0.5f,
            animationSpec = tween(durationMillis = 200)
        )

        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFFF6B35) else Color.White.copy(alpha = alpha)
        )
    }
}