package com.example.foodgram.views.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.foodgram.data.local.entities.SavedRestaurantEntity
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.viewmodels.saved.SavedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToRestaurantDetail: (String) -> Unit = {},
    viewModel: SavedViewModel = viewModel()
) {
    val savedItems by viewModel.savedItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Restaurants", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { innerPadding ->
        if (savedItems.isEmpty()) {
            SavedEmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedItems, key = { it.id }) { item ->
                    SavedRestaurantCard(
                        item = item,
                        onUnsave = { viewModel.unsave(item.restaurantId) },
                        onClick = { onNavigateToRestaurantDetail(item.restaurantId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
            Text("No saved restaurants yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Gray)
            Text("Tap the heart icon on any restaurant\nto save it here.", fontSize = 14.sp, color = Color.LightGray)
        }
    }
}

@Composable
private fun SavedRestaurantCard(
    item: SavedRestaurantEntity,
    onUnsave: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.image.isNotBlank()) {
                AsyncImage(
                    model = item.image,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0F0F0)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0F0F0)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(13.dp))
                    Text(" ${"%.1f".format(item.rating)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("  •  ${item.cuisine}", fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.price, fontSize = 12.sp, color = FoodGramOrange, fontWeight = FontWeight.Bold)
                    if (item.badge.isNotBlank()) {
                        Surface(color = FoodGramOrange.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                            Text(item.badge, color = FoodGramOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            IconButton(onClick = onUnsave) {
                Icon(Icons.Default.BookmarkRemove, contentDescription = "Remove from saved", tint = FoodGramOrange)
            }
        }
    }
}
