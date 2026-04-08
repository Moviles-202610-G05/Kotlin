package com.example.foodgram.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.foodgram.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newValue: String) {
        email = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun login(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty"
            return
        }

        isLoading = true
        errorMessage = null
        db.collection("user")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    isLoading = false
                    errorMessage = "User not found"
                } else {
                    val userDoc = documents.documents[0]
                    val dbPassword = userDoc.getString("password")
                    
                    if (dbPassword == password) {
                        UserSession.currentUserDocId = userDoc.id
                        isLoading = false
                        onSuccess()
                    } else {
                        isLoading = false
                        errorMessage = "Incorrect password"
                    }
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = exception.localizedMessage ?: "Connection error"
            }
    }
}