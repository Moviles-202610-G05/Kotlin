package com.example.foodgram.viewmodels.feed

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.feed.Comment
import com.example.foodgram.models.feed.PostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class CommentsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository()
    private val auth = FirebaseAuth.getInstance()

    var comments by mutableStateOf<List<Comment>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadComments(postId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                comments = repository.getComments(postId)
            } catch (e: Exception) {
                errorMessage = "Failed to load comments"
            }
            isLoading = false
        }
    }

    fun addComment(postId: String, text: String, onSuccess: () -> Unit = {}) {
        if (text.isBlank()) return
        viewModelScope.launch {
            isSending = true
            errorMessage = null
            try {
                repository.addComment(postId, text)
                comments = repository.getComments(postId)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Failed to post comment"
            }
            isSending = false
        }
    }

    val currentUsername: String
        get() = auth.currentUser?.displayName ?: "Anonymous"
}
