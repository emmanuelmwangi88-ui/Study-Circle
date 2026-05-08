package com.deepseek.studycircle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deepseek.studycircle.Screens.About.AboutScreen
import com.deepseek.studycircle.Screens.Login.LoginScreen
import com.deepseek.studycircle.Screens.Session.VideoCallScreen
import com.deepseek.studycircle.screens.settings.SettingsScreen
import com.deepseek.studycircle.Screens.Timer.TimerScreen
import com.deepseek.studycircle.data.UserViewModel
import com.deepseek.studycircle.models.Resource
import com.deepseek.studycircle.models.trendingResources
import com.deepseek.studycircle.screens.activitycenter.ActivityCenterScreen
import com.deepseek.studycircle.screens.credits.CreditCalculatorScreen
import com.deepseek.studycircle.screens.dashboard.DashboardScreen
import com.deepseek.studycircle.screens.grouphub.GroupsHubScreen
import com.deepseek.studycircle.screens.knowledgebank.KnowledgeBankScreen
import com.deepseek.studycircle.screens.profile.ProfileScreen
import com.deepseek.studycircle.screens.register.RegisterScreen
import com.deepseek.studycircle.screens.resourcedetails.ResourceDetailScreen
import com.deepseek.studycircle.screens.session.CreateSessionScreen
import com.deepseek.studycircle.screens.session.SessionScreen
import com.deepseek.studycircle.screens.splash.SplashScreen
import com.deepseek.studycircle.screens.uploadmaterials.UploadMaterialScreen
import com.deepseek.studycircle.screens.whiteboard.WhiteboardScreen


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
            
            // Search in static trending resources
            val staticResource = trendingResources.find { it.id == resourceId }
            
            if (staticResource != null) {
                ResourceDetailScreen(navController, staticResource, userViewModel = userViewModel)
            } else {
                // Search in dynamic uploaded materials
                val material = userViewModel.allMaterials.find { it.id == resourceId }
                if (material != null) {
                    val resource = Resource(
                        id = material.id,
                        title = material.title,
                        author = material.author,
                        authorBadge = "Student",
                        tag = "NEW",
                        type = "PDF",
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
                    // Fallback
                    ResourceDetailScreen(navController, trendingResources[0], userViewModel = userViewModel)
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
