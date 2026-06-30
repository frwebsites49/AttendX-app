package com.college.attendx.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }

    // Animation
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }

    // Check if user is already logged in
    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            isLoggedIn = true
            onLoginSuccess()
        }
    }

    // Colors
    val brutalYellow = Color(0xFFFFD500)
    val brutalBlack = Color(0xFF000000)
    val brutalBg = Color(0xFF121212)
    val brutalBorder = Color(0xFF2A2A2A)
    val brutalGray = Color(0xFF9E9E9E)
    val brutalRed = Color(0xFFFF3B30)
    val brutalGreen = Color(0xFF34C759)

    // Validation functions
    fun validateEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 6
    }

    fun submit() {
        keyboardController?.hide()

        // Validation
        when {
            email.isBlank() || password.isBlank() || (isRegistering && confirmPassword.isBlank()) -> {
                errorMessage = "Please fill in all fields"
                return
            }
            !validateEmail(email) -> {
                errorMessage = "Please enter a valid email address"
                return
            }
            isRegistering && !validatePassword(password) -> {
                errorMessage = "Password must be at least 6 characters"
                return
            }
            isRegistering && password != confirmPassword -> {
                errorMessage = "Passwords do not match"
                return
            }
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        val auth = Firebase.auth
        if (isRegistering) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        successMessage = "Account created successfully!"
                        Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()

                        // Send email verification
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Verification email sent! Please verify your email.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // Sign out until email is verified
                                    auth.signOut()

                                    // Switch to login mode
                                    isRegistering = false
                                    password = ""
                                    confirmPassword = ""

                                    // Show success message
                                    successMessage = "Verification email sent! Please check your inbox."
                                }
                            }
                        // Don't call onLoginSuccess() here - wait for email verification
                    } else {
                        errorMessage = task.exception?.message?.let {
                            when {
                                it.contains("email already in use") -> "Email already registered"
                                it.contains("invalid email") -> "Invalid email address"
                                it.contains("network") -> "Network error. Please check your connection."
                                else -> it
                            }
                        } ?: "Registration failed"
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user?.isEmailVerified == true) {
                            // User is verified - proceed to home screen
                            successMessage = "Welcome back!"
                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()

                            // Navigate to home screen
                            isLoggedIn = true
                            onLoginSuccess()
                        } else {
                            // Email not verified
                            errorMessage = "Please verify your email before logging in"
                            Toast.makeText(context, "Please verify your email first!", Toast.LENGTH_LONG).show()
                            auth.signOut()

                            // Resend verification email
                            user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "New verification email sent!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        errorMessage = task.exception?.message?.let {
                            when {
                                it.contains("user not found") -> "No account found with this email"
                                it.contains("wrong password") -> "Incorrect password"
                                it.contains("too many requests") -> "Too many attempts. Try again later"
                                it.contains("network") -> "Network error. Please check your connection."
                                else -> it
                            }
                        } ?: "Login failed"
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brutalBg)
            .clickable { keyboardController?.hide() }
    ) {
        AnimatedVisibility(
            visible = isVisible && !isLoggedIn,
            enter = fadeIn(animationSpec = tween(350)) +
                    slideInVertically(
                        initialOffsetY = { it / 6 },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // Header
                Text(
                    text = if (isRegistering) "CREATE ACCOUNT" else "WELCOME BACK",
                    color = brutalYellow,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 8.dp)
                        .width(50.dp)
                        .height(4.dp)
                        .background(brutalYellow)
                )
                Text(
                    text = if (isRegistering) "Sign up to get started" else "Sign in to continue",
                    color = brutalGray,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Email field
                BrutalistTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                        successMessage = null
                    },
                    placeholder = "Email",
                    leadingIcon = Icons.Filled.Email,
                    accentColor = brutalYellow,
                    borderColor = brutalBorder,
                    onEnterKey = { submit() }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password field
                BrutalistTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                        successMessage = null
                    },
                    placeholder = "Password",
                    leadingIcon = Icons.Filled.Lock,
                    accentColor = brutalYellow,
                    borderColor = brutalBorder,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                    onEnterKey = { submit() }
                )

                // Confirm password field (register mode)
                AnimatedVisibility(visible = isRegistering) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        BrutalistTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                                successMessage = null
                            },
                            placeholder = "Confirm Password",
                            leadingIcon = Icons.Filled.Lock,
                            accentColor = brutalYellow,
                            borderColor = brutalBorder,
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onTogglePasswordVisible = { confirmPasswordVisible = !confirmPasswordVisible },
                            onEnterKey = { submit() }
                        )
                    }
                }

                // Error/Success messages
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = it,
                        color = brutalRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                successMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = it,
                        color = brutalGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Login/Register button
                BrutalistButton(
                    text = if (isRegistering) "CREATE ACCOUNT" else "SIGN IN",
                    onClick = { submit() },
                    isLoading = isLoading,
                    backgroundColor = brutalYellow,
                    textColor = brutalBlack
                )

                Spacer(modifier = Modifier.height(12.dp))



                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(brutalBorder))
                    Text(
                        text = "  OR  ",
                        color = brutalGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(brutalBorder))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google Sign In button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onGoogleSignIn()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(2.dp, Color.Black),
                    enabled = !isLoading
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "CONTINUE WITH GOOGLE",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Toggle between login/register
                Text(
                    text = if (isRegistering)
                        "Already have an account? Sign In"
                    else
                        "Don't have an account? Create one",
                    color = brutalGray,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .clickable {
                            isRegistering = !isRegistering
                            errorMessage = null
                            successMessage = null
                            password = ""
                            confirmPassword = ""
                            passwordVisible = false
                            confirmPasswordVisible = false
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    accentColor: Color,
    borderColor: Color,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisible: (() -> Unit)? = null,
    onEnterKey: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val currentBorderColor = if (isFocused) accentColor else borderColor
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black)
            .border(BorderStroke(2.dp, currentBorderColor))
            .clickable {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isFocused) accentColor else Color(0xFF9E9E9E),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                interactionSource = interactionSource,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp
                ),
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                cursorBrush = SolidColor(accentColor),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = {
                        onEnterKey?.invoke()
                    }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color(0xFF9E9E9E),
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Clear button
            AnimatedVisibility(
                visible = value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onValueChange("") }
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (isPassword && onTogglePasswordVisible != null) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onTogglePasswordVisible() }
                )
            }
        }
    }
}

@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean,
    backgroundColor: Color,
    textColor: Color,
    leadingContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 4.dp, top = 4.dp)
                .background(Color(0xFF2A2A2A))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(end = 4.dp, bottom = 4.dp)
                .background(backgroundColor)
                .border(BorderStroke(2.dp, Color.Black))
                .clickable(enabled = !isLoading) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = textColor,
                    strokeWidth = 2.dp
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingContent?.invoke()
                    if (leadingContent != null) Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = text,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}