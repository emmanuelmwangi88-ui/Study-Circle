package com.deepseek.studycircle.screens.resourcedetails

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deepseek.studycircle.data.CreditCalculator
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Resource
import com.deepseek.studycircle.models.Review
import com.deepseek.studycircle.models.trendingResources
import com.deepseek.studycircle.navigation.ROUTE_PROFILE
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResourceDetailScreen(
    navController: NavHostController, 
    resource: Resource,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val user by userViewModel.userData
    val reviews = userViewModel.resourceReviews
    val isBookmarked = user?.bookmarks?.get(resource.id.toString()) == true
    
    var showReviewDialog by remember { mutableStateOf(false) }
    var userRating by remember { mutableStateOf(5f) }
    var reviewText by remember { mutableStateOf("") }
    var isSubmittingReview by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var isUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(resource.id) {
        userViewModel.fetchReviews(resource.id)
    }

    fun shareResource() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Study Resource: ${resource.title}")
            putExtra(Intent.EXTRA_TEXT,
                "Check out this ${resource.type} resource: ${resource.title} by ${resource.author}.\n\nJoin StudyCircle to get more materials!")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Resource via"))
    }

    fun downloadResource() {
        if (!resource.fileUrl.isNullOrEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.fileUrl))
                context.startActivity(intent)
            } catch (e: Exception) {
                scope.launch { snackbarHostState.showSnackbar("Could not open file URL") }
            }
        } else {
            scope.launch {
                isDownloading = true
                snackbarHostState.showSnackbar("Starting download: ${resource.title}...")
                delay(2000)
                isDownloading = false
                snackbarHostState.showSnackbar("Download complete!")
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a Review", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { i ->
                            IconButton(onClick = { userRating = i.toFloat() }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (i <= userRating) Gold else Color.LightGray
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Your Review") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmittingReview = true
                        userViewModel.submitReview(context, resource.id, userRating, reviewText) { success ->
                            if (success) {
                                showReviewDialog = false
                                reviewText = ""
                            }
                            isSubmittingReview = false
                        }
                    },
                    enabled = !isSubmittingReview && reviewText.isNotBlank()
                ) {
                    if (isSubmittingReview) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(shadowElevation = 24.dp, color = StudySurface) {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text("Access Cost", color = StudyTextSecondary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = StudyAccentOrangeText, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(CreditCalculator.formatCredits(resource.cost), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = StudyTextPrimary)
                                Text(" credits", fontSize = 14.sp, color = StudyTextSecondary)
                            }
                        }
                        Button(
                            onClick = { 
                                if (!isUnlocked) {
                                    userViewModel.performTransaction(
                                        type = CreditCalculator.TransactionType.DOWNLOAD,
                                        customAmount = -resource.cost,
                                        description = "Unlocked: ${resource.title}"
                                    ) { success ->
                                        if (success) {
                                            isUnlocked = true
                                            scope.launch { snackbarHostState.showSnackbar("Resource unlocked!") }
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Not enough credits!") }
                                        }
                                    }
                                } else {
                                    downloadResource()
                                }
                            },
                            modifier = Modifier.height(56.dp).fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUnlocked) StudyTeal else StudyPrimary
                            ),
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(
                                    imageVector = if (isUnlocked) Icons.Default.Download else Icons.Default.LockOpen, 
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isUnlocked) "Download" else "Unlock", 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(StudyBackground).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFE5E7EB))) {
                IconButton(
                    onClick = { navController.popBackStack() }, 
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Row(modifier = Modifier.padding(16.dp).align(Alignment.TopEnd), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { shareResource() }, modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { userViewModel.toggleBookmark(resource.id, !isBookmarked) }, 
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, 
                            contentDescription = "Save", 
                            tint = if (isBookmarked) StudyAccentOrangeText else Color.White
                        )
                    }
                }
                
                Icon(
                    imageVector = if (resource.type.uppercase() == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Description, 
                    contentDescription = null, 
                    modifier = Modifier.size(90.dp).align(Alignment.Center), 
                    tint = if (resource.type.uppercase() == "PDF") Color(0xFFE74C3C) else StudyPrimary
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Surface(color = StudyPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = resource.category.ifEmpty { "General Study" }, 
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = StudyPrimary
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(text = resource.title, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = StudyTextPrimary)
                
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(StudySurface).padding(12.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(text = resource.author, fontWeight = FontWeight.Bold, color = StudyTextPrimary)
                        Text(text = "${resource.authorBadge} Contributor", color = StudyTextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = StudySurface)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        DetailStatItem(Icons.Default.Star, resource.rating.toString(), "Rating", Gold)
                        DetailStatItem(Icons.Default.FileDownload, resource.downloads.toString(), "Downloads")
                        DetailStatItem(Icons.Default.Description, "${resource.pages}", "Pages")
                        DetailStatItem(Icons.Default.SdStorage, resource.size, "Size")
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Description", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudyTextPrimary)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "A high-quality study material covering essential topics of ${resource.title}. Master the subject efficiently with these verified notes.", 
                    color = StudyTextSecondary, fontSize = 15.sp, lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Reviews (${reviews.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudyTextPrimary)
                    TextButton(onClick = { showReviewDialog = true }) {
                        Text("Write a Review", color = StudyPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                
                if (reviews.isEmpty()) {
                    Text("No reviews yet. Be the first to review!", color = StudyTextSecondary, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
                } else {
                    reviews.forEach { review ->
                        ReviewCard(review)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailStatItem(icon: ImageVector, value: String, label: String, iconColor: Color = StudyTextSecondary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 14.sp)
        Text(label, color = StudyTextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = StudySurface)) {
        Row(Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(StudyPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { 
                Text(review.user.take(1).uppercase(), color = StudyPrimary, fontWeight = FontWeight.Bold) 
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(review.user, fontWeight = FontWeight.Bold, color = StudyTextPrimary)
                    Text(review.date, fontSize = 12.sp, color = StudyTextSecondary)
                }
                Row(Modifier.padding(vertical = 4.dp)) { 
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Default.Star, 
                            contentDescription = null, 
                            tint = if (i < review.rating) Gold else Color.LightGray, 
                            modifier = Modifier.size(14.dp)
                        ) 
                    } 
                }
                Text(text = review.text, fontSize = 14.sp, color = StudyTextSecondary)
            }
        }
    }
}
