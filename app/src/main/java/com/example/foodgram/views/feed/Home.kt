package com.example.foodgram.views.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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

// Dummy data for the prototype
data class Post(
    val id: Int,
    val username: String,
    val location: String,
    val time: String,
    val caption: String,
    val likes: String,
    val comments: Int
)

val dummyPosts = listOf(
    Post(1, "ChefMario", "La Tarta Milano", "2h ago", "The best truffle pasta I've had in the city!", "1,234", 88),
    Post(2, "SushiLover", "Oishii Sushi Bar", "5h ago", "Fresh catch from this morning. You can really taste the difference.", "856", 42)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onNavigateToMap: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = FoodGramOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "FoodGram",
                            color = FoodGramOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Likes")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Feed") },
                    label = { Text("FEED") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("SEARCH") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("PROFILE") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMenu,
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                    label = { Text("Menu") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMap,
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("MAP") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                containerColor = FoodGramOrange,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Post")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            items(dummyPosts) { post ->
                PostItem(post)
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(post.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${post.location} • ${post.time}", color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
        }

        // Image Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFEEEEEE)),
            contentAlignment = Alignment.BottomStart
        ) {
            // Location tag on image
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(post.location, color = Color.White, fontSize = 10.sp)
                }
            }
        }

        // Actions
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
            IconButton(onClick = { }) { Icon(Icons.Default.FavoriteBorder, contentDescription = null) }
            Text(post.likes, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
            IconButton(onClick = { }) { Icon(Icons.Default.Email, contentDescription = null) }
            Text("${post.comments}", modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
            IconButton(onClick = { }) { Icon(Icons.Default.Share, contentDescription = null) }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) { Icon(Icons.Default.Star, contentDescription = null) }
        }

        // Caption
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "${post.username}  ${post.caption}",
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Text(
                text = "#foodie #truffle #pasta #delish",
                color = FoodGramOrange,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "View all ${post.comments} comments",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
