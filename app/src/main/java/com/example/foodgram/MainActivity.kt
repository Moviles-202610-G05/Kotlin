package com.example.foodgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Import your own files
import com.example.foodgram.navigation.Home
import com.example.foodgram.navigation.Profile
import com.example.foodgram.navigation.MainFeed
import com.example.foodgram.ui.theme.FoodGramTheme
import com.example.foodgram.views.HomeScreen
import com.example.foodgram.views.ProfileScreen
import com.example.foodgram.views.MainFeedScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodGramTheme {
                // 1. Create the controller
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 2. Set up the NavHost (The Container)
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 3. Define the destinations
                        composable<Home> {
                            HomeScreen(onNavigateToProfile = { navController.navigate(Profile) }, onNavigateToMainFeed = { navController.navigate(MainFeed) })
                        }
                        composable<MainFeed> {
                            MainFeedScreen()
                        }
                        composable<Profile> {
                            ProfileScreen(onBack = { navController.popBackStack() })
                        }

                    }
                }
            }
        }
    }
}