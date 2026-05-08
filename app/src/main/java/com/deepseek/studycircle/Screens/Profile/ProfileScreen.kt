package com.deepseek.studycircle.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.BadgeItem
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.models.badges
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user by userViewModel.userData
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedBio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(user) {
        user?.let {
            if (editedName.isEmpty()) editedName = it.name
            if (editedBio.isEmpty()) editedBio = it.bio
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUploading) showEditDialog = false },
            title = { Text("Customize Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    )
                    {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!user?.imageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp))
                        }
                    }
                    Text(
                        text = "Tap to change photo",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp),
                        color = StudyPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isUploading = true
                        if (selectedImageUri != null) {
                            userViewModel.uploadFileToCloudinary(context, selectedImageUri!!) { imageUrl ->
                                if (imageUrl != null) {
                                    userViewModel.updateUserProfile(context, editedName, editedBio, imageUrl) { success ->
                                        if (success) {
                                            showEditDialog = false
                                            selectedImageUri = null
                                            scope.launch { snackbarHostState.showSnackbar("Profile updated!") }
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Failed to update database.") }
                                        }
                                        isUploading = false
                                    }
                                } else {
                                    isUploading = false
                                }
                            }
                        } else {
                            userViewModel.updateUserProfile(context, editedName, editedBio, user?.imageUri ?: "") { success ->
                                if (success) {
                                    showEditDialog = false
                                    scope.launch { snackbarHostState.showSnackbar("Profile updated!") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Failed to update database.") }
                                }
                                isUploading = false
                            }
                        }
                    },
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !isUploading) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("Edit", fontWeight = FontWeight.Bold,
                            color = StudyPrimary,
                            fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(color = StudyBackground)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudySurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(StudyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.imageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.imageUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(user?.name ?: "Loading...", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = StudyTextPrimary)
                    Text(user?.bio ?: "No bio yet", color = StudyTextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = StudyAccentOrange, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = user?.role?.uppercase() ?: "USER", 
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = StudyAccentOrangeText, 
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatItem("GLOBAL RANK", "4.92", "+0.02")
                        StatItem("CREDITS", user?.credits?.toString() ?: "0", "Top 5%")
                        StatItem("REPUTATION", user?.reputation?.toString() ?: "0.0", "Gold")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Subject Expertise", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = StudySurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    listOf("Mathematics" to 4.9, "Physics" to 4.8, "Biology" to 4.7, "Chemistry" to 4.5).forEach { (subj, score) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(subj, modifier = Modifier.weight(1f))
                            LinearProgressIndicator(
                                progress = { score.toFloat() / 5f },
                                modifier = Modifier.weight(2f).height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Text(" $score", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Expertise Badges", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(badges) { badge ->
                    BadgeCard(badge)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, color = StudyPrimary, fontSize = 20.sp)
        Text(label, color = StudyTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = StudyTeal, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BadgeCard(badge: BadgeItem) {
    Card(
        modifier = Modifier.width(110.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = StudyAccentOrange, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = badge.icon, contentDescription = null, tint = StudyAccentOrangeText)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(badge.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            Text("LVL ${badge.level}", fontSize = 10.sp, color = StudyTextSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}
