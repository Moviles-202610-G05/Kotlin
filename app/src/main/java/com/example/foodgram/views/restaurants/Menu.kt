package com.example.foodgram.views.restaurants

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import coil.compose.AsyncImage
import com.example.foodgram.R
import com.example.foodgram.models.restaurants.MenuItem
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.views.components.FoodGramNavigationBar
import com.example.foodgram.views.components.FoodGramScreen
import com.example.foodgram.viewmodels.restaurants.MenuManagementViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RestaurantMenuManagementScreen(
    viewModel: MenuManagementViewModel = viewModel(),
    onNavigateToFeed: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMap: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Items") }

    val emptySeats = uiState.emptySeats
    val occupiedSeats = uiState.occupiedSeats
    val totalSeats = uiState.totalSeats
    val menuItems = uiState.menuItems

    Scaffold(
        bottomBar = {
            FoodGramNavigationBar(
                currentScreen = FoodGramScreen.MENU,
                onNavigateToFeed = onNavigateToFeed,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToMenu = { /* Already here */ },
                onNavigateToMap = onNavigateToMap
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = "Logo",
                        tint = FoodGramOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        uiState.restaurantName ?: "FoodGram",
                        color = FoodGramOrange,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = { /* Add New logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = FoodGramOrange),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add New", fontSize = 14.sp)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search dishes by name or category...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FoodGramOrange) },
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedBorderColor = FoodGramOrange
                ),
                singleLine = true
            )

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All Items", "Appetizers", "Main Courses").forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FoodGramOrange,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = Color.LightGray.copy(alpha = 0.5f),
                            selectedBorderColor = Color.Transparent,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Capacity Management Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFF5F2), // Very light orange/peach
                border = androidx.compose.foundation.BorderStroke(1.dp, FoodGramOrange.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventSeat, contentDescription = null, tint = FoodGramOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurant Capacity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Live Update", color = FoodGramOrange, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Empty", color = Color.Gray, fontSize = 12.sp)
                                Text("$emptySeats", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Occupied", color = Color.Gray, fontSize = 12.sp)
                                Text("$occupiedSeats", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Total", color = Color.Gray, fontSize = 12.sp)
                                Text("$totalSeats", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.updateEmptySeats(emptySeats - 1) },
                                modifier = Modifier.size(32.dp).background(Color.White, CircleShape).border(1.dp, Color.LightGray, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { viewModel.updateEmptySeats(emptySeats + 1) },
                                modifier = Modifier.size(32.dp).background(FoodGramOrange, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Menu Items List
            val filteredItems = menuItems.filter {
                (selectedCategory == "All Items" || it.category == selectedCategory) &&
                it.name.contains(searchQuery, ignoreCase = true)
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        selectedCategory.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FoodGramOrange
                    )
                    Text(
                        "${filteredItems.size} ITEMS",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems) { item ->
                    MenuManagementItemCard(
                        item = item,
                        onStockChange = { updatedInStock ->
                            viewModel.updateStock(item.id, updatedInStock)
                        },
                        onDeleteClick = {
                            viewModel.deleteMenuItem(item.id)
                        },
                        onPriceUpdate = { newPrice ->
                            viewModel.updatePrice(item.id, newPrice)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Save Changes logic */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FoodGramOrange)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save All Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun MenuManagementItemCard(
    item: MenuItem,
    onStockChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onPriceUpdate: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditPriceDialog by remember { mutableStateOf(false) }
    var newPrice by remember { mutableStateOf(item.price) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete ${item.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditPriceDialog) {
        AlertDialog(
            onDismissRequest = { showEditPriceDialog = false },
            title = { Text("Update Price") },
            text = {
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text("Price") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPriceUpdate(newPrice)
                        showEditPriceDialog = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPriceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(90.dp)) {
                AsyncImage(
                    model = item.image,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(
                        "$${item.price}",
                        fontWeight = FontWeight.Bold,
                        color = FoodGramOrange,
                        fontSize = 16.sp
                    )
                }
                Text(
                    item.description,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row {
                        IconButton(
                            onClick = { showEditPriceDialog = true },
                            modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp).background(Color(0xFFF5F5F5), CircleShape)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (item.inStock) "IN STOCK" else "OUT OF STOCK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.inStock) Color.Gray else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = item.inStock,
                            onCheckedChange = onStockChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = FoodGramOrange,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }
            }
        }
    }
}
