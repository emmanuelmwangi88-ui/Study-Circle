package com.deepseek.studycircle.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deepseek.studycircle.Screens.About.AboutScreen
import com.deepseek.studycircle.Screens.Dashboard.DashboardScreen
import com.deepseek.studycircle.Screens.Login.LoginScreen
import com.deepseek.studycircle.Screens.Profile.ProfileScreen
import com.deepseek.studycircle.Screens.Session.VideoCallScreen
import com.deepseek.studycircle.Screens.Settings.SettingsScreen
import com.deepseek.studycircle.Screens.Timer.TimerScreen
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Resource
import com.deepseek.studycircle.Screens.ActivityCenter.ActivityCenterScreen
import com.deepseek.studycircle.Screens.credits.CreditCalculatorScreen
import com.deepseek.studycircle.Screens.GroupHub.GroupChatScreen
import com.deepseek.studycircle.Screens.GroupHub.GroupsHubScreen
import com.deepseek.studycircle.Screens.Knowledgebank.KnowledgeBankScreen
import com.deepseek.studycircle.Screens.Register.RegisterScreen
import com.deepseek.studycircle.Screens.ResourceDetails.ResourceDetailScreen
import com.deepseek.studycircle.Screens.Session.CreateSessionScreen
import com.deepseek.studycircle.Screens.Session.SessionScreen
import com.deepseek.studycircle.Screens.Splash.SplashScreen
import com.deepseek.studycircle.Screens.Uploadmaterials.UploadMaterialScreen
import com.deepseek.studycircle.Screens.Whitebooard.WhiteboardScreen


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASHSCREEN,
    userViewModel: UserViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(ROUTE_DASHBOARD) {
            DashboardScreen(navController, userViewModel = userViewModel)
        }
        composable(ROUTE_LOGIN) {
            LoginScreen(navController)
        }
        composable(ROUTE_REGISTER) {
            RegisterScreen(navController)
        }
        composable(ROUTE_SPLASHSCREEN) {
            SplashScreen(navController)
        }
        composable(ROUTE_WHITEBOARD) {
            WhiteboardScreen(navController, userViewModel = userViewModel)
        }
        composable(ROUTE_UPLOAD) {
            UploadMaterialScreen(navController, userViewModel = userViewModel)
        }
        composable(ROUTE_GROUP) {
            GroupsHubScreen(navController, userViewModel = userViewModel)
        }
        composable(
            route = "$ROUTE_GROUP_CHAT/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupChatScreen(navController, groupId, userViewModel)
        }
        composable(ROUTE_PROFILE) {
            ProfileScreen(navController, userViewModel = userViewModel)
        }
        composable(ROUTE_ACTIVITY) {
            ActivityCenterScreen(navController, userViewModel = userViewModel)
        }
        composable(
            route = "resource/{resourceId}",
            arguments = listOf(navArgument("resourceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resourceId = backStackEntry.arguments?.getString("resourceId")
            
            // Search in dynamic uploaded materials
            val material = userViewModel.allMaterials.find { it.id == resourceId }
            if (material != null) {
                val resource = Resource(
                    id = material.id,
                    title = material.title,
                    author = material.author,
                    authorBadge = "Student",
                    tag = "NEW",
                    type = material.fileType,
                    pages = 1,
                    size = "N/A",
                    downloads = 0,
                    rating = 5.0,
                    reviews = 0,
                    category = material.category,
                    cost = material.cost,
                    isBookmarked = false,
                    fileUrl = material.fileUrl
                )
                ResourceDetailScreen(navController, resource, userViewModel = userViewModel)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Resource not found")
                }
            }
        }
        composable(ROUTE_SETTINGS) {
           SettingsScreen(navController)
        }
        composable(ROUTE_KNOWLEDGE) {
            KnowledgeBankScreen(navController, userViewModel = userViewModel)
        }
        composable(ROUTE_SESSION) {
            SessionScreen(navController, userViewModel = userViewModel)
        }
        composable(ROUTE_CREATE_SESSION) {
            CreateSessionScreen(navController, userViewModel = userViewModel)
        }
        composable(
            route = ROUTE_VIDEO_CALL,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            VideoCallScreen(navController, sessionId, userViewModel)
        }
        composable(ROUTE_TIMER) {
            TimerScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_CREDITS) {
            CreditCalculatorScreen(navController, userViewModel = userViewModel)
        }
        composable(ROUTE_ABOUT) {
            AboutScreen(navController)
        }
    }
}
