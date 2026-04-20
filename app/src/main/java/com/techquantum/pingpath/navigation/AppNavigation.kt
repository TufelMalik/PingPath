package com.techquantum.pingpath.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.techquantum.pingpath.modules.addalert.AddAlertScreen
import com.techquantum.pingpath.modules.addalert.AlertRepositoryImpl
import com.techquantum.pingpath.modules.addalert.AlertViewModel
import com.techquantum.pingpath.modules.addalert.GetTimeOptionsUseCase
import com.techquantum.pingpath.modules.addalert.SearchLocationsUseCase
import com.techquantum.pingpath.modules.alertdetails.AlertDetailsScreen
import com.techquantum.pingpath.modules.alertdetails.GetAlertDetailsUseCaseImpl
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

    NavHost(
        navController = navController,
        startDestination = startDest
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

        // ── Add Alert (multi-step wizard) ───────────────────
        composable(Routes.ADD_ALERT) {
            val context = androidx.compose.ui.platform.LocalContext.current
            // Manual DI – will be replaced with Hilt later
            val repository = AlertRepositoryImpl(context)
            val searchUseCase = SearchLocationsUseCase(repository)
            val timeUseCase = GetTimeOptionsUseCase(repository)
            val viewModel = AlertViewModel(searchUseCase, timeUseCase, repository)

            AddAlertScreen(
                viewModel = viewModel,
                onClose = {
                    navController.safePopBackStack()
                },
                onSaved = {
                    // Pop back to Alerts List after saving
                    navController.safePopBackStack(Routes.ALERTS_LIST, inclusive = false)
                }
            )
        }

        // ── Alert Detail (Live tracking view) ───────────────
        composable(
            route = Routes.ALERT_DETAIL,
            arguments = listOf(navArgument("alertId") { type = NavType.StringType })
        ) { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId").orEmpty()

            // Manual DI – will be replaced with Hilt later
            val repository = com.techquantum.pingpath.modules.alertdetails.AlertRepositoryImpl()
            val useCase = GetAlertDetailsUseCaseImpl(repository)
            val viewModel = com.techquantum.pingpath.modules.alertdetails.AlertViewModel(useCase)

            AlertDetailsScreen(
                viewModel = viewModel,
                onBack = {
                    navController.safePopBackStack()
                }
            )
        }
    }
}
