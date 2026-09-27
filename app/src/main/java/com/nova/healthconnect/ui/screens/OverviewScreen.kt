package com.nova.healthconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.healthconnect.health.SyncState
import com.nova.healthconnect.ui.components.ConcentricRingsView
import com.nova.healthconnect.ui.components.NovaButton
import com.nova.healthconnect.ui.components.NovaButtonVariant
import com.nova.healthconnect.ui.components.NovaCard
import com.nova.healthconnect.ui.components.NovaMetricCard
import com.nova.healthconnect.ui.components.NovaSectionHeader
import com.nova.healthconnect.ui.components.NovaTopBar
import com.nova.healthconnect.ui.navigation.Screen
import com.nova.healthconnect.ui.theme.Dimensions
import com.nova.healthconnect.ui.theme.NovaBackground
import com.nova.healthconnect.ui.theme.NovaBorder
import com.nova.healthconnect.ui.theme.NovaMint
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTealDark
import com.nova.healthconnect.ui.theme.NovaTextMuted
import com.nova.healthconnect.ui.theme.NovaTextPrimary
import com.nova.healthconnect.ui.theme.NovaTextSecondary
import com.nova.healthconnect.ui.theme.NovaViolet
import com.nova.healthconnect.ui.theme.NovaVioletSoft
import com.nova.healthconnect.ui.viewmodels.OverviewViewModel

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onNavigate: (Screen) -> Unit
) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
    ) {
        NovaTopBar(
            userName = dashboardData.user.name,
            userStatus = dashboardData.user.status,
            onSyncClick = { viewModel.syncHealthConnect() },
            isSyncing = syncState is SyncState.Syncing
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimensions.ScreenPadding, vertical = 12.dp)
        ) {
            // Last Sync Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(NovaMint.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "Sync",
                        tint = NovaTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (syncState) {
                            is SyncState.Syncing -> "Syncing with Health Connect..."
                            is SyncState.Success -> "Health Connect: ${lastSyncTime ?: "Synced"}"
                            is SyncState.Error -> "Sync offline — using local biometrics"
                            else -> "Health Connect: ${lastSyncTime ?: "Ready"}"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = NovaTealDark
                    )
                }

                Text(
                    text = "SYNC NOW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = NovaTeal,
                    modifier = Modifier.clickable { viewModel.syncHealthConnect() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Daily Insight Card
            NovaCard(
                borderColor = NovaTeal.copy(alpha = 0.3f),
                containerColor = NovaMint.copy(alpha = 0.25f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NovaTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI",
                            tint = NovaMint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AUTONOMOUS RECOVERY INSIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = NovaTealDark
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dashboardData.dailyInsight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NovaTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Concentric Rings & Readiness Section
            NovaSectionHeader(
                title = "Cognitive State",
                actionText = "Full Metrics",
                onActionClick = { onNavigate(Screen.Health) }
            )

            NovaCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConcentricRingsView(
                        ring1Progress = dashboardData.cognitive.readinessScore / 100f,
                        ring2Progress = dashboardData.health.recoveryScore / 100f,
                        ring3Progress = (dashboardData.focus.totalMinutes.toFloat() / dashboardData.focus.targetMinutes).coerceIn(0f, 1f),
                        centerText = "${dashboardData.cognitive.readinessScore}%",
                        centerLabel = "READINESS",
                        size = 140.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        MetricLegendItem(
                            color = NovaTeal,
                            title = "Readiness Score",
                            value = "${dashboardData.cognitive.readinessScore}/100"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        MetricLegendItem(
                            color = NovaViolet,
                            title = "Autonomic Recovery",
                            value = "${dashboardData.health.recoveryScore}%"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        MetricLegendItem(
                            color = Color(0xFFE5A93C),
                            title = "Focus Capacity",
                            value = "${dashboardData.focus.totalMinutes}/${dashboardData.focus.targetMinutes}m"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Health Connect Biometrics Grid
            NovaSectionHeader(
                title = "Health Connect Biometrics",
                actionText = "Manage",
                onActionClick = { onNavigate(Screen.Health) }
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                NovaMetricCard(
                    title = "Daily Steps",
                    value = String.format("%,d", dashboardData.health.steps),
                    unit = "/ 10,000",
                    icon = Icons.Rounded.DirectionsRun,
                    accentColor = NovaTeal,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                NovaMetricCard(
                    title = "Sleep Duration",
                    value = "${dashboardData.health.sleepHours}",
                    unit = "hrs",
                    icon = Icons.Rounded.Nightlight,
                    accentColor = NovaViolet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                NovaMetricCard(
                    title = "Resting Heart Rate",
                    value = "${dashboardData.health.restingHeartRate}",
                    unit = "bpm",
                    icon = Icons.Rounded.Favorite,
                    accentColor = Color(0xFFE53935),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                NovaMetricCard(
                    title = "Active Burn",
                    value = "${dashboardData.health.caloriesBurned.toInt()}",
                    unit = "kcal",
                    icon = Icons.Rounded.LocalFireDepartment,
                    accentColor = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Focus Section
            NovaSectionHeader(
                title = "Deep Work Sprints",
                actionText = "Start Sprint",
                onActionClick = { onNavigate(Screen.Focus) }
            )

            NovaCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${dashboardData.focus.totalMinutes} min completed",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = NovaTextPrimary
                        )
                        Text(
                            text = "${dashboardData.focus.sessionsCompleted} sessions • ${dashboardData.focus.currentStreak} day streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = NovaTextMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NovaVioletSoft)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Peak: ${dashboardData.cognitive.predictedPeak}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NovaViolet
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Habit Protocol Checklist
            NovaSectionHeader(
                title = "Daily Neuro-Habits",
                actionText = "Calibrate",
                onActionClick = { onNavigate(Screen.CheckIn) }
            )

            NovaCard {
                dashboardData.habits.forEachIndexed { index, habit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleHabit(habit.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (habit.completed) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = habit.name,
                            tint = if (habit.completed) NovaTeal else NovaTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = habit.icon,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (habit.completed) FontWeight.Normal else FontWeight.Medium
                            ),
                            color = if (habit.completed) NovaTextMuted else NovaTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index < dashboardData.habits.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(NovaBorder.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NovaButton(
                    text = "Launch Focus",
                    onClick = { onNavigate(Screen.Focus) },
                    icon = Icons.Rounded.Timer,
                    modifier = Modifier.weight(1f)
                )
                NovaButton(
                    text = "AI Companion",
                    onClick = { onNavigate(Screen.AiChat) },
                    icon = Icons.Rounded.AutoAwesome,
                    variant = NovaButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MetricLegendItem(
    color: Color,
    title: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = NovaTextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = NovaTextPrimary
            )
        }
    }
}
