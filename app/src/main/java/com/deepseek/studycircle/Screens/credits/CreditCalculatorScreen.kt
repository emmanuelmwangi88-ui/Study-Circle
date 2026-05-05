package com.deepseek.studycircle.screens.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.deepseek.studycircle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCalculatorScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credit System", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudySurface,
                    titleContentColor = StudyTextPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(StudyBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CreditHeaderCard()
            }

            item {
                Text(
                    "How to Earn Credits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary
                )
            }

            item {
                CreditActionItem(
                    title = "First Login Bonus",
                    description = "Get started with a warm welcome!",
                    amount = "+2000 CR",
                    icon = Icons.Default.Stars,
                    color = Gold
                )
            }

            item {
                CreditActionItem(
                    title = "Upload Materials",
                    description = "Share your notes or resources with others.",
                    amount = "+50 CR",
                    icon = Icons.Default.AddCircle,
                    color = StudyTeal
                )
            }

            item {
                CreditActionItem(
                    title = "Help a Peer",
                    description = "Answer questions in study groups.",
                    amount = "+20 CR",
                    icon = Icons.Default.AddCircle,
                    color = StudyTeal
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "How to Use Credits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary
                )
            }

            item {
                CreditActionItem(
                    title = "Download Resources",
                    description = "Access high-quality study materials.",
                    amount = "-100 CR",
                    icon = Icons.Default.RemoveCircle,
                    color = StudyAccentOrangeText
                )
            }

            item {
                CreditActionItem(
                    title = "Book a Tutor",
                    description = "Get 1-on-1 help from experts.",
                    amount = "-500 CR",
                    icon = Icons.Default.RemoveCircle,
                    color = StudyAccentOrangeText
                )
            }
        }
    }
}

@Composable
fun CreditHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudyAccentOrange),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Stars,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Study Circle Credits",
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Your fuel for academic success. Earn by sharing, spend by learning.",
                color = Color.DarkGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CreditActionItem(
    title: String,
    description: String,
    amount: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = StudyTextPrimary)
                Text(description, fontSize = 12.sp, color = StudyTextSecondary)
            }
            Text(
                amount,
                fontWeight = FontWeight.ExtraBold,
                color = if (amount.startsWith("+")) StudyTeal else StudyAccentOrangeText,
                fontSize = 16.sp
            )
        }
    }
}
