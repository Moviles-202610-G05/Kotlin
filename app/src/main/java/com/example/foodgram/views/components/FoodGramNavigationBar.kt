package com.example.foodgram.views.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.foodgram.utils.UserSession

enum class FoodGramScreen {
    FEED, SEARCH, PROFILE, MENU, MAP
}

@Composable
fun FoodGramNavigationBar(
    currentScreen: FoodGramScreen,
    onNavigateToFeed: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val isRestaurante = UserSession.currentUserRole == "RESTAURANTE"

    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = currentScreen == FoodGramScreen.FEED,
            onClick = onNavigateToFeed,
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Feed") },
            label = { Text("FEED") }
        )
        NavigationBarItem(
            selected = currentScreen == FoodGramScreen.SEARCH,
            onClick = onNavigateToSearch,
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("SEARCH") }
        )
        NavigationBarItem(
            selected = currentScreen == FoodGramScreen.PROFILE,
            onClick = onNavigateToProfile,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("PROFILE") }
        )
        NavigationBarItem(
            selected = currentScreen == FoodGramScreen.MENU,
            onClick = onNavigateToMenu,
            icon = {
                Icon(
                    if (isRestaurante) Icons.Default.Dashboard else Icons.Default.RestaurantMenu,
                    contentDescription = if (isRestaurante) "Dashboard" else "Menu"
                )
            },
            label = { Text(if (isRestaurante) "DASHBOARD" else "MENU") }
        )
        NavigationBarItem(
            selected = currentScreen == FoodGramScreen.MAP,
            onClick = onNavigateToMap,
            icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
            label = { Text("MAP") }
        )
    }
}
