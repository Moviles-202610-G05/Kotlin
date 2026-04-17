package com.example.foodgram.viewmodels.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodgram.models.feed.Post
import com.example.foodgram.models.feed.PostRepository
import kotlinx.coroutines.launch

class FeedViewModel(private val repository: PostRepository = PostRepository()) : ViewModel() {
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
            posts = repository.getPosts()
            isLoading = false
        }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch {
            // Optimistic UI update
            val updatedPosts = posts.map {
                if (it.id == post.id) {
                    val newIsLiked = !it.isLiked
                    val newLikesCount = if (newIsLiked) it.likesCount + 1 else it.likesCount - 1
                    it.copy(isLiked = newIsLiked, likesCount = newLikesCount)
                } else it
            }
            posts = updatedPosts
            
            repository.toggleLike(post.id, post.isLiked)
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(postId, text)
            // Refresh posts to show updated comment count
            loadPosts()
        }
    }
}
