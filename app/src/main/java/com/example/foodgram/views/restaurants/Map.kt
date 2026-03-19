package com.example.foodgram.views.restaurants

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodgram.ui.theme.FoodGramOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToFeed: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // --- Map Placeholder ---
        // In a real app, you'd use Google Maps Compose here.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0F2F1)) // Light blue-green map-like color
        ) {
            // Simulated Map Markers
            MapMarker(Modifier.align(Alignment.Center).offset(x = (-50).dp, y = (-100).dp), Icons.Default.LunchDining)
            MapMarker(Modifier.align(Alignment.Center).offset(x = 60.dp, y = (-20).dp), Icons.Default.LocalPizza)
            MapMarker(Modifier.align(Alignment.Center).offset(x = (-30).dp, y = 80.dp), Icons.Default.Restaurant)
        }

        // --- Top UI Overlay ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp) // Adjusted for status bar/edge-to-edge
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search for food...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FoodGramOrange) },
                        trailingIcon = { Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Filter")
                    }
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier.padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip("All", Icons.Default.Restaurant, true)
                }
                item {
                    CategoryChip("Burgers", Icons.Default.LunchDining, false)
                }
                item {
                    CategoryChip("Pizza", Icons.Default.LocalPizza, false)
                }
            }
        }

        // --- Side Buttons (Layer and Location) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MapSideButton(Icons.Default.Layers)
            MapSideButton(Icons.Default.MyLocation)
        }

        // --- Bottom Cards Overlay ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp) // Leave space for bottom nav
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    RestaurantMapCard("Burger Palace", "4.8", "0.5 miles away", "TOP RATED")
                }
                item {
                    RestaurantMapCard("Lucky Sushi", "4.7", "1.2 miles away", "FEATURED")
                }
            }
        }

        // --- Bottom Navigation Bar ---
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shadowElevation = 8.dp
        ) {
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

@Composable
fun MapMarker(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Simple Marker pin shape using Box/Icons
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = FoodGramOrange,
            modifier = Modifier.size(48.dp)
        )
        Surface(
            modifier = Modifier.size(24.dp).offset(y = (-4).dp),
            shape = CircleShape,
            color = Color.White
        ) {
            Icon(icon, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.padding(4.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (selected) FoodGramOrange else Color.White,
        shadowElevation = 2.dp,
        onClick = {}
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected) Color.White else Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text,
                color = if (selected) Color.White else Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun MapSideButton(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.size(44.dp),
        onClick = {}
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color.Black)
        }
    }
}

@Composable
fun RestaurantMapCard(name: String, rating: String, distance: String, tag: String) {
    Surface(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Image Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(14.dp))
                    Text(" $rating", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(" $distance", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = FoodGramOrange.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        tag,
                        color = FoodGramOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
