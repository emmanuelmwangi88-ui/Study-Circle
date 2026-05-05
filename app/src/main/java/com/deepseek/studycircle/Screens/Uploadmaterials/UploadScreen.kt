package com.deepseek.studycircle.screens.uploadmaterials

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.data.CreditCalculator
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMaterialScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Upload Material", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StudyBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            val rewardAmount = CreditCalculator.getAmountForType(CreditCalculator.TransactionType.UPLOAD)
            Text(
                text = "Share your knowledge and earn $rewardAmount credits per resource.",
                color = StudyTextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            UploadSectionHeader(number = "1", title = "Resource Details")
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Resource Title") },
                placeholder = { Text("e.g. Advanced Calculus III Master Notes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                placeholder = { Text("Mathematics, Physics, Chemistry, etc.") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            UploadSectionHeader(number = "2", title = "File Upload")
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudySurface)
                    .border(1.dp, if (selectedFileUri != null) StudyTeal else StudyPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .clickable { filePickerLauncher.launch("*/*") },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (selectedFileUri != null) Icons.Default.Description else Icons.Default.CloudUpload, 
                        contentDescription = null, 
                        tint = if (selectedFileUri != null) StudyTeal else StudyPrimary, 
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (selectedFileUri != null) "File Selected" else "Select document to upload", 
                        fontWeight = FontWeight.Bold, 
                        color = StudyTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { 
                    if (selectedFileUri != null) {
                        isUploading = true
                        userViewModel.uploadFileToCloudinary(context, selectedFileUri!!) { url ->
                            if (url != null) {
                                userViewModel.performTransaction(
                                    type = CreditCalculator.TransactionType.UPLOAD,
                                    description = "Uploaded: $title"
                                ) { success ->
                                    if (success) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Resource published! +$rewardAmount Credits")
                                            navController.popBackStack()
                                        }
                                    }
                                    isUploading = false
                                }
                            } else {
                                isUploading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Upload failed. Please try again.")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = title.isNotEmpty() && category.isNotEmpty() && selectedFileUri != null && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Publish Resource", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UploadSectionHeader(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        Surface(color = StudyPrimary, shape = CircleShape, modifier = Modifier.size(24.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 18.sp)
    }
}
