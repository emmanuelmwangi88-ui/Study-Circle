package com.deepseek.studycircle.Screens.Knowledgebank

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.R
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Resource
import com.deepseek.studycircle.models.UploadMaterial
import com.deepseek.studycircle.models.User
import com.deepseek.studycircle.ui.theme.*

@Composable
fun KnowledgeBankScreen(navController: NavHostController, userViewModel: UserViewModel = viewModel()) {
    val uploadedMaterials = userViewModel.allMaterials
    val user by userViewModel.userData

    KnowledgeBankContent(
        uploadedMaterials = uploadedMaterials,
        user = user,
        onBackClick = { navController.popBackStack() },
        onResourceClick = { resourceId -> navController.navigate("resource/$resourceId") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBankContent(
    uploadedMaterials: List<UploadMaterial>,
    user: User?,
    onBackClick: () -> Unit,
    onResourceClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All Topics") }
    
    val userBookmarks = user?.bookmarks ?: emptyMap()

    val categories = listOf("All Topics", "Chemistry", "Mathematics", "Physics", "Biology", "Languages")

    val mappedResources = remember(uploadedMaterials, userBookmarks) {
        uploadedMaterials.map { mat ->
            Resource(
                id = mat.id,
                title = mat.title,
                author = mat.author,
                authorBadge = "Student",
                tag = "NEW",
                type = mat.fileType,
                pages = 1,
                size = "N/A",
                downloads = 0,
                rating = 5.0,
                reviews = 0,
                category = mat.category,
                cost = mat.cost,
                isBookmarked = userBookmarks[mat.id] == true,
                fileUrl = mat.fileUrl,
                authorImage = mat.authorImage
            )
        }
    }
    
    val filteredResources = remember(searchQuery, selectedCategory, mappedResources) {
        mappedResources.filter { res ->
            val matchesSearch = res.title.contains(searchQuery, ignoreCase = true) || 
                              res.author.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All Topics" || res.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Bank", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discover and exchange peer-reviewed study materials.",
                    color = StudyTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search subjects, notes...",
                            color = StudyTextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = StudyTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = StudyTextSecondary
                                )
                            }
                        }
                    },
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

            Spacer(modifier = Modifier.height(16.dp))

            SectionHeader(title = "Categories")
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(categories) { cat ->
                    CategoryChip(
                        text = cat,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (filteredResources.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No resources found", color = StudyTextSecondary)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp, 
                            contentDescription = null, 
                            tint = StudyAccentOrangeText, 
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Trending Resources" else "Search Results",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudyTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 12.dp)
                ) {
                    items(filteredResources) { res ->
                        ResourceCard(
                            res = res,
                            modifier = Modifier.width(260.dp),
                            onClick = { onResourceClick(res.id.toString()) }
                        )
                    }
                }
            }

            SectionHeader(title = "Recently Added")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp)
            ) {
                items(mappedResources.sortedByDescending { it.id }.take(10)) { res ->
                    ResourceCard(
                        res = res,
                        modifier = Modifier.width(260.dp),
                        onClick = { onResourceClick(res.id.toString()) }
                    )
                }
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
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) StudyPrimary else StudySurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else StudyTextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ResourceCard(res: Resource, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (res.type.uppercase() == "PDF") Icons.Default.PictureAsPdf else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (res.type.uppercase() == "PDF") Color(0xFFE74C3C) else StudyPrimary,
                    modifier = Modifier.size(64.dp)
                )

                Surface(
                    color = StudyAccentOrange.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Text(
                        text = "${res.cost} Credits",
                        color = StudyAccentOrangeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (res.authorImage.isNotEmpty()) {
                        AsyncImage(
                            model = res.authorImage,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.person),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = res.author, 
                        color = StudyTextSecondary, 
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = res.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = StudyBackground,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = res.type,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudyTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = StudyTextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${res.downloads}", color = StudyTextSecondary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Barter", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground =  true)
@Composable
fun KnowledgeBankContentPreview() {
    StudycircleTheme {
        KnowledgeBankContent(
            uploadedMaterials = listOf(
                UploadMaterial(id = "1", title = "Organic Chemistry Notes", author = "Alice", category = "Chemistry", cost = 100),
                UploadMaterial(id = "2", title = "Calculus II Problems", author = "Bob", category = "Mathematics", cost = 150)
            ),
            user = User(name = "John Doe"),
            onBackClick = {},
            onResourceClick = {}
        )
    }
}
