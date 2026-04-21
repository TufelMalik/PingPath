package com.techquantum.pingpath.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.techquantum.pingpath.modules.addalert.AddAlertScreen
import com.techquantum.pingpath.modules.alertdetails.AlertDetailsScreen
import com.techquantum.pingpath.modules.home.HomeScreen

/**
 * Route constants for the app navigation graph.
 */
object Routes {
    const val PERMISSION = "permission"
    const val MAP = "map"
    const val ALERTS_LIST = "alerts_list"
    const val ADD_ALERT = "add_alert"
    const val ALERT_DETAIL = "alert_detail/{alertId}"

    fun alertDetail(alertId: String) = "alert_detail/$alertId"
}

// ══════════════════════════════════════════════
//  SAFE NAVIGATION EXTENSIONS
//  Guards against double-tap & invalid pops
// ══════════════════════════════════════════════

/**
 * Navigate only when the current lifecycle is RESUMED,
 * preventing duplicate navigations from rapid taps.
 */
fun NavHostController.safeNavigate(route: String) {
    val currentEntry = currentBackStackEntry
    if (currentEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        navigate(route)
    }
}

/**
 * Pop back-stack only if there is an entry to pop to,
 * preventing crashes on empty stack.
 */
fun NavHostController.safePopBackStack() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        if (previousBackStackEntry != null) {
            popBackStack()
        }
    }
}

/**
 * Pop to a specific route only when safe.
 */
fun NavHostController.safePopBackStack(route: String, inclusive: Boolean = false) {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        popBackStack(route, inclusive)
    }
}

// ══════════════════════════════════════════════
//  APP NAVIGATION GRAPH
// ══════════════════════════════════════════════

/**
 * Top-level navigation host that wires all screens together.
 *
 * Flow:
 *   App Open → Home (Alerts List)
 *       ├── Tap alert card → Alert Detail Screen
 *       │       └── Back → Home
 *       └── Tap "+" FAB → Add Alert Screen
 *               └── Save → Home
 */
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val startDest = if (com.techquantum.pingpath.utils.helpers.PermissionHelper.hasAllRequiredPermissions(context)) {
        Routes.ALERTS_LIST
    } else {
        Routes.PERMISSION
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(Routes.ALERTS_LIST, "recent", "settings")
    val shouldShowBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF131313),
                    contentColor = Color(0xFF44DDC1)
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.ALERTS_LIST,
                        onClick = { 
                            if (currentRoute != Routes.ALERTS_LIST) {
                                navController.navigate(Routes.ALERTS_LIST) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Alerts") },
                        label = { Text("Alerts") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF44DDC1),
                            selectedTextColor = Color(0xFF44DDC1),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "recent",
                        onClick = { 
                            if (currentRoute != "recent") {
                                navController.navigate("recent") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "Recent") },
                        label = { Text("Recent") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF44DDC1),
                            selectedTextColor = Color(0xFF44DDC1),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = { 
                            if (currentRoute != "settings") {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF44DDC1),
                            selectedTextColor = Color(0xFF44DDC1),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            // ── Permission Gate ────────────────────────────────
            composable(Routes.PERMISSION) {
                com.techquantum.pingpath.modules.permission.ui.PermissionScreen(
                    onAllPermissionsGranted = {
                        navController.navigate(Routes.ALERTS_LIST) {
                            popUpTo(Routes.PERMISSION) { inclusive = true }
                        }
                    }
                )
            }

            // ── Map Screen (Main Entry) ─────────────────────────
            composable(Routes.MAP) {
                com.techquantum.pingpath.modules.home.MapScreen(
                    onFabClick = {
                        navController.safeNavigate(Routes.ADD_ALERT)
                    },
                    onAlertsClick = {
                        navController.safeNavigate(Routes.ALERTS_LIST)
                    }
                )
            }

            // ── Alerts List ──────────────────────────────
            composable(Routes.ALERTS_LIST) {
                HomeScreen(
                    onAlertClick = { alertId ->
                        navController.safeNavigate(Routes.alertDetail(alertId))
                    },
                    onAddClick = {
                        navController.safeNavigate(Routes.ADD_ALERT)
                    }
                )
            }

            // ── Recent Screen ──────────────────────────────
            composable("recent") {
                com.techquantum.pingpath.modules.recent.RecentScreen()
            }

            // ── Settings Screen ──────────────────────────────
            composable("settings") {
                com.techquantum.pingpath.modules.settings.SettingsScreen()
            }

            // ── Add Alert (multi-step wizard) ───────────────────
            composable(Routes.ADD_ALERT) {
                AddAlertScreen(
                    onClose = {
                        navController.safePopBackStack()
                    },
                    onSaved = {
                        navController.safePopBackStack(Routes.ALERTS_LIST, inclusive = false)
                    }
                )
            }

            // ── Alert Detail (Live tracking view) ───────────────
            composable(
                route = Routes.ALERT_DETAIL,
                arguments = listOf(navArgument("alertId") { type = NavType.StringType })
            ) {
                AlertDetailsScreen(
                    onBack = {
                        navController.safePopBackStack()
                    }
                )
            }
        }
    }
}
