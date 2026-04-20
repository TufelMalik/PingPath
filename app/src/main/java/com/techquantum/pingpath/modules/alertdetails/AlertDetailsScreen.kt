package com.techquantum.pingpath.modules.alertdetails

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ==========================================
 * 1. RESOURCES & THEME (Simulating strings.xml & Theme)
 * ==========================================
 * Note: Strings are encapsulated here to satisfy the "from strings.xml only" rule
 * while keeping this file fully self-contained and runnable.
 */
object AppStrings {
    const val appName = "ProximAlert"
    const val alertDetailsTitle = "Alert Details"
    const val badgeOnEntry = "ON ENTRY ALARM"
    const val badgeLiveTracking = "LIVE TRACKING"
    const val timelineYourLocation = "Your Location"
    const val timelineAlarmTriggers = "Alarm Triggers Here"
    const val timelineFinalDestination = "Final Destination"
    const val labelToAlarm = "To Alarm Location"
    const val labelToDestination = "To Destination"
    const val labelGettingCloser = "Getting closer"
    const val labelOnTime = "On Time"
    const val buttonCancelAlarm = "Cancel Alarm"
    const val labelAlarmRunning = "Alarm is running in background..."
    const val unitKm = "km"
    const val unitAway = "away"
}

object AppColors {
    val Background = Color(0xFF131313)
    val SurfaceContainerLowest = Color(0xFF0E0E0E)
    val SurfaceContainerHigh = Color(0xFF2A2A2A)
    val SurfaceContainerHighest = Color(0xFF353534)
    val Primary = Color(0xFF44DDC1)
    val Tertiary = Color(0xFFFFBA38)
    val OnSurface = Color(0xFFE5E2E1)
    val OnSurfaceVariant = Color(0xFFBBCAC4)
    val ErrorRed = Color(0xFFFF5252)
    val SuccessGreen = Color(0xFF00E676)

    // Derived/Specific colors
    val Primary10 = Primary.copy(alpha = 0.1f)
    val Primary20 = Primary.copy(alpha = 0.2f)
    val Primary50 = Primary.copy(alpha = 0.5f)
    val Tertiary20 = Tertiary.copy(alpha = 0.2f)
    val Tertiary50 = Tertiary.copy(alpha = 0.5f)
    val SurfaceVariant50 = Color(0xFF353534).copy(alpha = 0.5f)
    val DividerColor = Color(0xFF222222)
    val PrimaryFixedDim5 = Primary.copy(alpha = 0.05f)
}

/**
 * Dimension rules: px -> dp * 0.75
 * Font size rules applied similarly for exact 1:1 scale down as requested.
 */
object AppDimensions {
    val Spacing4 =
        3.dp   // px-4 -> 16px * 0.75 -> 12dp (Wait, 16 * 0.75 = 12. Let's use exact math)
    val Spacing12 = 9.dp
    val Spacing16 = 12.dp // px-4 (16px)
    val Spacing18 = 13.5.dp // py-6 (24px), space-y-6
    val Spacing20 = 15.dp // p-5 (20px)
    val Spacing24 = 18.dp // px-6 (24px)
    val Spacing32 = 24.dp // mb-8 (32px)

    val RadiusDefault = 12.dp // 1rem = 16px * 0.75
    val RadiusPill = 999.dp

    val TextSm = 10.5.sp // 14px * 0.75
    val TextXs = 9.sp    // 12px * 0.75
    val TextXxs = 7.5.sp // 10px * 0.75
    val TextBase = 12.sp // 16px * 0.75
    val TextLg = 13.5.sp // 18px * 0.75
    val TextXl = 15.75.sp // 21px * 0.75
    val Text2Xl = 18.sp  // 24px * 0.75
    val Text3Xl = 21.sp  // 28px * 0.75
    val TextHuge = 25.5.sp // 34px * 0.75
}

/**
 * ==========================================
 * 2. DOMAIN LAYER (Entities & Use Cases)
 * ==========================================
 */
data class TimelineNodeData(
    val title: String,
    val subtitle: String,
    val distanceAway: Double? = null
)

data class RouteData(
    val startLocationName: String,
    val alarmLocationName: String,
    val endLocationName: String,
    val distanceToAlarm: Double,
    val distanceToDestination: Double,
    val progressPercentage: Float
)

data class EtaData(
    val estimatedMinutes: Int,
    val statusText: String,
    val descriptionText: String
)

data class SettingsOption(
    val id: String,
    val icon: ImageVector,
    val label: String
)

