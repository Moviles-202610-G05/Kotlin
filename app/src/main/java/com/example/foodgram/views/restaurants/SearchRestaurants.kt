package com.example.foodgram.views.restaurants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodgram.models.restaurants.MapRestaurant
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.viewmodels.saved.SavedViewModel
import com.example.foodgram.viewmodels.search.SearchViewModel
import com.example.foodgram.views.components.FoodGramNavigationBar
import com.example.foodgram.views.components.FoodGramScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRestaurantsScreen(
    onNavigateToFeed: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onNavigateToMap: (String?) -> Unit = {},
    onNavigateToRestaurantDetail: (String) -> Unit = {},
    searchViewModel: SearchViewModel = viewModel(),
    savedViewModel: SavedViewModel = viewModel()
) {
    val savedIds by savedViewModel.savedIds.collectAsState()
    val filteredRestaurants = remember(
        searchViewModel.restaurants,
        searchViewModel.searchQuery,
        searchViewModel.selectedCategory
    ) { searchViewModel.getFilteredRestaurants() }

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
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchViewModel.searchQuery,
                    onValueChange = { searchViewModel.searchQuery = it },
                    placeholder = { Text("Search restaurants, cuisines...", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FoodGramOrange) },
                    trailingIcon = {
                        if (searchViewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchViewModel.searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFF0F0F0))
                )
            }

            // Category Chips
            item {
                LazyRow(
                    modifier = Modifier.padding(vertical = 16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchViewModel.categories) { category ->
                        FilterChip(
                            selected = searchViewModel.selectedCategory.equals(category, ignoreCase = true),
                            onClick = { searchViewModel.selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FoodGramOrange,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Loading or restaurant list
            if (searchViewModel.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FoodGramOrange)
                    }
                }
            } else if (filteredRestaurants.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No restaurants found", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(filteredRestaurants, key = { it.id }) { restaurant ->
                    SearchRestaurantCard(
                        restaurant = restaurant,
                        isSaved = restaurant.id in savedIds,
                        onToggleSave = { savedViewModel.toggleSave(restaurant) },
                        onRestaurantClick = {
                            if (restaurant.id.isNotEmpty()) onNavigateToRestaurantDetail(restaurant.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchRestaurantCard(
    restaurant: MapRestaurant,
    isSaved: Boolean = false,
    onToggleSave: () -> Unit = {},
    onRestaurantClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onRestaurantClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFFF5F5F5))) {
                if (restaurant.image.isNotBlank()) {
                    AsyncImage(
                        model = restaurant.image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Rating badge
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(14.dp))
                        Text("${"%.1f".format(restaurant.rating)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(" (${restaurant.nuberReviews})", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                // Heart button
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Icon(
                        if (isSaved) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isSaved) "Remove from saved" else "Save restaurant",
                        tint = if (isSaved) FoodGramOrange else Color.White
                    )
                }

                // Badge
                if (restaurant.badge.isNotEmpty()) {
                    Surface(
                        color = FoodGramOrange,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                    ) {
                        Text(
                            restaurant.badge,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Text(restaurant.price, color = FoodGramOrange, fontWeight = FontWeight.Bold)
                }
                if (restaurant.cuisine.isNotBlank()) {
                    Text(restaurant.cuisine, color = Color.Gray, fontSize = 12.sp)
                }
                if (restaurant.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(restaurant.description, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (restaurant.time.isNotBlank()) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Text(" ${restaurant.time}", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    if (restaurant.distance.isNotBlank()) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Text(" ${restaurant.distance}", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = onRestaurantClick,
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

@Composable
fun RecommendedCard(name: String, rating: String) {
    Box(
        modifier = Modifier.width(180.dp).height(120.dp)
            .clip(RoundedCornerShape(16.dp)).background(Color.LightGray)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        Surface(
            modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(12.dp))
                Text(rating, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
    }
}
