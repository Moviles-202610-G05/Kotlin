package com.example.foodgram.views.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodgram.navigation.NutritionGoals
import com.example.foodgram.navigation.PersonalInfo
import com.example.foodgram.ui.theme.OrangeFoodGram
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.foodgram.utils.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    navController: NavController,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToMap: (String?) -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToNutritionGoals: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit,
    onLogout: () -> Unit
) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()


    var name by remember { mutableStateOf("Loading...") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var caloriesGoal by remember { mutableStateOf(2000f) }
    var proteinGoal by remember { mutableStateOf(150f) }
    var carbsGoal by remember { mutableStateOf(200f) }
    var fatGoal by remember { mutableStateOf(67f) }

    var ordersCount by remember { mutableStateOf(0) }
    var reviewsCount by remember { mutableStateOf(0) }
    var savedCount by remember { mutableStateOf(0) }

    val userId = UserSession.currentUserDocId

    DisposableEffect(userId) {
        if (userId == null) {
            onDispose { }
        } else {
            val listener = db.collection("user").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    if (snapshot.exists()) {
                        name = snapshot.getString("name") ?: "User"
                        username = snapshot.getString("username") ?: ""
                        email = snapshot.getString("email") ?: ""
                        location = snapshot.getString("location") ?: "Not defined"

                        caloriesGoal = snapshot.getDouble("caloriesGoal")?.toFloat() ?: 2000f
                        proteinGoal = snapshot.getDouble("proteinGoal")?.toFloat() ?: 150f
                        carbsGoal = snapshot.getDouble("carbsGoal")?.toFloat() ?: 200f
                        fatGoal = snapshot.getDouble("fatGoal")?.toFloat() ?: 67f
                        ordersCount = snapshot.getLong("ordersCount")?.toInt() ?: 0
                        reviewsCount = snapshot.getLong("reviewsCount")?.toInt() ?: 0
                        savedCount = snapshot.getLong("savedCount")?.toInt() ?: 0

                    }
                }

            onDispose {
                listener.remove()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = OrangeFoodGram,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FoodGram",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeFoodGram
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 0.5.dp, shape = RectangleShape)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Feed") },
                    label = { Text("FEED") },
                    selected = false,
                    onClick = onNavigateToHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("SEARCH") },
                    selected = false,
                    onClick = onNavigateToSearch
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("PROFILE") },
                    selected = true,
                    onClick = { /* Current */ },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = OrangeFoodGram)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                    label = { Text("MENU") },
                    selected = false,
                    onClick = onNavigateToMenu
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("MAP") },
                    selected = false,
                    onClick = { onNavigateToMap(null) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))


            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(width = 3.dp, color = OrangeFoodGram, shape = CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))


            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " $location",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))


            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 15.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCard(
                        value = ordersCount.toString(),
                        label = "ORDERS",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToOrders() }
                    )
                    DividerStat()
                    StatCard(
                        value = reviewsCount.toString(),
                        label = "REVIEWS",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToReviews() }
                    )
                    DividerStat()
                    StatCard(
                        value = savedCount.toString(),
                        label = "SAVED",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSaved() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))


            SectionHeader(
                title = "Nutrition Goals",
                action = "Details",
                onAction = onNavigateToNutritionGoals
            )
            Spacer(modifier = Modifier.height(12.dp))
            NutritionCard(
                caloriesGoal = caloriesGoal,
                proteinGoal = proteinGoal,
                carbsGoal = carbsGoal,
                fatGoal = fatGoal
            )

            Spacer(modifier = Modifier.height(32.dp))


            SectionHeader(title = "Account Settings", action = "")
            Spacer(modifier = Modifier.height(12.dp))

            SettingsItem(
                icon = Icons.Default.PersonOutline,
                title = "Personal Information",
                subtitle = null,
                bgColor = OrangeFoodGram.copy(alpha = 0.1f),
                onTap = {
                    navController.navigate(PersonalInfo)
                }
            )
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Post & Privacy Settings",
                subtitle = "Configure social publications",
                bgColor = OrangeFoodGram.copy(alpha = 0.1f),
                onTap = onNavigateToPrivacySettings
            )

            Spacer(modifier = Modifier.height(24.dp))


            OutlinedButton(
                onClick = {
                    auth.signOut()
                    UserSession.currentUserDocId = null
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFFF2600)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        color = OrangeFoodGram,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = OrangeFoodGram
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF607D8B),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun DividerStat() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color(0xFFEEEEEE))
    )
}

@Composable
fun SectionHeader(title: String, action: String, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (action.isNotEmpty()) {
            Text(
                text = action,
                color = OrangeFoodGram,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onAction?.invoke() }
            )
        }
    }
}

@Composable
fun NutritionCard(
    caloriesGoal: Float,
    proteinGoal: Float,
    carbsGoal: Float,
    fatGoal: Float
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Daily Calorie Goal", color = Color.Gray)

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${caloriesGoal.toInt()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " kcal",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = OrangeFoodGram,
                trackColor = Color(0xFFEEEEEE)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Macro Targets", color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroItem(
                    label = "Protein",
                    consumed = "${proteinGoal.toInt()}g",
                    color = Color(0xFFFF5252),
                    progress = 1f
                )
                MacroItem(
                    label = "Carbs",
                    consumed = "${carbsGoal.toInt()}g",
                    color = Color(0xFFFF9800),
                    progress = 1f
                )
                MacroItem(
                    label = "Fat",
                    consumed = "${fatGoal.toInt()}g",
                    color = Color(0xFFFFC107),
                    progress = 1f
                )
            }
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    consumed: String,
    color: Color,
    progress: Float
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = consumed,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(80.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFFEEEEEE)
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    bgColor: Color,
    onTap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onTap() },
        shape = RoundedCornerShape(15.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OrangeFoodGram
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}