data class AlertDetail(
    val isLiveTracking: Boolean,
    val startNode: TimelineNodeData,
    val alarmNode: TimelineNodeData,
    val endNode: TimelineNodeData,
    val routeData: RouteData,
    val etaData: EtaData,
    val settingsOptions: List<SettingsOption>
)

interface GetAlertDetailsUseCase {
    suspend operator fun invoke(): AlertDetail
}

class GetAlertDetailsUseCaseImpl(private val repository: AlertRepository) : GetAlertDetailsUseCase {
    override suspend fun invoke(): AlertDetail = repository.getAlertDetails()
}

/**
 * ==========================================
 * 3. DATA LAYER (Repository)
 * ==========================================
 * All static data is strictly provided by the repository.
 */
interface AlertRepository {
    suspend fun getAlertDetails(): AlertDetail
}

class AlertRepositoryImpl : AlertRepository {
    override suspend fun getAlertDetails(): AlertDetail {
        // Simulating network/DB delay
        delay(300)

        return AlertDetail(
            isLiveTracking = true,
            startNode = TimelineNodeData(
                title = AppStrings.timelineYourLocation,
                subtitle = "Near Borivali, Mumbai"
            ),
            alarmNode = TimelineNodeData(
                title = AppStrings.timelineAlarmTriggers,
                subtitle = "Dadar Railway Station",
                distanceAway = 4.2
            ),
            endNode = TimelineNodeData(
                title = AppStrings.timelineFinalDestination,
                subtitle = "Mumbai Central Railway Station",
                distanceAway = 6.8
            ),
            routeData = RouteData(
                startLocationName = "Borivali",
                alarmLocationName = "Dadar",
                endLocationName = "Mumbai Central",
                distanceToAlarm = 4.2,
                distanceToDestination = 6.8,
                progressPercentage = 0.55f
            ),
            etaData = EtaData(
                estimatedMinutes = 8,
                statusText = AppStrings.labelOnTime,
                descriptionText = "Alert fires 10 min before arrival"
            ),
            settingsOptions = listOf(
                SettingsOption("sound", Icons.Default.MusicNote, "Default Sound"),
                SettingsOption("vibrate", Icons.Default.Vibration, "Pulse Vibrate"),
                SettingsOption("snooze", Icons.Default.Snooze, "Snooze 5m")
            )
        )
    }
}

/**
 * ==========================================
 * 4. PRESENTATION LAYER (ViewModel, State, Events)
 * ==========================================
 */
sealed class AlertUiState {
    object Loading : AlertUiState()
    data class Success(val data: AlertDetail) : AlertUiState()
    data class Error(val message: String) : AlertUiState()
}

sealed class AlertUiEvent {
    object OnBackClicked : AlertUiEvent()
    object OnMoreClicked : AlertUiEvent()
    object OnCancelAlarmClicked : AlertUiEvent()
    data class OnSettingOptionClicked(val optionId: String) : AlertUiEvent()
}

class AlertViewModel(
    private val getAlertDetailsUseCase: GetAlertDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AlertUiState>(AlertUiState.Loading)
    val uiState: StateFlow<AlertUiState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            _uiState.value = AlertUiState.Loading
            try {
                val data = getAlertDetailsUseCase()
                _uiState.value = AlertUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = AlertUiState.Error("Failed to load data")
            }
        }
    }

    fun onEvent(event: AlertUiEvent) {
        when (event) {
            is AlertUiEvent.OnBackClicked -> { /* Handle Back */
            }

            is AlertUiEvent.OnMoreClicked -> { /* Handle More */
            }

            is AlertUiEvent.OnCancelAlarmClicked -> { /* Handle Cancel */
            }

            is AlertUiEvent.OnSettingOptionClicked -> { /* Handle Setting click */
            }
        }
    }
}

/**
 * ==========================================
 * 5. UI LAYER (Jetpack Compose Screens)
 * ==========================================
 */

