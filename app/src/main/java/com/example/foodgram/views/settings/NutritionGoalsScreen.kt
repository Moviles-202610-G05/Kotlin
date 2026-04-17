package com.example.foodgram.views.settings


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodgram.ui.theme.OrangeFoodGram
import com.example.foodgram.utils.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionGoalsScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val userId = UserSession.currentUserDocId

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var calories by remember { mutableStateOf(2000.0) }
    var protein by remember { mutableStateOf(150.0) }
    var carbs by remember { mutableStateOf(200.0) }
    var fat by remember { mutableStateOf(67.0) }

    var caloriesText by remember { mutableStateOf("2000") }
    var proteinText by remember { mutableStateOf("150") }
    var carbsText by remember { mutableStateOf("200") }
    var fatText by remember { mutableStateOf("67") }

    val proteinPct = 0.30
    val carbsPct = 0.40
    val fatPct = 0.30

    val maxProtein = 300.0
    val maxCarbs = 500.0
    val maxFat = 200.0

    fun syncFromCalories(newCalories: Double) {
        calories = newCalories
        protein = (newCalories * proteinPct) / 4.0
        carbs = (newCalories * carbsPct) / 4.0
        fat = (newCalories * fatPct) / 9.0

        caloriesText = calories.roundToInt().toString()
        proteinText = protein.roundToInt().toString()
        carbsText = carbs.roundToInt().toString()
        fatText = fat.roundToInt().toString()
    }

    fun syncFromProtein(newProtein: Double) {
        protein = newProtein
        calories = (protein * 4.0) + (carbs * 4.0) + (fat * 9.0)

        caloriesText = calories.roundToInt().toString()
        proteinText = protein.roundToInt().toString()
    }

    fun syncFromCarbs(newCarbs: Double) {
        carbs = newCarbs
        calories = (protein * 4.0) + (carbs * 4.0) + (fat * 9.0)

        caloriesText = calories.roundToInt().toString()
        carbsText = carbs.roundToInt().toString()
    }

    fun syncFromFat(newFat: Double) {
        fat = newFat
        calories = (protein * 4.0) + (carbs * 4.0) + (fat * 9.0)

        caloriesText = calories.roundToInt().toString()
        fatText = fat.roundToInt().toString()
    }

    LaunchedEffect(userId) {
        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("user").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val loadedCalories = snapshot.getDouble("caloriesGoal") ?: 2000.0
                    val loadedProtein = snapshot.getDouble("proteinGoal") ?: 150.0
                    val loadedCarbs = snapshot.getDouble("carbsGoal") ?: 200.0
                    val loadedFat = snapshot.getDouble("fatGoal") ?: 67.0

                    calories = loadedCalories
                    protein = loadedProtein
                    carbs = loadedCarbs
                    fat = loadedFat

                    caloriesText = loadedCalories.roundToInt().toString()
                    proteinText = loadedProtein.roundToInt().toString()
                    carbsText = loadedCarbs.roundToInt().toString()
                    fatText = loadedFat.roundToInt().toString()
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun saveChanges() {
        if (userId == null) {
            Toast.makeText(navController.context, "No active session", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true

        val updates = mapOf(
            "caloriesGoal" to calories,
            "proteinGoal" to protein,
            "carbsGoal" to carbs,
            "fatGoal" to fat
        )

        db.collection("user").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                isSaving = false
                Toast.makeText(navController.context, "Nutrition goals updated", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            .addOnFailureListener { e ->
                isSaving = false
                Toast.makeText(navController.context, e.localizedMessage ?: "Error saving changes", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Nutrition Goals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = OrangeFoodGram
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Button(
                    onClick = { saveChanges() },
                    enabled = !isLoading && !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeFoodGram),
                    shape = MaterialTheme.shapes.large
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save Changes",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeFoodGram)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Calorie Targets",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Daily Calorie Goal",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            AssistChip(
                                onClick = { },
                                label = { Text("Active Goal") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = caloriesText,
                                onValueChange = { newValue ->
                                    caloriesText = newValue
                                    val parsed = newValue.toDoubleOrNull()
                                    if (parsed != null && parsed > 0) {
                                        syncFromCalories(parsed)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                placeholder = {
                                    Text("0", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                            Text(
                                text = "KCAL",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row {
                            GoalChip("Protein 30%", OrangeFoodGram)
                            Spacer(modifier = Modifier.width(8.dp))
                            GoalChip("Carbs 40%", androidx.compose.ui.graphics.Color(0xFFFF9800))
                            Spacer(modifier = Modifier.width(8.dp))
                            GoalChip("Fat 30%", androidx.compose.ui.graphics.Color(0xFFFFC107))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Macronutrient Distribution",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        MacroRow(
                            label = "Protein",
                            valueText = proteinText,
                            value = protein,
                            max = maxProtein,
                            onTextChanged = { val parsed = it.toDoubleOrNull(); if (parsed != null && parsed in 0.0..maxProtein) syncFromProtein(parsed) },
                            onSliderChanged = { syncFromProtein(it.toDouble()) },
                            color = androidx.compose.ui.graphics.Color(0xFFFF5252)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        MacroRow(
                            label = "Carbs",
                            valueText = carbsText,
                            value = carbs,
                            max = maxCarbs,
                            onTextChanged = { val parsed = it.toDoubleOrNull(); if (parsed != null && parsed in 0.0..maxCarbs) syncFromCarbs(parsed) },
                            onSliderChanged = { syncFromCarbs(it.toDouble()) },
                            color = androidx.compose.ui.graphics.Color(0xFFFF9800)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        MacroRow(
                            label = "Fat",
                            valueText = fatText,
                            value = fat,
                            max = maxFat,
                            onTextChanged = { val parsed = it.toDoubleOrNull(); if (parsed != null && parsed in 0.0..maxFat) syncFromFat(parsed) },
                            onSliderChanged = { syncFromFat(it.toDouble()) },
                            color = androidx.compose.ui.graphics.Color(0xFFFFC107)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun GoalChip(text: String, color: androidx.compose.ui.graphics.Color) {
    AssistChip(
        onClick = { },
        label = { Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color
        )
    )
}

@Composable
private fun MacroRow(
    label: String,
    valueText: String,
    value: Double,
    max: Double,
    onTextChanged: (String) -> Unit,
    onSliderChanged: (Float) -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = color,
                modifier = Modifier.size(12.dp)
            ) { }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    TextField(
                        value = valueText,
                        onValueChange = onTextChanged,
                        modifier = Modifier.width(64.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                    Text(
                        text = "g",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value.toFloat().coerceIn(0f, max.toFloat()),
            onValueChange = onSliderChanged,
            valueRange = 0f..max.toFloat(),
            colors = SliderDefaults.colors(
                activeTrackColor = color,
                thumbColor = color
            )
        )
    }
}