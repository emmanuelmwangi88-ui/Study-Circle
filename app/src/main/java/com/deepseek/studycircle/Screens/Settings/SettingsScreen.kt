package com.deepseek.studycircle.Screens.Settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.data.AuthViewModel
import com.deepseek.studycircle.navigation.*
import com.deepseek.studycircle.ui.theme.StudyTextPrimary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel(navController, context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUTE_DASHBOARD) }) {
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
                text = "Account Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Manage your account and preferences", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(20.dp))

            val settingsOptions = listOf(
                "Account" to ROUTE_PROFILE,
                "Credits" to ROUTE_CREDITS,
                "Notifications" to ROUTE_ACTIVITY,
                "About Study Circle" to ROUTE_ABOUT,
                "Help & Support" to "https://www.instagram.com/itadori_manu/",
                "Sign Out" to ROUTE_LOGIN
            )

            settingsOptions.forEach { (label, route) ->
                TextButton(
                    onClick = {
                        if (label == "Sign Out") {
                            authViewModel.logout()
                        } else if (label == "Help & Support") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(route))
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            navController.navigate(route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        fontSize = 18.sp,
                        color = if (label == "Sign Out") Color.Red else Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(rememberNavController())
}
