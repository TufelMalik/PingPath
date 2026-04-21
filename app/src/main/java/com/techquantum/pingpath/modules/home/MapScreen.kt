package com.techquantum.pingpath.modules.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.techquantum.pingpath.modules.home.components.OsmMapView
import org.osmdroid.util.GeoPoint

@Composable
fun MapScreen(
    hasInternet: Boolean = true,
    hasGps: Boolean = true,
    userLocation: GeoPoint? = null,
    destination: GeoPoint? = null,
    onSearchClick: () -> Unit = {},
    onFabClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = if (destination != null) MaterialTheme.colorScheme.primary else Color.Gray,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alert")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map Background
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                userLocation = userLocation,
                destination = destination
            )

            // Overlays at the top
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                if (!hasInternet) {
                    Text(
                        text = "No internet connection",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red)
                            .padding(8.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                if (!hasGps) {
                    Text(
                        text = "Location unavailable. Please enable GPS.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFA500))
                            .padding(8.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                // Search Bar Placeholder
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Destination") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        IconButton(onClick = onAlertsClick) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Alerts List")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White, MaterialTheme.shapes.small),
                    singleLine = true
                )
            }
        }
    }
}
