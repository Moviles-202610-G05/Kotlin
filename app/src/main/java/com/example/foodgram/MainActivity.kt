package com.example.foodgram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.foodgram.navigation.*
import com.example.foodgram.ui.theme.FoodGramTheme
import com.example.foodgram.views.feed.HomeScreen
import com.example.foodgram.views.restaurants.MapScreen
import com.example.foodgram.views.restaurants.SearchRestaurantsScreen
import com.example.foodgram.views.restaurants.RestaurantMenuManagementScreen
import com.example.foodgram.views.auth.LoginScreen
import com.example.foodgram.views.auth.RegistrationTypeView
import com.example.foodgram.views.RestaurantRegisterView
import com.example.foodgram.views.auth.AccountType
import com.example.foodgram.views.profile.UserScreen
import com.example.foodgram.views.settings.PersonalInfoSettings
import com.example.foodgram.views.auth.MenuRegisterView
import com.example.foodgram.views.auth.StudentRegisterScreen
import com.example.foodgram.viewmodels.auth.RestaurantRegisterViewModel
import com.example.foodgram.views.auth.ForgotPasswordScreen
import com.example.foodgram.views.settings.NutritionGoalsScreen
import com.example.foodgram.views.tracker.TrackerScreen
import com.example.foodgram.utils.UserSession


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserSession.init(this)
        
        enableEdgeToEdge()
        setContent {
            FoodGramTheme {
                val navController = rememberNavController()
                val restaurantViewModel: RestaurantRegisterViewModel = viewModel()

                // Simplicidad máxima para evitar bloqueos en el Main Thread
                val startDest = if (UserSession.currentUserUid != null) Home else Login

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDest,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<Login> {
                            LoginScreen(
                                onNavigateToHome = { navController.navigate(Home) },
                                onNavigateToSignUp = { navController.navigate(Register) },
                                onNavigateToForgotPassword = { navController.navigate(ForgotPassword) }
                            )
                        }
                        composable<ForgotPassword> {
                            ForgotPasswordScreen(
                                onBackClick = { navController.navigateUp() },
                                onResetSuccess = { 
                                    navController.navigate(Login) {
                                        popUpTo(ForgotPassword) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable<Register> {
                            RegistrationTypeView(
                                onBackClick = { navController.navigateUp() },
                                onLoginClick = { 
                                    navController.navigate(Login) {
                                        popUpTo(Login) { inclusive = true }
                                    }
                                },
                                onContinueClick = { type ->
                                if (type == AccountType.RESTAURANTE) {
                                        navController.navigate(RestaurantRegister)
                                    } else {
                                        navController.navigate(StudentRegister)
                                    }
                                }
                            )
                        }
                        composable<StudentRegister> {
                            StudentRegisterScreen(
                                onBackClick = { navController.navigateUp() },
                                onRegisterSuccess = {
                                    navController.navigate(Home) {
                                        popUpTo(Login) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable<RestaurantRegister> {
                            RestaurantRegisterView(
                                viewModel = restaurantViewModel,
                                onBackClick = { navController.navigateUp() },
                                onAddMenuClick = { navController.navigate(MenuRegister) }
                            )
                        }
                        
                        composable<MenuRegister> {
                            MenuRegisterView(
                                viewModel = restaurantViewModel,
                                onBackClick = { navController.navigateUp() },
                                onFinishClick = { 
                                    restaurantViewModel.finishRegistration {
                                        navController.navigate(Home) {
                                            popUpTo(Login) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable<Home> {
                            val navigateToMenuOrScan = {
                                if (UserSession.currentUserRole == "RESTAURANTE") {
                                    navController.navigate(Menu)
                                } else {
                                    navController.navigate(Tracker)
                                }
                            }
                            HomeScreen(
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMenu = navigateToMenuOrScan,
                                onNavigateToMap = { id -> navController.navigate(RestaurantsMap(id)) },
                                onNavigateToRestaurantDetail = { id -> 
                                    navController.navigate(RestaurantDetail(id))
                                }
                            )
                        }

                        composable<RestaurantDetail> { backStackEntry ->
                            val detail: RestaurantDetail = backStackEntry.toRoute()
                            val navigateToMenuOrScan = {
                                if (UserSession.currentUserRole == "RESTAURANTE") {
                                    navController.navigate(Menu)
                                } else {
                                    navController.navigate(Tracker)
                                }
                            }
                            com.example.foodgram.views.restaurants.RestaurantDetailScreen(
                                restaurantId = detail.id,
                                onNavigateBack = { navController.navigateUp() },
                                onNavigateToFeed = { navController.navigate(Home) },
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMenu = navigateToMenuOrScan,
                                onNavigateToMap = { id -> navController.navigate(RestaurantsMap(id)) }
                            )
                        }

                        composable<Search> {
                            val navigateToMenuOrScan = {
                                if (UserSession.currentUserRole == "RESTAURANTE") {
                                    navController.navigate(Menu)
                                } else {
                                    navController.navigate(Tracker)
                                }
                            }
                            SearchRestaurantsScreen(
                                onNavigateToFeed = { navController.navigate(Home) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMenu = navigateToMenuOrScan,
                                onNavigateToMap = { id -> navController.navigate(RestaurantsMap(id)) },
                                onNavigateToRestaurantDetail = { id ->
                                    navController.navigate(RestaurantDetail(id))
                                }
                            )
                        }

                        composable<Profile> {
                            val navigateToMenuOrScan = {
                                if (UserSession.currentUserRole == "RESTAURANTE") {
                                    navController.navigate(Menu)
                                } else {
                                    navController.navigate(Tracker)
                                }
                            }
                            UserScreen(
                                navController = navController,
                                onNavigateToHome = { navController.navigate(Home) },
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToMenu = navigateToMenuOrScan,
                                onNavigateToMap = { id -> navController.navigate(RestaurantsMap(id)) },
                                onNavigateToOrders = { /* TODO */ },
                                onNavigateToReviews = { /* TODO */ },
                                onNavigateToSaved = { /* TODO */ },
                                onNavigateToNutritionGoals = { navController.navigate(NutritionGoals) },
                                onNavigateToPrivacySettings = { /* TODO */ },
                                onLogout = {
                                    UserSession.logout()
                                    navController.navigate(Login) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable<PersonalInfo> {
                                PersonalInfoSettings(navController = navController)
                        }
                        composable<NutritionGoals> {
                            NutritionGoalsScreen(navController = navController)
                        }
                        composable<Menu> {
                            RestaurantMenuManagementScreen(
                                onNavigateToFeed = { navController.navigate(Home) },
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMap = { navController.navigate(RestaurantsMap()) }
                            )
                        }
                        composable<Tracker> {
                            TrackerScreen(
                                onBackClick = { navController.navigateUp() },
                                onSaveSuccess = { navController.navigate(Home) }
                            )
                        }
                        composable<RestaurantsMap> { backStackEntry ->
                            val mapRoute: RestaurantsMap = backStackEntry.toRoute()
                            val navigateToMenuOrScan = {
                                if (UserSession.currentUserRole == "RESTAURANTE") {
                                    navController.navigate(Menu)
                                } else {
                                    navController.navigate(Tracker)
                                }
                            }
                            MapScreen(
                                restaurantId = mapRoute.restaurantId,
                                onNavigateToFeed = { navController.navigate(Home) },
                                onNavigateToSearch = { navController.navigate(Search) },
                                onNavigateToProfile = { navController.navigate(Profile) },
                                onNavigateToMenu = navigateToMenuOrScan,
                                onNavigateToRestaurantDetail = { id ->
                                    navController.navigate(RestaurantDetail(id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
