package com.deepseek.studycircle.screens.activitycenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.data.CreditCalculator
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Resource
import com.deepseek.studycircle.models.trendingResources
import com.deepseek.studycircle.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCenterScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val user by userViewModel.userData
    val transactions = userViewModel.userTransactions
    val creditsValue = user?.credits ?: 0L
    val rank = CreditCalculator.getRank(creditsValue)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        },
        containerColor = StudyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StudyBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Track your academic exchanges and reputation milestones.",
                color = StudyTextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Reputation",
                    value = user?.reputation?.toString() ?: "0.0",
                    subtitle = "Active Learner",
                    icon = Icons.Default.Star,
                    color = StudyAccentOrange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Credits",
                    value = CreditCalculator.formatCredits(creditsValue),
                    subtitle = rank,
                    icon = Icons.Default.Stars,
                    color = StudySecondary.copy(alpha = 0.2f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Transaction History
            Text(
                text = "Transaction History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StudySurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (transactions.isEmpty()) {
                        Text(
                            "No transactions yet.",
                            modifier = Modifier.padding(vertical = 20.dp)
                                .align(Alignment.CenterHorizontally),
                            color = StudyTextSecondary
                        )
                    } else {
                        val displayTransactions = transactions.take(5)
                        for ((index, transaction) in displayTransactions.withIndex()) {
                            TransactionItem(
                                title = transaction.description,
                                amount = (if (transaction.amount > 0) "+" else "") + transaction.amount.toString(),
                                time = SimpleDateFormat(
                                    "MMM dd, HH:mm",
                                    Locale.getDefault()
                                ).format(Date(transaction.timestamp)),
                                isExpense = transaction.amount < 0
                            )
                            if (index < displayTransactions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = StudyBackground
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            BookmarkedResourcesSection(navController, user?.bookmarks ?: emptyMap())
            Spacer(modifier = Modifier.height(20.dp))
            UpcomingSessionsSection()
            Spacer(modifier = Modifier.height(20.dp))
            MentorStandingCard(user?.name ?: "Learner", creditsValue)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                color = color,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = StudyTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontSize = 11.sp, color = StudyTextSecondary)
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary
            )
            Text(subtitle, fontSize = 10.sp, color = StudyTeal)
        }
    }
}

@Composable
fun TransactionItem(title: String, amount: String, time: String, isExpense: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = if (isExpense) StudyAccentOrange.copy(alpha = 0.1f) else StudyTeal.copy(
                alpha = 0.1f
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isExpense) Icons.Default.RemoveCircle else Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = if (isExpense) StudyAccentOrangeText else StudyTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary
            )
            Text(time, fontSize = 12.sp, color = StudyTextSecondary)
        }
        Text(
            text = amount,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isExpense) StudyAccentOrangeText else StudyTeal
        )
    }
}

@Composable
fun BookmarkedResourcesSection(
    navController: NavHostController,
    bookmarks: Map<String, Boolean>
) {
    val bookmarkedResources = trendingResources.filter { bookmarks[it.id] == true }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Text(
                    "Saved Bookmarks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary
                )
                Surface(
                    color = StudyTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${bookmarkedResources.size} SAVED",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyTeal
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (bookmarkedResources.isEmpty()) {
                Text(
                    "No bookmarks saved yet.",
                    fontSize = 14.sp,
                    color = StudyTextSecondary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            } else {
                bookmarkedResources.forEach { resource ->
                    BookmarkItem(resource) {
                        navController.navigate("resource/${resource.id}")
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun MentorStandingCard(name: String, credits: Long) {
    val rank = CreditCalculator.getRank(credits)
    val progress = CreditCalculator.getProgressToNextRank(credits)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudyPrimary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(rank, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(
                    "Rank Progress",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = StudyAccentOrange,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun BookmarkItem(resource: Resource, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(StudyBackground, RoundedCornerShape(12.dp)).padding(12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Bookmark,
            contentDescription = null,
            tint = StudyPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                resource.title,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary,
                fontSize = 14.sp
            )
            Text(resource.author, fontSize = 12.sp, color = StudyTextSecondary)
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "View",
            tint = StudyTextSecondary
        )
    }
}

@Composable
fun UpcomingSessionsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Upcoming Sessions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary
            )
        }
    }
}

@Composable
fun SessionSmallItem(time: String, title: String, student: String) {
    Column {
        Surface(color = StudyTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
            Text(
                time,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTeal
            )
        }
        Text(
            title,
            fontWeight = FontWeight.Bold,
            color = StudyTextPrimary,
            fontSize = 14.sp
        )
        Text(student, fontSize = 12.sp, color = StudyTextSecondary)
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityCenterScreenPreview() {
    ActivityCenterScreen(rememberNavController())
}
