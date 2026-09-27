package com.nova.healthconnect.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val isBottomBarItem: Boolean = false
) {
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Sign In")
    object Overview : Screen("overview", "Overview", Icons.Rounded.Home, isBottomBarItem = true)
    object Health : Screen("health", "Health", Icons.Rounded.Favorite, isBottomBarItem = true)
    object Focus : Screen("focus", "Focus", Icons.Rounded.Timer, isBottomBarItem = true)
    object CheckIn : Screen("checkin", "Check-in", Icons.Rounded.CheckCircle, isBottomBarItem = true)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Person, isBottomBarItem = true)
    object AiChat : Screen("ai_chat", "NOVA Assistant", Icons.Rounded.AutoAwesome, isBottomBarItem = false)

    companion object {
        val bottomNavItems = listOf(
            Overview,
            Health,
            Focus,
            CheckIn,
            Settings
        )
    }
}
