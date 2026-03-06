package com.example.foodgram.views.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodgram.ui.theme.OrangeFoodGram

@Composable
fun LoginScreen(onNavigateToHome: () -> Unit,onNavigateToSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(text = "Login", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // Header Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Welcome Back!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = "Delicious food is just a few clicks away.",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Input Fields
        CustomTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            placeholder = "Enter your email",
            leadingIcon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Enter your password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        // Forgot Password
        TextButton(
            onClick = { /* TODO */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = OrangeFoodGram)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = onNavigateToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeFoodGram),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Divider
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text("  OR CONTINUE WITH  ", color = Color.Gray, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Social Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SocialButton(text = "Google", modifier = Modifier.weight(1f))
            SocialButton(text = "Apple", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sign Up Footer
        Row {
            Text("Don't have an account? ", color = Color.Gray)
            Text(
                "Sign Up",
                color = OrangeFoodGram,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable{
                    onNavigateToSignUp()
                }
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = Color.LightGray) },
            trailingIcon = {
                if (isPassword) Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.LightGray)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFF3F4F6),
                focusedBorderColor = Color(0xFFFF6F31)
            )
        )
    }
}

@Composable
fun SocialButton(text: String, modifier: Modifier) {
    OutlinedButton(
        onClick = { /* TODO */ },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Text(text, color = Color.Black)
    }
}
