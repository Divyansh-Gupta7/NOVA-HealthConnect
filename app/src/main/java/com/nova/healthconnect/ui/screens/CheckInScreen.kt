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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.healthconnect.ui.components.NovaButton
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
import com.nova.healthconnect.ui.viewmodels.CheckInViewModel
import com.nova.healthconnect.ui.viewmodels.ReactionState

@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel,
    onCalibrationSaved: () -> Unit
) {
    val dashboardData by viewModel.dashboardData.collectAsState()
    val sleepQuality by viewModel.sleepQuality.collectAsState()
    val stressLevel by viewModel.stressLevel.collectAsState()
    val energyLevel by viewModel.energyLevel.collectAsState()
    val selectedHabitIds by viewModel.selectedHabitIds.collectAsState()
    val reactionState by viewModel.reactionState.collectAsState()
    val reactionTimeMs by viewModel.reactionTimeMs.collectAsState()

    var isSubmitted by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = Dimensions.ScreenPadding, vertical = 20.dp)
    ) {
        Text(
            text = "Daily Neuro-Calibration",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = NovaTextPrimary
        )
        Text(
            text = "Somatic assessments & reflex latency measurement",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = NovaTextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Interactive Neuro-Reflex Reaction Test
        NovaSectionHeader(title = "Interactive Neuro-Reflex Test")
        NovaCard {
            Text(
                text = "Tap to initialize. When the panel turns GREEN, tap as swiftly as possible.",
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            val boxColor = when (reactionState) {
                ReactionState.READY -> NovaTeal.copy(alpha = 0.08f)
                ReactionState.WAITING -> Color(0xFFE5A93C).copy(alpha = 0.2f)
                ReactionState.CLICK_NOW -> Color(0xFF00C853)
                ReactionState.FINISHED -> NovaMint
                ReactionState.TOO_EARLY -> NovaRed.copy(alpha = 0.15f)
            }

            val boxBorder = when (reactionState) {
                ReactionState.READY -> NovaBorder
                ReactionState.WAITING -> Color(0xFFE5A93C)
                ReactionState.CLICK_NOW -> Color(0xFF00C853)
                ReactionState.FINISHED -> NovaTeal
                ReactionState.TOO_EARLY -> NovaRed
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(boxColor)
                    .clickable { viewModel.onReactionClicked() }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (reactionState) {
                        ReactionState.READY -> {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = "Speed",
                                tint = NovaTeal,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TAP TO BEGIN REFLEX CALIBRATION",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = NovaTealDark
                            )
                        }
                        ReactionState.WAITING -> {
                            Text(
                                text = "WAIT FOR GREEN...",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFC67C00)
                            )
                        }
                        ReactionState.CLICK_NOW -> {
                            Text(
                                text = "⚡ TAP NOW! ⚡",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                ),
                                color = Color.White
                            )
                        }
                        ReactionState.FINISHED -> {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = "Bolt",
                                tint = NovaTeal,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "$reactionTimeMs ms",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = NovaTealDark
                            )
                            Text(
                                text = if (reactionTimeMs < 300) "Optimal CNS responsiveness" else "Moderate neural fatigue",
                                style = MaterialTheme.typography.bodySmall,
                                color = NovaTealDark
                            )
                        }
                        ReactionState.TOO_EARLY -> {
                            Text(
                                text = "Too early! Tap to retry.",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = NovaRed
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Somatic Sliders
        NovaSectionHeader(title = "Subjective Somatic Ratings")
        NovaCard {
            // Sleep Quality
            Text(
                text = "Perceived Sleep Quality: $sleepQuality%",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = NovaTextPrimary
            )
            Slider(
                value = sleepQuality.toFloat(),
                onValueChange = { viewModel.sleepQuality.value = it.toInt() },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = NovaTeal,
                    activeTrackColor = NovaTeal,
                    inactiveTrackColor = NovaBorder
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Energy Level
            Text(
                text = "Energy Index: $energyLevel / 5",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = NovaTextPrimary
            )
            Slider(
                value = energyLevel.toFloat(),
                onValueChange = { viewModel.energyLevel.value = it.toInt() },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = NovaViolet,
                    activeTrackColor = NovaViolet,
                    inactiveTrackColor = NovaBorder
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stress Level
            Text(
                text = "Cognitive Stress / Load: $stressLevel / 5",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = NovaTextPrimary
            )
            Slider(
                value = stressLevel.toFloat(),
                onValueChange = { viewModel.stressLevel.value = it.toInt() },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFE5A93C),
                    activeTrackColor = Color(0xFFE5A93C),
                    inactiveTrackColor = NovaBorder
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Habits Checklist
        NovaSectionHeader(title = "Daily Habits Checklist")
        NovaCard {
            dashboardData.habits.forEach { habit ->
                val isChecked = selectedHabitIds.contains(habit.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleHabit(habit.id) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = habit.name,
                        tint = if (isChecked) NovaTeal else NovaTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = habit.icon, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isChecked) FontWeight.Normal else FontWeight.Medium
                        ),
                        color = if (isChecked) NovaTextMuted else NovaTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSubmitted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NovaMint)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓ Calibration successfully logged to NOVA terminal!",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = NovaTealDark
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        NovaButton(
            text = "Submit Daily Calibration",
            onClick = {
                viewModel.submitCalibration {
                    isSubmitted = true
                    onCalibrationSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
