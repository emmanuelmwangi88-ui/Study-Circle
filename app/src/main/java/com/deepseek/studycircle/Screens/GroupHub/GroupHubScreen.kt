package com.deepseek.studycircle.screens.grouphub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.deepseek.studycircle.models.StudyGroup
import com.deepseek.studycircle.models.studyGroups
import com.deepseek.studycircle.navigation.ROUTE_SESSION
import com.deepseek.studycircle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsHubScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Groups", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(ROUTE_SESSION)},
                containerColor = StudyPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Create Group")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(color = StudyBackground)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    "Collaborate with peers, share resources, and master complex subjects.",
                    color = StudyTextSecondary,
                    fontSize = 14.sp
                )
            }

            item {
                SectionHeader("My Groups")
            }
            
            items(studyGroups.take(2)) { group ->
                GroupCard(group, true) {
                    navController.navigate(ROUTE_SESSION)
                }
            }

            item {
                SectionHeader("Suggested for You")
            }
            
            items(studyGroups.takeLast(2)) { group ->
                GroupCard(group, false) {
                    // Navigate to group details/join
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = StudyTextPrimary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun GroupCard(group: StudyGroup, isActive: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(group.name, fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    group.description,
                    fontSize = 13.sp,
                    color = StudyTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, null, tint = StudyTextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${group.members} members • ${group.dailyPosts} posts/day",
                        fontSize = 11.sp,
                        color = StudyTextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) StudyTeal.copy(alpha = 0.1f) else StudyAccentOrange
            ) {
                Text(
                    text = if (isActive) "Active" else "Join",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isActive) StudyTeal else StudyAccentOrangeText,
                    fontSize = 12.sp,
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
