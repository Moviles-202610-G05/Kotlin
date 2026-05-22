package com.example.foodgram.viewmodels.feed

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.data.cache.FastCache
import com.example.foodgram.data.local.AppDatabase
import com.example.foodgram.data.local.entities.PostEntity
import com.example.foodgram.models.feed.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = AppDatabase.getDatabase(application)
    private val postDao = database.postDao()
    private val fastCache = FastCache

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            isLoading = true

            // 1. FastCache check
            val cached = fastCache.get("feed_posts") as? List<Post>
            if (cached != null) {
                posts = cached
            }

            // 2. Room check
            if (posts.isEmpty()) {
                val local = postDao.getAllPosts().first()
                if (local.isNotEmpty()) {
                    posts = local.map { it.toPost() }
                }
            }

            try {
                val currentUserId = auth.currentUser?.uid
                // 3. Firebase fetch
                val snapshot = db.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()
                
                val basePosts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }

                if (basePosts.isNotEmpty()) {
                    posts = basePosts
                    
                    if (currentUserId != null) {
                        val updatedPosts = basePosts.map { post ->
                            async {
                                try {
                                    val likeDoc = db.collection("posts").document(post.id)
                                        .collection("likes").document(currentUserId).get().await()
                                    post.copy(isLiked = likeDoc.exists())
                                } catch (e: Exception) {
                                    post
                                }
                            }
                        }.awaitAll()
                        posts = updatedPosts
                    }

                    // Sync Cache & Room
                    fastCache.put("feed_posts", posts)
                    postDao.deleteAll()
                    postDao.insertPosts(posts.map { it.toEntity() })
                }
            } catch (e: Exception) {
                // Keep existing
            }
            isLoading = false
        }
    }

    private fun Post.toEntity() = PostEntity(
        id = id,
        username = username,
        userId = userId,
        restaurantName = restaurantName,
        restaurantId = restaurantId,
        profilePhoto = profilePhoto,
        description = description,
        photoUrl = photoUrl,
        likesCount = likesCount,
        commentsCount = commentsCount,
        createdAt = createdAt?.seconds,
        isLiked = isLiked
    )

    private fun PostEntity.toPost() = Post(
        id = id,
        username = username,
        userId = userId,
        restaurantName = restaurantName,
        restaurantId = restaurantId,
        profilePhoto = profilePhoto,
        description = description,
        photoUrl = photoUrl,
        likesCount = likesCount,
        commentsCount = commentsCount,
        createdAt = createdAt?.let { com.google.firebase.Timestamp(it, 0) },
        isLiked = isLiked
    )

    fun toggleLike(post: Post) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val currentIsLiked = post.isLiked
            
            // Optimistic UI update
            posts = posts.map {
                if (it.id == post.id) {
                    val newIsLiked = !it.isLiked
                    val newLikesCount = if (newIsLiked) it.likesCount + 1 else it.likesCount - 1
                    it.copy(isLiked = newIsLiked, likesCount = newLikesCount)
                } else it
            }
            
            try {
                val postRef = db.collection("posts").document(post.id)
                val likeRef = postRef.collection("likes").document(userId)

                db.runTransaction { transaction ->
                    if (currentIsLiked) {
                        transaction.delete(likeRef)
                        transaction.update(postRef, "likesCount", FieldValue.increment(-1))
                    } else {
                        transaction.set(likeRef, mapOf(
                            "userId" to userId,
                            "createdAt" to FieldValue.serverTimestamp()
                        ))
                        transaction.update(postRef, "likesCount", FieldValue.increment(1))
                    }
                }.await()
            } catch (e: Exception) {
                // Rollback on failure
                loadPosts()
            }
        }
    }

    fun addComment(postId: String, text: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                val commentRef = postRef.collection("comments").document()

                db.runBatch { batch ->
                    batch.set(commentRef, mapOf(
                        "userId" to user.uid,
                        "username" to (user.displayName ?: "Anonymous"),
                        "text" to text,
                        "createdAt" to FieldValue.serverTimestamp()
                    ))
                    batch.update(postRef, "commentsCount", FieldValue.increment(1))
                }.await()
                loadPosts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
