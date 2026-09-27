package com.nova.healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nova.healthconnect.auth.AuthState
import com.nova.healthconnect.ui.navigation.NovaBottomNavigation
import com.nova.healthconnect.ui.navigation.Screen
import com.nova.healthconnect.ui.screens.AiChatScreen
import com.nova.healthconnect.ui.screens.CheckInScreen
import com.nova.healthconnect.ui.screens.FocusScreen
import com.nova.healthconnect.ui.screens.HealthScreen
import com.nova.healthconnect.ui.screens.LoginScreen
import com.nova.healthconnect.ui.screens.OverviewScreen
import com.nova.healthconnect.ui.screens.SettingsScreen
import com.nova.healthconnect.ui.screens.SplashScreen
import com.nova.healthconnect.ui.theme.NovaHealthConnectTheme
import com.nova.healthconnect.ui.viewmodels.AiChatViewModel
import com.nova.healthconnect.ui.viewmodels.CheckInViewModel
import com.nova.healthconnect.ui.viewmodels.FocusViewModel
import com.nova.healthconnect.ui.viewmodels.HealthViewModel
import com.nova.healthconnect.ui.viewmodels.OverviewViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaHealthConnectTheme {
                NovaAppContent()
            }
        }
    }
}

@Composable
fun NovaAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authManager = NovaApplication.instance.authManager
    val authState by authManager.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val showBottomBar = Screen.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NovaBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                // Splash Screen
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            val target = if (authState is AuthState.Authenticated) {
                                Screen.Overview.route
                            } else {
                                Screen.Login.route
                            }
                            navController.navigate(target) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Login Screen
                composable(Screen.Login.route) {
                    val isLoading = authState is AuthState.Loading
                    val errorMessage = (authState as? AuthState.Error)?.message

                    LoginScreen(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onLogin = { email, pass ->
                            coroutineScope.launch {
                                val result = authManager.login(email, pass)
                                if (result.isSuccess) {
                                    navController.navigate(Screen.Overview.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onDemoLogin = {
                            authManager.loginAsDemo()
                            navController.navigate(Screen.Overview.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Overview (Dashboard)
                composable(Screen.Overview.route) {
                    val overviewViewModel: OverviewViewModel = viewModel()
                    OverviewScreen(
                        viewModel = overviewViewModel,
                        onNavigate = { destination ->
                            navController.navigate(destination.route)
                        }
                    )
                }

                // Health Connect
                composable(Screen.Health.route) {
                    val healthViewModel: HealthViewModel = viewModel()
                    HealthScreen(
                        viewModel = healthViewModel
                    )
                }

                // Focus Sprint Timer
                composable(Screen.Focus.route) {
                    val focusViewModel: FocusViewModel = viewModel()
                    FocusScreen(
                        viewModel = focusViewModel
                    )
                }

                // Check-in & Reaction Calibration
                composable(Screen.CheckIn.route) {
                    val checkInViewModel: CheckInViewModel = viewModel()
                    CheckInScreen(
                        viewModel = checkInViewModel,
                        onCalibrationSaved = {
                            // Calibration saved
                        }
                    )
                }

                // Settings & Configuration
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                // AI Chat Companion
                composable(Screen.AiChat.route) {
                    val aiChatViewModel: AiChatViewModel = viewModel()
                    AiChatScreen(
                        viewModel = aiChatViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}