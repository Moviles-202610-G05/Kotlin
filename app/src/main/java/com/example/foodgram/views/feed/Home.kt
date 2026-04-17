package com.example.foodgram.views.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.viewmodels.feed.FeedViewModel
import com.example.foodgram.models.feed.Post
import java.text.SimpleDateFormat
import java.util.*

fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {},
    onNavigateToMap: (String?) -> Unit = {},
    onNavigateToRestaurantDetail: (String) -> Unit = {},
    viewModel: FeedViewModel = viewModel()
) {
    var showCommentDialog by remember { mutableStateOf<Post?>(null) }

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
                    icon = { Icon(Icons.Default.Camera, contentDescription = "Scan") },
                    label = { Text("SCAN") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToMap(null) },
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
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = FoodGramOrange)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    items(viewModel.posts) { post ->
                        PostItem(
                            post = post,
                            onLikeClick = { viewModel.toggleLike(post) },
                            onCommentClick = { showCommentDialog = post },
                            onRestaurantClick = { onNavigateToRestaurantDetail(post.restaurantId) }
                        )
                    }
                }
            }

            if (showCommentDialog != null) {
                CommentDialog(
                    post = showCommentDialog!!,
                    onDismiss = { showCommentDialog = null },
                    onSendComment = { text ->
                        viewModel.addComment(showCommentDialog!!.id, text)
                        showCommentDialog = null
                    }
                )
            }
        }
    }
}

@Composable
fun CommentDialog(
    post: Post,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Comment") },
        text = {
            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Write a comment...") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (commentText.isNotBlank()) onSendComment(commentText) },
                enabled = commentText.isNotBlank()
            ) {
                Text("Post", color = FoodGramOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRestaurantClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!post.profilePhoto.isNullOrEmpty()) {
                AsyncImage(
                    model = post.profilePhoto,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.clickable { onRestaurantClick() }) {
                Text(post.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.restaurantName} • ${formatTimestamp(post.createdAt)}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
        }

        // Image
        AsyncImage(
            model = post.photoUrl,
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(Color(0xFFEEEEEE))
                .clickable { onRestaurantClick() },
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Actions
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) Color.Red else Color.Black
                )
            }
            Text("${post.likesCount}", modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
            
            IconButton(onClick = onCommentClick) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment")
            }
            Text("${post.commentsCount}", modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
            
            IconButton(onClick = { }) { Icon(Icons.Default.Share, contentDescription = null) }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) { Icon(Icons.Default.StarBorder, contentDescription = null) }
        }

        // Caption
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "${post.username}  ${post.description}",
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (post.commentsCount > 0) {
                TextButton(
                    onClick = onCommentClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = "View all ${post.commentsCount} comments",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
