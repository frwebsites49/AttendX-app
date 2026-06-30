package com.college.attendx.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.college.attendx.UserProfileData
import com.college.attendx.models.UserProfile
import com.college.attendx.repositories.FirebaseRepository
import com.college.attendx.utils.SecurityUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun ProfileManagementScreen(
    user: FirebaseUser?,
    userProfile: UserProfileData?,
    onUpdateProfile: (UserProfileData) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { FirebaseRepository() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(userProfile?.name ?: "") }
    var rollNumber by remember { mutableStateOf(userProfile?.rollNumber ?: "") }
    var division by remember { mutableStateOf(userProfile?.division ?: "") }
    var group by remember { mutableStateOf(userProfile?.group ?: "") }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isProfileComplete = userProfile?.isComplete == true

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
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF1A1A1A),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PROFILE",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1A1A)
                    )
                }
                Row(
                    modifier = Modifier.clickable { onBack() },
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
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(Color(0xFFFF6B35))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profile status with icon
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfileComplete) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isProfileComplete) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (isProfileComplete) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProfileComplete) "Profile Complete" else "Profile Incomplete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isProfileComplete) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFE8E8)
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = Color(0xFFFF0000),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it,
                            color = Color(0xFFFF0000),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isEditing) {
                // View Mode
                ProfileInfoRow(label = "Name", value = userProfile?.name ?: "Not set")
                ProfileInfoRow(label = "Roll Number", value = userProfile?.rollNumber ?: "Not set")
                ProfileInfoRow(label = "Division", value = userProfile?.division ?: "Not set")
                ProfileInfoRow(label = "Group", value = userProfile?.group ?: "Not set")
                ProfileInfoRow(label = "Email", value = user?.email ?: "Not set")

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isEditing = true
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProfileComplete) "EDIT PROFILE" else "COMPLETE PROFILE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // Edit Mode
                Text(
                    text = "Edit Your Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // ✅ SECURITY: Input fields with validation hints
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = Color(0xFF1A1A1A)) },
                    placeholder = { Text("Enter your full name", color = Color(0xFF999999)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B35),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFFFF6B35),
                        unfocusedLabelColor = Color(0xFF666666),
                        cursorColor = Color(0xFFFF6B35),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedPlaceholderColor = Color(0xFF999999),
                        unfocusedPlaceholderColor = Color(0xFF999999)
                    ),
                    shape = RoundedCornerShape(0.dp),
                    supportingText = {
                        Text(
                            text = "Enter your full name as per college records",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it },
                    label = { Text("Roll Number", color = Color(0xFF1A1A1A)) },
                    placeholder = { Text("Enter your roll number", color = Color(0xFF999999)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B35),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFFFF6B35),
                        unfocusedLabelColor = Color(0xFF666666),
                        cursorColor = Color(0xFFFF6B35),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedPlaceholderColor = Color(0xFF999999),
                        unfocusedPlaceholderColor = Color(0xFF999999)
                    ),
                    shape = RoundedCornerShape(0.dp),
                    supportingText = {
                        Text(
                            text = "Format: 2-10 alphanumeric characters (e.g., CS101)",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = division,
                    onValueChange = { division = it.uppercase() },
                    label = { Text("Division", color = Color(0xFF1A1A1A)) },
                    placeholder = { Text("e.g., A, B, C", color = Color(0xFF999999)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B35),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFFFF6B35),
                        unfocusedLabelColor = Color(0xFF666666),
                        cursorColor = Color(0xFFFF6B35),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedPlaceholderColor = Color(0xFF999999),
                        unfocusedPlaceholderColor = Color(0xFF999999)
                    ),
                    shape = RoundedCornerShape(0.dp),
                    supportingText = {
                        Text(
                            text = "Single letter (e.g., A, B, C)",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it.uppercase() },
                    label = { Text("Group", color = Color(0xFF1A1A1A)) },
                    placeholder = { Text("e.g., G1, G2, G3", color = Color(0xFF999999)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6B35),
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedLabelColor = Color(0xFFFF6B35),
                        unfocusedLabelColor = Color(0xFF666666),
                        cursorColor = Color(0xFFFF6B35),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A),
                        focusedPlaceholderColor = Color(0xFF999999),
                        unfocusedPlaceholderColor = Color(0xFF999999)
                    ),
                    shape = RoundedCornerShape(0.dp),
                    supportingText = {
                        Text(
                            text = "1-4 alphanumeric characters (e.g., G1)",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // ✅ SECURITY: Validate input before saving
                            val validationResult = SecurityUtils.validateUserProfile(
                                UserProfile(
                                    name = name,
                                    rollNumber = rollNumber,
                                    division = division,
                                    group = group
                                )
                            )

                            if (!validationResult.isValid) {
                                errorMessage = validationResult.message
                                return@Button
                            }

                            if (name.isBlank() || rollNumber.isBlank() || division.isBlank() || group.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            errorMessage = null
                            isSaving = true

                            scope.launch {
                                val profileToSave = UserProfile(
                                    name = name,
                                    rollNumber = rollNumber,
                                    division = division,
                                    group = group
                                )

                                val result = repository.saveUserProfile(profileToSave)
                                isSaving = false

                                result.onSuccess {
                                    val updatedProfile = UserProfileData(
                                        name = name,
                                        rollNumber = rollNumber,
                                        division = division,
                                        group = group,
                                        email = user?.email ?: "",
                                        isComplete = true
                                    )
                                    onUpdateProfile(updatedProfile)
                                    isEditing = false
                                }.onFailure { e ->
                                    errorMessage = e.message ?: "Failed to save profile. Please try again."
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B35)
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Save,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            isEditing = false
                            errorMessage = null
                            name = userProfile?.name ?: ""
                            rollNumber = userProfile?.rollNumber ?: ""
                            division = userProfile?.division ?: ""
                            group = userProfile?.group ?: ""
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEEEEEE)
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = null,
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CANCEL", color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All fields are required",
                    color = Color(0xFF999999),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF666666),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value.ifEmpty { "Not set" },
            color = if (value.isNotEmpty()) Color(0xFF1A1A1A) else Color(0xFF999999),
            fontSize = 14.sp,
            fontWeight = if (value.isNotEmpty()) FontWeight.Medium else FontWeight.Normal
        )
    }
}