package com.example.foodgram.viewmodels.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ForgotPasswordViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var statusListener: ListenerRegistration? = null

    var email by mutableStateOf("")
    var recoveryCode by mutableStateOf("")
    var newPassword by mutableStateOf("")
    
    var step by mutableStateOf(1)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var statusMessage by mutableStateOf<String?>(null)

    fun sendRecoveryRequest() {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            errorMessage = "Please enter your email"
            return
        }

        isLoading = true
        errorMessage = null
        statusMessage = "Verifying email..."

        db.collection("user")
            .whereEqualTo("email", cleanEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    isLoading = false
                    errorMessage = "No account found with this email"
                } else {
                    statusMessage = "Requesting code..."
                    val generatedCode = (100000..999999).random().toString() 
                    
                    val resetRequest = hashMapOf(
                        "email" to cleanEmail,
                        "code" to generatedCode,
                        "timestamp" to System.currentTimeMillis(),
                        "status" to "requesting"
                    )

                    // Aca creamos un documento en el cual se guarda el codigo de verificacion temporalmente
                    db.collection("password_resets")
                        .add(resetRequest)
                        .addOnSuccessListener { docRef ->
                            isLoading = false
                            step = 2
                            statusMessage = "Waiting for server to send email..."
                            startStatusListener(docRef.id)
                        }
                        .addOnFailureListener { e ->
                            //Logs hechos con IA, no estaba funcionando el codigo y se debugeo con IA
                            isLoading = false
                            errorMessage = "Error: ${e.localizedMessage}"
                        }
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = "Connection error: ${e.localizedMessage}"
            }
    }

    private fun startStatusListener(docId: String) {
        statusListener?.remove()
        statusListener = db.collection("password_resets").document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                val status = snapshot?.getString("status")
                Log.d("ForgotPassword", "Event Status: $status")
                
                when (status) {
                    "sent" -> {
                        statusMessage = "Code sent! Check your inbox."
                        errorMessage = null
                    }
                    "error" -> {
                        errorMessage = "Server failed to send email. Check function logs."
                        statusMessage = null
                    }
                }
            }
    }

    fun verifyCode() {
        if (recoveryCode.isBlank()) return
        isLoading = true
        errorMessage = null // Clear error when retrying
        val cleanEmail = email.trim().lowercase()
        
        // Codigo para buscar el request mas reciente en los request de reset password
        db.collection("password_resets")
            .whereEqualTo("email", cleanEmail)
            .whereEqualTo("code", recoveryCode)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    isLoading = false
                    errorMessage = null // Si escribimos el mensaje mal, y luego bien, limpiamos el mensaje de error en la pantalla reset password
                    step = 3
                    statusListener?.remove()
                } else {
                    isLoading = false
                    errorMessage = "Invalid code. Please try again."
                }
            }
    }

    fun updatePassword(onSuccess: () -> Unit) {
        if (newPassword.length < 6) {
            errorMessage = "Password is too short"
            return
        }
        isLoading = true
        errorMessage = null
        val cleanEmail = email.trim().lowercase()
        //Borramos si se autenticó correctamente para no llenarnos de codigos en la bd
        db.collection("user").whereEqualTo("email", cleanEmail).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userDocId = documents.documents[0].id
                    db.collection("user").document(userDocId)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            // Clean up reset requests for this email
                            db.collection("password_resets")
                                .whereEqualTo("email", cleanEmail)
                                .get()
                                .addOnSuccessListener { requests ->
                                    for (doc in requests) doc.reference.delete()
                                }
                            isLoading = false
                            onSuccess()
                        }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        statusListener?.remove()
    }
}
