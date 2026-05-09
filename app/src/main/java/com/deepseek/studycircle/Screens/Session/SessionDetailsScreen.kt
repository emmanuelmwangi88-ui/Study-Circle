package com.deepseek.studycircle.screens.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Session
import com.deepseek.studycircle.ui.theme.StudyBackground
import com.deepseek.studycircle.ui.theme.StudySurface
import com.deepseek.studycircle.ui.theme.StudycircleTheme

@Composable
fun SessionDetailsScreen(
    navController: NavHostController,
    sessionId: String,
    userViewModel: UserViewModel
) {
    val session = userViewModel.allSessions.find { it.id == sessionId }

    SessionDetailsContent(
        session = session,
        onBackClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsContent(
    session: Session?,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            if (session != null) {
                Text("Title: ${session.title}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Topic: ${session.topic}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Host: ${session.student}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Date/Time: ${session.dateTime}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Session ID: ${session.id}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* TODO: Implement Reschedule */ }) {
                    Text("Reschedule")
                }
            } else {
                Text("Session not found.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SessionDetailsContentPreview() {
    StudycircleTheme {
        SessionDetailsContent(
            session = Session(
                id = "1",
                title = "Advanced Physics",
                topic = "Quantum Mechanics",
                student = "Dr. Smith",
                dateTime = "Oct 25, 14:00"
            ),
            onBackClick = {}
        )
    }
}
