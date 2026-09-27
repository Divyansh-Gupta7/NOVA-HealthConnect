package com.nova.healthconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.healthconnect.ui.theme.NovaMint
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTextMuted
import com.nova.healthconnect.ui.theme.NovaTextPrimary

@Composable
fun NovaMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    subtext: String? = null,
    icon: (@Composable () -> Unit)? = null,
    badgeText: String? = null,
    badgeColor: Color = NovaMint,
    onClick: (() -> Unit)? = null
) {
    NovaCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NovaMint, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .background(badgeColor, RoundedCornerShape(9999.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = NovaTeal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = NovaTextPrimary
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = NovaTextMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            if (subtext != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextMuted
                )
            }
        }
    }
}

@Composable
fun NovaMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    onClick: (() -> Unit)? = null
) {
    NovaMetricCard(
        title = title,
        value = value,
        modifier = modifier,
        unit = unit,
        subtext = subtext,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        },
        onClick = onClick
    )
}
