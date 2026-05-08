package com.deepseek.studycircle.screens.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.ui.theme.StudyBackground
import com.deepseek.studycircle.ui.theme.StudySurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailsScreen(
    navController: NavHostController,
    sessionId: String,
    userViewModel: UserViewModel
) {
    val session = userViewModel.allSessions.find { it.id == sessionId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details", fontWeight = FontWeight.Bold) },
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
