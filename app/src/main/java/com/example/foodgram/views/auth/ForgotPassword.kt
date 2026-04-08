package com.example.foodgram.views.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodgram.ui.theme.OrangeFoodGram
import com.example.foodgram.viewmodels.auth.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
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
            Text(text = "Reset Password", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Step Header ---
        Column(modifier = Modifier.fillMaxWidth()) {
            val (title, subtitle) = when (viewModel.step) {
                1 -> "Forgot Password?" to "Enter your email to receive a recovery code."
                2 -> "Check your Email" to "We've sent a recovery code to your email address."
                else -> "New Password" to "Set a new password for your account."
            }
            Text(
                text = title,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1C1E)
            )
            Text(
                text = subtitle,
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

        // --- Input Section ---
        when (viewModel.step) {
            1 -> {
                CustomTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = "Email Address",
                    placeholder = "Enter your registered email",
                    leadingIcon = Icons.Default.Email
                )
            }
            2 -> {
                CustomTextField(
                    value = viewModel.recoveryCode,
                    onValueChange = { viewModel.recoveryCode = it },
                    label = "Recovery Code",
                    placeholder = "Enter the 6-digit code",
                    leadingIcon = Icons.Default.Pin
                )
            }
            3 -> {
                CustomTextField(
                    value = viewModel.newPassword,
                    onValueChange = { viewModel.newPassword = it },
                    label = "New Password",
                    placeholder = "Enter at least 6 characters",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Action Button ---
        Button(
            onClick = {
                when (viewModel.step) {
                    1 -> viewModel.sendRecoveryRequest()
                    2 -> viewModel.verifyCode()
                    3 -> viewModel.updatePassword(onResetSuccess)
                }
            },
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
                val buttonText = when (viewModel.step) {
                    1 -> "Send Code"
                    2 -> "Verify Code"
                    else -> "Reset Password"
                }
                Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
