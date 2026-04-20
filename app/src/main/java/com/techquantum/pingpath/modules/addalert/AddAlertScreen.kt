package com.techquantum.pingpath.modules.addalert

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
fun AddAlertScreen(
    viewModel: AlertViewModel,
    onClose: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (state.currentStep == 4) ReviewBottomBar(viewModel, onSaved = onSaved)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AddAlertTopAppBar(onClose = onClose)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp) // px-6 -> 18.dp
            ) {
                Crossfade(targetState = state.currentStep, label = "Step Transition") { step ->
                    when (step) {
                        1 -> Step1Destination(state, viewModel)
                        2 -> Step2AlarmLocation(state, viewModel)
                        3 -> Step3TimeSelection(state, viewModel)
                        4 -> Step4Review(state, viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertTopAppBar(onClose: () -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "New Alert",
                style = AppTypography.titleMedium,
                color = OnSurfaceText
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = OnSurfaceVariantText)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        )
    )
}

// ==========================================
// STEP 1: DESTINATION
// ==========================================
@Composable
fun Step1Destination(state: AlertUIState, viewModel: AlertViewModel) {

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(18.dp)) // mt-6 -> 18.dp
        Text("Where are you\ngoing?", style = AppTypography.headlineLarge, color = OnSurfaceText)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Search and select your destination",
            style = AppTypography.bodyMedium,
            color = OnSurfaceVariantText
        )

        Spacer(modifier = Modifier.height(15.dp)) // mt-5 -> 15.dp

        // Input Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(PrimaryCyan, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Destination", style = AppTypography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }

        // Active Search Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp) // h-[56px] -> 42.dp
                .background(SurfaceContainer, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
                .border(
                    1.dp,
                    SurfaceContainerHigh,
                    RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp)
                )
                .padding(horizontal = 12.dp), // px-4 -> 12.dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                null,
                tint = PrimaryCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(9.dp))
            BasicTextField(
                value = state.destinationQuery,
                onValueChange = { viewModel.onEvent(AlertUIEvent.OnDestinationQueryChange(it)) },
                textStyle = AppTypography.bodyLarge.copy(color = OnSurfaceText),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.Mic,
                null,
                tint = OnSurfaceVariantText,
                modifier = Modifier.size(18.dp)
            )
        }

        Box(
            modifier = Modifier.background(
                SurfaceContainer,
                RoundedCornerShape(bottomStart = 9.dp, bottomEnd = 9.dp)
            )
        ) {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(state.destinationResults, key = { it.id }) { location ->
                    val isFirst = state.destinationResults.indexOf(location) == 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clickable {
                                viewModel.onEvent(
                                    AlertUIEvent.OnDestinationSelected(
                                        location
                                    )
                                )
                            }
                            .background(if (isFirst) Color.White.copy(alpha = 0.05f) else Color.Transparent)
                            .border(
                                width = 2.dp,
                                color = if (isFirst) PrimaryCyan else Color.Transparent
                            )
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(27.dp)
                                .background(
                                    SurfaceContainerHigh,
                                    RoundedCornerShape(3.dp)
                                ), // 36px -> 27dp
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (location.type == LocationType.TRAIN) Icons.Default.Train else Icons.Default.Subway,
                                contentDescription = null,
                                tint = OnSurfaceVariantText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(9.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                location.name,
                                style = AppTypography.bodyLarge,
                                color = OnSurfaceText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                location.address,
                                style = AppTypography.bodyMedium.copy(fontSize = 11.sp),
                                color = OnSurfaceVariantText
                            )
                        }
                        Icon(
                            Icons.Default.NorthEast,
                            null,
                            tint = if (isFirst) PrimaryCyan else OnSurfaceVariantText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (!isFirst) HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        thickness = DividerDefaults.Thickness,
                        color = SurfaceContainerHigh
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(9.dp))) {
            com.techquantum.pingpath.modules.home.components.OsmMapView(
                modifier = Modifier.fillMaxSize(),
                userLocation = org.osmdroid.util.GeoPoint(19.0760, 72.8777), // Fallback center
                onMapClick = { geoPoint ->
                    viewModel.onEvent(AlertUIEvent.OnMapClicked(geoPoint.latitude, geoPoint.longitude, isDestination = true))
                }
            )
        }
    }
}

