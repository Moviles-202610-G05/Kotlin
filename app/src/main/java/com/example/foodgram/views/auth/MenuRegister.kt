package com.example.foodgram.views.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodgram.views.SectionHeader

data class MenuItem(
    val name: String,
    val price: String,
    val category: String,
    val description: String,
    val inStock: Boolean = true
)

@Composable
fun MenuRegisterView(
    onBackClick: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {
    var dishName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Main Course") }
    var description by remember { mutableStateOf("") }
    var inStock by remember { mutableStateOf(true) }
    
    val menuItems = remember { mutableStateListOf<MenuItem>() }

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

        // --- Dish Photo ---
        Text(
            "Dish Photo",
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        DishPhotoUploadBox()

        Spacer(modifier = Modifier.height(16.dp))

        // --- Dish Details ---
        Text("Dish Name", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(
            value = dishName,
            onValueChange = { dishName = it },
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
                    onValueChange = { price = it },
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
            onValueChange = { description = it },
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
                    menuItems.add(MenuItem(dishName, price, category, description, inStock))
                    dishName = ""
                    price = ""
                    description = ""
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
        ) {
            Text("Finish Registration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.RocketLaunch, contentDescription = null)
        }
    }
}

@Composable
fun DishPhotoUploadBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color(0xFFFF7043).copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .drawBehind {
                val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                drawRoundRect(color = Color(0xFFFF7043).copy(alpha = 0.3f), style = stroke, cornerRadius = CornerRadius(24.dp.toPx()))
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFFFF7043), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Upload Photo", color = Color(0xFFFF7043), fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(36.dp).background(Color(0xFFFF7043), CircleShape),
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
                // Placeholder for dish image
                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$\u0024{item.price} • \u0024{item.category}", color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFC5CAE9))
        }
    }
}
