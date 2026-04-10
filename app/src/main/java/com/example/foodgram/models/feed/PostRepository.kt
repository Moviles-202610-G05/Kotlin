package com.example.foodgram.models.feed

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class Post(
    val id: String = "",
    val username: String = "",
    val userId: String = "",
    val restaurantName: String = "",
    val restaurantId: String = "",
    val profilePhoto: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val likesCount: Long = 0,
    val commentsCount: Long = 0,
    val createdAt: Timestamp? = null,
    val isLiked: Boolean = false
)

data class Comment(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
)

class PostRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getPosts(): List<Post> {
        return try {
            val currentUserId = auth.currentUser?.uid
            val snapshot = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            // Map the documents to Post objects first
            val basePosts = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            }

            // If user is not logged in, just return posts without like status
            if (currentUserId == null) return basePosts

            // In a real app, you might want to optimize this to avoid many network calls
            // For now, let's keep it simple but ensure it doesn't get stuck
            basePosts.map { post ->
                try {
                    val likeDoc = db.collection("posts").document(post.id)
                        .collection("likes").document(currentUserId).get().await()
                    post.copy(isLiked = likeDoc.exists())
                } catch (e: Exception) {
                    post // If like check fails, assume not liked
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleLike(postId: String, currentIsLiked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = db.collection("posts").document(postId)
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
    }

    suspend fun addComment(postId: String, text: String) {
        val user = auth.currentUser ?: return
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
    }

    suspend fun getComments(postId: String): List<Comment> {
        return try {
            val snapshot = db.collection("posts").document(postId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
