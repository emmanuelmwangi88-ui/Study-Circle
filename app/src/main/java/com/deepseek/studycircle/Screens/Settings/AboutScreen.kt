package com.deepseek.studycircle.Screens.Settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.navigation.ROUTE_SETTINGS
import com.deepseek.studycircle.ui.theme.StudyTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Studycircle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUTE_SETTINGS) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Studycircle v1.0.0",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Studycircle is a collaborative learning platform designed to connect students and mentors. Our mission is to facilitate academic exchanges and help everyone achieve their learning goals through peer-to-peer support.",
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Key Features:",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "• Peer Tutoring & Mentorship\n• Collaborative Whiteboards\n• Resource Sharing\n• Activity Tracking\n• Reputation System",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Developer Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Developed by Emmanuel Mwangi.",
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AboutScreen(rememberNavController())
}
