package com.example.foodgram.views.restaurants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
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

data class Restaurant(
    val id: Int,
    val name: String,
    val rating: String,
    val reviews: String,
    val description: String,
    val time: String,
    val distance: String,
    val priceRange: String,
    val tag: String? = null
)

val recommendedRestaurants = listOf(
    "Oasis Garden" to "4.9",
    "Steakhouse" to "4.7"
)

val restaurants = listOf(
    Restaurant(1, "La Trattoria Milano", "4.8", "2.4k", "Authentic wood-fired pizzas, handmade tagliatelle, and traditional tiramisu in an intimat...", "25-35 min", "1.2 km", "$$$", "TOP RATED"),
    Restaurant(2, "The Prime Grill", "4.6", "1.8k", "Premium dry-aged steaks and an extensive wine cellar. Experience elevated American...", "40-50 min", "2.8 km", "$$$$", null),
    Restaurant(3, "Sakura Zen Sushi", "4.9", "950", "Masterfully crafted omakase experience with fish flown in daily from Tsukiji Market. Pure zen i...", "30-45 min", "1.5 km", "$$$", "NEW FAVORITE")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRestaurantsScreen(
    onNavigateToFeed: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onNavigateToMap: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = false, onClick = onNavigateToFeed, icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }, label = { Text("FEED") })
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Search, contentDescription = null) }, label = { Text("SEARCH") })
                NavigationBarItem(selected = false, onClick = onNavigateToProfile, icon = { Icon(Icons.Default.Person, contentDescription = null) }, label = { Text("PROFILE") })
                NavigationBarItem(selected = false, onClick = onNavigateToMenu, icon = { Icon(Icons.Default.Menu, contentDescription = null) }, label = { Text("Menu") })
                NavigationBarItem(selected = false, onClick = onNavigateToMap, icon = { Icon(Icons.Default.Place, contentDescription = null) }, label = { Text("MAP") })
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FoodGram", color = FoodGramOrange, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, contentDescription = "Filter", tint = FoodGramOrange)
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search restaurants, cuisines...", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FoodGramOrange) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFF0F0F0))
                )
            }

            // Category Chips
            item {
                LazyRow(modifier = Modifier.padding(vertical = 16.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("Italian") },
                            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FoodGramOrange, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    item {
                        FilterChip(selected = false, onClick = {}, label = { Text("Mexican") }, leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(16.dp)) })
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    item {
                        FilterChip(selected = false, onClick = {}, label = { Text("Fast Food") }, leadingIcon = { Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp)) })
                    }
                }
            }

            // Recommended Section
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recommended for You", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    TextButton(onClick = {}) { Text("See All", color = FoodGramOrange) }
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recommendedRestaurants) { (name, rating) ->
                        RecommendedCard(name, rating)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Restaurant List
            items(restaurants) { restaurant ->
                RestaurantListItem(restaurant)
            }
        }
    }
}

@Composable
fun RecommendedCard(name: String, rating: String) {
    Box(modifier = Modifier.width(180.dp).height(120.dp).clip(RoundedCornerShape(16.dp)).background(Color.LightGray)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        Surface(modifier = Modifier.padding(8.dp).align(Alignment.TopStart), color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(12.dp))
                Text(rating, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
    }
}

@Composable
fun RestaurantListItem(restaurant: Restaurant) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFFF5F5F5))) {
                // Image placeholder
                Row(modifier = Modifier.padding(12.dp).align(Alignment.TopStart)) {
                    Surface(color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(14.dp))
                            Text(restaurant.rating, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(" (${restaurant.reviews})", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
                IconButton(onClick = {}, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Color.White)
                }

                restaurant.tag?.let {
                    Surface(color = FoodGramOrange, shape = RoundedCornerShape(12.dp), modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                        Text(it, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(restaurant.priceRange, color = FoodGramOrange, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(restaurant.description, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(" ${restaurant.time}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Text(" ${restaurant.distance}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = FoodGramOrange),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("View Menu", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
