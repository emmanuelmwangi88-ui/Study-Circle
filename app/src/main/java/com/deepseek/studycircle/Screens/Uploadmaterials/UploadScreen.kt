package com.deepseek.studycircle.Screens.Uploadmaterials

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.deepseek.studycircle.data.CreditCalculator
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun UploadMaterialScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    UploadMaterialContent(
        isUploading = isUploading,
        onBackClick = { navController.popBackStack() },
        onUpload = { title, category, description, uri ->
            isUploading = true
            userViewModel.uploadFileToCloudinary(context, uri) { url, extension, error ->
                if (url != null) {
                    userViewModel.saveMaterial(title, category, description, url, extension ?: "PDF") { saved ->
                        if (saved) {
                            userViewModel.performTransaction(
                                type = CreditCalculator.TransactionType.UPLOAD,
                                description = "Uploaded: $title"
                            ) { _ ->
                                isUploading = false
                                scope.launch {
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            isUploading = false
                        }
                    }
                } else {
                    isUploading = false
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMaterialContent(
    isUploading: Boolean,
    onBackClick: () -> Unit,
    onUpload: (String, String, String, Uri) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }

    val categories = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Computer Science", "History", "Engineering", "Languages")

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        if (uri != null) {
            selectedFileName = getFileName(context, uri)
            selectedFileSize = getFileSize(context, uri)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Upload Study Material", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            val rewardAmount = CreditCalculator.UPLOAD_REWARD
            
            // Reward Banner
            Surface(
                color = StudyPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = StudyPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Earn $rewardAmount credits for each resource you share. Help others and grow your rank!",
                        color = StudyTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            UploadSectionHeader(number = "1", title = "Tell us about the resource")
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Resource Title") },
                placeholder = { Text("e.g. Organic Chemistry Final Review") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudyPrimary,
                    unfocusedContainerColor = StudySurface
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StudyTextSecondary, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudyPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 250) description = it },
                label = { Text("Brief Description") },
                placeholder = { Text("What topics are covered in this document?") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    Text("${description.length}/250", textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                },
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = StudySurface)
            )

            Spacer(modifier = Modifier.height(32.dp))

            UploadSectionHeader(number = "2", title = "Select PDF or Document")
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StudySurface)
                    .border(
                        width = 1.dp, 
                        color = if (selectedFileUri != null) StudyTeal else StudyPrimary.copy(alpha = 0.2f), 
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { filePickerLauncher.launch("*/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedFileUri != null) {
                    IconButton(
                        onClick = { selectedFileUri = null; selectedFileName = "" },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Clear, "Remove file", tint = Color.Gray)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    val isPdf = selectedFileName.lowercase().endsWith(".pdf")
                    Icon(
                        imageVector = when {
                            isPdf -> Icons.Default.PictureAsPdf
                            selectedFileUri != null -> Icons.Default.Description
                            else -> Icons.Default.CloudUpload
                        },
                        contentDescription = null, 
                        tint = when {
                            isPdf -> Color(0xFFE74C3C)
                            selectedFileUri != null -> StudyTeal
                            else -> StudyPrimary
                        }, 
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedFileUri != null) selectedFileName else "Tap to choose a file", 
                        fontWeight = FontWeight.Bold, 
                        color = StudyTextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (selectedFileUri != null && selectedFileSize.isNotEmpty()) {
                        Text(
                            text = "Size: $selectedFileSize",
                            fontSize = 12.sp,
                            color = StudyTextSecondary
                        )
                    } else if (selectedFileUri == null) {
                        Text("Supports PDF, DOCX, PPTX, etc.", fontSize = 12.sp, color = StudyTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { 
                    selectedFileUri?.let { uri ->
                        onUpload(title, selectedCategory, description, uri)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                enabled = title.isNotBlank() && selectedCategory.isNotEmpty() && selectedFileUri != null && !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Publish Resource", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
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
        Text(title, fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 16.sp)
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) result = cursor.getString(nameIndex)
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) result = result?.substring(cut + 1)
    }
    return result ?: "selected_file"
}

private fun getFileSize(context: Context, uri: Uri): String {
    var fileSize: Long = 0
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
            }
        } finally {
            cursor?.close()
        }
    }
    
    return when {
        fileSize <= 0 -> ""
        fileSize < 1024 -> "$fileSize B"
        fileSize < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", fileSize / 1024.0)
        else -> String.format(Locale.US, "%.1f MB", fileSize / (1024.0 * 1024.0))
    }
}

@Preview(showBackground = true)
@Composable
fun UploadMaterialContentPreview() {
    StudycircleTheme {
        UploadMaterialContent(
            isUploading = false,
            onBackClick = {},
            onUpload = { _, _, _, _ -> }
        )
    }
}
