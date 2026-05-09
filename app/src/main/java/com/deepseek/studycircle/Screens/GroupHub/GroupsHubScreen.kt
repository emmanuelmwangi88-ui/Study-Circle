package com.deepseek.studycircle.Screens.GroupHub

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
import com.deepseek.studycircle.navigation.ROUTE_GROUP_CHAT
import com.deepseek.studycircle.navigation.ROUTE_SESSION
import com.deepseek.studycircle.ui.theme.*

@Composable
fun GroupsHubScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val allGroups by userViewModel.allStudyGroups.collectAsState()
    val userGroups by userViewModel.userStudyGroups.collectAsState()

    GroupsHubContent(
        allGroups = allGroups,
        userGroups = userGroups,
        onBackClick = { navController.popBackStack() },
        onCreateGroup = { name, description, category ->
            userViewModel.createStudyGroup(name, description, category)
        },
        onJoinGroup = { groupId ->
            userViewModel.joinStudyGroup(groupId)
        },
        onGroupClick = { groupId ->
            navController.navigate("$ROUTE_GROUP_CHAT/$groupId")
        },
        onLiveSessionsClick = {
            navController.navigate(ROUTE_SESSION)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsHubContent(
    allGroups: List<StudyGroup>,
    userGroups: List<StudyGroup>,
    onBackClick: () -> Unit,
    onCreateGroup: (String, String, String) -> Unit,
    onJoinGroup: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onLiveSessionsClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Science", "Mathematics", "Arts", "Languages", "Technicals", "Religion")
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    val filteredGroups = allGroups.filter { group ->
        (selectedCategory == "All" || group.category == selectedCategory) &&
        (searchQuery.isBlank() || group.name.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Study Groups", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateGroupDialog = true }) {
                        Icon(Icons.Default.Add, "Create Group")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateGroupDialog = true },
                containerColor = StudyPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Create group") },
                text = { Text("Create Group") },
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
                if (userGroups.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Your Active Groups")
                    }
                    items(userGroups) { group ->
                        EnhancedGroupCard(
                            group = group,
                            isActive = true,
                            onCardClick = { onGroupClick(group.id.toString()) },
                            onButtonClick = { onGroupClick(group.id.toString()) }
                        )
                    }
                }

                item {
                    SectionHeader(title = "Discover Groups")
                }
                items(filteredGroups.filter { group -> userGroups.none { it.id == group.id } }) { group ->
                    EnhancedGroupCard(
                        group = group,
                        isActive = false,
                        onCardClick = { onJoinGroup(group.id.toString()) },
                        onButtonClick = { onJoinGroup(group.id.toString()) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLiveSessionsClick() },
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

        if (showCreateGroupDialog) {
            CreateGroupDialog(
                categories = categories.filter { it != "All" },
                onDismiss = { showCreateGroupDialog = false },
                onCreate = { name, description, category ->
                    onCreateGroup(name, description, category)
                    showCreateGroupDialog = false
                }
            )
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
fun EnhancedGroupCard(
    group: StudyGroup,
    isActive: Boolean,
    onCardClick: () -> Unit,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
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
                        "${group.members?.size ?: 0} Members",
                        fontSize = 12.sp,
                        color = StudyTextSecondary
                    )
                }

                if (!isActive) {
                    Button(
                        onClick = onButtonClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Join", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    IconButton(onClick = onButtonClick) {
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var isExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Study Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Group Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, description, selectedCategory) },
                enabled = name.isNotBlank() && description.isNotBlank() && selectedCategory.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GroupsHubContentPreview() {
    StudycircleTheme {
        GroupsHubContent(
            allGroups = listOf(
                StudyGroup(name = "Quantum Physics", description = "Exploring the subatomic world.", category = "Science", members = mapOf("1" to true, "2" to true)),
                StudyGroup(name = "Medieval History", description = "Discussing the Middle Ages.", category = "Arts", members = mapOf("1" to true))
            ),
            userGroups = listOf(
                StudyGroup(name = "Mobile App Dev", description = "Learning Kotlin and Compose.", category = "Technicals", members = mapOf("1" to true, "me" to true))
            ),
            onBackClick = {},
            onCreateGroup = { _, _, _ -> },
            onJoinGroup = {},
            onGroupClick = {},
            onLiveSessionsClick = {}
        )
    }
}
