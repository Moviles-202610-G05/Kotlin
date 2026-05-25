package com.example.foodgram.views.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.foodgram.models.feed.Comment
import com.example.foodgram.ui.theme.FoodGramOrange
import com.example.foodgram.viewmodels.feed.CommentsViewModel
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCommentsScreen(
    postId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: CommentsViewModel = viewModel()
) {
    LaunchedEffect(postId) {
        viewModel.loadComments(postId)
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new comment arrives
    LaunchedEffect(viewModel.comments.size) {
        if (viewModel.comments.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.comments.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Comments", fontWeight = FontWeight.Bold)
                        if (viewModel.comments.isNotEmpty()) {
                            Text("${viewModel.comments.size} comments", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            CommentInputBar(
                value = inputText,
                onValueChange = { inputText = it },
                isSending = viewModel.isSending,
                onSend = {
                    viewModel.addComment(postId, inputText) {
                        inputText = ""
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F8F8)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                viewModel.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = FoodGramOrange
                    )
                }

                viewModel.comments.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("No comments yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                        Text("Be the first to comment!", fontSize = 13.sp, color = Color.LightGray)
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(viewModel.comments, key = { it.id }) { comment ->
                            CommentItem(comment = comment)
                        }
                    }
                }
            }

            viewModel.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp),
                    containerColor = Color(0xFFB71C1C)
                ) {
                    Text(error, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar circle with first letter
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(avatarColor(comment.username)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = comment.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(comment.username, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(timeAgo(comment.createdAt), color = Color.Gray, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(comment.text, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Write a comment...", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = FoodGramOrange
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = value.isNotBlank() && !isSending,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = FoodGramOrange)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

private fun timeAgo(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val now = System.currentTimeMillis()
    val diffMs = now - timestamp.toDate().time
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> formatTimestamp(timestamp)
    }
}

private fun avatarColor(username: String): Color {
    val colors = listOf(
        Color(0xFFE53935), Color(0xFF8E24AA), Color(0xFF1E88E5),
        Color(0xFF00897B), Color(0xFF43A047), Color(0xFFFF7043),
        Color(0xFF6D4C41), Color(0xFF546E7A)
    )
    return colors[username.hashCode().and(0x7FFFFFFF) % colors.size]
}
