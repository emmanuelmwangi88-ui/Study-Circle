package com.deepseek.studycircle.screens.grouphub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.StudyGroup
import com.deepseek.studycircle.models.studyGroups
import com.deepseek.studycircle.navigation.ROUTE_SESSION
import com.deepseek.studycircle.ui.theme.*

/**
 * STUDY CIRCLE COMMUNITY HUB (GroupsHubScreen)
 * 
 * Flow Documentation:
 * 1. Discover: Users enter this screen to find study communities (Groups).
 * 2. Connect: The screen features a "Live Sessions" bridge, connecting asynchronous group 
 *    discussion with real-time video learning.
 * 3. Navigation: 
 *    - To Live Hub: Use the Extended FAB or the "Ongoing Live Sessions" card.
 *    - To Session Details: Clicking on active groups or live peeks redirects to [SessionScreen].
 *
 * Data: Displays static [studyGroups] for community discovery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsHubScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Science", "Mathematics", "Arts", "Engineering", "Business")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Groups", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        },
        floatingActionButton = {
            // Bridges Groups to Live Sessions
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(ROUTE_SESSION) },
                containerColor = StudyPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.VideoCall, "Sessions") },
                text = { Text("Live Sessions") },
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = StudyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- Header & Search ---
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Join communities of learners like you.",
                    color = StudyTextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for groups...", color = StudyTextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = StudyTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = StudySurface,
                        focusedContainerColor = StudySurface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = StudyPrimary.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }

            // --- Categories ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        border = null
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Active Groups ---
                item {
                    SectionHeader(title = "Your Active Groups", action = "View All")
                }
                
                items(studyGroups.take(2)) { group ->
                    EnhancedGroupCard(
                        group = group,
                        isActive = true,
                        onClick = { navController.navigate(ROUTE_SESSION) }
                    )
                }

                // --- BRIDGE TO LIVE SESSIONS ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(ROUTE_SESSION) },
                        colors = CardDefaults.cardColors(containerColor = StudyTeal.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LiveTv, null, tint = StudyTeal)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Ongoing Live Sessions", fontWeight = FontWeight.Bold, color = StudyTeal)
                                Text("Join peer study rooms now", fontSize = 12.sp, color = StudyTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = StudyTextPrimary
        )
        if (action != null) {
            Text(
                text = action,
                fontSize = 14.sp,
                color = StudyPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
fun EnhancedGroupCard(group: StudyGroup, isActive: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) StudyPrimary.copy(alpha = 0.1f) else StudyTeal.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.ChatBubble else Icons.Default.Explore,
                        contentDescription = null,
                        tint = if (isActive) StudyPrimary else StudyTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        group.name,
                        fontWeight = FontWeight.Bold,
                        color = StudyTextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        "${group.members} Members • ${group.dailyPosts} Posts today",
                        fontSize = 12.sp,
                        color = StudyTextSecondary
                    )
                }

                if (!isActive) {
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Join", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    IconButton(onClick = onClick) {
                        Icon(Icons.Default.ChevronRight, null, tint = StudyTextSecondary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                group.description,
                fontSize = 14.sp,
                color = StudyTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            
            if (isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.7f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = StudyTeal,
                    trackColor = StudyTeal.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "70% Activity match this week",
                    fontSize = 10.sp,
                    color = StudyTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
fun GroupsHubScreenPreview() {
    GroupsHubScreen(rememberNavController())
}
