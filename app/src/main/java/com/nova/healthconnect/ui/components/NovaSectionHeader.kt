package com.nova.healthconnect.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTextPrimary

@Composable
fun NovaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            if (!badge.isNullOrBlank()) {
                Text(
                    text = badge.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NovaTeal,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = NovaTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        if (action != null) {
            action()
        }
    }
}
