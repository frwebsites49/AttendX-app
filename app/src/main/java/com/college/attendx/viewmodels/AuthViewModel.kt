package com.college.attendx.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.college.attendx.models.UserProfile
import com.college.attendx.models.UserRole
import com.college.attendx.repositories.FirebaseRepository
import com.college.attendx.utils.AdminConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _userRole = MutableStateFlow<UserRole>(UserRole.STUDENT)
    val userRole: StateFlow<UserRole> = _userRole

    init {
        checkAuthState()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val result = repository.getUserProfile()
                if (result.isSuccess) {
                    val profile = result.getOrNull()
                    android.util.Log.d("AuthViewModel", "Profile loaded: ${profile?.name}")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error loading profile: ${e.message}")
            }
        }
    }

    /**
     * FIX: this used to ONLY check AdminConfig's hardcoded email list -
     * the Firestore "admins" collection dynamic check (already written
     * in FirebaseRepository.getUserRole()) was never actually being
     * called from here. That meant adding a UID to Firestore's admins
     * collection had zero effect; only editing AdminConfig.kt and
     * rebuilding the app could grant admin.
     *
     * Now this calls repository.getUserRole(), which checks BOTH:
     *   1. AdminConfig's static list (still works, for convenience)
     *   2. Firestore /admins/{uid} (the dynamic, no-rebuild-needed way)
     * Either one grants admin - see FirebaseRepository.kt for the order
     * (static list checked first, then falls through to Firestore).
     */
    fun checkAuthState() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    android.util.Log.d("AuthViewModel", "=== USER LOGGED IN ===")
                    android.util.Log.d("AuthViewModel", "Email: ${currentUser.email}")
                    android.util.Log.d("AuthViewModel", "UID: ${currentUser.uid}")

                    val role = repository.getUserRole()
                    android.util.Log.d("AuthViewModel", "Role determined (static + Firestore check): $role")
                    _userRole.value = role

                    val isComplete = repository.isProfileComplete()
                    android.util.Log.d("AuthViewModel", "Profile complete: $isComplete")

                    // Admins skip the profile-completeness gate entirely -
                    // a teacher doesn't fill in roll number/division/group,
                    // that data is student-only.
                    if (role == UserRole.ADMIN || isComplete) {
                        _authState.value = AuthState.Authenticated(role)
                        android.util.Log.d("AuthViewModel", "Auth state: Authenticated with role $role")
                    } else {
                        _authState.value = AuthState.NeedsProfile
                        android.util.Log.d("AuthViewModel", "Auth state: NeedsProfile")
                    }
                } else {
                    android.util.Log.d("AuthViewModel", "No user logged in")
                    _authState.value = AuthState.Unauthenticated
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error: ${e.message}")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Same fix applied here - forceRefreshRole() (called right after a
     * fresh sign-in) now also goes through the real dynamic check
     * instead of only AdminConfig.
     */
    fun forceRefreshRole() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    android.util.Log.d("AuthViewModel", "Force refreshing role for: ${currentUser.email}")

                    val role = repository.getUserRole()
                    android.util.Log.d("AuthViewModel", "Role from dynamic check: $role")
                    _userRole.value = role

                    _authState.value = AuthState.Authenticated(role)
                    android.util.Log.d("AuthViewModel", "Role forced to: $role")
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Error forcing role refresh: ${e.message}")
            }
        }
    }

    fun saveUserProfile(profile: UserProfile, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            _errorMessage.value = null

            try {
                val result = repository.saveUserProfile(profile)
                if (result.isSuccess) {
                    _profileState.value = ProfileState.Success
                    checkAuthState()
                    onComplete(true, null)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to save profile"
                    _profileState.value = ProfileState.Error(error)
                    _errorMessage.value = error
                    onComplete(false, error)
                }
            } catch (e: Exception) {
                val error = e.message ?: "Unknown error occurred"
                _profileState.value = ProfileState.Error(error)
                _errorMessage.value = error
                onComplete(false, error)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
            _userRole.value = UserRole.STUDENT
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object NeedsProfile : AuthState()
    data class Authenticated(val role: UserRole) : AuthState()
}

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}