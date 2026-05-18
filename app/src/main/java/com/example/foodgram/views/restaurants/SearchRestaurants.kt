package com.example.foodgram.views.restaurants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.foodgram.views.components.FoodGramNavigationBar
import com.example.foodgram.views.components.FoodGramScreen

import com.example.foodgram.models.auth.Restaurant

val recommendedRestaurants = listOf(
    "Oasis Garden" to "4.9",
    "Steakhouse" to "4.7"
)

val restaurants = listOf(
    Restaurant(
        id = "1",
        name = "La Trattoria Milano",
        rating = 4.8,
        nuberReviews = 2400,
        description = "Authentic wood-fired pizzas, handmade tagliatelle, and traditional tiramisu in an intimat...",
        time = "25-35 min",
        distance = "1.2 km",
        price = "$$$",
        badge = "TOP RATED"
    ),
    Restaurant(
        id = "2",
        name = "The Prime Grill",
        rating = 4.6,
        nuberReviews = 1800,
        description = "Premium dry-aged steaks and an extensive wine cellar. Experience elevated American...",
        time = "40-50 min",
        distance = "2.8 km",
        price = "$$$$",
        badge = ""
    ),
    Restaurant(
        id = "3",
        name = "Sakura Zen Sushi",
        rating = 4.9,
        nuberReviews = 950,
        description = "Masterfully crafted omakase experience with fish flown in daily from Tsukiji Market. Pure zen i...",
        time = "30-45 min",
        distance = "1.5 km",
        price = "$$$",
        badge = "NEW FAVORITE"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRestaurantsScreen(
    onNavigateToFeed: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onNavigateToMap: (String?) -> Unit = {},
    onNavigateToRestaurantDetail: (String) -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            FoodGramNavigationBar(
                currentScreen = FoodGramScreen.SEARCH,
                onNavigateToFeed = onNavigateToFeed,
                onNavigateToSearch = { },
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToMenu = onNavigateToMenu,
                onNavigateToMap = { onNavigateToMap(null) }
            )
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
                RestaurantListItem(
                    restaurant = restaurant,
                    onRestaurantClick = { onNavigateToRestaurantDetail(restaurant.id.toString()) }
                )
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
fun RestaurantListItem(restaurant: Restaurant, onRestaurantClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().clickable { onRestaurantClick() },
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
                            Text(restaurant.rating.toString(), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(" (${restaurant.nuberReviews})", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
                IconButton(onClick = {}, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = Color.White)
                }

                if (restaurant.badge.isNotEmpty()) {
                    Surface(color = FoodGramOrange, shape = RoundedCornerShape(12.dp), modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                        Text(restaurant.badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(restaurant.price, color = FoodGramOrange, fontWeight = FontWeight.Bold)
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
