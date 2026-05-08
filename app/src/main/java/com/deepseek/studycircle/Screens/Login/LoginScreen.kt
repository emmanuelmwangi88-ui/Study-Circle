package com.deepseek.studycircle.Screens.Login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.R
import com.deepseek.studycircle.navigation.ROUTE_REGISTER
import com.deepseek.studycircle.data.AuthViewModel
import com.deepseek.studycircle.ui.theme.*
import com.deepseek.studycircle.StudyCircle

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val authViewModel = remember { AuthViewModel(navController, context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudyBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.studycirclelogo),
            contentDescription = "Study Circle Logo",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome Back",
            color = StudyTextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Please login to your account",
            color = StudyTextSecondary,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email, 
                    contentDescription = null, 
                    tint = StudyPrimary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StudyTextPrimary,
                unfocusedTextColor = StudyTextPrimary,
                focusedBorderColor = StudyPrimary,
                unfocusedBorderColor = StudyTextSecondary.copy(alpha = 0.5f),
                cursorColor = StudyPrimary,
                focusedLabelColor = StudyPrimary,
                unfocusedLabelColor = StudyTextSecondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock, 
                    contentDescription = null, 
                    tint = StudyPrimary
                )
            },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = StudyTextSecondary)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StudyTextPrimary,
                unfocusedTextColor = StudyTextPrimary,
                focusedBorderColor = StudyPrimary,
                unfocusedBorderColor = StudyTextSecondary.copy(alpha = 0.5f),
                cursorColor = StudyPrimary,
                focusedLabelColor = StudyPrimary,
                unfocusedLabelColor = StudyTextSecondary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StudyPrimary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("LOGIN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.navigate(ROUTE_REGISTER) }) {
            Text(
                text = "Don't have an account? Register",
                color = StudyPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    StudyCircle {
        LoginScreen(rememberNavController())
    }
}
