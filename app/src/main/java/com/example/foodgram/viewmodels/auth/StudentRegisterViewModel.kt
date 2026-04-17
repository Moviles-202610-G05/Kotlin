package com.example.foodgram.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.foodgram.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentRegisterViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var name by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var universityId by mutableStateOf("")

    fun onNameChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != name) {
                name = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Name must be less than 25 characters"
        }
    }

    fun onUsernameChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != username) {
                username = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Username must be less than 25 characters"
        }
    }

    fun onEmailChange(newValue: String) {
        if (newValue.length < 35) {
            if (newValue != email) {
                email = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Email must be less than 35 characters"
        }
    }

    fun onPasswordChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != password) {
                password = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Password must be less than 25 characters"
        }
    }

    fun onUniversityIdChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            if (newValue.length < 9) {
                if (newValue != universityId) {
                    universityId = newValue
                    errorMessage = null
                }
            } else {
                errorMessage = "University ID must be less than 9 characters"
            }
        } else {
            errorMessage = "University ID must only contain numbers"
        }
    }
    
    // Preferences logic
    val selectedPreferences = mutableStateListOf<String>()
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
        if (selectedPreferences.contains(preference)) {
            selectedPreferences.remove(preference)
        } else {
            selectedPreferences.add(preference)
        }
    }

    fun register(onSuccess: () -> Unit) {
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank() || universityId.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        isLoading = true
        errorMessage = null

        // 1. Create user in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val userData = hashMapOf(
                        "name" to name,
                        "username" to username,
                        "email" to email,
                        "password" to password, // Still keeping it for now as per your request, though Auth handles it
                        "universityId" to universityId,
                        "roll" to "ESTUDIANTE",
                        "preferences" to selectedPreferences.toList(),
                        "carrier" to "",
                        "ordersCount" to 0,
                        "reviewsCount" to 0,
                        "savedCount" to 0,
                        "uid" to uid // Store the Auth UID for easier linking
                    )

                    // 2. Save additional data to Firestore
                    db.collection("user")
                        .add(userData)
                        .addOnSuccessListener { documentReference ->
                            UserSession.currentUserDocId = documentReference.id
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