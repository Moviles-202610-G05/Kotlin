package com.example.foodgram.views.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodgram.ui.theme.FoodGramBackground
import com.example.foodgram.ui.theme.OrangeFoodGram
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.foodgram.utils.UserSession
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoSettings(
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()



    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val userId = UserSession.currentUserDocId

    LaunchedEffect(Unit) {
        if (userId != null) {
            db.collection("user").document(userId).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        name = snapshot.getString("name") ?: ""
                        username = snapshot.getString("username") ?: ""
                        email = snapshot.getString("email") ?: ""
                        location = snapshot.getString("location") ?: ""
                    }
                }
        }
    }

    fun save() {
        if (userId == null) {
            Toast.makeText(navController.context, "No hay sesión activa", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true

        val updates = mapOf(
            "name" to name,
            "username" to username,
            "email" to email,
            "location" to location
        )

        db.collection("user").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                isLoading = false
                Toast.makeText(navController.context, "Profile updated", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(navController.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun cancel() {
        focusManager.clearFocus()
        navController.popBackStack()
    }

    Scaffold(
        containerColor = FoodGramBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Personal Info",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { cancel() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = OrangeFoodGram,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FoodGramBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = FoodGramBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { save() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeFoodGram,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Update Profile",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .clickable { focusManager.clearFocus() }
        ) {
            Spacer(modifier = Modifier.height(32.dp))


            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .border(3.dp, OrangeFoodGram, CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(OrangeFoodGram)
                        .clickable { /* TODO: Handle edit photo */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Change Photo",
                color = OrangeFoodGram,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))


            InfoField(
                label = "OWNER NAME",
                value = name,
                onValueChange = { name = it },
                icon = Icons.Outlined.PersonOutline,
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoField(
                label = "USERNAME",
                value = username,
                onValueChange = { username = it },
                icon = Icons.Default.AlternateEmail,
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoField(
                label = "EMAIL",
                value = email,
                onValueChange = { email = it },
                icon = Icons.Outlined.MailOutline,
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoField(
                label = "LOCATION",
                value = location,
                onValueChange = { location = it },
                icon = Icons.Outlined.LocationOn,
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Composable
fun InfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeFoodGram,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = OrangeFoodGram
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}