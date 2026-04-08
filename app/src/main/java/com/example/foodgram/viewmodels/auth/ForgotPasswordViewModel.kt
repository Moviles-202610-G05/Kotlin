package com.example.foodgram.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Random

class ForgotPasswordViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    var email by mutableStateOf("")
    var recoveryCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    
    var step by mutableStateOf(1) // 1: Email, 2: Code, 3: New Password
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun sendRecoveryRequest() {
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return
        }

        isLoading = true
        errorMessage = null

        // Check if user exists first
        db.collection("user")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    isLoading = false
                    errorMessage = "No account found with this email"
                } else {
                    // EVENT-DRIVEN TRIGGER: Create a document in 'password_resets'
                    // In a real app, a Cloud Function would pick this up and send the email.
                    val generatedCode = (100000..999999).random().toString() 
                    
                    val resetRequest = hashMapOf(
                        "email" to email,
                        "code" to generatedCode,
                        "timestamp" to System.currentTimeMillis(),
                        "status" to "pending"
                    )

                    db.collection("password_resets").document(email).set(resetRequest)
                        .addOnSuccessListener {
                            isLoading = false
                            step = 2 // Move to enter code screen
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = "Error sending request: ${it.localizedMessage}"
                        }
                }
            }
    }

    fun verifyCode() {
        isLoading = true
        db.collection("password_resets").document(email).get()
            .addOnSuccessListener { doc ->
                val serverCode = doc.getString("code")
                if (serverCode == recoveryCode) {
                    isLoading = false
                    step = 3 // Move to new password screen
                } else {
                    isLoading = false
                    errorMessage = "Invalid recovery code"
                }
            }
    }

    fun updatePassword(onSuccess: () -> Unit) {
        isLoading = true
        // Find the user document to update
        db.collection("user").whereEqualTo("email", email).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocId = documents.documents[0].id
                    db.collection("user").document(userDocId)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            // Clean up the reset request
                            db.collection("password_resets").document(email).delete()
                            isLoading = false
                            onSuccess()
                        }
                }
            }
    }
}
