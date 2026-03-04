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
import com.example.foodgram.navigation.Register
import com.example.foodgram.navigation.MainFeed
import com.example.foodgram.navigation.RestaurantRegister
import com.example.foodgram.ui.theme.FoodGramTheme
import com.example.foodgram.views.HomeScreen
import com.example.foodgram.views.RegistrationTypeView
import com.example.foodgram.views.MainFeedScreen
import com.example.foodgram.views.AccountType
import com.example.foodgram.views.RestaurantRegisterView

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
                            HomeScreen(onNavigateToProfile = { navController.navigate(Register) }, onNavigateToMainFeed = { navController.navigate(MainFeed) })
                        }
                        composable<MainFeed> {
                            MainFeedScreen()
                        }
                        composable<Register> {
                            RegistrationTypeView(
                                onBackClick = { navController.navigateUp() },
                                onLoginClick = { navController.navigate(Home) },
                                onContinueClick = { type ->
                                    if (type == AccountType.OWNER) {
                                        navController.navigate(RestaurantRegister)
                                    } else {
                                        // Handle Student registration or other types
                                        navController.navigate(Home)
                                    }
                                }
                            )
                        }
                        composable<RestaurantRegister> {
                            RestaurantRegisterView()
                        }

                    }
                }
            }
        }
    }
}
