package com.deepseek.studycircle.screens.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deepseek.studycircle.data.CreditCalculator
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.CreditTransaction
import com.deepseek.studycircle.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCalculatorScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = viewModel()
) {
    val user by userViewModel.userData
    val transactions = userViewModel.userTransactions
    val credits = user?.credits ?: 0L
    val rank = CreditCalculator.getRank(credits)
    val progress = CreditCalculator.getProgressToNextRank(credits)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Credits", fontWeight = FontWeight.Bold) },
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
                CreditStatusCard(credits, rank, progress)
            }

            item {
                Text(
                    "Transaction History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StudySurface)
                    ) {
                        Text(
                            "No transactions yet. Start earning credits!",
                            modifier = Modifier.padding(24.dp),
                            color = StudyTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Credit Guide",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary
                )
            }

            item {
                CreditGuideItem(
                    title = "Upload Materials",
                    amount = "+${CreditCalculator.UPLOAD_REWARD} CR",
                    icon = Icons.Default.CloudUpload,
                    color = StudyTeal
                )
            }

            item {
                CreditGuideItem(
                    title = "Download Resources",
                    amount = "-${CreditCalculator.DOWNLOAD_COST} CR",
                    icon = Icons.Default.FileDownload,
                    color = StudyAccentOrangeText
                )
            }
        }
    }
}

@Composable
fun CreditStatusCard(credits: Long, rank: String, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudyAccentOrange),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Balance", color = Color.Black.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text(
                        "${CreditCalculator.formatCredits(credits)} CR",
                        color = Color.Black,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Stars,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.padding(12.dp).size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(rank, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("${(progress * 100).toInt()}% to next rank", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.6f))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color.Black,
                trackColor = Color.Black.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: CreditTransaction) {
    val isGain = transaction.amount > 0
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val date = sdf.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = (if (isGain) StudyTeal else StudyAccentOrangeText).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isGain) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (isGain) StudyTeal else StudyAccentOrangeText,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.description.ifEmpty { transaction.type },
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary,
                    maxLines = 1
                )
                Text(date, fontSize = 12.sp, color = StudyTextSecondary)
            }
            
            Text(
                "${if (isGain) "+" else ""}${transaction.amount}",
                fontWeight = FontWeight.ExtraBold,
                color = if (isGain) StudyTeal else StudyAccentOrangeText,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun CreditGuideItem(title: String, amount: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = StudyTextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(amount, fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 14.sp)
    }
}
