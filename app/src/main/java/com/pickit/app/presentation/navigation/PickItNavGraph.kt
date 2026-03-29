package com.pickit.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pickit.app.presentation.ui.add.AddScreen
import com.pickit.app.presentation.ui.detail.DetailScreen
import com.pickit.app.presentation.ui.home.HomeScreen
import com.pickit.app.presentation.ui.preview.PreviewScreen
import com.pickit.app.presentation.ui.settings.SettingsScreen
import com.pickit.app.presentation.ui.stats.StatsScreen

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

@Composable
fun PickItAppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val bottomItems = listOf(
        BottomItem(Routes.Home, "首页", Icons.Outlined.Home),
        BottomItem(Routes.Stats, "统计", Icons.Outlined.BarChart),
        BottomItem(Routes.Settings, "设置", Icons.Outlined.Settings),
    )
    val showBottomBar = currentDestination?.route in bottomItems.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == Routes.Home) {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.Add) },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "新增商品")
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    viewModel = hiltViewModel(),
                    onOpenDetail = { navController.navigate(Routes.detail(it)) },
                    onOpenAdd = { navController.navigate(Routes.Add) },
                    contentPadding = innerPadding,
                )
            }
            composable(Routes.Add) {
                AddScreen(
                    onBack = { navController.popBackStack() },
                    onStartRecognition = { imageUri, note ->
                        navController.navigate(Routes.preview(imageUri, note))
                    },
                    contentPadding = innerPadding,
                )
            }
            composable(
                route = Routes.Preview,
                arguments = listOf(
                    navArgument("imageUri") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("note") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                PreviewScreen(
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                    onSaved = { productId ->
                        navController.navigate(Routes.detail(productId)) {
                            popUpTo(Routes.Home)
                        }
                    },
                    contentPadding = innerPadding,
                )
            }
            composable(Routes.Detail) {
                DetailScreen(
                    viewModel = hiltViewModel(),
                    onBack = { navController.popBackStack() },
                    contentPadding = innerPadding,
                )
            }
            composable(Routes.Stats) {
                StatsScreen(
                    viewModel = hiltViewModel(),
                    contentPadding = innerPadding,
                )
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    viewModel = hiltViewModel(),
                    contentPadding = innerPadding,
                )
            }
        }
    }
}
