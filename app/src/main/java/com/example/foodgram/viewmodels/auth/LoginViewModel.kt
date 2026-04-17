package com.example.foodgram.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.foodgram.models.ui.LoginForm
import com.example.foodgram.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var form by mutableStateOf(LoginForm())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChange(newValue: String) {
        if (newValue.length < 30) {
            form = form.copy(email = newValue)
            errorMessage = null
        } else {
            errorMessage = "Email must be less than 30 characters"
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

    fun login(onSuccess: () -> Unit) {
        if (form.email.isBlank() || form.password.isBlank()) {
            errorMessage = "Email and password cannot be empty"
            return
        }

        isLoading = true
        errorMessage = null

        auth.signInWithEmailAndPassword(form.email, form.password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    // Search for the firestore document by UID first, fallback to Email
                    db.collection("user")
                        .whereEqualTo("uid", uid)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val userDoc = documents.documents[0]
                                UserSession.currentUserDocId = userDoc.id
                                isLoading = false
                                onSuccess()
                            } else {
                                // Fallback to email search if UID isn't set yet (for old accounts)
                                db.collection("user")
                                    .whereEqualTo("email", form.email)
                                    .get()
                                    .addOnSuccessListener { emailDocs ->
                                        if (!emailDocs.isEmpty) {
                                            val userDoc = emailDocs.documents[0]
                                            
                                            // Update the document with the UID for future logins
                                            db.collection("user").document(userDoc.id)
                                                .update("uid", uid)
                                            
                                            UserSession.currentUserDocId = userDoc.id
                                            isLoading = false
                                            onSuccess()
                                        } else {
                                            isLoading = false
                                            errorMessage = "User record not found"
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = "Error fetching profile"
                        }
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = when (exception.message) {
                    null -> "Authentication failed"
                    else -> if (exception.message!!.contains("password")) "Incorrect password"
                           else if (exception.message!!.contains("no user")) "User not found"
                           else exception.localizedMessage
                }
            }
    }
}
