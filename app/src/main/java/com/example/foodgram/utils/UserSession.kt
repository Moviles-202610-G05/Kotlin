package com.example.foodgram.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

object UserSession {
    private const val PREFS_NAME = "FoodGramPrefs"
    private const val KEY_DOC_ID = "userDocId"
    private const val KEY_PROFILE_PHOTO_PATH = "profilePhotoPath"
    private const val KEY_PROFILE_PHOTO_URL = "profilePhotoUrl"

    private var sharedPrefs: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    var currentUserDocId: String?
        get() = sharedPrefs?.getString(KEY_DOC_ID, null)
        set(value) {
            sharedPrefs?.edit()?.putString(KEY_DOC_ID, value)?.apply()
        }

    var currentProfilePhotoPath: String?
        get() = sharedPrefs?.getString(KEY_PROFILE_PHOTO_PATH, null)
        set(value) {
            sharedPrefs?.edit()?.putString(KEY_PROFILE_PHOTO_PATH, value)?.apply()
        }

    var currentProfilePhotoUrl: String?
        get() = sharedPrefs?.getString(KEY_PROFILE_PHOTO_URL, null)
        set(value) {
            sharedPrefs?.edit()?.putString(KEY_PROFILE_PHOTO_URL, value)?.apply()
        }

    val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    val currentUserEmail: String?
        get() = FirebaseAuth.getInstance().currentUser?.email

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        currentUserDocId = null
        currentProfilePhotoPath = null
        currentProfilePhotoUrl = null
    }
}