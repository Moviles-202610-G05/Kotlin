package com.example.foodgram.views.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodgram.ui.theme.OrangeFoodGram

@Composable
fun RegistrationTypeView(
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onContinueClick: (AccountType) -> Unit
) {
    // State to track which card is selected
    var selectedType by remember { mutableStateOf(AccountType.STUDENT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Join Us", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = buildAnnotatedString {
                    append("Join Our ")
                    withStyle(style = SpanStyle(color = OrangeFoodGram)) {
                        append("Community")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose your account type to get started. You can always change this later in settings.",
                color = Color.Gray,
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- Selection Cards ---
        AccountTypeCard(
            title = "I'm a Student",
            description = "Discover exclusive deals, track your meals, and save on campus dining.",
            icon = Icons.Default.School,
            isSelected = selectedType == AccountType.STUDENT,
            onClick = { selectedType = AccountType.STUDENT }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AccountTypeCard(
            title = "I'm a Restaurant Owner",
            description = "Manage your digital menu, reach hungry foodies, and grow your local business.",
            icon = Icons.Default.Restaurant,
            isSelected = selectedType == AccountType.RESTAURANTE,
            onClick = { selectedType = AccountType.RESTAURANTE }
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- Action Button ---
        Button(
            onClick = { onContinueClick(selectedType) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeFoodGram),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Continue to Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Footer ---
        Row {
            Text("Already have an account? ", color = Color.Gray)
            Text(
                text = "Log in",
                color = OrangeFoodGram,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

@Composable
fun AccountTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) OrangeFoodGram else Color(0xFFF3F4F6)
    val iconBgColor = if (isSelected) Color(0xFFFFEFE8) else Color(0xFFF9FAFB)
    val iconTint = if (isSelected) OrangeFoodGram else Color(0xFFD1D5DB)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = iconBgColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = description, color = Color.Gray, fontSize = 14.sp, lineHeight = 18.sp)
            }
        }
    }
}

enum class AccountType { STUDENT, RESTAURANTE }