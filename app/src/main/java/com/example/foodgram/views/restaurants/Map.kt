package com.example.foodgram.views.restaurants

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.viewmodels.map.MapViewModel
import com.example.foodgram.models.restaurants.MapRestaurant
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category.lowercase()) {
        "all" -> Icons.Default.Apps
        "burgers" -> Icons.Default.Fastfood
        "pizza" -> Icons.Default.LocalPizza
        "sushi" -> Icons.Default.RestaurantMenu
        "italian" -> Icons.Default.Restaurant
        "fast food" -> Icons.Default.ElectricBolt
        else -> Icons.Default.Restaurant
    }
}

fun calculateDistanceMeters(start: LatLng, end: LatLng): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    return results[0]
}

fun formatDistance(meters: Float): String {
    val km = meters / 1000f
    return "%.1f km away".format(km)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    restaurantId: String? = null,
    onNavigateToFeed: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onNavigateToRestaurantDetail: (String) -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val defaultLocation = LatLng(4.60971, -74.08175) // Bogotá 
    
    var userLocation by remember { mutableStateOf(defaultLocation) }
    
    val sortedRestaurants = remember(userLocation, viewModel.restaurants, viewModel.selectedCategory) {
        viewModel.getFilteredRestaurants(userLocation)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    // Effect to center on specific restaurant if restaurantId is provided
    LaunchedEffect(restaurantId, viewModel.restaurants) {
        if (restaurantId != null && viewModel.restaurants.isNotEmpty()) {
            val targetRestaurant = viewModel.restaurants.find { it.id == restaurantId }
            targetRestaurant?.let {
                // First ensure "All" is selected so the marker is visible
                viewModel.selectedCategory = "All"
                
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(it.location, 17f),
                    durationMs = 1000
                )
            }
        }
    }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasLocationPermission = it }

    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000L
            ).setMinUpdateIntervalMillis(2000L).build()

            val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    result.lastLocation?.let {
                        val newLatLng = LatLng(it.latitude, it.longitude)
                        userLocation = newLatLng
                    }
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
            )

            onDispose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            onDispose {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            sortedRestaurants.forEach { restaurant ->
                if (restaurant.name.isNotBlank()) {
                    key(restaurant.id) {
                        Marker(
                            state = rememberMarkerState(position = restaurant.location),
                            title = restaurant.name,
                            snippet = "${restaurant.rating} stars",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                        )
                    }
                }
            }
        }

        // My Location Button
        FloatingActionButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(userLocation, 16f),
                        durationMs = 1000
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 230.dp, end = 16.dp),
            containerColor = Color.White,
            contentColor = FoodGramOrange,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Go to my location")
        }

        Column(modifier = Modifier.fillMaxWidth().padding(top = 48.dp)) {
            // Search Bar
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(28.dp), color = Color.White, shadowElevation = 4.dp) {
                    TextField(
                        value = "", onValueChange = {},
                        placeholder = { Text("Search for food...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = FoodGramOrange) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Categories
            LazyRow(modifier = Modifier.padding(top = 16.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.categories) { category ->
                    CategoryChip(
                        text = category,
                        icon = getCategoryIcon(category),
                        selected = viewModel.selectedCategory.equals(category, ignoreCase = true),
                        onSelected = { viewModel.selectedCategory = category }
                    )
                }
            }
        }

        // Proximity Cards
        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = FoodGramOrange)
            } else {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(sortedRestaurants) { restaurant ->
                        RestaurantMapCard(
                            name = restaurant.name,
                            rating = restaurant.rating.toString(),
                            distance = formatDistance(calculateDistanceMeters(userLocation, restaurant.location)),
                            badge = restaurant.badge,
                            badge2 = restaurant.badge2,
                            imageUrl = restaurant.image,
                            onClick = {
                                scope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(restaurant.location, 17f),
                                        durationMs = 1000
                                    )
                                }
                            },
                            onDoubleTap = {
                                if (restaurant.id.isNotEmpty()) {
                                    onNavigateToRestaurantDetail(restaurant.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Bottom Nav
        Surface(modifier = Modifier.align(Alignment.BottomCenter), shadowElevation = 8.dp) {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = false, onClick = onNavigateToFeed, icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("FEED") })
                NavigationBarItem(selected = false, onClick = onNavigateToSearch, icon = { Icon(Icons.Default.Search, null) }, label = { Text("SEARCH") })
                NavigationBarItem(selected = false, onClick = onNavigateToProfile, icon = { Icon(Icons.Default.Person, null) }, label = { Text("PROFILE") })
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Map, null) }, label = { Text("MAP") })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onSelected: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = if (selected) FoodGramOrange else Color.White, shadowElevation = 2.dp, onClick = onSelected) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (selected) Color.White else Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = if (selected) Color.White else Color.Black, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RestaurantMapCard(
    name: String,
    rating: String,
    distance: String,
    badge: String,
    badge2: String?,
    imageUrl: String,
    onClick: () -> Unit = {},
    onDoubleTap: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onDoubleTap
            ),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = FoodGramOrange, modifier = Modifier.size(14.dp))
                    Text(" $rating", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(" • $distance", color = Color.Gray, fontSize = 12.sp)
                }
                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (badge.isNotEmpty()) {
                        BadgeChip(text = badge)
                    }
                    if (!badge2.isNullOrEmpty()) {
                        BadgeChip(text = badge2)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeChip(text: String) {
    Surface(color = FoodGramOrange.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(text, color = FoodGramOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}
