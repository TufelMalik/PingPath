package com.techquantum.pingpath.modules.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techquantum.pingpath.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ==========================================
// 1. STRINGS (Simulating strings.xml)
// ==========================================
object R {
    object string {
        const val app_name = "ProximAlert"
        const val section_title = "My Alerts"
        const val active_format = "%d ACTIVE"
        const val status_live = "LIVE"
        const val btn_cancel = "Cancel"
        const val distance_format = "%s km remaining"
        const val eta_format = "~%s min"
    }
}

// ==========================================
// 2. THEME & COLORS (Hex Matches)
// ==========================================
object AppColors {
    val Background = Color(0xFF0D0D0D)
    val SurfaceDark = Color(0xFF1A1A1A)
    val SurfaceContainer = Color(0xFF353534)
    val PrimaryMain = Color(0xFF44DDC1)
    val PrimaryActive = Color(0xFF00BFA5)
    val PrimaryActiveBg = Color(0xFF1A2A24)
    val TextWhite = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF9E9E9E)
    val LiveGreen = Color(0xFF00E676)
    val CancelRed = Color(0xFFFF5252)
    val ProgressTrack = Color(0xFF242424)
}

// ==========================================
// 3. CLEAN ARCHITECTURE: DOMAIN LAYER
// ==========================================
data class AlertDomainModel(
    val id: String,
    val triggerType: String,
    val destination: String,
    val contextText: String,
    val distanceRemaining: String,
    val eta: String,
    val progressPercent: Float,
    val configSummary: String
)

// ==========================================
// 5. PRESENTATION LAYER: VIEWMODEL & UI STATE
// ==========================================
data class ProximAlertUiState(
    val alerts: List<AlertDomainModel> = emptyList(),
    val activeCount: Int = 0,
    val isLoading: Boolean = true
)

sealed class ProximAlertEvent {
    data class CancelClicked(val alertId: String) : ProximAlertEvent()
    object AddFabClicked : ProximAlertEvent()
}

@HiltViewModel
class ProximAlertViewModel @Inject constructor(
    private val repository: AlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProximAlertUiState())
    val uiState: StateFlow<ProximAlertUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            repository.getActiveAlerts().collect { alerts ->
                val domainAlerts = alerts.map { it.toDomain() }
                _uiState.update { 
                    it.copy(
                        alerts = domainAlerts, 
                        activeCount = domainAlerts.size,
                        isLoading = false
                    ) 
                }
            }
        }
    }

    fun onEvent(event: ProximAlertEvent) {
        when (event) {
            is ProximAlertEvent.CancelClicked -> {
                viewModelScope.launch {
                    repository.updateAlertStatus(event.alertId, "CANCELLED")
                }
            }
            is ProximAlertEvent.AddFabClicked -> {
                // Navigate to Add Alert screen
            }
        }
    }
}

// Mapper: Local Entity -> Domain Model
fun com.techquantum.pingpath.data.local.entities.AlertEntity.toDomain() = AlertDomainModel(
    id = id,
    triggerType = "ON ENTRY",
    destination = destinationName,
    contextText = "Alarm at $alarmLocationName",
    distanceRemaining = "N/A",
    eta = "N/A",
    progressPercent = 0.0f,
    configSummary = "Sound"
)


// ==========================================
// 6. UI COMPOSABLES (Strict px->dp x0.75 conversions applied)
// HTML Sizes: p-5(20px)->15dp, gap-4(16px)->12dp, gap-1(4px)->3dp, rounded-[16px]->12dp
// Fonts: 17px->12.75sp, 15px->11.25sp, 13px->9.75sp, 11px->8.25sp
// ==========================================

