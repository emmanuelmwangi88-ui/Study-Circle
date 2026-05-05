package com.deepseek.studycircle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.deepseek.studycircle.navigation.AppNavHost
import com.deepseek.studycircle.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyCircle {
                AppNavHost()
            }
        }
    }
}

@Composable
fun StudyCircle(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = StudyPrimary,
            secondary = StudySecondary,
            background = StudyBackground,
            surface = StudySurface,
            onPrimary = Color.White,
            onSecondary = StudyTextPrimary,
            onBackground = StudyTextPrimary,
            onSurface = StudyTextPrimary
        ),
        typography = Typography,
        content = content
    )
}
