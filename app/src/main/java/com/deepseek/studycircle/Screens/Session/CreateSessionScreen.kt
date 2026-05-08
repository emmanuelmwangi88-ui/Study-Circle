package com.deepseek.studycircle.screens.session

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Session
import com.deepseek.studycircle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var createdSession by remember { mutableStateOf<Session?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (createdSession == null) "Host Study Session" else "Session Active",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        }
    ) { padding ->
        if (createdSession == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(StudyBackground)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Session Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudyTextPrimary)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic/Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isBlank() || topic.isBlank()) {
                            Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isCreating = true
                        userViewModel.createSession(title, topic) { session ->
                            isCreating = false
                            if (session != null) {
                                createdSession = session
                            } else {
                                Toast.makeText(context, "Failed to create session", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                    enabled = !isCreating
                ) {
                    if (isCreating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Schedule Session", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(StudyBackground)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = StudyTeal, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Session Ready!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = StudyTextPrimary)
                Text(text = createdSession?.title ?: "", fontSize = 18.sp, color = StudyTextSecondary)

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        val sessionId = createdSession?.id ?: ""
                        navController.navigate("video_call/$sessionId")
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary)
                ) {
                    Icon(Icons.Default.VideoCall, null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Join Session", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Return to Hub", fontSize = 16.sp, color = StudyTextPrimary)
                }
            }
        }
    }
}