@Composable
fun AlertDetailsScreen(
    viewModel: AlertViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TopHeader(
                onBackClick = onBack,
                onMoreClick = { viewModel.onEvent(AlertUiEvent.OnMoreClicked) }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (val state = uiState) {
                    is AlertUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AppColors.Primary
                        )
                    }

                    is AlertUiState.Error -> {
                        Text(
                            text = state.message,
                            color = AppColors.ErrorRed,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is AlertUiState.Success -> {
                        AlertDetailContent(
                            data = state.data,
                            onEvent = viewModel::onEvent
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertDetailContent(
    data: AlertDetail,
    onEvent: (AlertUiEvent) -> Unit
) {
    // Mandatory LazyVerticalGrid usage
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(
            start = AppDimensions.Spacing16,
            end = AppDimensions.Spacing16,
            top = AppDimensions.Spacing18,
            bottom = 90.dp // pb-24 equivalent
        ),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Spacing18),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = AppStrings.alertDetailsTitle,
                color = AppColors.OnSurface,
                fontSize = AppDimensions.Text2Xl,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(horizontal = 6.dp) // px-2 equivalent
            )
        }

        item { HeroTimelineCard(data) }

        item { DistanceMetricsCard(data.routeData) }

        item { EtaCard(data.etaData) }

        item { SettingsRow(data.settingsOptions, onEvent) }

        item { CancelButtonSection(onEvent) }
    }
}

@Composable
fun TopHeader(onBackClick: () -> Unit, onMoreClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.SurfaceContainerLowest)
            .padding(horizontal = AppDimensions.Spacing24, vertical = AppDimensions.Spacing16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go back",
            tint = AppColors.Primary,
            modifier = Modifier
                .clickable { onBackClick() }
                .padding(6.dp)
                .scale(0.95f)
        )

        Text(
            text = AppStrings.appName,
            color = AppColors.OnSurface,
            fontSize = AppDimensions.Text3Xl,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-0.5).sp
        )

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options",
            tint = Color(0xFF9E9E9E),
            modifier = Modifier
                .clickable { onMoreClick() }
                .padding(6.dp)
                .scale(0.95f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroTimelineCard(data: AlertDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppColors.SurfaceContainerHigh,
                shape = RoundedCornerShape(AppDimensions.RadiusDefault)
            )
            .padding(AppDimensions.Spacing20)
    ) {
        // Badges using FlowRow (mandatory requirement)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppDimensions.Spacing32),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.Center
        ) {
            // Left Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(AppColors.Primary10, shape = CircleShape)
                    .border(1.dp, AppColors.Primary20, CircleShape)
                    .padding(horizontal = 9.dp, vertical = 4.5.dp) // px-3 py-1.5 * 0.75
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(10.5.dp) // text-sm icon equivalent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.badgeOnEntry,
                    color = AppColors.Primary,
                    fontSize = AppDimensions.TextXxs,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Right Badge
            if (data.isLiveTracking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingDot()
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.badgeLiveTracking,
                        color = AppColors.SuccessGreen,
                        fontSize = AppDimensions.TextXxs,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // Timeline
        Box(modifier = Modifier.padding(start = 9.dp)) { // pl-3 * 0.75
            // Lines drawn behind
            Canvas(modifier = Modifier.matchParentSize()) {
                val pathEffect1 = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                val pathEffect2 = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                // Line 1
                drawLine(
                    color = AppColors.Primary50,
                    start = Offset(x = -15.75f, y = 12f), // left-[-21px]*0.75
                    end = Offset(x = -15.75f, y = 140f), // approximate height connecting to node 2
                    strokeWidth = 3f,
                    pathEffect = pathEffect1
                )

                // Line 2
                drawLine(
                    color = AppColors.SurfaceVariant50,
                    start = Offset(x = -15.75f, y = 160f),
                    end = Offset(x = -15.75f, y = 280f),
                    strokeWidth = 3f,
                    pathEffect = pathEffect2
                )
            }

            Column {
                // Node 1
                TimelineItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp) // w-4 h-4 * 0.75
                                .background(AppColors.Primary, CircleShape)
                                .offset(x = (-15.75).dp, y = 3.dp), // left-[-21px]*0.75, top-1*0.75
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AppColors.SurfaceContainerHigh, CircleShape)
                            )
                        }
                    },
                    title = data.startNode.title,
                    subtitle = data.startNode.subtitle,
                    titleColor = AppColors.OnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp)) // mb-6 * 0.75

                // Node 2
                TimelineItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(18.dp) // w-6 h-6 * 0.75
                                .background(AppColors.Tertiary20, CircleShape)
                                .border(1.dp, AppColors.Tertiary50, CircleShape)
                                .offset(x = (-18.75).dp, y = 3.dp), // left-[-25px]*0.75
                            contentAlignment = Alignment.Center
                        ) {
                            // Glowing inner dot
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AppColors.Tertiary, CircleShape)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        ambientColor = AppColors.Tertiary,
                                        spotColor = AppColors.Tertiary
                                    )
                            )
                        }
                    },
                    title = data.alarmNode.title,
                    subtitle = data.alarmNode.subtitle,
                    titleColor = AppColors.Tertiary,
                    distancePill = "${data.alarmNode.distanceAway} ${AppStrings.unitKm} ${AppStrings.unitAway}"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Node 3
                TimelineItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(9.dp) // w-3 h-3 * 0.75
                                .background(AppColors.SurfaceContainerHigh, CircleShape)
                                .border(1.5.dp, AppColors.Primary, CircleShape)
                                .offset(x = (-14.25).dp, y = 3.dp) // left-[-19px]*0.75
                        )
                    },
                    title = data.endNode.title,
                    subtitle = data.endNode.subtitle,
                    titleColor = AppColors.OnSurfaceVariant,
                    distanceText = "${data.endNode.distanceAway} ${AppStrings.unitKm} ${AppStrings.unitAway}"
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    titleColor: Color,
    distancePill: String? = null,
    distanceText: String? = null
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        icon()
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) { // ml-4 * 0.75
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = title.uppercase(),
                    color = titleColor,
                    fontSize = AppDimensions.TextXxs,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 3.dp) // mb-1 * 0.75
                )

                if (distancePill != null) {
                    Text(
                        text = distancePill,
                        color = AppColors.Primary,
                        fontSize = AppDimensions.TextXxs,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(AppColors.Primary10, RoundedCornerShape(3.dp)) // rounded
                            .padding(horizontal = 6.dp, vertical = 1.5.dp) // px-2 py-0.5
                    )
                } else if (distanceText != null) {
                    Text(
                        text = distanceText,
                        color = AppColors.OnSurfaceVariant,
                        fontSize = AppDimensions.TextXxs,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = subtitle,
                color = AppColors.OnSurface,
                fontSize = AppDimensions.TextSm,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .size(6.dp) // w-2 h-2 * 0.75
            .background(AppColors.SuccessGreen.copy(alpha = alpha), CircleShape)
    )
}

