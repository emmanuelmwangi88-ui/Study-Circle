package com.deepseek.studycircle.screens.tutoringbridge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.deepseek.studycircle.R
import com.deepseek.studycircle.models.Tutor
import com.deepseek.studycircle.models.tutors
import com.deepseek.studycircle.navigation.ROUTE_PROFILE
import com.deepseek.studycircle.navigation.ROUTE_SESSION
import com.deepseek.studycircle.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutoringBridgeScreen(navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tutoring Bridge", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUTE_SESSION) }) {
                        Icon(Icons.Default.VideoCall, contentDescription = "Sessions")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudySurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(StudyBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Connect with peer experts for one-on-one deep dives.",
                color = StudyTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Featured Expert",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = StudyTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            FeaturedTutorCard(tutors.first()) {
                navController.navigate(ROUTE_PROFILE)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top-Rated Tutors",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudyTextPrimary
                )
                Text(
                    text = "View all",
                    color = StudyPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { 
                        scope.launch { snackbarHostState.showSnackbar("Browsing all tutors...") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(tutors.drop(1)) { tutor ->
                    SmallTutorCard(
                        tutor = tutor, 
                        modifier = Modifier.width(260.dp),
                        onProfileClick = { navController.navigate(ROUTE_PROFILE) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SessionSchedulerCard {
                scope.launch { snackbarHostState.showSnackbar("Booking confirmed!") }
            }

            Spacer(modifier = Modifier.height(24.dp))
            YourStandingCard()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FeaturedTutorCard(tutor: Tutor, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = tutor.imageUrl.ifEmpty { R.drawable.person },
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.person)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tutor.name, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = StudyPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = tutor.expertise, color = StudyTextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = StudyAccentOrange, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "${tutor.creditsPer15Min * 4} Credits/hr", 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                            color = StudyAccentOrangeText, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "PhD candidate specializing in advanced systems. Offering practical insights into complex engineering problems.",
                color = StudyTextSecondary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = StudyAccentOrangeText, modifier = Modifier.size(16.dp))
                Text(" ${tutor.rating}", fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("Physics", "Engineering", "Calculus")) { tag ->
                        Surface(color = StudyBackground, shape = RoundedCornerShape(6.dp)) {
                            Text(text = tag, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 11.sp, color = StudyTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallTutorCard(tutor: Tutor, modifier: Modifier = Modifier, onProfileClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = tutor.imageUrl.ifEmpty { R.drawable.person },
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.person)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(tutor.name, fontWeight = FontWeight.Bold, color = StudyTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(tutor.expertise, fontSize = 12.sp, color = StudyTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = StudyAccentOrangeText, modifier = Modifier.size(14.dp))
                Text(" ${tutor.rating} (${tutor.sessions}+ Sessions)", fontSize = 12.sp, color = StudyTextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudyPrimary.copy(alpha = 0.1f), contentColor = StudyPrimary),
                contentPadding = PaddingValues(all = 0.dp)
            ) {
                Text("View Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SessionSchedulerCard(onConfirm: () -> Unit) {
    var selectedDay by remember { mutableStateOf(2) }
    var selectedTime by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = StudyPrimary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Session Scheduler", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
            
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("October 2024", fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val days = listOf(29, 30, 1, 2, 3, 4, 5)
                        days.forEach { day ->
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape)
                                    .background(if (selectedDay == day) StudyPrimary else Color.Transparent)
                                    .clickable { selectedDay = day },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$day", color = if (selectedDay == day) Color.White else StudyTextPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onConfirm() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Booking")
                    }
                }
            }
        }
    }
}

@Composable
fun YourStandingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudySurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Your Standing", fontWeight = FontWeight.Bold, color = StudyTextPrimary, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TOTAL EARNED", fontSize = 11.sp, color = StudyTextSecondary)
                    Text("1,450", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = StudyPrimary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TutoringBridgeScreenPreview() {
    TutoringBridgeScreen(rememberNavController())
}
