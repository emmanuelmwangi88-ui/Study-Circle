package com.deepseek.studycircle.Screens.GroupHub

import android.net.Uri
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.ChatMessage
import com.deepseek.studycircle.models.StudyGroup
import com.deepseek.studycircle.ui.theme.*

@Composable
fun GroupChatScreen(
    navController: NavHostController,
    groupId: String,
    userViewModel: UserViewModel = viewModel()
) {
    val group by userViewModel.getGroupById(groupId).collectAsState(initial = null)
    val messages = userViewModel.groupMessages[groupId] ?: emptyList()
    val currentUser = userViewModel.userData.value

    GroupChatContent(
        group = group,
        messages = messages,
        currentUserUid = currentUser?.uid,
        onBackClick = { navController.popBackStack() },
        onVideoCallClick = { navController.navigate("video_call/$groupId") },
        onSendMessage = { text -> userViewModel.sendChatMessage(groupId, text) },
        onUploadFile = { context, uri, onResult ->
            userViewModel.uploadFileToCloudinary(context, uri) { url, type, error ->
                if (url != null) {
                    userViewModel.sendChatMessage(groupId, "Sent a file", fileUrl = url, fileType = type)
                }
                onResult(url, type, error)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatContent(
    group: StudyGroup?,
    messages: List<ChatMessage>,
    currentUserUid: String?,
    onBackClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onUploadFile: (android.content.Context, Uri, (String?, String?, String?) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var messageText by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            onUploadFile(context, it) { url, type, error ->
                isUploading = false
                if (url == null) {
                    Toast.makeText(context, error ?: "Failed to upload file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(group?.name ?: "Group Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${group?.members?.size ?: 0} members", fontSize = 12.sp, color = StudyTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onVideoCallClick) {
                        Icon(Icons.Default.VideoCall, "Group Call", tint = StudyPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        },
        containerColor = StudyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    ChatMessageItem(message, currentUserUid)
                }
            }

            if (isUploading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = StudyPrimary)
            }

            // Message Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = StudySurface
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(Icons.Default.AttachFile, "Attach File")
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message...", color = StudyTextSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = StudyBackground,
                            focusedContainerColor = StudyBackground,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = StudyPrimary.copy(alpha = 0.5f),
                            focusedTextColor = StudyTextPrimary,
                            unfocusedTextColor = StudyTextPrimary
                        )
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank() && !isUploading
                    ) {
                        Icon(
                            Icons.Default.Send,
                            "Send", 
                            tint = if (messageText.isNotBlank()) StudyPrimary else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage, currentUserUid: String?) {
    val isCurrentUser = message.senderId == currentUserUid

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isCurrentUser) {
            ChatProfileImage(imageUrl = message.senderImage)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                            bottomEnd = if (isCurrentUser) 0.dp else 16.dp
                        )
                    )
                    .background(if (isCurrentUser) StudyPrimary.copy(alpha = 0.2f) else StudySurface)
                    .padding(12.dp)
            ) {
                Column {
                    if (!isCurrentUser) {
                        Text(
                            text = message.senderName,
                            fontWeight = FontWeight.Bold,
                            color = StudyPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    
                    if (message.fileUrl != null) {
                        FileAttachmentPreview(message.fileUrl, message.fileType ?: "FILE")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (message.text != "Sent a file" || message.fileUrl == null) {
                        Text(
                            text = message.text,
                            color = StudyTextPrimary,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            Text(
                text = formatTimestamp(message.timestamp),
                fontSize = 9.sp,
                color = StudyTextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            ChatProfileImage(imageUrl = message.senderImage)
        }
    }
}

@Composable
fun FileAttachmentPreview(url: String, type: String) {
    val isImage = type.uppercase() in listOf("JPG", "JPEG", "PNG", "WEBP")
    
    if (isImage) {
        AsyncImage(
            model = url,
            contentDescription = "Attached Image",
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .heightIn(max = 200.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.05f))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (type.uppercase() == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Description,
                contentDescription = null,
                tint = if (type.uppercase() == "PDF") Color(0xFFE74C3C) else StudyPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "File Attachment ($type)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = StudyTextPrimary
            )
        }
    }
}

@Composable
fun ChatProfileImage(imageUrl: String, size: Int = 32) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(StudyPrimary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size((size * 0.6).dp),
                tint = StudyPrimary
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return if (timestamp == 0L) "" else {
        DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
}

@Preview(showBackground = true)
@Composable
fun GroupChatContentPreview() {
    StudycircleTheme {
        GroupChatContent(
            group = StudyGroup(name = "Physics Study Group", members = mapOf("1" to true, "2" to true)),
            messages = listOf(
                ChatMessage(senderId = "1", senderName = "Alice", text = "Hello everyone!", timestamp = System.currentTimeMillis() - 100000),
                ChatMessage(senderId = "2", senderName = "Bob", text = "Hi Alice!", timestamp = System.currentTimeMillis() - 50000),
                ChatMessage(senderId = "me", senderName = "Me", text = "How are you guys?", timestamp = System.currentTimeMillis())
            ),
            currentUserUid = "me",
            onBackClick = {},
            onVideoCallClick = {},
            onSendMessage = {},
            onUploadFile = { _, _, _ -> }
        )
    }
}