// ==========================================
// STEP 2: ALARM LOCATION
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step2AlarmLocation(state: AlertUIState, viewModel: AlertViewModel) {

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "Now, where should the\nalarm fire?",
            style = AppTypography.headlineMedium,
            color = OnSurfaceText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Pick the location where your alarm triggers",
            style = AppTypography.bodyMedium,
            color = OnSurfaceVariantText
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Locked Step 1
        TimelineItem(
            color = PrimaryCyan,
            isActive = false,
            title = "Destination",
            content = state.selectedDestination?.name ?: "",
            onChange = { viewModel.onEvent(AlertUIEvent.OnChangeStepRequested(1)) }
        )

        // Active Step 2
        TimelineItem(
            color = TertiaryOrange,
            isActive = true,
            title = "Alarm Location",
            content = "",
            isLast = true
        ) {
            Column {
                // Active Input bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(SurfaceContainerHigh, RoundedCornerShape(9.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        null,
                        tint = TertiaryOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    BasicTextField(
                        value = state.alarmQuery,
                        onValueChange = { viewModel.onEvent(AlertUIEvent.OnAlarmQueryChange(it)) },
                        textStyle = AppTypography.bodyLarge.copy(color = OnSurfaceText),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = OnSurfaceVariantText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Dropdown using Mandatory LazyVerticalGrid
                Box(
                    modifier = Modifier
                        .background(SurfaceContainer, RoundedCornerShape(9.dp))
                        .fillMaxWidth()
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(state.alarmResults, key = { it.id }) { location ->
                            val isFirst = state.alarmResults.indexOf(location) == 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onEvent(
                                            AlertUIEvent.OnAlarmSelected(
                                                location
                                            )
                                        )
                                    }
                                    .background(if (isFirst) SurfaceContainerHigh else Color.Transparent)
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isFirst) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(24.dp)
                                            .background(TertiaryOrange)
                                    )
                                    Spacer(modifier = Modifier.width(9.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = if (isFirst) TertiaryOrange else OnSurfaceVariantText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(9.dp))
                                Column {
                                    Text(
                                        location.name,
                                        style = AppTypography.bodyLarge,
                                        color = if (isFirst) TertiaryOrange else OnSurfaceVariantText
                                    )
                                    if (isFirst) Text(
                                        location.address,
                                        style = AppTypography.bodyMedium.copy(fontSize = 11.sp),
                                        color = OnSurfaceVariantText
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                // Info Tooltip
                Row(
                    modifier = Modifier
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
                        .padding(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(18.dp)
                            .background(TertiaryOrange.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.Outlined.Lightbulb,
                        null,
                        tint = TertiaryOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "This is usually a stop or two before your final destination",
                        style = AppTypography.bodyMedium.copy(fontSize = 10.sp),
                        color = OnSurfaceVariantText
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(9.dp))) {
                    com.techquantum.pingpath.modules.home.components.OsmMapView(
                        modifier = Modifier.fillMaxSize(),
                        userLocation = org.osmdroid.util.GeoPoint(19.0760, 72.8777), // Fallback center
                        onMapClick = { geoPoint ->
                            viewModel.onEvent(AlertUIEvent.OnMapClicked(geoPoint.latitude, geoPoint.longitude, isDestination = false))
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// STEP 3: TIME SELECTION
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3TimeSelection(state: AlertUIState, viewModel: AlertViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "How early should we alert you?",
            style = AppTypography.headlineMedium,
            color = OnSurfaceText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Set how many minutes before arrival",
            style = AppTypography.bodyMedium,
            color = OnSurfaceVariantText
        )
        Spacer(modifier = Modifier.height(24.dp))

        TimelineItem(
            color = PrimaryCyan,
            isActive = false,
            title = "Destination",
            content = state.selectedDestination?.name ?: "",
            onChange = { viewModel.onEvent(AlertUIEvent.OnChangeStepRequested(1)) })
        TimelineItem(
            color = TertiaryOrange,
            isActive = false,
            title = "Alarm Location",
            content = state.selectedAlarmLocation?.name ?: "",
            onChange = { viewModel.onEvent(AlertUIEvent.OnChangeStepRequested(2)) })

        TimelineItem(
            color = SecondaryPurple,
            isActive = true,
            title = "Alert Me Before",
            content = "",
            isLast = true,
            headerAction = {
                Text(
                    "How is this calculated?",
                    style = AppTypography.bodyMedium.copy(fontSize = 10.sp),
                    color = SecondaryPurple
                )
            }) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(SurfaceContainer, RoundedCornerShape(9.dp))
                        .border(1.dp, SecondaryPurple.copy(alpha = 0.5f), RoundedCornerShape(9.dp))
                        .clickable { viewModel.onEvent(AlertUIEvent.ToggleTimeDropdown) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = SecondaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(9.dp))
                    Text(
                        state.selectedTime?.label ?: "Select time before arrival...",
                        style = AppTypography.bodyLarge,
                        color = OnSurfaceVariantText,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ExpandMore, null, tint = SecondaryPurple)
                }

                if (state.isTimeDropdownOpen) {
                    Popup(alignment = Alignment.TopCenter) {
                        Box(
                            modifier = Modifier
                                .padding(top = 48.dp)
                                .fillMaxWidth(0.9f)
                                .background(Color(0xFF242424), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                        ) {
                            LazyVerticalGrid(columns = GridCells.Fixed(1)) {
                                items(state.timeOptions) { time ->
                                    Text(
                                        text = time.label,
                                        style = AppTypography.bodyLarge,
                                        color = OnSurfaceVariantText,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.onEvent(
                                                    AlertUIEvent.OnTimeSelected(
                                                        time
                                                    )
                                                )
                                            }
                                            .padding(horizontal = 15.dp, vertical = 12.dp)
                                    )
                                }
                                item {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        thickness = DividerDefaults.Thickness,
                                        color = Color(0xFF333333)
                                    )
                                }
                                item {
                                    Text(
                                        "Custom...",
                                        style = AppTypography.bodyLarge,
                                        color = OnSurfaceVariantText,
                                        modifier = Modifier.padding(
                                            horizontal = 15.dp,
                                            vertical = 12.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STEP 4: REVIEW
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step4Review(state: AlertUIState, viewModel: AlertViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) { // Space for bottom bar
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "Almost done!\nReview & save.",
            style = AppTypography.headlineLarge,
            color = OnSurfaceText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Tap save to start monitoring",
            style = AppTypography.bodyMedium,
            color = OnSurfaceVariantText
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Summarized Locked Steps
        SummaryTimelineItem(
            color = PrimaryCyan,
            title = "Destination",
            content = state.selectedDestination?.name ?: ""
        )
        SummaryTimelineItem(
            color = TertiaryOrange,
            title = "Arrival",
            content = state.selectedAlarmLocation?.name ?: ""
        )
        SummaryTimelineItem(
            color = SecondaryPurple,
            title = "Time",
            content = state.selectedTime?.label ?: "",
            subContent = "~8 min ETA at current speed",
            isLast = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Alert Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainer, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .padding(15.dp)
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(96.dp)
                    .offset(x = 24.dp, y = (-24).dp)
                    .blur(48.dp)
                    .background(PrimaryCyan.copy(alpha = 0.1f), CircleShape)
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.FactCheck,
                        null,
                        tint = PrimaryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Alert Summary",
                        style = AppTypography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceText
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))

                // Visual Route
                Row {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PrimaryCyan, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(24.dp)
                                .background(Color(0xFF353534))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(TertiaryOrange, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            state.selectedDestination?.name ?: "",
                            style = AppTypography.bodyMedium,
                            color = OnSurfaceVariantText
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            state.selectedAlarmLocation?.name ?: "",
                            style = AppTypography.bodyMedium,
                            color = OnSurfaceText
                        )
                        Text(
                            "alarm fires here",
                            style = AppTypography.bodyMedium.copy(fontSize = 10.sp),
                            color = TertiaryOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
                // Time pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerHighest, RoundedCornerShape(6.dp))
                        .padding(9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = SecondaryPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            state.selectedTime?.label ?: "",
                            style = AppTypography.bodyMedium,
                            color = SecondaryPurple
                        )
                    }
                    Text(
                        "🔊 Sound · \uD83D\uDCF3 Vibration",
                        style = AppTypography.bodyMedium.copy(fontSize = 10.sp),
                        color = OnSurfaceVariantText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Settings Row using mandatory FlowRow
        state.hardwareSettings?.let { settings ->
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainer, RoundedCornerShape(6.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                maxItemsInEachRow = 3
            ) {
                SettingCell("Sound", settings.sound)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color(0xFF353534))
                        .align(Alignment.CenterVertically)
                )
                SettingCell("Vibration", settings.vibration)
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color(0xFF353534))
                        .align(Alignment.CenterVertically)
                )
                SettingCell("Snooze", settings.snooze)
            }
        }
    }
}

@Composable
fun ReviewBottomBar(viewModel: AlertViewModel, onSaved: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xE60E0E0E)) // Bottom fade backdrop
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                viewModel.onEvent(AlertUIEvent.OnSaveClicked)
                onSaved()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp), // ~h-14
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(26.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(listOf(PrimaryCyan, PrimaryCyanDark)))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF00473C))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Save Alert",
                        style = AppTypography.titleMedium,
                        color = Color(0xFF00473C),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Info,
                null,
                tint = OnSurfaceVariantText,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                "Runs in background even when app is closed",
                style = AppTypography.bodyMedium.copy(fontSize = 10.sp),
                color = OnSurfaceVariantText
            )
        }
    }
}

// ==========================================
// REUSABLE UI COMPONENTS
// ==========================================
@Composable
fun SettingCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(9.dp)) {
        Text(label.uppercase(), style = AppTypography.labelMedium, color = OnSurfaceVariantText)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            value,
            style = AppTypography.bodyMedium,
            color = OnSurfaceText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TimelineItem(
    color: Color,
    isActive: Boolean,
    title: String,
    content: String,
    isLast: Boolean = false,
    headerAction: @Composable (() -> Unit)? = null,
    onChange: (() -> Unit)? = null,
    activeContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
                    .border(2.dp, color.copy(alpha = 0.2f), CircleShape)
            )
            if (!isLast) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                androidx.compose.foundation.Canvas(
                    Modifier
                        .width(2.dp)
                        .height(64.dp)
                        .padding(top = 6.dp)
                ) {
                    drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (!isActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerHighest, RoundedCornerShape(9.dp))
                        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(9.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(title.uppercase(), style = AppTypography.labelMedium, color = color)
                        Text(content, style = AppTypography.bodyLarge, color = OnSurfaceText)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "CHANGE",
                            style = AppTypography.bodyMedium,
                            color = color,
                            modifier = Modifier.clickable { onChange?.invoke() })
                        Spacer(modifier = Modifier.width(9.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        title,
                        style = AppTypography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceText
                    )
                    headerAction?.invoke()
                }
                Spacer(modifier = Modifier.height(9.dp))
                activeContent?.invoke()
            }
        }
    }
}

