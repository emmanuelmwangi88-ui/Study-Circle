package com.deepseek.studycircle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.deepseek.studycircle.screens.dashboard.DashboardScreen
import com.deepseek.studycircle.screens.grouphub.GroupsHubScreen
import com.deepseek.studycircle.screens.resourcedetails.ResourceDetailScreen
import com.deepseek.studycircle.screens.session.SessionScreen
import com.deepseek.studycircle.models.trendingResources
import com.deepseek.studycircle.screens.activitycenter.ActivityCenterScreen
import com.deepseek.studycircle.screens.knowledgebank.KnowledgeBankScreen
import com.deepseek.studycircle.screens.login.LoginScreen
import com.deepseek.studycircle.screens.profile.ProfileScreen
import com.deepseek.studycircle.screens.register.RegisterScreen
import com.deepseek.studycircle.screens.settings.SettingsScreen
import com.deepseek.studycircle.Screens.Settings.AboutScreen
import com.deepseek.studycircle.screens.uploadmaterials.UploadMaterialScreen
import com.deepseek.studycircle.screens.whiteboard.WhiteboardScreen
import com.deepseek.studycircle.screens.splash.SplashScreen
import com.deepseek.studycircle.screens.tutoringbridge.TutoringBridgeScreen
import com.deepseek.studycircle.screens.credits.CreditCalculatorScreen
import com.deepseek.studycircle.Screens.Timer.TimerScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASHSCREEN
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(ROUTE_DASHBOARD) {
            DashboardScreen(navController)
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
            WhiteboardScreen(navController)
        }
        composable(ROUTE_UPLOAD) {
            UploadMaterialScreen(navController)
        }
        composable(ROUTE_GROUP) {
            GroupsHubScreen(navController)
        }
        composable(ROUTE_PROFILE) {
            ProfileScreen(navController)
        }
        composable(ROUTE_ACTIVITY) {
            ActivityCenterScreen(navController)
        }
        composable(ROUTE_TUTORING) {
            TutoringBridgeScreen(navController)
        }
        composable(
            route = "resource/{resourceId}",
            arguments = listOf(navArgument("resourceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val resourceId = backStackEntry.arguments?.getInt("resourceId")
            val resource = trendingResources.find { it.id == resourceId } ?: trendingResources[0]
            ResourceDetailScreen(navController, resource)
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(navController)
        }
        composable(ROUTE_KNOWLEDGE) {
            KnowledgeBankScreen(navController)
        }
        composable(ROUTE_SESSION) {
            SessionScreen(navController)
        }
        composable(ROUTE_TIMER) {
            TimerScreen(onBack = { navController.popBackStack() })
        }
        composable(ROUTE_CREDITS) {
            CreditCalculatorScreen(navController)
        }
        composable(ROUTE_ABOUT) {
            AboutScreen(navController)
        }
    }
}
