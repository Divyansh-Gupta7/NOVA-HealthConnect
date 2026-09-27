package com.nova.healthconnect.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.health.connect.client.PermissionController
import com.nova.healthconnect.health.SyncState
import com.nova.healthconnect.ui.components.NovaButton
import com.nova.healthconnect.ui.components.NovaButtonVariant
import com.nova.healthconnect.ui.components.NovaCard
import com.nova.healthconnect.ui.components.NovaSectionHeader
import com.nova.healthconnect.ui.theme.Dimensions
import com.nova.healthconnect.ui.theme.NovaBackground
import com.nova.healthconnect.ui.theme.NovaBorder
import com.nova.healthconnect.ui.theme.NovaGreen
import com.nova.healthconnect.ui.theme.NovaMint
import com.nova.healthconnect.ui.theme.NovaRed
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTealDark
import com.nova.healthconnect.ui.theme.NovaTextMuted
import com.nova.healthconnect.ui.theme.NovaTextPrimary
import com.nova.healthconnect.ui.theme.NovaViolet
import com.nova.healthconnect.ui.viewmodels.HealthViewModel

@Composable
fun HealthScreen(
    viewModel: HealthViewModel
) {
    val hasPermissions by viewModel.hasPermissions.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isAvailable = viewModel.isAvailable

    // Health Connect Permission Launcher Contract
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        viewModel.checkPermissions()
        if (granted.containsAll(viewModel.permissions)) {
            viewModel.syncNow()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = Dimensions.ScreenPadding, vertical = 20.dp)
    ) {
        // Screen Header
        Text(
            text = "Health Connect Terminal",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = NovaTextPrimary
        )
        Text(
            text = "On-device biometric ingestion via Android Health Connect",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = NovaTextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Connection & Permission Status Card
        NovaCard(
            borderColor = if (hasPermissions) NovaTeal.copy(alpha = 0.5f) else Color(0xFFE5A93C),
            containerColor = if (hasPermissions) NovaMint.copy(alpha = 0.3f) else Color(0xFFFFF9E6)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (hasPermissions) NovaTeal else Color(0xFFE5A93C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasPermissions) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                        contentDescription = "Status",
                        tint = if (hasPermissions) NovaMint else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasPermissions) "✓ Health Connect Linked" else "Permission Required",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NovaTextPrimary
                    )
                    Text(
                        text = if (hasPermissions) "Last synchronized: ${lastSyncTime ?: "Today"}" else "Grant read permissions for Steps, Sleep, Heart Rate, and Energy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasPermissions) {
                NovaButton(
                    text = "Connect Health Data",
                    onClick = {
                        try {
                            permissionLauncher.launch(viewModel.permissions)
                        } catch (_: Exception) {
                            // Handled safely
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NovaButton(
                        text = if (syncState is SyncState.Syncing) "Syncing..." else "Sync Now",
                        onClick = { viewModel.syncNow() },
                        enabled = syncState !is SyncState.Syncing,
                        icon = Icons.Rounded.Sync,
                        modifier = Modifier.weight(1f)
                    )
                    NovaButton(
                        text = "Manage",
                        onClick = {
                            try {
                                permissionLauncher.launch(viewModel.permissions)
                            } catch (_: Exception) {}
                        },
                        variant = NovaButtonVariant.Secondary,
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Synchronized Biometric Breakdown
        NovaSectionHeader(title = "Ingested Biometric Signals")

        // 1. Steps Card
        BiometricDetailCard(
            title = "Daily Locomotion (Steps)",
            value = String.format("%,d", dashboardData.health.steps),
            goalText = "Goal: 10,000 steps",
            progress = (dashboardData.health.steps / 10000f).coerceIn(0f, 1f),
            icon = Icons.Rounded.DirectionsRun,
            accentColor = NovaTeal
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Sleep Architecture Card
        BiometricDetailCard(
            title = "Sleep & Rest Duration",
            value = "${dashboardData.health.sleepHours} hrs",
            goalText = "Quality Score: ${dashboardData.health.sleepQuality}%",
            progress = (dashboardData.health.sleepHours / 8.0).toFloat().coerceIn(0f, 1f),
            icon = Icons.Rounded.Nightlight,
            accentColor = NovaViolet
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Heart Rate Card
        BiometricDetailCard(
            title = "Resting Heart Rate",
            value = "${dashboardData.health.restingHeartRate} bpm",
            goalText = "Autonomic Baseline: Stable",
            progress = 0.78f,
            icon = Icons.Rounded.Favorite,
            accentColor = Color(0xFFE53935)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Energy Expenditure
        BiometricDetailCard(
            title = "Active Energy Burn",
            value = "${dashboardData.health.caloriesBurned.toInt()} kcal",
            goalText = "Exercise Minutes: ${dashboardData.health.activeMinutes} min",
            progress = (dashboardData.health.caloriesBurned / 600f).toFloat().coerceIn(0f, 1f),
            icon = Icons.Rounded.LocalFireDepartment,
            accentColor = Color(0xFFF57C00)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy and Security Guarantee
        NovaCard(
            borderColor = NovaBorder,
            containerColor = Color.White
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = "Privacy",
                    tint = NovaTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zero-Leakage Privacy Policy",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = NovaTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Biometric readings stay strictly between Android Health Connect, your secure device sandbox, and your authenticated NOVA instance. No data is brokered to third-party ad networks.",
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextMuted
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BiometricDetailCard(
    title: String,
    value: String,
    goalText: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    NovaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextMuted
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NovaTextPrimary
                    )
                }
            }

            Text(
                text = goalText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = NovaTextMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            trackColor = NovaBorder.copy(alpha = 0.5f)
        )
    }
}
