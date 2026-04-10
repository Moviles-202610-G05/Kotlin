package com.example.foodgram.views.restaurants

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.foodgram.models.restaurants.MenuItem
import com.example.foodgram.models.restaurants.RestaurantReview
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.viewmodels.restaurants.RestaurantDetailViewModel
import com.example.foodgram.views.restaurants.MapRestaurant
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RestaurantDetailScreen(
    restaurantId: String,
    initialData: MapRestaurant? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToFeed: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    viewModel: RestaurantDetailViewModel = viewModel()
) {
    LaunchedEffect(restaurantId) {
        viewModel.loadRestaurantDetail(restaurantId, initialData)
    }

    val restaurant = viewModel.restaurant ?: initialData
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Menu", "Location", "Reviews")

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = false, onClick = onNavigateToFeed, icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("FEED") })
                NavigationBarItem(selected = true, onClick = onNavigateToSearch, icon = { Icon(Icons.Default.Search, null) }, label = { Text("SEARCH") })
                NavigationBarItem(selected = false, onClick = onNavigateToProfile, icon = { Icon(Icons.Default.Person, null) }, label = { Text("PROFILE") })
                NavigationBarItem(selected = false, onClick = onNavigateToMenu, icon = { Icon(Icons.Default.Restaurant, null) }, label = { Text("MENU") })
                NavigationBarItem(selected = false, onClick = onNavigateToMap, icon = { Icon(Icons.Default.Map, null) }, label = { Text("MAP") })
            }
        }
    ) { padding ->
        if (restaurant == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FoodGramOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Image
                Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                    AsyncImage(
                        model = restaurant.image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = coil.compose.rememberAsyncImagePainter(
                            model = "https://maps.googleapis.com/maps/api/staticmap?center=${restaurant.lat},${restaurant.long}&zoom=15&size=600x300&key=AIzaSyCY2NXIVhpZbJ3vdCzQzbKPt0h6yShDZMA"
                        )
                    )
                    
                    // Badges
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)) {
                        Surface(color = FoodGramOrange, shape = RoundedCornerShape(8.dp)) {
                            Text("TOP RATED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Text(restaurant.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onNavigateBack, modifier = Modifier.padding(16.dp).background(Color.White.copy(alpha = 0.5f), CircleShape)) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }

                // Info Section
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${restaurant.cuisine} • ${restaurant.price} • ${restaurant.cuisine}", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Star, null, tint = FoodGramOrange, modifier = Modifier.size(16.dp))
                        Text(" ${restaurant.rating}", fontWeight = FontWeight.Bold)
                    }
                    Text("Open until ${restaurant.time} • ${restaurant.nuberReviews}+ reviews", color = Color.Gray, fontSize = 12.sp)
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // Availability Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF0FFF4), // Light green
                        border = BorderStroke(1.dp, Color(0xFFC6F6D5))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF48BB78), CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("LIVE AVAILABILITY", color = Color(0xFF2F855A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Updated 1m ago", color = Color.Gray, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${restaurant.spotsA}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                                Text(" Available Now", fontSize = 16.sp, color = Color(0xFF2D3748), modifier = Modifier.padding(bottom = 4.dp))
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Total: ${restaurant.spots} spots", fontSize = 12.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { restaurant.spotsA.toFloat() / restaurant.spots.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = Color(0xFF48BB78),
                                trackColor = Color(0xFFEDF2F7)
                            )
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = FoodGramOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = FoodGramOrange
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                when (selectedTab) {
                    0 -> MenuSection(viewModel.menuItems)
                    1 -> LocationSection(restaurant)
                    2 -> ReviewsSection(viewModel.reviews)
                }
            }
        }
    }
}

@Composable
fun MenuSection(items: List<MenuItem>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RestaurantMenu, null, tint = FoodGramOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Signature Dishes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                AsyncImage(
                    model = item.image,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("$${item.price}", fontWeight = FontWeight.Bold, color = FoodGramOrange)
                    }
                    Text(item.description, color = Color.Gray, fontSize = 12.sp, maxLines = 2)
                }
            }
        }
    }
}

@Composable
fun LocationSection(restaurant: MapRestaurant) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = FoodGramOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Location & Directions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Surface(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(24.dp), color = Color.LightGray) {
            // Static map preview
            AsyncImage(
                model = "https://maps.googleapis.com/maps/api/staticmap?center=${restaurant.lat},${restaurant.long}&zoom=15&size=600x300&key=AIzaSyCY2NXIVhpZbJ3vdCzQzbKPt0h6yShDZMA",
                contentDescription = "Map view",
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(restaurant.direction, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FoodGramOrange)
        ) {
            Icon(Icons.Default.Navigation, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Get Directions")
        }
    }
}

@Composable
fun ReviewsSection(reviews: List<RestaurantReview>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.StarBorder, null, tint = FoodGramOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reviews", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        reviews.forEach { review ->
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Color.LightGray, CircleShape), contentAlignment = Alignment.Center) {
                        Text(review.username.take(1).uppercase())
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(review.username, fontWeight = FontWeight.Bold)
                        Text(formatReviewDate(review.createdAt), color = Color.Gray, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        repeat(5) { i ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i < review.rating) FoodGramOrange else Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(review.comment, fontSize = 14.sp, color = Color.DarkGray)
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = Color(0xFFF7FAFC))
            }
        }
    }
}

fun formatReviewDate(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val diff = System.currentTimeMillis() - timestamp.toDate().time
    val days = diff / (1000 * 60 * 60 * 24)
    return when {
        days < 1 -> "Today"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(timestamp.toDate())
    }
}
