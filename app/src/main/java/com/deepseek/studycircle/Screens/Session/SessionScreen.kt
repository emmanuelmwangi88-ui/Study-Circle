package com.deepseek.studycircle.Screens.Session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Session
import com.deepseek.studycircle.navigation.ROUTE_CREATE_SESSION
import com.deepseek.studycircle.ui.theme.*

/**
 * SessionScreen displays all current and upcoming live study sessions.
 */
@Composable
fun SessionScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val allCombinedSessions = userViewModel.allSessions
    val currentUserId = userViewModel.userData.value?.uid

    SessionContent(
        allCombinedSessions = allCombinedSessions,
        currentUserId = currentUserId,
        onBackClick = { navController.popBackStack() },
        onCreateSessionClick = { navController.navigate(ROUTE_CREATE_SESSION) },
        onDeleteSession = { sessionId -> userViewModel.deleteSession(sessionId) { } },
        onJoinSession = { sessionId -> navController.navigate("video_call/$sessionId") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionContent(
    allCombinedSessions: List<Session>,
    currentUserId: String?,
    onBackClick: () -> Unit,
    onCreateSessionClick: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onJoinSession: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredSessions = remember(searchQuery, allCombinedSessions) {
        allCombinedSessions.filter { session ->
            searchQuery.isBlank() || 
            session.title.contains(searchQuery, ignoreCase = true) || 
            session.student.contains(searchQuery, ignoreCase = true) ||
            session.topic.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Sessions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSessionClick,
                containerColor = StudyPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Start a new session")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StudyBackground)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search sessions, hosts, topics...", color = StudyTextSecondary, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = StudyTextSecondary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = StudyTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = StudySurface,
                    focusedContainerColor = StudySurface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = StudyPrimary.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val liveSessions = filteredSessions.filter { it.isLive }
                val scheduledSessions = filteredSessions.filter { !it.isLive }

                if (liveSessions.isNotEmpty()) {
                    item {
                        Text(
                            text = if (searchQuery.isEmpty()) "Ongoing Study Sessions" else "Matching Live Sessions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StudyTextPrimary
                        )
                    }
                    items(liveSessions) { session ->
                        LiveVideoCard(
                            session = session,
                            onJoin = { onJoinSession(session.id) },
                            isOwner = session.creatorId == currentUserId,
                            onDelete = { onDeleteSession(session.id) }
                        )
                    }
                }

                if (scheduledSessions.isNotEmpty()) {
                    item {
                        Text(
                            text = if (searchQuery.isEmpty()) "Scheduled" else "Matching Scheduled Sessions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudyTextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(scheduledSessions) { session ->
                        SessionCard(
                            session = session,
                            isOwner = session.creatorId == currentUserId,
                            onDelete = { onDeleteSession(session.id) },
                            onJoin = { onJoinSession(session.id) }
                        )
                    }
                }

                if (filteredSessions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text(text = "No sessions found", color = StudyTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveVideoCard(
    session: Session,
    onJoin: () -> Unit,
    isOwner: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.DarkGray)
            ) {
                if (session.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = session.thumbnailUrl,
                        contentDescription = "Session Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Surface(
                    color = StudyAccentOrange,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                ) {
                    Text(
                        "LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (isOwner) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .align(Alignment.Center)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(session.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Host: ${session.student}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VideoCall, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Join Session")
                }
            }
        }
    }
}

@Composable
fun SessionCard(
    session: Session,
    isOwner: Boolean,
    onDelete: () -> Unit,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = StudyTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = session.dateTime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyTeal
                    )
                }

                if (isOwner) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = session.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudyTextPrimary)
            Text(text = "Tutor: ${session.student}", fontSize = 14.sp, color = StudyTextSecondary)

            if (session.topic.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Topics: ${session.topic}", fontSize = 12.sp, color = StudyTextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onJoin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary)
                ) {
                    Text("Join", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { /* Details */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudyPrimary.copy(alpha = .2f))
                ) {
                    Text("Details", fontSize = 13.sp, color = StudyTextPrimary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SessionContentPreview() {
    StudycircleTheme {
        SessionContent(
            allCombinedSessions = listOf(
                Session(id = "1", title = "Advanced Physics", student = "Dr. Smith", isLive = true),
                Session(id = "2", title = "Linear Algebra", student = "Jane Doe", dateTime = "Oct 25, 14:00")
            ),
            currentUserId = "me",
            onBackClick = {},
            onCreateSessionClick = {},
            onDeleteSession = {},
            onJoinSession = {}
        )
    }
}
