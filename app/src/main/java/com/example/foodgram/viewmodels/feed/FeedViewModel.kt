package com.example.foodgram.viewmodels.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.feed.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class FeedViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
            try {
                val currentUserId = auth.currentUser?.uid
                // 1. Limit to last 50 posts and order by newest
                val snapshot = db.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()
                
                val basePosts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                }

                // 2. Draw interface immediately with base data (without likes)
                posts = basePosts

                if (currentUserId != null) {
                    // 3. Fetch all likes in parallel using async/awaitAll
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
                    
                    // 4. Update the UI again once likes are known
                    posts = updatedPosts
                }
            } catch (e: Exception) {
                // Keep existing posts or show empty
            }
            isLoading = false
        }
    }

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
