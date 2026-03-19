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
import com.example.foodgram.views.restaurants.MapScreen
import com.example.foodgram.views.restaurants.SearchRestaurantsScreen

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
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMenu = { navController.navigate(Menu) },
                                onNavigateToMap = { navController.navigate(com.example.foodgram.navigation.Map) }
                            )
                        }
                        
                        composable<Search> {
                            SearchRestaurantsScreen(
                                onNavigateToFeed = { navController.navigate(Home) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMenu = { navController.navigate(Menu) },
                                onNavigateToMap = { navController.navigate(com.example.foodgram.navigation.Map) }
                            )
                        }
                        
                        composable<Profile> { /* TODO */ }
                        composable<Menu> { /* TODO */ }
                        
                        // Using the fully qualified name for Map to avoid conflict with kotlin.collections.Map
                        composable<com.example.foodgram.navigation.Map> {
                            MapScreen(
                                onNavigateToFeed = { navController.navigate(Home) },
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMenu = { navController.navigate(Menu) }
                            )
                        }
                    }
                }
            }
        }
    }
}
