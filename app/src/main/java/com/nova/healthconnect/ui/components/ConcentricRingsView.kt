package com.nova.healthconnect.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.healthconnect.ui.theme.NovaBlue
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTextMuted
import com.nova.healthconnect.ui.theme.NovaTextPrimary
import com.nova.healthconnect.ui.theme.NovaViolet

@Composable
fun ConcentricRingsView(
    focusPercent: Float,      // 0..100
    burnPercent: Float,       // 0..100
    sleepPercent: Float,      // 0..100
    equilibriumScore: Int,    // 0..100
    alphaHz: Float = 9.4f,
    size: Dp = 220.dp,
    modifier: Modifier = Modifier
) {
    val animFocus = remember { Animatable(0f) }
    val animBurn = remember { Animatable(0f) }
    val animSleep = remember { Animatable(0f) }

    LaunchedEffect(focusPercent, burnPercent, sleepPercent) {
        animFocus.animateTo(
            targetValue = focusPercent.coerceIn(0f, 100f) / 100f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(focusPercent, burnPercent, sleepPercent) {
        animBurn.animateTo(
            targetValue = burnPercent.coerceIn(0f, 100f) / 100f,
            animationSpec = tween(1400, easing = FastOutSlowInEasing)
        )
    }
    LaunchedEffect(focusPercent, burnPercent, sleepPercent) {
        animSleep.animateTo(
            targetValue = sleepPercent.coerceIn(0f, 100f) / 100f,
            animationSpec = tween(1600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val spacing = 20.dp.toPx()
            val canvasCenter = Offset(this.size.width / 2, this.size.height / 2)

            // Outer Ring: Focus Flow
            val radiusOuter = (this.size.minDimension / 2) - (strokeWidth / 2)
            drawCircle(
                color = NovaTeal.copy(alpha = 0.12f),
                radius = radiusOuter,
                center = canvasCenter,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = NovaTeal,
                startAngle = -90f,
                sweepAngle = animFocus.value * 360f,
                useCenter = false,
                topLeft = Offset(canvasCenter.x - radiusOuter, canvasCenter.y - radiusOuter),
                size = Size(radiusOuter * 2, radiusOuter * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Middle Ring: Burn / Exercise
            val radiusMiddle = radiusOuter - spacing
            drawCircle(
                color = NovaViolet.copy(alpha = 0.12f),
                radius = radiusMiddle,
                center = canvasCenter,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = NovaViolet,
                startAngle = -90f,
                sweepAngle = animBurn.value * 360f,
                useCenter = false,
                topLeft = Offset(canvasCenter.x - radiusMiddle, canvasCenter.y - radiusMiddle),
                size = Size(radiusMiddle * 2, radiusMiddle * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Inner Ring: Sleep Alignment
            val radiusInner = radiusMiddle - spacing
            drawCircle(
                color = NovaBlue.copy(alpha = 0.12f),
                radius = radiusInner,
                center = canvasCenter,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = NovaBlue,
                startAngle = -90f,
                sweepAngle = animSleep.value * 360f,
                useCenter = false,
                topLeft = Offset(canvasCenter.x - radiusInner, canvasCenter.y - radiusInner),
                size = Size(radiusInner * 2, radiusInner * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "EQUILIBRIUM",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                ),
                color = NovaTextMuted
            )
            Text(
                text = "$equilibriumScore",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp
                ),
                color = NovaTextPrimary
            )
            Text(
                text = "${String.format("%.1f", alphaHz)} Hz",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = NovaTeal
            )
        }
    }
}
