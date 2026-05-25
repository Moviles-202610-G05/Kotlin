package com.example.foodgram.views.profile

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodgram.data.cache.FastCache
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.data.local.entities.ReviewEntity
import com.example.foodgram.models.restaurants.ReviewRestaurant
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserReviewsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val reviewDao = AppDatabase.getDatabase(application).reviewDao()
    private val fastCache = FastCache

    var userReviews by mutableStateOf<List<ReviewRestaurant>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun loadUserReviews(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true

            // 1. Caching
            val cached = fastCache.get("user_reviews_$userId") as? List<ReviewRestaurant>
            if (cached != null) {
                userReviews = cached
            }

            // 2. Local Storage Fallback
            if (userReviews.isEmpty()) {
                val localData = reviewDao.getReviewsByUser(userId).first()
                if (localData.isNotEmpty()) {
                    userReviews = localData.map { it.toReviewRestaurant() }
                }
            }

            // 3. Network Fetch
            try {
                val remoteData = db.collection("reviews")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                    .toObjects(ReviewRestaurant::class.java)

                if (remoteData.isNotEmpty()) {
                    // Update Cache and DB
                    fastCache.put("user_reviews_$userId", remoteData)
                    reviewDao.insertReviews(remoteData.map { it.toEntity() })
                    userReviews = remoteData.sortedByDescending { it.createdAt }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun ReviewRestaurant.toEntity() = ReviewEntity(
        id = id,
        restaurant = restaurant,
        userId = userId,
        name = name,
        rating = rating,
        comment = comment,
        date = date,
        avatar = avatar,
        avatarColor = avatarColor,
        createdAt = createdAt?.seconds
    )

    private fun ReviewEntity.toReviewRestaurant() = ReviewRestaurant(
        id = id,
        restaurant = restaurant,
        userId = userId,
        name = name,
        rating = rating,
        comment = comment,
        date = date,
        avatar = avatar,
        avatarColor = avatarColor,
        createdAt = createdAt?.let { com.google.firebase.Timestamp(it, 0) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserReviewsScreen(
    userId: String,
    onBackClick: () -> Unit,
    viewModel: UserReviewsViewModel = viewModel()
) {
    LaunchedEffect(userId) {
        viewModel.loadUserReviews(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reviews", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (viewModel.isLoading && viewModel.userReviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        } else if (viewModel.userReviews.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You haven't written any reviews yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF9F9F9))
            ) {
                items(viewModel.userReviews) { review ->
                    UserReviewCard(review)
                }
            }
        }
    }
}

@Composable
fun UserReviewCard(review: ReviewRestaurant) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Show Restaurant Name prominent instead of User Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.restaurant, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(review.date, color = Color.Gray, fontSize = 12.sp)
                }
                Row {
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i < review.rating) Color(0xFFFF9800) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(review.comment, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}