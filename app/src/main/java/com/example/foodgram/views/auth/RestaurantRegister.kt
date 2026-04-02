package com.example.foodgram.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.foodgram.ui.theme.OrangeFoodGram

data class RestaurantFormState(
    val ownerName: String = "",
    val restaurantName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val username: String = "",
    val cuisineType: String = ""
)

@Composable
fun RestaurantRegisterView(onBackClick: () -> Unit = {}) {
    var formState by remember { mutableStateOf(RestaurantFormState()) }

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
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Progress Indicator ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Create Account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Step 1 of 2: Basic Information", color = Color.Gray)
            }
            Text("50%", color = Color(0xFFFF5722), fontWeight = FontWeight.Bold)
        }

        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = Color(0xFFFF5722),
            trackColor = Color(0xFFFFE0D4)
        )

        // --- Form Sections ---
        SectionHeader("OWNER DETAILS")
        CustomInputField(
            value = formState.ownerName,
            onValueChange = { formState = formState.copy(ownerName = it) },
            label = "Full Name of the owner",
            icon = Icons.Default.Person
        )

        SectionHeader("RESTAURANT DETAILS")
        CustomInputField(
            value = formState.restaurantName,
            onValueChange = { formState = formState.copy(restaurantName = it) },
            label = "Restaurant Name",
            icon = Icons.Default.Storefront
        )
        CustomInputField(
            value = formState.email,
            onValueChange = { formState = formState.copy(email = it) },
            label = "Business Email",
            icon = Icons.Default.Email
        )
        CustomInputField(
            value = formState.phone,
            onValueChange = { formState = formState.copy(phone = it) },
            label = "Phone Number",
            icon = Icons.Default.Phone
        )
        CustomInputField(
            value = formState.address,
            onValueChange = { formState = formState.copy(address = it) },
            label = "Address",
            icon = Icons.Default.LocationOn
        )
        CustomInputField(
            value = formState.username,
            onValueChange = { formState = formState.copy(username = it) },
            label = "Username",
            icon = Icons.Default.AlternateEmail
        )
        
        SimpleCuisineDropdown(
            selectedCuisine = formState.cuisineType,
            onCuisineSelected = { newCuisine ->
                formState = formState.copy(cuisineType = newCuisine)
            }
        )

        // --- Photo Uploader ---
        Text(
            "Restaurant Photo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            fontWeight = FontWeight.Bold
        )
        PhotoUploadBox()

        // --- Footer & Button ---
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* Action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Text("Add menu", color = Color.White)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = Color(0xFFFF7043),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        placeholder = { Text(label, color = Color.Gray) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.6f)
            )
        },
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedBorderColor = Color(0xFFFF7043)
        ),
        singleLine = true
    )
}

@Composable
fun PhotoUploadBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = OrangeFoodGram.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 12.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
                drawRoundRect(
                    color = Color(0xFFFF7043).copy(alpha = 0.5f),
                    style = stroke,
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = Color(0xFFFF7043),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Upload Photo",
                color = Color(0xFFFF7043),
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(40.dp)
                .background(Color(0xFFFF7043), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SimpleCuisineDropdown(
    selectedCuisine: String,
    onCuisineSelected: (String) -> Unit
) {
    val cuisines = listOf("Italian", "Chinese", "Mexican", "Indian", "Continental", "Fast Food")
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedCuisine,
            onValueChange = {},
            readOnly = true,
            label = { Text("Cuisine Type") },
            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.Gray.copy(alpha = 0.6f)) },
            trailingIcon = { 
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, 
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.3f),
                focusedBorderColor = Color(0xFFFF7043)
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            cuisines.forEach { cuisine ->
                DropdownMenuItem(
                    text = { Text(cuisine) },
                    onClick = {
                        onCuisineSelected(cuisine)
                        expanded = false
                    }
                )
            }
        }
    }
}
