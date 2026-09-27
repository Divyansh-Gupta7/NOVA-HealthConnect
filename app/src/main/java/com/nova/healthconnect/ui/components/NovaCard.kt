package com.nova.healthconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nova.healthconnect.ui.theme.Dimensions
import com.nova.healthconnect.ui.theme.NovaBorderSoft
import com.nova.healthconnect.ui.theme.NovaCardShape
import com.nova.healthconnect.ui.theme.NovaSurface

@Composable
fun NovaCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NovaSurface,
    borderColor: Color = NovaBorderSoft,
    borderWidth: Dp = Dimensions.cardBorderWidth,
    elevation: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = NovaCardShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(modifier = Modifier.padding(Dimensions.cardPadding)) {
            content()
        }
    }
}
