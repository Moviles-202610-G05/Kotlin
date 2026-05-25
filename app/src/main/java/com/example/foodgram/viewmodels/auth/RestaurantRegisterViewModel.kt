package com.example.foodgram.viewmodels.auth

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.foodgram.models.restaurants.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.foodgram.models.ui.RestaurantRegisterForm
import com.example.foodgram.models.auth.Restaurant
import com.example.foodgram.models.auth.User
import com.example.foodgram.models.auth.UserRole
import com.example.foodgram.utils.UserSession
import java.util.UUID

class RestaurantRegisterViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var form by mutableStateOf(RestaurantRegisterForm())
        private set

    val menuItems = mutableStateListOf<MenuItem>()

    fun onOwnerNameChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(ownerName = newValue)
            errorMessage = null
        } else {
            errorMessage = "Owner name must be less than 25 characters"
        }
    }

    fun onRestaurantNameChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(restaurantName = newValue)
            errorMessage = null
        } else {
            errorMessage = "Restaurant name must be less than 25 characters"
        }
    }

    fun onEmailChange(newValue: String) {
        if (newValue.length < 35) {
            form = form.copy(email = newValue)
            errorMessage = null
        } else {
            errorMessage = "Email must be less than 35 characters"
        }
    }

    fun onPhoneChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            if (newValue.length <= 10) {
                form = form.copy(phone = newValue)
                errorMessage = null
            } else {
                errorMessage = "Phone number must be 10 characters"
            }
        } else {
            errorMessage = "Phone number must only contain numbers"
        }
    }

    fun onAddressChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(address = newValue)
            errorMessage = null
        } else {
            errorMessage = "Address must be less than 25 characters"
        }
    }

    fun onUsernameChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(username = newValue)
            errorMessage = null
        } else {
            errorMessage = "Username must be less than 25 characters"
        }
    }

    fun onPasswordChange(newValue: String) {
        if (newValue.length < 25) {
            form = form.copy(password = newValue)
            errorMessage = null
        } else {
            errorMessage = "Password must be less than 25 characters"
        }
    }

    fun onCuisineTypeChange(newValue: String) {
        form = form.copy(cuisineType = newValue)
    }

    fun onRestaurantImageChange(newValue: Uri?) {
        form = form.copy(restaurantImageUri = newValue)
    }

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    fun addMenuItemStepByStep(item: MenuItem) {
        val index = menuItems.size
        val temporaryItem = item.copy(isUploading = true)
        menuItems.add(temporaryItem)

        uploadImage(item.imageUri, "menu") { imageUrl ->
            val finalImageUrl = imageUrl ?: ""
            val menuData = hashMapOf(
                "name" to item.name,
                "price" to item.price,
                "description" to item.description,
                "category" to item.category,
                "image" to finalImageUrl,
                "restaurant" to form.restaurantName,
                "availability" to (if (item.inStock) 1 else 0)
            )

            db.collection("menu").add(menuData)
                .addOnSuccessListener { docRef ->
                    val updatedItem = temporaryItem.copy(
                        id = docRef.id,
                        image = finalImageUrl,
                        isUploading = false
                    )
                    if (index < menuItems.size) {
                        menuItems[index] = updatedItem
                    }
                }
                .addOnFailureListener {
                    errorMessage = "Failed to register dish: ${it.localizedMessage}"
                    if (index < menuItems.size) {
                        menuItems[index] = temporaryItem.copy(isUploading = false)
                    }
                }
        }
    }

    fun finishRegistration(onSuccess: () -> Unit) {
        if (form.restaurantName.isBlank() || form.username.isBlank() || form.password.isBlank()) {
            errorMessage = "Basic restaurant info is required"
            return
        }

        isLoading = true
        errorMessage = null

        uploadImage(form.restaurantImageUri, "restaurants") { restaurantImageUrl ->
            uploadMenuItems(0, mutableListOf()) { menuWithUrls ->
                saveRestaurantToFirestore(restaurantImageUrl) {
                    saveMenuItemsToFirestore(menuWithUrls) {
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
        val restaurant = Restaurant(
            name = form.restaurantName,
            cuisine = form.cuisineType,
            description = form.description,
            restaurantImage = imageUrl ?: "",
            price = form.priceRange,
            ownerEmail = form.email,
            phone = form.phone,
            address = form.address
        )

        db.collection("restaurants").document(form.restaurantName).set(restaurant)
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
                "restaurant" to form.restaurantName
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
        auth.createUserWithEmailAndPassword(form.email, form.password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val user = User(
                        uid = uid,
                        name = form.ownerName,
                        username = form.username,
                        email = form.email,
                        roll = UserRole.RESTAURANTE,
                        restaurantName = form.restaurantName
                    )

                    db.collection("user").add(user)
                        .addOnSuccessListener { documentReference ->
                            // Save auth data to session to prevent missing name in UI
                            UserSession.currentUserDocId = documentReference.id
                            UserSession.currentUserRole = UserRole.RESTAURANTE.name
                            UserSession.currentUserName = form.ownerName

                            isLoading = false
                            onSuccess()
                        }
                        .addOnFailureListener {
                            errorMessage = "Auth successful but database failed: ${it.localizedMessage}"
                            isLoading = false
                        }
                }
            }
            .addOnFailureListener {
                errorMessage = "Authentication failed: ${it.localizedMessage}"
                isLoading = false
            }
    }
}