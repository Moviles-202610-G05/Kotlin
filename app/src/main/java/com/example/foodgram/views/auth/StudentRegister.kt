package com.example.foodgram.views.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodgram.ui.theme.OrangeFoodGram
import com.example.foodgram.viewmodels.auth.StudentRegisterViewModel

@Composable
fun StudentRegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: StudentRegisterViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Student Registration", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = "Fill in your details to join the community.",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Error Message ---
        viewModel.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // --- Input Fields ---
        CustomTextField(
            value = viewModel.name,
            onValueChange = { viewModel.name = it },
            label = "Full Name",
            placeholder = "Enter your full name",
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = viewModel.username,
            onValueChange = { viewModel.username = it },
            label = "Username",
            placeholder = "Choose a username",
            leadingIcon = Icons.Default.AlternateEmail
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = "Email Address",
            placeholder = "Enter your email",
            leadingIcon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = viewModel.universityId,
            onValueChange = { viewModel.universityId = it },
            label = "University ID",
            placeholder = "Enter your ID number",
            leadingIcon = Icons.Default.Badge
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = "Password",
            placeholder = "Create a password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Food Preferences Section ---
        Text(
            text = "Food Preferences",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = "Select the types of food you love.",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OptGroup(
            options = viewModel.availablePreferences,
            selectedOptions = viewModel.selectedPreferences,
            onOptionClick = { viewModel.togglePreference(it) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Register Button ---
        Button(
            onClick = { viewModel.register(onRegisterSuccess) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeFoodGram),
            shape = RoundedCornerShape(28.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Register", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptGroup(
    options: List<String>,
    selectedOptions: List<String>,
    onOptionClick: (String) -> Unit
) {
    // Replaced FlowRow with a custom flow-like behavior using a standard Row and wrapping logic 
    // to avoid the NoSuchMethodError crash.
    // For now, I'll use a simpler grid-like approach or just a Column of Rows.
    
    val rows = options.chunked(3) // Split options into rows of 3
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    val isSelected = selectedOptions.contains(option)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onOptionClick(option) },
                        label = { Text(option) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeFoodGram,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF3F4F6),
                            labelColor = Color.Gray
                        ),
                        border = null,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}
