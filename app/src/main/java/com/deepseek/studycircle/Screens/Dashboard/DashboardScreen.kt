package com.deepseek.studycircle.Screens.Dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.data.CreditCalculator
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Resource
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.navigation.*
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DashboardScreen(
    navController: NavHostController,
    onNotificationClick: () -> Unit = { navController.navigate(ROUTE_ACTIVITY) },
    onProfileClick: () -> Unit = { navController.navigate(ROUTE_PROFILE) },
    userViewModel: UserViewModel = viewModel()
) {
    val user by userViewModel.userData
    val materials = userViewModel.allMaterials
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    DashboardContent(
        user = user,
        materials = materials,
        snackbarHostState = snackbarHostState,
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
        onCreditsClick = { navController.navigate(ROUTE_CREDITS) },
        onLibraryClick = { navController.navigate(ROUTE_KNOWLEDGE) },
        onTimerClick = { navController.navigate(ROUTE_TIMER) },
        onGroupsClick = { navController.navigate(ROUTE_GROUP) },
        onSettingsClick = { navController.navigate(ROUTE_SETTINGS) },
        onSessionClick = { navController.navigate(ROUTE_SESSION) },
        onWhiteboardClick = { navController.navigate(ROUTE_WHITEBOARD) },
        onUploadClick = { navController.navigate(ROUTE_UPLOAD) },
        onResourceClick = { resourceId -> navController.navigate("resource/$resourceId") },
        onLikeAuthor = { authorId -> 
            userViewModel.likeUser(authorId) { success ->
                if (success) {
                    scope.launch { snackbarHostState.showSnackbar("Liked Author! Reputation increased.") }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    user: User?,
    materials: List<com.deepseek.studycircle.models.UploadMaterial>,
    snackbarHostState: SnackbarHostState,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCreditsClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onTimerClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSessionClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onUploadClick: () -> Unit,
    onResourceClick: (String) -> Unit,
    onLikeAuthor: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val creditsValue = user?.credits ?: 0L
    
    val studyTimeMillis = user?.studyTimeMillis ?: 0L
    val hours = studyTimeMillis / (1000 * 60 * 60)
    val minutes = (studyTimeMillis % (1000 * 60 * 60)) / (1000 * 60)
    val studyTimeString = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    val recentResources = remember(materials) {
        materials.map { mat ->
            Resource(
                id = mat.id,
                title = mat.title,
                author = mat.author,
                authorId = mat.authorId,
                authorBadge = "Student",
                tag = "NEW",
                type = mat.fileType,
                pages = mat.pages,
                size = mat.fileSize,
                downloads = mat.downloadCount,
                rating = mat.rating,
                reviews = 0,
                category = mat.category,
                cost = mat.cost,
                isBookmarked = false,
                fileUrl = mat.fileUrl,
                authorImage = mat.authorImage
            )
        }.sortedByDescending { it.id }.take(5)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        modifier = Modifier.padding(end = 8.dp).clickable { onCreditsClick() }
                    ) {
                        Text(
                            "📈 CR ${CreditCalculator.formatCredits(creditsValue)}",
                            color = StudyAccentOrangeText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    IconButton(onClick = onNotificationClick) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Activity")
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StudyPrimary.copy(alpha = 0.1f))
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.imageUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = user?.imageUri,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp),
                                tint = StudyPrimary
                            )
                        }
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
                        onLibraryClick()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = "Timer",
                        tint = Color.Red) },
                    label = { Text("Timer") },
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onTimerClick()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
                    label = { Text("Groups") },
                    selected = selectedTab == 3,
                    onClick = { 
                        selectedTab = 3
                        onGroupsClick()
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        onSettingsClick()
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
                        value = studyTimeString,
                        icon = Icons.Filled.Timer,
                        color = StudyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Credits",
                        value = CreditCalculator.formatCredits(creditsValue),
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
                        onClick = onLibraryClick
                    )
                    Homecard(
                        title = "Live Sessions",
                        icon = Icons.Filled.VideoLibrary,
                        background = StudyTeal,
                        modifier = Modifier.weight(1f),
                        onClick = onSessionClick
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
                        onClick = onWhiteboardClick
                    )
                    Homecard(
                        title = "Upload",
                        icon = Icons.Filled.CloudUpload,
                        background = StudyAccentOrangeText,
                        modifier = Modifier.weight(1f),
                        onClick = onUploadClick
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
                        "Recent Materials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StudyTextPrimary
                    )
                    TextButton(onClick = onLibraryClick) {
                        Text("View all", color = StudyPrimary)
                    }
                }
            }

            if (recentResources.isEmpty()) {
                item {
                    Text("No materials uploaded yet.", color = StudyTextSecondary, modifier = Modifier.padding(vertical = 16.dp))
                }
            } else {
                items(recentResources) { resource ->
                    ResourceItem(
                        resource = resource,
                        onClick = { onResourceClick(resource.id.toString()) },
                        onLikeClick = { onLikeAuthor(resource.authorId) }
                    )
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
fun ResourceItem(resource: Resource, onClick: () -> Unit, onLikeClick: () -> Unit = {}) {
    var isLiked by remember { mutableStateOf(false) }

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
                IconButton(
                    onClick = {
                        if (!isLiked) {
                            onLikeClick()
                            isLiked = true
                        }
                    }, 
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.ThumbUpAlt else Icons.Default.ThumbUp,
                        contentDescription = "Like Author",
                        tint = if (isLiked) StudyTeal else StudyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                Text(
                    String.format(Locale.getDefault(), "%.1f", resource.rating),
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
fun DashboardContentPreview() {
    StudycircleTheme {
        DashboardContent(
            user = User(name = "John Doe", credits = 1500, studyTimeMillis = 3600000),
            materials = emptyList(),
            snackbarHostState = SnackbarHostState(),
            onNotificationClick = {},
            onProfileClick = {},
            onCreditsClick = {},
            onLibraryClick = {},
            onTimerClick = {},
            onGroupsClick = {},
            onSettingsClick = {},
            onSessionClick = {},
            onWhiteboardClick = {},
            onUploadClick = {},
            onResourceClick = {},
            onLikeAuthor = {}
        )
    }
}
