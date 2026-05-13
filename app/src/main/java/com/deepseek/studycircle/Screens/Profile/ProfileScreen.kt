package com.deepseek.studycircle.Screens.Profile

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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.BadgeItem
import com.deepseek.studycircle.models.allAvailableBadges
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val user by userViewModel.userData
    val snackbarHostState = remember { SnackbarHostState() }

    val showEditDialog = remember { mutableStateOf(false) }
    val showEditScoresDialog = remember { mutableStateOf(false) }

    var editedName by remember { mutableStateOf("") }
    var editedBio by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    var subjectScores by remember {
        mutableStateOf(
            mapOf(
                "Mathematics" to 0f,
                "Physics" to 0f,
                "Biology" to 0f,
                "Chemistry" to 0f
            )
        )
    }

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

    if (showEditDialog.value) {
        AlertDialog(
            onDismissRequest = { if (!isUploading) showEditDialog.value = false },
            title = { Text("Customize Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
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
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = "Tap to change photo",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp),
                        color = StudyPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
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
                            userViewModel.uploadFileToCloudinary(context, selectedImageUri!!) { imageUrl, _, error ->
                                if (imageUrl != null) {
                                    userViewModel.updateUserProfile(editedName, editedBio, imageUrl) { success ->
                                        if (success) {
                                            showEditDialog.value = false
                                            selectedImageUri = null
                                            scope.launch { snackbarHostState.showSnackbar("Profile updated!") }
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Failed to update database.") }
                                        }
                                        isUploading = false
                                    }
                                } else {
                                    isUploading = false
                                    scope.launch { snackbarHostState.showSnackbar(error ?: "Image upload failed.") }
                                }
                            }
                        } else {
                            userViewModel.updateUserProfile(editedName, editedBio, user?.imageUri ?: "") { success ->
                                if (success) {
                                    showEditDialog.value = false
                                    scope.launch { snackbarHostState.showSnackbar("Profile updated!") }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Failed to update database.") }
                                }
                                isUploading = false
                            }
                        }
                    },
                    enabled = !isUploading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save Changes")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog.value = false }, enabled = !isUploading) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditScoresDialog.value) {
        EditScoresDialog(
            initialScores = subjectScores,
            onDismiss = { showEditScoresDialog.value = false },
            onSave = { updatedScores ->
                subjectScores = updatedScores
                showEditScoresDialog.value = false
                scope.launch { snackbarHostState.showSnackbar("Scores updated!") }
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
                    IconButton(onClick = { showEditDialog.value = true }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = StudyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(StudyPrimary.copy(alpha = 0.1f))
                            .clickable { showEditDialog.value = true },
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
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = StudyPrimary,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        user?.name ?: "Loading...",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = StudyTextPrimary
                    )
                    Text(
                        user?.bio ?: "No bio yet",
                        color = StudyTextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Subject Expertise", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StudyTextPrimary)
                IconButton(onClick = { showEditScoresDialog.value = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit scores", tint = StudyPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = StudySurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    subjectScores.forEach { (subj, score) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subj, modifier = Modifier.weight(1f), color = StudyTextPrimary, fontSize = 14.sp)
                            LinearProgressIndicator(
                                progress = { score / 5f },
                                modifier = Modifier.weight(2f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = StudyPrimary,
                                trackColor = StudyBackground
                            )
                            Text(
                                " $score",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(40.dp),
                                textAlign = TextAlign.End,
                                color = StudyTextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            val userBadges = user?.badges ?: emptyList()
            val displayedBadges = allAvailableBadges.filter { it.id in userBadges }

            if (displayedBadges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Expertise Badges", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StudyTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(displayedBadges) { badge ->
                        BadgeCard(badge)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(32.dp))
                Text("Expertise Badges", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StudyTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Complete activities to earn badges!", color = StudyTextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun EditScoresDialog(
    initialScores: Map<String, Float>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Float>) -> Unit
) {
    val subjects = listOf("Mathematics", "Physics", "Biology", "Chemistry", "Languages", "Technicals")
    val scores = remember { mutableStateMapOf<String, Float>().apply { putAll(initialScores) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Subject Scores", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                subjects.forEach { subject ->
                    val currentScore = scores[subject] ?: 0f
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subject, fontWeight = FontWeight.Medium)
                            Text(
                                text = "%.1f".format(currentScore),
                                fontWeight = FontWeight.Bold,
                                color = StudyPrimary
                            )
                        }
                        Slider(
                            value = currentScore,
                            onValueChange = { scores[subject] = it },
                            valueRange = 0f..5f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = StudyPrimary,
                                activeTrackColor = StudyPrimary,
                                inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(scores.toMap()) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = StudyAccentOrange,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = badge.icon, contentDescription = null, tint = StudyAccentOrangeText)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(badge.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("LVL ${badge.level}", fontSize = 10.sp, color = StudyTextSecondary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}