@Composable
fun SummaryTimelineItem(
    color: Color,
    title: String,
    content: String,
    subContent: String? = null,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(12.dp))
            }
            if (!isLast) {
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                androidx.compose.foundation.Canvas(
                    Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .padding(top = 6.dp)
                ) {
                    drawLine(
                        color = color.copy(alpha = 0.3f),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .background(SurfaceContainer, RoundedCornerShape(9.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title.uppercase(), style = AppTypography.labelMedium, color = color)
                Text(
                    "Change",
                    style = AppTypography.bodyMedium.copy(fontSize = 10.sp),
                    color = color
                )
            }
            Text(
                content,
                style = AppTypography.bodyLarge,
                color = OnSurfaceText,
                fontWeight = FontWeight.SemiBold
            )
            if (subContent != null) Text(
                subContent,
                style = AppTypography.bodyMedium.copy(fontSize = 10.sp),
                color = OnSurfaceVariantText
            )
        }
    }
}

@Preview(
    name = "Add Alert Screen Preview",
    showBackground = true,
    backgroundColor = 0xFF0D0D0D, // BackgroundDark
    device = "id:pixel_6"
)
@Composable
fun AddAlertPreview() {
    val context = LocalContext.current
    val mockRepository = AlertRepositoryImpl(context)
    val mockSearchUseCase = SearchLocationsUseCase(mockRepository)
    val mockTimeUseCase = GetTimeOptionsUseCase(mockRepository)
    val mockViewModel = AlertViewModel(mockSearchUseCase, mockTimeUseCase, mockRepository)

    ProximAlertTheme {
        AddAlertScreen(viewModel = mockViewModel, onClose = {}, onSaved = {})
    }
}