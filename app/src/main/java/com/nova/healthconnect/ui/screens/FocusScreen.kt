package com.nova.healthconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.nova.healthconnect.ui.components.NovaButton
import com.nova.healthconnect.ui.components.NovaButtonVariant
import com.nova.healthconnect.ui.components.NovaCard
import com.nova.healthconnect.ui.components.NovaSectionHeader
import com.nova.healthconnect.ui.theme.Dimensions
import com.nova.healthconnect.ui.theme.NovaBackground
import com.nova.healthconnect.ui.theme.NovaBorder
import com.nova.healthconnect.ui.theme.NovaMint
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTealDark
import com.nova.healthconnect.ui.theme.NovaTextMuted
import com.nova.healthconnect.ui.theme.NovaTextPrimary
import com.nova.healthconnect.ui.theme.NovaViolet
import com.nova.healthconnect.ui.theme.NovaVioletSoft
import com.nova.healthconnect.ui.viewmodels.FocusTimerState
import com.nova.healthconnect.ui.viewmodels.FocusViewModel

@Composable
fun FocusScreen(
    viewModel: FocusViewModel
) {
    val timerState by viewModel.timerState.collectAsState()
    val durationMinutes by viewModel.durationMinutes.collectAsState()
    val secondsRemaining by viewModel.secondsRemaining.collectAsState()
    val distractionsCount by viewModel.distractionsCount.collectAsState()
    val selectedSoundscape by viewModel.selectedSoundscape.collectAsState()

    val totalSeconds = durationMinutes * 60
    val progress = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds else 0f

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = Dimensions.ScreenPadding, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Deep Work Sprint",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = NovaTextPrimary
            )
            Text(
                text = "Cognitive flow immersion with distraction logging",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = NovaTextMuted
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preset Duration Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(15, 25, 45, 60).forEach { mins ->
                val isSelected = durationMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NovaTeal else Color.White)
                        .border(1.dp, if (isSelected) NovaTeal else NovaBorder, RoundedCornerShape(12.dp))
                        .clickable(enabled = timerState == FocusTimerState.IDLE) {
                            viewModel.setDuration(mins)
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mins}m",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else NovaTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Circular Timer Display
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = NovaBorder.copy(alpha = 0.5f),
                strokeWidth = 10.dp
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = NovaTeal,
                strokeWidth = 10.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = NovaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (timerState) {
                        FocusTimerState.RUNNING -> "FLOW ACTIVE"
                        FocusTimerState.PAUSED -> "PAUSED"
                        FocusTimerState.COMPLETED -> "SPRINT COMPLETE ✦"
                        else -> "READY"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (timerState == FocusTimerState.RUNNING) NovaTeal else NovaTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (timerState) {
                FocusTimerState.IDLE -> {
                    NovaButton(
                        text = "Begin Deep Sprint",
                        onClick = { viewModel.startTimer() },
                        icon = Icons.Rounded.PlayArrow,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
                FocusTimerState.RUNNING -> {
                    NovaButton(
                        text = "Pause",
                        onClick = { viewModel.pauseTimer() },
                        icon = Icons.Rounded.Pause,
                        variant = NovaButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    NovaButton(
                        text = "+ Distraction",
                        onClick = { viewModel.logDistraction() },
                        icon = Icons.Rounded.WarningAmber,
                        variant = NovaButtonVariant.Outline,
                        modifier = Modifier.weight(1f)
                    )
                }
                FocusTimerState.PAUSED -> {
                    NovaButton(
                        text = "Resume",
                        onClick = { viewModel.startTimer() },
                        icon = Icons.Rounded.PlayArrow,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    NovaButton(
                        text = "Reset",
                        onClick = { viewModel.resetTimer() },
                        icon = Icons.Rounded.Refresh,
                        variant = NovaButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
                FocusTimerState.COMPLETED -> {
                    NovaButton(
                        text = "Sprint Recorded! Start New",
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Soundscape Mode Selector
        NovaSectionHeader(title = "Binaural & Acoustic Immersion")
        NovaCard {
            listOf("Binaural 40Hz", "Pink Noise", "Alpha Waves 10Hz", "Silence").forEach { sound ->
                val isSelected = selectedSoundscape == sound
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NovaMint.copy(alpha = 0.5f) else Color.Transparent)
                        .clickable { viewModel.setSoundscape(sound) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Headphones,
                            contentDescription = sound,
                            tint = if (isSelected) NovaTeal else NovaTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sound,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) NovaTealDark else NovaTextPrimary
                        )
                    }

                    if (isSelected) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            ),
                            color = NovaTeal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Distractions Log Card
        NovaCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Distraction Friction Score",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextMuted
                    )
                    Text(
                        text = "$distractionsCount interruptions logged",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NovaTextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (distractionsCount == 0) NovaMint else NovaVioletSoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (distractionsCount == 0) "Zero Friction" else "Tracked",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (distractionsCount == 0) NovaTealDark else NovaViolet
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
