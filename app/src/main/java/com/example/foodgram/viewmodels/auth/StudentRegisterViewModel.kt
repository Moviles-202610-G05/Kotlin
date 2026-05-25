package com.example.foodgram.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.foodgram.models.ui.StudentRegisterForm
import com.example.foodgram.models.auth.User
import com.example.foodgram.models.auth.UserRole
import com.example.foodgram.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentRegisterViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var form by mutableStateOf(StudentRegisterForm())
        private set

    fun onNameChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(name = newValue)
            errorMessage = null
        } else {
            errorMessage = "Name must be less than 25 characters"
        }
    }

    fun onUsernameChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(username = newValue)
            errorMessage = null
        } else {
            errorMessage = "Username must be less than 25 characters"
        }
    }

    fun onEmailChange(newValue: String) {
        if (newValue.length < 35) {
            form = form.copy(email = newValue)
            errorMessage = null
        } else {
            errorMessage = "Email must be less than 35 characters"
        }
    }

    fun onPasswordChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(password = newValue)
            errorMessage = null
        } else {
            errorMessage = "Password must be less than 25 characters"
        }
    }

    fun onUniversityIdChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            if (newValue.length < 9) {
                form = form.copy(universityId = newValue)
                errorMessage = null
            } else {
                errorMessage = "University ID must be less than 9 characters"
            }
        } else {
            errorMessage = "University ID must only contain numbers"
        }
    }

    val availablePreferences = listOf(
        "Vegan", "Fast Food", "Keto", "Gluten-Free",
        "Vegetarian", "Desserts", "Healthy", "Pizza",
        "Sushi", "Burgers", "Mexican", "Italian"
    )

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun togglePreference(preference: String) {
        val currentPrefs = form.selectedPreferences.toMutableList()
        if (currentPrefs.contains(preference)) {
            currentPrefs.remove(preference)
        } else {
            currentPrefs.add(preference)
        }
        form = form.copy(selectedPreferences = currentPrefs)
    }

    fun register(onSuccess: () -> Unit) {
        if (form.name.isBlank() || form.username.isBlank() || form.email.isBlank() || form.password.isBlank() || form.universityId.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        isLoading = true
        errorMessage = null

        auth.createUserWithEmailAndPassword(form.email, form.password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val user = User(
                        uid = uid,
                        name = form.name,
                        username = form.username,
                        email = form.email,
                        roll = UserRole.ESTUDIANTE,
                        universityId = form.universityId,
                        preferences = form.selectedPreferences
                    )

                    db.collection("user")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            // Update all session variables locally to bypass needing a fresh login
                            UserSession.currentUserDocId = documentReference.id
                            UserSession.currentUserRole = UserRole.ESTUDIANTE.name
                            UserSession.currentUserName = form.name

                            isLoading = false
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            errorMessage = "Auth successful but database failed: ${exception.localizedMessage}"
                        }
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = exception.localizedMessage ?: "Registration failed"
            }
    }
}