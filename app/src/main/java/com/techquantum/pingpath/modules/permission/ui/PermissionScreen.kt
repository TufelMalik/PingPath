package com.techquantum.pingpath.modules.permission.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.techquantum.pingpath.modules.addalert.AppTypography
import com.techquantum.pingpath.modules.addalert.BackgroundDark
import com.techquantum.pingpath.modules.addalert.PrimaryCyan
import com.techquantum.pingpath.modules.addalert.SurfaceContainer
import com.techquantum.pingpath.modules.addalert.OnSurfaceVariantText
import com.techquantum.pingpath.utils.helpers.PermissionHelper

@Composable
fun PermissionScreen(
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasFineLocation by remember { mutableStateOf(PermissionHelper.hasFineLocationPermission(context)) }
    var hasBackgroundLocation by remember { mutableStateOf(PermissionHelper.hasBackgroundLocationPermission(context)) }
    var hasNotification by remember { mutableStateOf(PermissionHelper.hasNotificationPermission(context)) }
    var hasFullScreen by remember { mutableStateOf(PermissionHelper.hasFullScreenIntentPermission(context)) }

    // Re-check permissions when returning to the app (e.g. from Settings)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasFineLocation = PermissionHelper.hasFineLocationPermission(context)
                hasBackgroundLocation = PermissionHelper.hasBackgroundLocationPermission(context)
                hasNotification = PermissionHelper.hasNotificationPermission(context)
                hasFullScreen = PermissionHelper.hasFullScreenIntentPermission(context)
                
                if (hasFineLocation && hasBackgroundLocation && hasNotification && hasFullScreen) {
                    onAllPermissionsGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val fineLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasFineLocation = isGranted
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasBackgroundLocation = isGranted
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotification = isGranted
    }

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to ProximAlert",
                style = AppTypography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To monitor your journey and wake you up on time, we need a few permissions to work correctly.",
                style = AppTypography.bodyLarge,
                color = OnSurfaceVariantText
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 1. Fine Location
            PermissionRow(
                icon = Icons.Default.LocationOn,
                title = "Precise Location",
                description = "Used to track your progress towards your destination.",
                isGranted = hasFineLocation,
                isEnabled = true,
                onRequest = { fineLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Background Location
            PermissionRow(
                icon = Icons.Default.LocationOn,
                title = "Background Tracking",
                description = "Allows us to keep tracking your journey even when the app is closed.",
                isGranted = hasBackgroundLocation,
                isEnabled = hasFineLocation,
                onRequest = { 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    } else {
                        hasBackgroundLocation = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Notifications
            PermissionRow(
                icon = Icons.Default.Notifications,
                title = "Alert Notifications",
                description = "Required to show the active alert status and trigger the alarm.",
                isGranted = hasNotification,
                isEnabled = hasBackgroundLocation,
                onRequest = { 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        hasNotification = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Full Screen Intent
            PermissionRow(
                icon = Icons.Default.Smartphone,
                title = "Full-Screen Alarm",
                description = "Allows the alarm to turn on the screen and bypass the lock screen.",
                isGranted = hasFullScreen,
                isEnabled = hasNotification,
                onRequest = { PermissionHelper.openFullScreenIntentSettings(context) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (hasFineLocation && hasBackgroundLocation && hasNotification && hasFullScreen) {
                        onAllPermissionsGranted()
                    } else {
                        PermissionHelper.openAppSettings(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasFineLocation && hasBackgroundLocation && hasNotification && hasFullScreen) PrimaryCyan else SurfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (hasFineLocation && hasBackgroundLocation && hasNotification && hasFullScreen) "Start App" else "Open App Settings",
                    color = if (hasFineLocation && hasBackgroundLocation && hasNotification && hasFullScreen) Color.Black else Color.White,
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isEnabled: Boolean,
    onRequest: () -> Unit
) {
    val alpha = if (isEnabled || isGranted) 1f else 0.4f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainer.copy(alpha = alpha), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFF2A2A2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) PrimaryCyan else OnSurfaceVariantText
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.titleMedium,
                color = Color.White.copy(alpha = alpha)
            )
            Text(
                text = description,
                style = AppTypography.bodyMedium,
                color = OnSurfaceVariantText.copy(alpha = alpha)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        if (isGranted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Granted",
                tint = PrimaryCyan,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Button(
                onClick = onRequest,
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Allow", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
