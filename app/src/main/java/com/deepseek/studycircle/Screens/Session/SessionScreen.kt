package com.deepseek.studycircle.screens.session

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.models.Session
import com.deepseek.studycircle.models.upcomingSessions
import com.deepseek.studycircle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Sessions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StudyBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val liveSessions = upcomingSessions.filter { it.isLive }
            val scheduledSessions = upcomingSessions.filter { !it.isLive }

            if (liveSessions.isNotEmpty()) {
                item {
                    Text(
                        "Ongoing Sessions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudyTextPrimary
                    )
                }
                items(liveSessions) { session ->
                    LiveVideoCard(session)
                }
            }

            item {
                Text(
                    "Upcoming Sessions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (scheduledSessions.isEmpty() && liveSessions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No sessions found", color = StudyTextSecondary)
                    }
                }
            } else {
                items(scheduledSessions) { session ->
                    SessionCard(session)
                }
            }
        }
    }
}

@Composable
fun LiveVideoCard(session: Session) {
    val context = LocalContext.current
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
                    color = Color.Red,
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
                Text("with ${session.student}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        if (session.zoomLink.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, session.zoomLink.toUri())
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VideoCall, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Join Zoom Session")
                }
            }
        }
    }
}

@Composable
fun SessionCard(session: Session) {
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
                    onClick = { /* Details */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary)
                ) {
                    Text("Details", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { /* Reschedule */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudyPrimary.copy(alpha = 0.2f))
                ) {
                    Text("Reschedule", fontSize = 13.sp, color = StudyTextPrimary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SessionScreenPreview() {
    SessionScreen(rememberNavController())
}
