package com.college.attendx.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp  // ✅ FIX: Changed from androidx.compute to androidx.compose
import com.college.attendx.models.UserProfile
import com.college.attendx.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onProfileComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var division by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    val academicYears = listOf(
        "2023-2024", "2024-2025", "2025-2026",
        "2026-2027", "2027-2028"
    )

    val courses = listOf(
        "Computer Science", "Information Technology", "Electronics",
        "Mechanical", "Civil", "Electrical", "Commerce", "Arts", "Science"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ ICON CHANGE: Added icon to header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                Icons.Filled.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Complete Your Profile",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Text(
            text = "Please fill in your details to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            placeholder = { Text("Enter your full name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Roll Number
        OutlinedTextField(
            value = rollNumber,
            onValueChange = { rollNumber = it },
            label = { Text("Roll Number") },
            placeholder = { Text("Enter your roll number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Division
        OutlinedTextField(
            value = division,
            onValueChange = { division = it },
            label = { Text("Division") },
            placeholder = { Text("e.g., A, B, C") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Group
        OutlinedTextField(
            value = group,
            onValueChange = { group = it },
            label = { Text("Group") },
            placeholder = { Text("e.g., 1, 2, 3") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mobile Number
        OutlinedTextField(
            value = mobileNumber,
            onValueChange = { mobileNumber = it },
            label = { Text("Mobile Number") },
            placeholder = { Text("Enter your mobile number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Academic Year Dropdown
        var expandedAcademicYear by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedAcademicYear,
            onExpandedChange = { expandedAcademicYear = it }
        ) {
            OutlinedTextField(
                value = academicYear,
                onValueChange = {},
                readOnly = true,
                label = { Text("Academic Year") },
                placeholder = { Text("Select academic year") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAcademicYear) }
            )
            ExposedDropdownMenu(
                expanded = expandedAcademicYear,
                onDismissRequest = { expandedAcademicYear = false }
            ) {
                academicYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year) },
                        onClick = {
                            academicYear = year
                            expandedAcademicYear = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Course Dropdown
        var expandedCourse by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedCourse,
            onExpandedChange = { expandedCourse = it }
        ) {
            OutlinedTextField(
                value = course,
                onValueChange = {},
                readOnly = true,
                label = { Text("Course") },
                placeholder = { Text("Select your course") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) }
            )
            ExposedDropdownMenu(
                expanded = expandedCourse,
                onDismissRequest = { expandedCourse = false }
            ) {
                courses.forEach { courseOption ->
                    DropdownMenuItem(
                        text = { Text(courseOption) },
                        onClick = {
                            course = courseOption
                            expandedCourse = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Submit Button with icon
        Button(
            onClick = {
                if (validateFields(name, division, group, rollNumber, mobileNumber, academicYear, course)) {
                    isLoading = true
                    errorMessage = null

                    val profile = UserProfile(
                        name = name,
                        division = division,
                        group = group,
                        rollNumber = rollNumber,
                        mobileNumber = mobileNumber,
                        academicYear = academicYear,
                        course = course
                    )

                    authViewModel.saveUserProfile(profile) { success, error ->
                        isLoading = false
                        if (success) {
                            onProfileComplete()
                        } else {
                            errorMessage = error ?: "Failed to save profile"
                        }
                    }
                } else {
                    errorMessage = "Please fill in all fields"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    Icons.Filled.Save,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save & Continue")
            }
        }
    }
}

private fun validateFields(
    name: String,
    division: String,
    group: String,
    rollNumber: String,
    mobileNumber: String,
    academicYear: String,
    course: String
): Boolean {
    return name.isNotBlank() &&
            division.isNotBlank() &&
            group.isNotBlank() &&
            rollNumber.isNotBlank() &&
            mobileNumber.isNotBlank() &&
            academicYear.isNotBlank() &&
            course.isNotBlank()
}