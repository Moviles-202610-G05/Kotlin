package com.example.foodgram.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.foodgram.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore

class StudentRegisterViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var name by mutableStateOf("")
    var username by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var universityId by mutableStateOf("")
    
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

                val userData = hashMapOf(
                    "name" to name,
                    "username" to username,
                    "email" to email,
                    "password" to password,
                    "universityId" to universityId,
                    "roll" to "ESTUDIANTE",
                    "preferences" to selectedPreferences.toList(),
                    "carrier" to "",
                    "ordersCount" to 0,
                    "reviewsCount" to 0,
                    "savedCount" to 0
                )

        db.collection("user")
            .add(userData)
            .addOnSuccessListener {
                    documentReference ->
                val docId = documentReference.id
                UserSession.currentUserDocId = docId
                isLoading = false
                onSuccess()
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = exception.localizedMessage ?: "Registration failed"
            }
    }
}