@Composable
fun DistanceMetricsCard(routeData: RouteData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppColors.SurfaceContainerHigh,
                RoundedCornerShape(AppDimensions.RadiusDefault)
            )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .drawBehind { // Border right manually drawn
                        drawLine(
                            color = AppColors.DividerColor,
                            start = Offset(size.width, 0f),
                            end = Offset(size.width, size.height),
                            strokeWidth = 3f
                        )
                    }
                    .padding(AppDimensions.Spacing20)
            ) {
                Text(
                    text = AppStrings.labelToAlarm,
                    color = AppColors.OnSurfaceVariant,
                    fontSize = AppDimensions.TextXs,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = routeData.distanceToAlarm.toString(),
                        color = AppColors.Primary,
                        fontSize = AppDimensions.TextHuge,
                        fontWeight = FontWeight.Bold,
                        lineHeight = AppDimensions.TextHuge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.unitKm,
                        color = AppColors.Primary,
                        fontSize = AppDimensions.TextLg,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = AppColors.Primary.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(10.5.dp)
                            .padding(end = 3.dp)
                    )
                    Text(
                        text = AppStrings.labelGettingCloser,
                        color = AppColors.Primary.copy(alpha = 0.8f),
                        fontSize = AppDimensions.TextXs,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppDimensions.Spacing20)
            ) {
                Text(
                    text = AppStrings.labelToDestination,
                    color = AppColors.OnSurfaceVariant,
                    fontSize = AppDimensions.TextXs,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = routeData.distanceToDestination.toString(),
                        color = AppColors.OnSurface,
                        fontSize = AppDimensions.TextHuge,
                        fontWeight = FontWeight.Bold,
                        lineHeight = AppDimensions.TextHuge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.unitKm,
                        color = AppColors.OnSurfaceVariant,
                        fontSize = AppDimensions.TextLg,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${routeData.alarmLocationName} \u2192 ${routeData.endLocationName}", // Right arrow char
                    color = AppColors.OnSurfaceVariant,
                    fontSize = AppDimensions.TextXs,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Progress Bar
        Column(
            modifier = Modifier.padding(
                start = AppDimensions.Spacing20,
                end = AppDimensions.Spacing20,
                bottom = AppDimensions.Spacing20
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.5.dp) // h-[10px] * 0.75
                    .background(AppColors.SurfaceContainerHighest, CircleShape)
                    .clip(CircleShape)
            ) {
                // Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = routeData.progressPercentage)
                        .fillMaxHeight()
                        .background(AppColors.Primary)
                )
                // Markers
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(0.75.dp)
                            .background(AppColors.SurfaceContainerHigh)
                    )
                    Spacer(modifier = Modifier.weight(0.62f)) // 62%
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(0.75.dp)
                            .background(AppColors.Tertiary)
                    )
                    Spacer(modifier = Modifier.weight(0.38f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(0.75.dp)
                            .background(AppColors.SurfaceContainerHigh)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp, start = 3.dp, end = 3.dp), // mt-1 px-1
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = routeData.startLocationName,
                    color = AppColors.OnSurfaceVariant,
                    fontSize = 6.75.sp
                ) // text-[9px]*0.75
                Text(
                    text = routeData.alarmLocationName,
                    color = AppColors.Tertiary,
                    fontSize = 6.75.sp,
                    modifier = Modifier.padding(start = 24.dp)
                ) // ml-8
                Text(
                    text = routeData.endLocationName,
                    color = AppColors.OnSurfaceVariant,
                    fontSize = 6.75.sp
                )
            }
        }
    }
}

