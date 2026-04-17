package com.example.foodgram.utils

import com.google.firebase.auth.FirebaseAuth

object UserSession {
    var currentUserDocId: String? = null
    
    val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        currentUserDocId = null
    }
}