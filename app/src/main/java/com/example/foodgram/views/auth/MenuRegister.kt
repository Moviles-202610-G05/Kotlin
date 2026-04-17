package com.example.foodgram.views.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.foodgram.viewmodels.auth.RestaurantRegisterViewModel
import com.example.foodgram.views.SectionHeader
import java.io.File
import java.io.FileOutputStream

data class MenuItem(
    val name: String,
    val price: String,
    val category: String,
    val description: String,
    val imageUri: Uri? = null,
    val inStock: Boolean = true
)

@Composable
fun MenuRegisterView(
    viewModel: RestaurantRegisterViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {
    var dishName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Main Course") }
    var description by remember { mutableStateOf("") }
    var inStock by remember { mutableStateOf(true) }
    var dishImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val menuItems = viewModel.menuItems

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFFF5722))
            }
            Text(
                "Partner with Us",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Progress Indicator ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Create your Menu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Step 2 of 2", color = Color.Gray)
            }
            Text("100%", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
        }

        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = Color(0xFFFF5722),
            trackColor = Color(0xFFFFE0D4)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error Message
        Box(modifier = Modifier.height(40.dp)) {
            viewModel.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // --- Dish Photo ---
        Text(
            "Dish Photo",
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        DishPhotoUploadBox(
            imageUri = dishImageUri,
            onImageSelected = { dishImageUri = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Dish Details ---
        Text("Dish Name", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(
            value = dishName,
            onValueChange = { 
                if (it.length < 25) {
                    if (it != dishName) {
                        dishName = it
                        viewModel.errorMessage = null
                    }
                } else {
                    viewModel.errorMessage = "Dish name must be less than 25 characters"
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            placeholder = { Text("e.g. Truffle Mushroom Pasta", color = Color.Gray) },
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFFFF7043)
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Price ($)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = price,
                    onValueChange = { 
                        if (it.matches(Regex("""^\d{0,2}(\.\d{0,2})?$"""))) {
                            if (it != price) {
                                price = it
                                viewModel.errorMessage = null
                            }
                        } else {
                            viewModel.errorMessage = "Price must be in 00.00 format"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    placeholder = { Text("15.00", color = Color.Gray) },
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedBorderColor = Color(0xFFFF7043)
                    )
                )
            }
            Column(modifier = Modifier.weight(1.2f)) {
                Text("Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                SimpleCategoryDropdown(category) { category = it }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Description", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(
            value = description,
            onValueChange = { 
                if (it.length <= 100) {
                    if (it != description) {
                        description = it
                        viewModel.errorMessage = null
                    }
                } else {
                    viewModel.errorMessage = "Description must be less than 100 characters"
                }
            },
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 4.dp),
            placeholder = { Text("Describe the ingredients, taste, and preparation method...", color = Color.Gray) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFFFF7043)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- In Stock Switch ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFFFF7043))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("In Stock", fontWeight = FontWeight.Bold)
                    Text("Make this dish visible on menu", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = inStock,
                    onCheckedChange = { inStock = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF7043))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Add to Menu Button ---
        Button(
            onClick = {
                if (dishName.isNotBlank() && price.isNotBlank()) {
                    menuItems.add(MenuItem(dishName, price, category, description, dishImageUri, inStock))
                    dishName = ""
                    price = ""
                    description = ""
                    dishImageUri = null
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Text("Add to Menu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Menu List ---
        Text(
            "MENU",
            modifier = Modifier.fillMaxWidth(),
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        menuItems.forEach { item ->
            MenuListItem(item)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Finish Button ---
        Button(
            onClick = onFinishClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043)),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Finish Registration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.RocketLaunch, contentDescription = null)
            }
        }
    }
}

@Composable
fun DishPhotoUploadBox(
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val file = File(context.cacheDir, "dish_photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            onImageSelected(Uri.fromFile(file))
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Upload Dish Photo") },
            text = { Text("Choose a source for your photo") },
            confirmButton = {
                TextButton(onClick = { 
                    galleryLauncher.launch("image/*")
                    showDialog = false 
                }) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        cameraLauncher.launch()
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showDialog = false 
                }) {
                    Text("Camera")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFFF7043).copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .clickable { showDialog = true }
            .drawBehind {
                val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                drawRoundRect(color = Color(0xFFFF7043).copy(alpha = 0.3f), style = stroke, cornerRadius = CornerRadius(24.dp.toPx()))
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Selected Dish Image",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFFFF7043), modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Upload Photo", color = Color(0xFFFF7043), fontWeight = FontWeight.Medium)
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(36.dp).background(Color(0xFFFF7043), CircleShape)
                .clickable { showDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun SimpleCategoryDropdown(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("Main Course", "Appetizer", "Dessert", "Beverage")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            trailingIcon = { Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null) },
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedBorderColor = Color(0xFFFF7043)
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { cat ->
                DropdownMenuItem(text = { Text(cat) }, onClick = { onCategorySelected(cat); expanded = false })
            }
        }
    }
}

@Composable
fun MenuListItem(item: MenuItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.LightGray)
            ) {
                if (item.imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUri),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("\u0024{item.price} • \u0024{item.category}", color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (item.inStock) Color(0xFF4CAF50) else Color(0xFFC5CAE9))
        }
    }
}
