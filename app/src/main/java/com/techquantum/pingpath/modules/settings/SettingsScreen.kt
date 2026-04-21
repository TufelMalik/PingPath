package com.techquantum.pingpath.modules.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Alarm Sound",
            value = "Default Digital"
        )
        
        SettingsItem(
            icon = Icons.Default.Public,
            title = "Region",
            value = "India"
        )
        
        SettingsItem(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            title = "Alert Volume",
            value = "80%"
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "PingPath v1.0.0",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFF44DDC1), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(value, fontSize = 14.sp, color = Color.Gray)
        }
        Text("Change", color = Color(0xFF44DDC1), fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = Color(0xFF2A2A2A))
}
