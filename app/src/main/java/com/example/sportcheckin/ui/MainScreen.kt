package com.example.sportcheckin.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sportcheckin.R
import com.example.sportcheckin.ui.home.HomeScreen
import com.example.sportcheckin.ui.profile.ProfileScreen
import com.example.sportcheckin.ui.stats.StatsScreen

private data class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
)

private val bottomNavItems = listOf(
    BottomNavItem("home", R.string.tab_home, R.drawable.ic_home_24),
    BottomNavItem("stats", R.string.tab_stats, R.drawable.ic_stats_24),
    BottomNavItem("profile", R.string.tab_profile, R.drawable.ic_profile_24)
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = stringResource(id = item.labelRes)
                            )
                        },
                        label = { Text(text = stringResource(id = item.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("stats") { StatsScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}