@Composable
fun HomeScreen(
    viewModel: ProximAlertViewModel = hiltViewModel(),
    onAlertClick: (String) -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.Background,
        floatingActionButton = { FabAction { onAddClick() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp) // HTML px-4 (16px) * 0.75 = 12.dp
        ) {
            TopAppBar()

            SectionHeader(activeCount = state.activeCount)
            
            Spacer(modifier = Modifier.height(9.dp)) // HTML gap-[12px] * 0.75 = 9.dp

            // Mandatory LazyVerticalGrid implementation
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp), // Responsive
                verticalArrangement = Arrangement.spacedBy(9.dp), // HTML gap-[12px] * 0.75 = 9.dp
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 60.dp) // Padding for FAB
            ) {
                items(state.alerts, key = { it.id }) { alert ->
                    AlertCard(
                        alert = alert,
                        onCancel = { viewModel.onEvent(ProximAlertEvent.CancelClicked(alert.id)) },
                        onClick = { onAlertClick(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAppBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp), // HTML px-4 py-4 (16px) -> 12.dp
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp) // HTML gap-2 (8px) -> 6.dp
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Logo",
                tint = AppColors.PrimaryMain,
                modifier = Modifier.size(18.dp) // HTML text-2xl (24px) -> 18.dp
            )
            Text(
                text = R.string.app_name,
                color = AppColors.TextWhite,
                fontSize = 15.sp, // HTML text-[20px] -> 15.sp
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp), // HTML gap-4 (16px) -> 12.dp
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = AppColors.TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Icon(
                imageVector = Icons.Filled.Settings, // Closest to 'tune'
                contentDescription = "Settings",
                tint = AppColors.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(activeCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 3.dp), // HTML mt-2 mb-1 (8px, 4px) -> 6.dp, 3.dp
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = R.string.section_title,
            color = AppColors.TextWhite,
            fontSize = 12.75.sp, // HTML text-[17px] -> 12.75.sp
            fontWeight = FontWeight.SemiBold
        )
        
        Box(
            modifier = Modifier
                .background(AppColors.PrimaryActiveBg, RoundedCornerShape(15.dp)) // HTML rounded-[20px] -> 15.dp
                .padding(horizontal = 9.dp, vertical = 3.dp) // HTML px-3 py-1 (12px, 4px) -> 9.dp, 3.dp
        ) {
            Text(
                text = R.string.active_format.format(activeCount),
                color = AppColors.PrimaryActive,
                fontSize = 8.25.sp, // HTML text-[11px] -> 8.25.sp
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp // tracking-wider
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlertCard(alert: AlertDomainModel, onCancel: () -> Unit, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceDark, RoundedCornerShape(12.dp)) // HTML rounded-[16px] -> 12.dp
            .clickable(onClick = onClick)
            .padding(15.dp) // HTML p-5 (20px) -> 15.dp
    ) {
        // Mandatory FlowColumn
        FlowColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp) // HTML gap-4 (16px) -> 12.dp
        ) {
            // ROW 1: Badges & Status (Mandatory FlowRow)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Trigger Type Badge
                Box(
                    modifier = Modifier
                        .background(AppColors.PrimaryActive, RoundedCornerShape(4.5.dp)) // HTML rounded-[6px] -> 4.5.dp
                        .padding(horizontal = 6.dp, vertical = 1.5.dp) // HTML px-2 py-0.5 (8px, 2px) -> 6.dp, 1.5.dp
                ) {
                    Text(
                        text = alert.triggerType,
                        color = Color.Black,
                        fontSize = 8.25.sp, // HTML text-[11px] -> 8.25.sp
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Live Status Indicator
                Row(
                    modifier = Modifier
                        .background(AppColors.SurfaceContainer.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 3.dp), // HTML px-2 py-1 (8px, 4px) -> 6.dp, 3.dp
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.5.dp) // HTML gap-1.5 (6px) -> 4.5.dp
                ) {
                    PulsingDot()
                    Text(
                        text = R.string.status_live,
                        color = AppColors.LiveGreen,
                        fontSize = 8.25.sp, // HTML text-[11px] -> 8.25.sp
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // ROW 2 & 3: Location Details (FlowColumn inside)
            FlowColumn(
                verticalArrangement = Arrangement.spacedBy(3.dp) // HTML gap-1 (4px) -> 3.dp
            ) {
                Text(
                    text = alert.destination,
                    color = AppColors.TextWhite,
                    fontSize = 11.25.sp, // HTML text-[15px] -> 11.25.sp
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 12.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.5.dp) // HTML gap-1.5 -> 4.5.dp
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(10.5.dp) // HTML text-[14px] -> 10.5.dp
                    )
                    Text(
                        text = alert.contextText,
                        color = AppColors.TextMuted,
                        fontSize = 9.75.sp // HTML text-[13px] -> 9.75.sp
                    )
                }
            }

            // ROW 4 & 5: Progress Metrics
            FlowColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp) // HTML mt-2 (8px) -> 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = R.string.distance_format.format(alert.distanceRemaining),
                        color = AppColors.PrimaryMain,
                        fontSize = 9.75.sp, // HTML text-[13px] -> 9.75.sp
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = R.string.eta_format.format(alert.eta),
                        color = AppColors.TextMuted,
                        fontSize = 9.sp // HTML text-[12px] -> 9.sp
                    )
                }
                
                // Progress Bar Visuals
                ProgressBarComponent(progress = alert.progressPercent)
            }

            // ROW 6: Footer Actions (Mandatory FlowRow)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp), // HTML mt-1 pt-3 border-t -> Simplified layout representation
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp) // HTML gap-1 (4px) -> 3.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Sound Config",
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(9.dp) // HTML text-[12px] -> 9.dp
                    )
                    Text(
                        text = alert.configSummary,
                        color = AppColors.TextMuted,
                        fontSize = 8.25.sp // HTML text-[11px] -> 8.25.sp
                    )
                }
                
                Text(
                    text = R.string.btn_cancel,
                    color = AppColors.CancelRed,
                    fontSize = 9.75.sp, // HTML text-[13px] -> 9.75.sp
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onCancel)
                )
            }
        }
    }
}

