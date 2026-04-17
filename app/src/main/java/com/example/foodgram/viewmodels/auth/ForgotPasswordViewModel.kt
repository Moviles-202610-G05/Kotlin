package com.example.foodgram.viewmodels.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var email by mutableStateOf("")
    
    var step by mutableStateOf(1)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var statusMessage by mutableStateOf<String?>(null)

    fun onEmailChange(newValue: String) {
        if (newValue.length <= 40) {
            email = newValue
            errorMessage = null
        } else {
            errorMessage = "Email is too long"
        }
    }

    fun sendRecoveryRequest() {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            errorMessage = "Please enter your email"
            return
        }

        isLoading = true
        errorMessage = null

        // First verify the email exists in our Firestore "user" collection
        db.collection("user")
            .whereEqualTo("email", cleanEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Fallback: Check in the "restaurants" collection or just allow Auth to handle it
                    // To be safe and user-friendly, if it's not in our "user" collection, 
                    // we still try sending it via Auth which will only work if the account exists there.
                    sendAuthResetEmail(cleanEmail)
                } else {
                    sendAuthResetEmail(cleanEmail)
                }
            }
            .addOnFailureListener { e ->
                // Even if Firestore fails, try sending the email via Auth
                sendAuthResetEmail(cleanEmail)
            }
    }

    private fun sendAuthResetEmail(cleanEmail: String) {
        auth.sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener {
                isLoading = false
                step = 2
                statusMessage = "Reset link sent! Please check your inbox and SPAM folder."
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Failed to send reset email"
            }
    }

    override fun onCleared() {
        super.onCleared()
    }
}
