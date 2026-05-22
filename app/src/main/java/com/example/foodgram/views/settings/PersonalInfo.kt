package com.example.foodgram.views.settings

import android.content.Context
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodgram.ui.theme.FoodGramBackground
import com.example.foodgram.ui.theme.OrangeFoodGram
import com.example.foodgram.utils.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoSettings(
    navController: NavController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    var profilePhotoPath by remember { mutableStateOf(UserSession.currentProfilePhotoPath) }
    var profilePhotoUrl by remember { mutableStateOf(UserSession.currentProfilePhotoUrl) }

    var isLoading by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var showPhotoSheet by remember { mutableStateOf(false) }
    var cameraTempFile by remember { mutableStateOf<File?>(null) }

    val userId = UserSession.currentUserDocId

    LaunchedEffect(userId) {
        if (userId != null) {
            try {
                val snapshot = db.collection("user").document(userId).get().await()
                if (snapshot.exists()) {
                    name = snapshot.getString("name") ?: ""
                    username = snapshot.getString("username") ?: ""
                    email = snapshot.getString("email") ?: ""
                    location = snapshot.getString("location") ?: ""

                    profilePhotoPath = snapshot.getString("photoPath") ?: UserSession.currentProfilePhotoPath
                    profilePhotoUrl = snapshot.getString("photoUrl") ?: UserSession.currentProfilePhotoUrl

                    UserSession.currentProfilePhotoPath = profilePhotoPath
                    UserSession.currentProfilePhotoUrl = profilePhotoUrl
                }
            } catch (_: Exception) {
            }
        }
    }

    fun save() {
        if (userId == null) {
            Toast.makeText(context, "No active session", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanName = sanitizeNameInput(name).trim().replace(Regex("\\s+"), " ")
        val cleanUsername = sanitizeUsernameInput(username).trim().lowercase(Locale.ROOT)
        val cleanEmail = sanitizeEmailInput(email).trim().lowercase(Locale.ROOT)
        val cleanLocation = sanitizeLocationInput(location).trim().replace(Regex("\\s+"), " ")

        name = cleanName
        username = cleanUsername
        email = cleanEmail
        location = cleanLocation

        nameError = validateName(cleanName)
        usernameError = validateUsername(cleanUsername)
        emailError = validateEmail(cleanEmail)
        locationError = validateLocation(cleanLocation)

        if (nameError != null || usernameError != null || emailError != null || locationError != null) {
            Toast.makeText(context, "Please fix the highlighted fields", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            isLoading = true
            try {
                val currentUser = auth.currentUser ?: throw IllegalStateException("No authenticated user")

                val currentAuthEmail = currentUser.email?.trim()?.lowercase(Locale.ROOT)
                if (cleanEmail != currentAuthEmail) {
                    val methods = auth.fetchSignInMethodsForEmail(cleanEmail).await().signInMethods.orEmpty()
                    if (methods.isNotEmpty()) {
                        emailError = "This email is already in use"
                        Toast.makeText(context, "This email is already in use", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    currentUser.updateEmail(cleanEmail).await()
                }

                val updates = mapOf(
                    "name" to cleanName,
                    "username" to cleanUsername,
                    "email" to cleanEmail,
                    "location" to cleanLocation,
                    "photoPath" to profilePhotoPath,
                    "photoUrl" to profilePhotoUrl
                )

                db.collection("user").document(userId)
                    .set(updates, SetOptions.merge())
                    .await()

                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun cancel() {
        focusManager.clearFocus()
        navController.popBackStack()
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val tempFile = cameraTempFile
        if (success && tempFile != null && userId != null) {
            scope.launch {
                isUploadingPhoto = true
                try {
                    val localFile = withContext(Dispatchers.IO) {
                        saveFileToProfileStorage(context, tempFile, userId)
                    }

                    val remoteUrl = withContext(Dispatchers.IO) {
                        uploadProfilePhotoToFirebase(localFile, userId)
                    }

                    withContext(Dispatchers.IO) {
                        db.collection("user").document(userId)
                            .set(
                                mapOf(
                                    "photoPath" to localFile.absolutePath,
                                    "photoUrl" to remoteUrl
                                ),
                                SetOptions.merge()
                            )
                            .await()
                    }

                    profilePhotoPath = localFile.absolutePath
                    profilePhotoUrl = remoteUrl
                    UserSession.currentProfilePhotoPath = localFile.absolutePath
                    UserSession.currentProfilePhotoUrl = remoteUrl

                    Toast.makeText(context, "Photo updated", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingPhoto = false
                    cameraTempFile = null
                }
            }
        } else {
            cameraTempFile = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {

            if (userId != null) {
                val tempFile = createCameraTempFile(context, userId)
                cameraTempFile = tempFile
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Its required permision to use the camera", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && userId != null) {
            scope.launch {
                isUploadingPhoto = true
                try {
                    val sourceFile = withContext(Dispatchers.IO) {
                        copyUriToTempFile(context, uri, userId)
                    }

                    val localFile = withContext(Dispatchers.IO) {
                        saveFileToProfileStorage(context, sourceFile, userId)
                    }

                    val remoteUrl = withContext(Dispatchers.IO) {
                        uploadProfilePhotoToFirebase(localFile, userId)
                    }

                    withContext(Dispatchers.IO) {
                        db.collection("user").document(userId)
                            .set(
                                mapOf(
                                    "photoPath" to localFile.absolutePath,
                                    "photoUrl" to remoteUrl
                                ),
                                SetOptions.merge()
                            )
                            .await()
                    }

                    profilePhotoPath = localFile.absolutePath
                    profilePhotoUrl = remoteUrl
                    UserSession.currentProfilePhotoPath = localFile.absolutePath
                    UserSession.currentProfilePhotoUrl = remoteUrl

                    Toast.makeText(context, "Photo updated", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingPhoto = false
                }
            }
        }
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
                    enabled = !isLoading && !isUploadingPhoto,
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
                    if (isLoading || isUploadingPhoto) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
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
        val avatarModel = when {
            !profilePhotoPath.isNullOrBlank() -> File(profilePhotoPath!!)
            !profilePhotoUrl.isNullOrBlank() -> profilePhotoUrl
            else -> null
        }

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
                        .background(Color(0xFFF0F0F0))
                        .clickable { showPhotoSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarModel != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarModel)
                                .crossfade(true)
                                .memoryCacheKey("profile_${userId ?: "guest"}")
                                .diskCacheKey("profile_${userId ?: "guest"}")
                                .build(),
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-2).dp, y = (-2).dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(OrangeFoodGram)
                        .clickable { showPhotoSheet = true },
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
                    .clickable { showPhotoSheet = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            InfoField(
                label = "OWNER NAME",
                value = name,
                onValueChange = {
                    name = sanitizeNameInput(it)
                    nameError = null
                },
                icon = Icons.Outlined.PersonOutline,
                keyboardType = KeyboardType.Text,
                maxLength = 40,
                helperText = "Only letters, spaces, hyphens and apostrophes",
                error = nameError
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoField(
                label = "USERNAME",
                value = username,
                onValueChange = {
                    username = sanitizeUsernameInput(it)
                    usernameError = null
                },
                icon = Icons.Default.AlternateEmail,
                keyboardType = KeyboardType.Text,
                maxLength = 20,
                helperText = "Letters, numbers, dots and underscores",
                error = usernameError
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoField(
                label = "EMAIL",
                value = email,
                onValueChange = {
                    email = sanitizeEmailInput(it)
                    emailError = null
                },
                icon = Icons.Outlined.MailOutline,
                keyboardType = KeyboardType.Email,
                maxLength = 80,
                helperText = "Must be valid and unique",
                error = emailError
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoField(
                label = "LOCATION",
                value = location,
                onValueChange = {
                    location = sanitizeLocationInput(it)
                    locationError = null
                },
                icon = Icons.Outlined.LocationOn,
                keyboardType = KeyboardType.Text,
                maxLength = 60,
                helperText = "City, neighborhood or short address",
                error = locationError
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showPhotoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPhotoSheet = false }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Update profile photo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            showPhotoSheet = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pick from gallery")
                    }

                    TextButton(
                        onClick = {
                            showPhotoSheet = false
                            if (userId != null) {

                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            } else {
                                Toast.makeText(context, "No active session", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take a photo")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun InfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    maxLength: Int,
    helperText: String,
    error: String? = null
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
                    isError = error != null,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangeFoodGram,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = OrangeFoodGram,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = helperText,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${value.length}/$maxLength",
                    fontSize = 11.sp,
                    color = if (value.length <= maxLength) Color.Gray else Color(0xFFD32F2F)
                )
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error,
                    fontSize = 11.sp,
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

private fun sanitizeNameInput(input: String): String {
    return input.filter { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }.take(40)
}

private fun sanitizeUsernameInput(input: String): String {
    return input.lowercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
        .take(20)
}

private fun sanitizeEmailInput(input: String): String {
    return input.filterNot { it.isWhitespace() }.take(80)
}

private fun sanitizeLocationInput(input: String): String {
    return input.filter {
        it.isLetterOrDigit() ||
                it.isWhitespace() ||
                it == ',' ||
                it == '.' ||
                it == '-' ||
                it == '#' ||
                it == '/'
    }.take(60)
}

private fun validateName(value: String): String? {
    return when {
        value.isBlank() -> "Name cannot be empty"
        value.length < 2 -> "Use at least 2 characters"
        value.length > 40 -> "Maximum 40 characters"
        else -> null
    }
}

private fun validateUsername(value: String): String? {
    return when {
        value.isBlank() -> "Username cannot be empty"
        value.length < 3 -> "Use at least 3 characters"
        value.length > 20 -> "Maximum 20 characters"
        !value.all { it.isLowerCase() || it.isDigit() || it == '_' || it == '.' } ->
            "Only letters, numbers, dots and underscores"
        value.first().isDigit() || value.first() == '.' || value.first() == '_' ->
            "Cannot start with a number, dot or underscore"
        value.last() == '.' || value.last() == '_' ->
            "Cannot end with a dot or underscore"
        else -> null
    }
}

private fun validateEmail(value: String): String? {
    return when {
        value.isBlank() -> "Email cannot be empty"
        !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Invalid email"
        value.length > 80 -> "Maximum 80 characters"
        else -> null
    }
}

private fun validateLocation(value: String): String? {
    return when {
        value.isBlank() -> "Location cannot be empty"
        value.length < 2 -> "Use at least 2 characters"
        value.length > 60 -> "Maximum 60 characters"
        else -> null
    }
}

private fun createCameraTempFile(context: Context, userId: String): File {
    val dir = File(context.cacheDir, "camera_profile")
    if (!dir.exists()) dir.mkdirs()
    return File.createTempFile("profile_${userId}_", ".jpg", dir)
}

private suspend fun saveFileToProfileStorage(
    context: Context,
    sourceFile: File,
    userId: String
): File = withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, "profile_photos")
    if (!dir.exists()) dir.mkdirs()

    val target = File(dir, "profile_$userId.jpg")
    sourceFile.inputStream().use { input ->
        target.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    target
}

private suspend fun copyUriToTempFile(
    context: Context,
    uri: Uri,
    userId: String
): File = withContext(Dispatchers.IO) {
    val dir = File(context.cacheDir, "profile_gallery")
    if (!dir.exists()) dir.mkdirs()

    val tempFile = File.createTempFile("gallery_${userId}_", ".jpg", dir)
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    } ?: error("Unable to read the selected image")

    tempFile
}

private suspend fun uploadProfilePhotoToFirebase(
    file: File,
    userId: String
): String = withContext(Dispatchers.IO) {
    val storageRef = FirebaseStorage.getInstance()
        .reference
        .child("profile_photos/$userId.jpg")

    storageRef.putFile(Uri.fromFile(file)).await()
    storageRef.downloadUrl.await().toString()
}