@Composable
private fun ProgressBarComponent(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp) // HTML h-[8px] -> 6.dp
            .background(AppColors.ProgressTrack, CircleShape)
    ) {
        // Fill
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress)
                .fillMaxHeight()
                .background(AppColors.PrimaryMain, CircleShape)
        ) {
            // Knob / Glow Dot
            GlowingKnob(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 3.dp) // HTML right-[-4px] -> -3.dp (offset positive for end alignment)
            )
        }
    }
}

@Composable
private fun GlowingKnob(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.size(9.dp) // HTML w-[12px] h-[12px] -> 9.dp
    ) {
        val paint = Paint().asFrameworkPaint().apply {
            color = AppColors.PrimaryMain.toArgb()
            maskFilter = android.graphics.BlurMaskFilter(
                15f, // HTML shadow-[0_0_10px...] mapped to visual equivalent blur radius
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        
        // Draw Shadow
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawCircle(
                center.x, center.y, size.width / 2, paint
            )
        }
        
        // Draw Solid Dot
        drawCircle(
            color = AppColors.PrimaryMain,
            radius = size.width / 2,
            center = center
        )
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(
        modifier = Modifier.size(4.5.dp) // HTML w-1.5 h-1.5 (6px) -> 4.5.dp
    ) {
        val paint = Paint().asFrameworkPaint().apply {
            color = AppColors.LiveGreen.copy(alpha = alpha).toArgb()
            maskFilter = android.graphics.BlurMaskFilter(
                12f * alpha, // Animate shadow spread
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        
        drawIntoCanvas {
            it.nativeCanvas.drawCircle(center.x, center.y, size.width / 2, paint)
        }

        drawCircle(
            color = AppColors.LiveGreen,
            radius = size.width / 2,
            center = center,
            alpha = alpha
        )
    }
}

@Composable
private fun FabAction(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(bottom = 18.dp, end = 18.dp) // HTML bottom-6 right-6 (24px) -> 18.dp
            .size(42.dp) // HTML w-14 h-14 (56px) -> 42.dp
            .shadow(elevation = 8.dp, shape = CircleShape)
            .background(AppColors.PrimaryActive, CircleShape)
            .clickable(onClick = onClick)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "Add Alert",
            tint = AppColors.TextWhite,
            modifier = Modifier.size(22.5.dp) // HTML text-3xl (30px) -> 22.5.dp
        )
    }
}


@Preview
@Composable
fun HomeScreenPreview(){
    HomeScreen()
}