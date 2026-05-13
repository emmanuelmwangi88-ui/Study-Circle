package com.deepseek.studycircle.Screens.Whitebooard

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.deepseek.studycircle.models.WhiteboardAnswer
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WhiteboardScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val answers = userViewModel.whiteboardAnswers
    val currentUser = userViewModel.userData.value
    val questionUrl = userViewModel.whiteboardQuestionUrl.value
    val expiresAt = userViewModel.whiteboardExpiresAt.value

    WhiteboardContent(
        answers = answers,
        currentUserUid = currentUser?.uid,
        questionUrl = questionUrl,
        expiresAt = expiresAt,
        onBackClick = { navController.popBackStack() },
        onUploadQuestion = { context, uri, onResult ->
            userViewModel.uploadFileToCloudinary(context, uri) { url, _, error ->
                if (url != null) {
                    userViewModel.setWhiteboardQuestion(url)
                }
                onResult(url, error)
            }
        },
        onPostAnswer = { text ->
            userViewModel.postWhiteboardAnswer(text)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteboardContent(
    answers: List<WhiteboardAnswer>,
    currentUserUid: String?,
    questionUrl: String?,
    expiresAt: Long?,
    onBackClick: () -> Unit,
    onUploadQuestion: (android.content.Context, Uri, (String?, String?) -> Unit) -> Unit,
    onPostAnswer: (String) -> Unit
) {
    val context = LocalContext.current
    var answerText by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    // Persistent Countdown Timer Logic
    var timerString by remember { mutableStateOf("60:00") }
    
    LaunchedEffect(expiresAt, questionUrl) {
        if (questionUrl != null && expiresAt != null) {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val remainingMillis = expiresAt - currentTime
                
                if (remainingMillis > 0) {
                    val remainingSeconds = remainingMillis / 1000
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    timerString = "%02d:%02d".format(minutes, seconds)
                } else {
                    timerString = "00:00"
                    break
                }
                delay(1000L)
            }
        } else {
            timerString = "60:00"
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            onUploadQuestion(context, it) { url, error ->
                isUploading = false
                if (url == null) {
                    Toast.makeText(context, error ?: "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Study Whiteboard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LIVE • Collaborative Session", fontSize = 12.sp, color = Gold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Leave", tint = TextWhite)
                    }
                },
                actions = {
                    if (questionUrl != null) {
                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    timerString,
                                    color = Gold, 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A1A), titleContentColor = TextWhite)
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFF0A0A1A))) {
            
            // 1. Question Canvas (The Whiteboard)
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF12122A))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Gold)
                } else if (questionUrl != null) {
                    AsyncImage(
                        model = questionUrl,
                        contentDescription = "Question Image",
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, "Change Question", tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(
                            Icons.Default.CloudUpload, 
                            contentDescription = null, 
                            modifier = Modifier.size(80.dp), 
                            tint = Color.White.copy(alpha = 0.05f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Upload Your Question", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Share a photo of the problem", color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Select Image")
                        }
                    }
                }
            }

            // 2. Collaborative Answers Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(CardSurface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Community Solutions", color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text("${answers.size} Replies", color = TextWhite, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(answers) { answer ->
                        AnswerItem(answer, currentUserUid == answer.userId)
                    }
                }

                Row(
                    modifier = Modifier.padding(bottom = 20.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Write your solution...", color = TextMuted) },
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = Gold,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = {
                            if (answerText.isNotBlank()) {
                                onPostAnswer(answerText)
                                answerText = ""
                            }
                        },
                        containerColor = Gold,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerItem(answer: WhiteboardAnswer, isMe: Boolean) {
    val isTutor = answer.userName.contains("Tutor") || answer.userName.contains("Dr.")

    Surface(
        color = if (isTutor) StudyPrimary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = if (isTutor) androidx.compose.foundation.BorderStroke(1.dp, StudyPrimary.copy(alpha = 0.2f)) else null
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isMe) Gold else if (isTutor) StudyPrimary else StudyTeal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    answer.userName.take(1).uppercase(), 
                    color = if (isMe) Color.Black else Color.White, 
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(answer.userName, fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                    if (isTutor) {
                        Spacer(Modifier.width(6.dp))
                        Surface(color = Gold, shape = RoundedCornerShape(4.dp)) {
                            Text("TUTOR", fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(answer.text, color = TextGray, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WhiteboardContentPreview() {
    StudycircleTheme {
        WhiteboardContent(
            answers = listOf(
                WhiteboardAnswer(id = "1", userName = "Alex Doe", userId = "u1", text = "I think the answer is x = 5 by using the quadratic formula.", timestamp = System.currentTimeMillis()),
                WhiteboardAnswer(id = "2", userName = "Dr. Smith", userId = "u2", text = "Correct Alex! Remember to check the discriminant first.", timestamp = System.currentTimeMillis()),
                WhiteboardAnswer(id = "3", userName = "Me", userId = "me", text = "Could you explain step 2 further?", timestamp = System.currentTimeMillis())
            ),
            currentUserUid = "me",
            questionUrl = null,
            expiresAt = null,
            onBackClick = {},
            onUploadQuestion = { _, _, _ -> },
            onPostAnswer = {}
        )
    }
}
