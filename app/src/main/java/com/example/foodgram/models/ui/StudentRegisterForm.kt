package com.example.foodgram.models.ui

data class StudentRegisterForm(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val universityId: String = "",
    val selectedPreferences: List<String> = emptyList()
)
