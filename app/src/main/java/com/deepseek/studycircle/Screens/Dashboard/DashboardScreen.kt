package com.deepseek.studycircle.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Resource
import com.deepseek.studycircle.models.trendingResources
import com.deepseek.studycircle.navigation.*
import com.deepseek.studycircle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    onNotificationClick: () -> Unit = { navController.navigate(ROUTE_ACTIVITY) },
    onProfileClick: () -> Unit = { navController.navigate(ROUTE_PROFILE) },
    userViewModel: UserViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val user by userViewModel.userData
    val creditsValue = user?.credits ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Study Circle",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Level up your learning",
                            style = MaterialTheme.typography.labelSmall,
                            color = StudyTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudySurface,
                    titleContentColor = StudyTextPrimary,
                    actionIconContentColor = StudyTextPrimary
                ),
                actions = {
                    Surface(
                        color = StudyAccentOrange,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp).clickable { navController.navigate(ROUTE_CREDITS) }
                    ) {
                        Text(
                            "📈 $creditsValue CR",
                            color = StudyAccentOrangeText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Activity")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudySurface,
                tonalElevation = 7.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = "resources") },
                    label = { Text("Library") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        navController.navigate(ROUTE_KNOWLEDGE)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = "Timer") },
                    label = { Text("Timer") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate(ROUTE_TIMER)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
                    label = { Text("Groups") },
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        navController.navigate(ROUTE_GROUP)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        navController.navigate(ROUTE_SETTINGS)
                    }
                )
            }
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
                Text(
                    "Welcome back, ${user?.name ?: "Learner"}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Study Hours",
                        value = "24h",
                        icon = Icons.Filled.Timer,
                        color = StudyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Credits",
                        value = creditsValue.toString(),
                        icon = Icons.Filled.Stars,
                        color = StudyTeal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    "Quick Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Homecard(
                        title = "Knowledge Bank",
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                        background = StudyBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(ROUTE_KNOWLEDGE) }
                    )
                    Homecard(
                        title = "Tutoring Bridge",
                        icon = Icons.Filled.School,
                        background = StudyTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(ROUTE_TUTORING) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Homecard(
                        title = "Whiteboard",
                        icon = Icons.Filled.Draw,
                        background = Color(0xFF9C27B0),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(ROUTE_WHITEBOARD) }
                    )
                    Homecard(
                        title = "Upload",
                        icon = Icons.Filled.CloudUpload,
                        background = StudyAccentOrangeText,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(ROUTE_UPLOAD) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Featured Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StudyTextPrimary
                    )
                    TextButton(onClick = { navController.navigate(ROUTE_KNOWLEDGE) }) {
                        Text("View all", color = StudyPrimary)
                    }
                }
            }

            items(trendingResources) { resource ->
                ResourceItem(resource) {
                    navController.navigate("resource/${resource.id}")
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = StudyTextSecondary)
        }
    }
}

@Composable
fun Homecard(
    title: String,
    icon: ImageVector,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = background),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(64.dp).align(Alignment.BottomEnd)
            )
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}

@Composable
fun ResourceItem(resource: Resource, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudySelectedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (resource.type.uppercase() == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (resource.type.uppercase() == "PDF") Color(0xFFE74C3C) else StudyPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    resource.title,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${resource.author} • ${resource.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudyTextSecondary
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                Text(
                    resource.rating.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardScreen(rememberNavController())
}
