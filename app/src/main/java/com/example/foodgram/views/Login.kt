package com.example.foodgram.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun HomeScreen(onNavigateToProfile: () -> Unit, onNavigateToMainFeed: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "LoginPage")
        Button(onClick = onNavigateToMainFeed) {
            Text(text = "Login")
        }
        Button(onClick = onNavigateToProfile) {
            Text(text = "Register")
        }
    }
}