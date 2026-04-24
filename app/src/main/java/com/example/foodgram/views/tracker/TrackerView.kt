package com.example.foodgram.views.tracker

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
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.foodgram.models.tracker.MealHistoryItem
import java.util.Locale
import com.example.foodgram.ui.theme.OrangeFoodGram
import com.example.foodgram.viewmodels.tracker.TrackerViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: TrackerViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.fetchMealHistory()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AI Food Scanner", 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5722)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFFFF5722))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Analyze Meal", 
                        style = MaterialTheme.typography.headlineSmall, 
                        fontWeight = FontWeight.Bold
                    )
                    Text("Powered by FoodGram AI", color = Color.Gray)
                }
                Icon(
                    Icons.Default.AutoAwesome, 
                    contentDescription = null, 
                    tint = OrangeFoodGram,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Image Preview / Selector (Improved UI)
            PhotoUploadBox(
                imageUri = selectedImageUri,
                isLoading = viewModel.isLoading,
                onImageSelected = { uri ->
                    selectedImageUri = uri
                    uri?.let { viewModel.analyzeImage(context, it) }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator(color = OrangeFoodGram)
                Text("Analyzing your meal...", modifier = Modifier.padding(top = 16.dp))
            }

            viewModel.errorMessage?.let {
                if (viewModel.analysisResult == null) {
                    Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }

            viewModel.analysisResult?.let { result ->
                AnalysisResultCard(result)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        viewModel.saveMealToHistory {
                            onSaveSuccess()
                            selectedImageUri = null
                        } 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeFoodGram),
                    enabled = !viewModel.isSaving
                ) {
                    if (viewModel.isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Saving...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to My History", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (viewModel.analysisResult == null && !viewModel.isLoading) {
                MealHistorySection(viewModel)
            }
        }
    }
}

@Composable
fun MealHistorySection(viewModel: TrackerViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Recent Scans",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (viewModel.isHistoryLoading) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeFoodGram)
            }
        } else if (viewModel.mealHistory.value.isEmpty()) {
            Text(
                "No scan history yet.",
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            viewModel.mealHistory.value.forEach { meal ->
                HistoryItem(meal)
            }
        }
    }
}

@Composable
fun HistoryItem(meal: MealHistoryItem) {
    val date = try {
        val sdfIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val dateObj = sdfIn.parse(meal.timestamp)
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(dateObj ?: Date())
    } catch (e: Exception) {
        meal.timestamp
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal Image
            Image(
                painter = rememberAsyncImagePainter(meal.imagePath),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    meal.dishName, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    maxLines = 1, 
                    overflow = TextOverflow.Ellipsis,
                    color = Color.DarkGray
                )
                Text(date, fontSize = 12.sp, color = Color.Gray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${meal.totalCalories.toInt()} kcal", 
                    fontWeight = FontWeight.Bold, 
                    color = OrangeFoodGram,
                    fontSize = 16.sp
                )
                Row {
                    NutrientMiniTag("P", "${meal.totalProteinG.toInt()}g")
                    Spacer(modifier = Modifier.width(4.dp))
                    NutrientMiniTag("C", "${meal.totalCarbsG.toInt()}g")
                    Spacer(modifier = Modifier.width(4.dp))
                    NutrientMiniTag("F", "${meal.totalFatG.toInt()}g")
                }
            }
        }
    }
}

@Composable
fun NutrientMiniTag(label: String, value: String) {
    Text(
        text = "$label:$value",
        fontSize = 10.sp,
        color = Color.Gray,
        modifier = Modifier
            .background(Color(0xFFF8F9FA), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
fun AnalysisResultCard(result: com.example.foodgram.models.tracker.MealAnalysis) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = result.dishName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeFoodGram
            )
            
            Text(
                text = "Confidence: ${result.confidence.uppercase()}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutrientInfo("Calories", String.format(Locale.US, "%.0f", result.totalCalories), "kcal")
                NutrientInfo("Protein", String.format(Locale.US, "%.1f", result.macronutrientsTotals.proteinG), "g")
                NutrientInfo("Carbs", String.format(Locale.US, "%.1f", result.macronutrientsTotals.carbsG), "g")
                NutrientInfo("Fat", String.format(Locale.US, "%.1f", result.macronutrientsTotals.fatG), "g")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Detected Components:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            result.components.forEach { component ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• ${component.food} (${component.portion})", fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Text(String.format(Locale.US, "%.0f kcal", component.calories), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PhotoUploadBox(
    imageUri: Uri?,
    isLoading: Boolean,
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
            val file = File(context.cacheDir, "meal_photo_${System.currentTimeMillis()}.jpg")
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

    if (showDialog && !isLoading) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Food Photo", fontWeight = FontWeight.Bold) },
            text = { Text("Take a photo of your meal or select one from your gallery for AI analysis.") },
            confirmButton = {
                Button(
                    onClick = { 
                        galleryLauncher.launch("image/*")
                        showDialog = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeFoodGram)
                ) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch()
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        showDialog = false 
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangeFoodGram)
                ) {
                    Text("Camera", color = OrangeFoodGram)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                color = if (isLoading) Color.LightGray.copy(alpha = 0.1f) else OrangeFoodGram.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = !isLoading) { showDialog = true }
            .drawBehind {
                val stroke = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
                drawRoundRect(
                    color = if (isLoading) Color.Gray.copy(alpha = 0.4f) else OrangeFoodGram.copy(alpha = 0.4f),
                    style = stroke,
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .then(if (isLoading) Modifier.alpha(0.5f) else Modifier),
                contentScale = ContentScale.Crop
            )
            if (isLoading) {
                CircularProgressIndicator(color = OrangeFoodGram)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            if (isLoading) Color.Gray.copy(alpha = 0.1f) else OrangeFoodGram.copy(alpha = 0.1f), 
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color.Gray)
                    } else {
                        Icon(
                            Icons.Default.CloudUpload, 
                            contentDescription = null, 
                            tint = OrangeFoodGram, 
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    if (isLoading) "Processing..." else "Upload Meal Photo", 
                    color = if (isLoading) Color.Gray else OrangeFoodGram, 
                    fontWeight = FontWeight.Bold
                )
                Text("Analyze calories & nutrients instantly", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun NutrientInfo(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = unit, fontSize = 10.sp, color = Color.Gray)
    }
}
