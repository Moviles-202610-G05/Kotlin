package com.example.foodgram.views.restaurants

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.foodgram.ui.theme.FoodGramOrange
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

data class MapRestaurant(
    val name: String,
    val rating: String,
    val tag: String,
    val category: String,
    val location: LatLng
)

val mapRestaurants = listOf(
    MapRestaurant("Burger Palace", "4.8", "TOP RATED", "Burgers", LatLng(37.423, -122.085)),
    MapRestaurant("Sushi Zen", "4.7", "FEATURED", "Sushi", LatLng(37.421, -122.084)),
    MapRestaurant("Pizza Hub", "4.5", "POPULAR", "Pizza", LatLng(37.425, -122.088)),
    MapRestaurant("Burger Joint", "4.2", "CHEAP", "Burgers", LatLng(37.419, -122.091)),
    MapRestaurant("Italian Delight", "4.9", "NEW", "Italian", LatLng(37.422, -122.082))
)

val categories = listOf("All", "Burgers", "Pizza", "Sushi", "Italian")

/**
 * Calculates distance in meters between two LatLng points.
 */
fun calculateDistanceMeters(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    return results[0]
}

/**
 * Formats distance from meters to a human-readable miles string.
 */
fun formatDistance(meters: Float): String {
    val miles = meters * 0.000621371f
    return "%.1f miles away".format(miles)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToFeed: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    // Default location (Mountain View)
    val mountainView = LatLng(37.4221, -122.0841)
    
    // States for filtering and sorting
    var selectedCategory by remember { mutableStateOf("All") }
    var userLocation by remember { mutableStateOf(mountainView) }
    
    // Processed list (Filtered by category, then Sorted by proximity)
    val sortedRestaurants = remember(selectedCategory, userLocation) {
        val filtered = if (selectedCategory == "All") {
            mapRestaurants
        } else {
            mapRestaurants.filter { it.category == selectedCategory }
        }
        
        filtered.sortedBy { calculateDistanceMeters(userLocation, it.location) }
    }

    // Camera State
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mountainView, 15f)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    // Helper to update location
    val updateLocation = {
        if (hasLocationPermission) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        val newLatLng = LatLng(it.latitude, it.longitude)
                        userLocation = newLatLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(newLatLng, 15f)
                    }
                }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            updateLocation()
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // --- REAL GOOGLE MAP ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            // Markers for sorted/filtered restaurants
            sortedRestaurants.forEach { restaurant ->
                Marker(
                    state = MarkerState(position = restaurant.location),
                    title = restaurant.name,
                    snippet = "${restaurant.rating} stars"
                )
            }
        }

        // --- UI Overlays ---
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp)
        ) {
            // Search Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    TextField(
                        value = "", onValueChange = {},
                        placeholder = { Text("Search for food...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FoodGramOrange) },
                        trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 4.dp, modifier = Modifier.size(48.dp)) {
                    IconButton(onClick = {}) { Icon(Icons.Outlined.Tune, contentDescription = "Filter") }
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier.padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryChip(
                        text = category,
                        icon = when(category) {
                            "Burgers" -> Icons.Default.LunchDining
                            "Pizza" -> Icons.Default.LocalPizza
                            "Sushi" -> Icons.Default.Restaurant
                            "Italian" -> Icons.Default.Restaurant
                            else -> Icons.Default.Restaurant
                        },
                        selected = selectedCategory == category,
                        onSelected = { selectedCategory = category }
                    )
                }
            }
        }

        // Floating Action Buttons
        Column(modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MapSideButton(Icons.Default.Layers)
            MapSideButton(Icons.Default.MyLocation, onClick = {
                if (hasLocationPermission) {
                    updateLocation()
                } else {
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            })
        }

        // Bottom Cards Carousel (Sorted by proximity)
        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)) {
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sortedRestaurants) { restaurant ->
                    val distanceMeters = calculateDistanceMeters(userLocation, restaurant.location)
                    RestaurantMapCard(
                        name = restaurant.name,
                        rating = restaurant.rating,
                        distance = formatDistance(distanceMeters),
                        tag = restaurant.tag
                    )
                }
            }
        }

        // Bottom Navigation
        Surface(modifier = Modifier.align(Alignment.BottomCenter), shadowElevation = 8.dp) {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = false, onClick = onNavigateToFeed, icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }, label = { Text("FEED") })
                NavigationBarItem(selected = false, onClick = onNavigateToSearch, icon = { Icon(Icons.Default.Search, contentDescription = null) }, label = { Text("SEARCH") })
                NavigationBarItem(selected = false, onClick = onNavigateToProfile, icon = { Icon(Icons.Default.Person, contentDescription = null) }, label = { Text("PROFILE") })
                NavigationBarItem(selected = false, onClick = onNavigateToMenu, icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) }, label = { Text("Menu") })
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Map, contentDescription = null) }, label = { Text("MAP") })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onSelected: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) FoodGramOrange else Color.White,
        shadowElevation = 2.dp,
        onClick = onSelected
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = if (selected) Color.White else Color.Black, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun MapSideButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Surface(shape = CircleShape, color = Color.White, shadowElevation = 4.dp, modifier = Modifier.size(44.dp), onClick = onClick) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = Color.Black) }
    }
}

@Composable
fun RestaurantMapCard(name: String, rating: String, distance: String, tag: String) {
    Surface(modifier = Modifier.width(280.dp), shape = RoundedCornerShape(32.dp), color = Color.White, shadowElevation = 6.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(14.dp))
                    Text(" $rating", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(" $distance", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = FoodGramOrange.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(tag, color = FoodGramOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        }
    }
}