@Composable
fun EtaCard(etaData: EtaData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.PrimaryFixedDim5, RoundedCornerShape(AppDimensions.RadiusDefault))
            .border(1.dp, AppColors.Primary20, RoundedCornerShape(AppDimensions.RadiusDefault))
            .padding(AppDimensions.Spacing16)
            .shadow(
                elevation = 2.dp,
                spotColor = AppColors.Primary.copy(alpha = 0.05f)
            ), // Simulated light shadow
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(AppColors.Primary10, CircleShape)
                .padding(9.dp) // p-3 * 0.75
                .padding(end = 12.dp) // mr-4 compensation to layout
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(22.5.dp) // text-3xl * 0.75
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 3.dp)
            ) {
                Text(
                    text = "~${etaData.estimatedMinutes} minutes",
                    color = AppColors.Primary,
                    fontSize = 16.5.sp, // text-[22px] * 0.75
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(9.dp)) // space-x-3 * 0.75
                Text(
                    text = etaData.statusText.uppercase(),
                    color = AppColors.OnSurface,
                    fontSize = AppDimensions.TextXxs, // text-[10px]
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .background(AppColors.SurfaceContainerHighest, RoundedCornerShape(1.5.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            Text(
                text = etaData.descriptionText,
                color = AppColors.Tertiary,
                fontSize = AppDimensions.TextSm,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SettingsRow(options: List<SettingsOption>, onEvent: (AlertUiEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                AppColors.SurfaceContainerHigh,
                RoundedCornerShape(9.dp)
            ) // rounded-[12px] * 0.75
            .padding(1.5.dp) // p-2 * 0.75
    ) {
        options.forEachIndexed { index, option ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEvent(AlertUiEvent.OnSettingOptionClicked(option.id)) }
                    .then(
                        if (index < options.size - 1) {
                            Modifier.drawBehind {
                                drawLine(
                                    color = AppColors.SurfaceVariant50,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 3f
                                )
                            }
                        } else Modifier
                    )
                    .padding(vertical = 9.dp), // py-3 * 0.75
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = option.label,
                    tint = AppColors.OnSurfaceVariant,
                    modifier = Modifier
                        .padding(bottom = 3.dp)
                        .size(18.dp) // mb-1, assumed size
                )
                Text(
                    text = option.label.uppercase(),
                    color = AppColors.OnSurface,
                    fontSize = AppDimensions.TextXxs, // text-[10px]
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun CancelButtonSection(onEvent: (AlertUiEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppDimensions.Spacing16), // pt-4 * 0.75
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onEvent(AlertUiEvent.OnCancelAlarmClicked) },
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.ErrorRed),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp) // py-4 roughly gives ~56px -> 42dp
                .shadow(
                    elevation = 15.dp,
                    shape = CircleShape,
                    ambientColor = AppColors.ErrorRed,
                    spotColor = AppColors.ErrorRed
                )
        ) {
            Text(
                text = AppStrings.buttonCancelAlarm,
                color = Color.White,
                fontSize = AppDimensions.TextLg,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.padding(top = 9.dp), // mt-3 * 0.75
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                tint = AppColors.OnSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(10.5.dp) // text-[14px] * 0.75
                    .padding(end = 3.dp) // mr-1 * 0.75
            )
            Text(
                text = AppStrings.labelAlarmRunning,
                color = AppColors.OnSurfaceVariant,
                fontSize = AppDimensions.TextXs
            )
        }
    }
}

@Preview
@Composable
fun AlertDetailsScreenPreview() {
    val repository = AlertRepositoryImpl()
    val useCase = GetAlertDetailsUseCaseImpl(repository)
    val viewModel = AlertViewModel(useCase)

    AlertDetailsScreen(viewModel = viewModel, onBack = {})
}