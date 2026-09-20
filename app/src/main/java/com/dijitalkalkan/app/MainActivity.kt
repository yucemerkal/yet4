package com.dijitalkalkan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dijitalkalkan.app.ui.screens.ActivityLogScreen
import com.dijitalkalkan.app.ui.screens.ChildHomeScreen
import com.dijitalkalkan.app.ui.screens.HomeScreen
import com.dijitalkalkan.app.ui.screens.PCodeScreen
import com.dijitalkalkan.app.ui.screens.ParentDashboardScreen
import com.dijitalkalkan.app.ui.theme.DijitalKalkanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DijitalKalkanTheme {
                DijitalKalkanApp()
            }
        }
    }
}

@Composable
fun DijitalKalkanApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onParentModeClick = { navController.navigate("pcode") },
                onChildModeClick = { navController.navigate("child_home") }
            )
        }
        composable("pcode") {
            PCodeScreen(
                onUnlocked = {
                    navController.navigate("parent_dashboard") {
                        popUpTo("pcode") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("parent_dashboard") {
            ParentDashboardScreen(
                onBack = { navController.popBackStack("home", inclusive = false) },
                onOpenActivityLog = { navController.navigate("activity_log") }
            )
        }
        composable("activity_log") {
            ActivityLogScreen(onBack = { navController.popBackStack() })
        }
        composable("child_home") {
            ChildHomeScreen(onBack = { navController.popBackStack() })
        }
    }
}
