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
import com.example.foodgram.navigation.*
import com.example.foodgram.ui.theme.FoodGramTheme
import com.example.foodgram.views.feed.HomeScreen
import com.example.foodgram.views.auth.LoginScreen
// Import other screens as needed
// import com.example.foodgram.views.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodGramTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Home,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<Home> {
                            HomeScreen(
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToMenu = { navController.navigate(Menu) },
                                onNavigateToMap = { navController.navigate(Map) }
                            )
                        }
                        
                        // Add other destinations here as you create them
                        composable<Profile> { 
                            // ProfileScreen(onBack = { navController.popBackStack() }) 
                        }
                        composable<Search> { /* SearchScreen() */ }
                        composable<Menu> { /* MenuScreen() */ }
                        //composable<Map> { /* MapScreen() */ }
                    }
                }
            }
        }
    }
}
