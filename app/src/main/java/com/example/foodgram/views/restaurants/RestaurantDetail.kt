package com.example.foodgram.views.restaurants

import androidx.compose.ui.text.style.TextAlign
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
import com.example.foodgram.models.restaurants.ReviewRestaurant
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.viewmodels.restaurants.RestaurantDetailViewModel
import com.example.foodgram.models.restaurants.MapRestaurant
import com.example.foodgram.views.components.FoodGramNavigationBar
import com.example.foodgram.views.components.FoodGramScreen
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
    onNavigateToMap: (String?) -> Unit = {},
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
            FoodGramNavigationBar(
                currentScreen = FoodGramScreen.SEARCH, // Detailed from Search context mostly
                onNavigateToFeed = onNavigateToFeed,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToMenu = onNavigateToMenu,
                onNavigateToMap = { onNavigateToMap(null) }
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FoodGramOrange)
            }
        } else if (restaurant == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Restaurant not found", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("The restaurant might have been removed.", color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = FoodGramOrange)
                ) {
                    Text("Go Back")
                }
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
                            model = "https://maps.googleapis.com/maps/api/staticmap?center=${restaurant.lat},${restaurant.long}&zoom=15&size=600x300&key=${com.example.foodgram.BuildConfig.MAPS_API_KEY}"
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
                                Text("Updated live", color = Color.Gray, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Available", color = Color(0xFF2D3748), fontSize = 12.sp)
                                    Text("${restaurant.spotsA}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Occupied", color = Color(0xFF2D3748), fontSize = 12.sp)
                                    Text("${restaurant.seatsOccupied}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D3748))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total", color = Color.Gray, fontSize = 12.sp)
                                    Text("${restaurant.numberOfSeats}", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val progress = if (restaurant.numberOfSeats > 0) (restaurant.numberOfSeats - restaurant.spotsA).toFloat() / restaurant.numberOfSeats.toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = FoodGramOrange,
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
                // Inside RestaurantDetailScreen
                when (selectedTab) {
                    0 -> MenuSection(viewModel.menuItems)
                    1 -> LocationSection(restaurant, onNavigateToMap) // Pass the callback here
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(80.dp)) {
                    AsyncImage(
                        model = item.image,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (!item.inStock) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "OUT OF STOCK",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Text(
                            item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (item.inStock) Color.Black else Color.Gray
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "$${item.price}",
                            fontWeight = FontWeight.Bold,
                            color = if (item.inStock) FoodGramOrange else Color.Gray
                        )
                    }
                    Text(
                        item.description,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun LocationSection(
    restaurant: MapRestaurant,
    onNavigateToMap: (String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal =20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = FoodGramOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Location & Directions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.LightGray
        ) {
            //Place holder
            val lat = restaurant.lat
            val lng = restaurant.long
            val apiKey = com.example.foodgram.BuildConfig.MAPS_API_KEY
            val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                    "center=$lat,$lng" +
                    "&zoom=15" +
                    "&size=600x300" +
                    "&markers=color:orange%7C$lat,$lng" +
                    "&key=$apiKey"

            AsyncImage(
                model = staticMapUrl,
                contentDescription = "Map view showing ${restaurant.name}",
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = restaurant.direction.ifEmpty { "Address not available" },
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onNavigateToMap(restaurant.id) }, // Triggers navigation to MapView
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FoodGramOrange)
        ) {
            Icon(Icons.Default.Navigation, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("View on Map")
        }
    }
}

@Composable
fun ReviewsSection(reviews: List<ReviewRestaurant>) {
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(review.avatarColor))
                                } catch (e: Exception) {
                                    Color.LightGray
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.avatar.ifEmpty { review.name.take(1).uppercase() },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(review.name, fontWeight = FontWeight.Bold)
                        Text(review.date, color = Color.Gray, fontSize = 10.sp)
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
