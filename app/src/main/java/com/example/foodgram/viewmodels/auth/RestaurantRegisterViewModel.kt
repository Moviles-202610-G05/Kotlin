package com.example.foodgram.viewmodels.auth

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.foodgram.views.auth.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class RestaurantRegisterViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --- Restaurant State ---
    var ownerName by mutableStateOf("")
    var restaurantName by mutableStateOf("")
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var address by mutableStateOf("")
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var cuisineType by mutableStateOf("")
    var description by mutableStateOf("")
    var priceRange by mutableStateOf("$$")
    var restaurantImageUri by mutableStateOf<Uri?>(null)

    fun onOwnerNameChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != ownerName) {
                ownerName = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Owner name must be less than 25 characters"
        }
    }

    fun onRestaurantNameChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != restaurantName) {
                restaurantName = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Restaurant name must be less than 25 characters"
        }
    }

    fun onEmailChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != email) {
                email = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Email must be less than 25 characters"
        }
    }

    fun onPhoneChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            if (newValue.length <= 10) {
                if (newValue != phone) {
                    phone = newValue
                    errorMessage = null
                }
            } else {
                errorMessage = "Phone number must be 10 characters"
            }
        } else {
            errorMessage = "Phone number must only contain numbers"
        }
    }

    fun onAddressChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != address) {
                address = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Address must be less than 25 characters"
        }
    }

    fun onUsernameChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != username) {
                username = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Username must be less than 25 characters"
        }
    }

    fun onPasswordChange(newValue: String) {
        if (newValue.length < 25) {
            if (newValue != password) {
                password = newValue
                errorMessage = null
            }
        } else {
            errorMessage = "Password must be less than 25 characters"
        }
    }

    // --- Menu State ---
    val menuItems = mutableStateListOf<MenuItem>()

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    fun finishRegistration(onSuccess: () -> Unit) {
        if (restaurantName.isBlank() || username.isBlank() || password.isBlank()) {
            errorMessage = "Basic restaurant info is required"
            return
        }

        isLoading = true
        errorMessage = null

        // 1. Upload Restaurant Image
        uploadImage(restaurantImageUri, "restaurants") { restaurantImageUrl ->
            // 2. Upload all Menu Item Images
            uploadMenuItems(0, mutableListOf()) { menuWithUrls ->
                // 3. Save Restaurant to Firestore
                saveRestaurantToFirestore(restaurantImageUrl) {
                    // 4. Save Menu Items to Firestore
                    saveMenuItemsToFirestore(menuWithUrls) {
                        // 5. Save Owner User for Login
                        saveOwnerUser(onSuccess)
                    }
                }
            }
        }
    }

    private fun uploadImage(uri: Uri?, folder: String, onComplete: (String?) -> Unit) {
        if (uri == null) {
            onComplete(null)
            return
        }
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val ref = storage.reference.child("$folder/$fileName")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    onComplete(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                errorMessage = "Upload failed: ${it.localizedMessage}"
                isLoading = false
            }
    }

    private fun uploadMenuItems(index: Int, results: MutableList<MenuItem>, onComplete: (List<MenuItem>) -> Unit) {
        if (index >= menuItems.size) {
            onComplete(results)
            return
        }

        val currentItem = menuItems[index]
        if (currentItem.imageUri != null) {
            uploadImage(currentItem.imageUri, "menu") { imageUrl ->
                results.add(currentItem.copy(imageUri = if (imageUrl != null) Uri.parse(imageUrl) else null))
                uploadMenuItems(index + 1, results, onComplete)
            }
        } else {
            results.add(currentItem)
            uploadMenuItems(index + 1, results, onComplete)
        }
    }

    private fun saveRestaurantToFirestore(imageUrl: String?, onComplete: () -> Unit) {
        val restaurantData = hashMapOf(
            "name" to restaurantName,
            "cuisine" to cuisineType,
            "description" to description,
            "restaurantImage" to (imageUrl ?: ""),
            "price" to priceRange,
            "rating" to 5.0, // Default for new
            "nuberReviews" to 0,
            "distance" to "0 km", // Placeholder
            "time" to "20-30 min", // Placeholder
            "lat" to 4.60971, // Placeholder
            "long" to -74.08175, // Placeholder
            "spots" to 100,
            "spotsA" to 100,
            "badge" to "NEW",
            "badge2" to "WELCOME"
        )

        db.collection("restaurants").document(restaurantName).set(restaurantData)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { 
                errorMessage = "Firestore Error: ${it.localizedMessage}"
                isLoading = false
            }
    }

    private fun saveMenuItemsToFirestore(items: List<MenuItem>, onComplete: () -> Unit) {
        if (items.isEmpty()) {
            onComplete()
            return
        }

        val batch = db.batch()
        items.forEach { item ->
            val docRef = db.collection("menu").document()
            val data = hashMapOf(
                "name" to item.name,
                "price" to item.price,
                "description" to item.description,
                "category" to item.category,
                "image" to (item.imageUri?.toString() ?: ""),
                "restaurant" to restaurantName
            )
            batch.set(docRef, data)
        }

        batch.commit()
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener {
                errorMessage = "Menu Error: ${it.localizedMessage}"
                isLoading = false
            }
    }

    private fun saveOwnerUser(onSuccess: () -> Unit) {
        val userData = hashMapOf(
            "name" to ownerName,
            "username" to username,
            "email" to email,
            "password" to password,
            "roll" to "OWNER",
            "restaurant" to restaurantName
        )

        db.collection("user").add(userData)
            .addOnSuccessListener { 
                isLoading = false
                onSuccess() 
            }
            .addOnFailureListener {
                errorMessage = "User Error: ${it.localizedMessage}"
                isLoading = false
            }
    }
}
