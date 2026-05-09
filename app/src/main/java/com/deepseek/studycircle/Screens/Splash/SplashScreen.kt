package com.deepseek.studycircle.Screens.Splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.R
import com.deepseek.studycircle.navigation.ROUTE_DASHBOARD
import com.deepseek.studycircle.navigation.ROUTE_LOGIN
import com.deepseek.studycircle.navigation.ROUTE_SPLASHSCREEN
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1500) // 1.5 second delay
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, go to Dashboard
            navController.navigate(ROUTE_DASHBOARD) {
                popUpTo(ROUTE_SPLASHSCREEN) { inclusive = true }
            }
        } else {
            // No user, go to Login
            navController.navigate(ROUTE_LOGIN) {
                popUpTo(ROUTE_SPLASHSCREEN) { inclusive = true }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.splash),
                contentDescription = "Study Circle Logo",
                modifier = Modifier.size(1500.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(rememberNavController())
}
