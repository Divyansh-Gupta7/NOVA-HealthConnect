package com.nova.healthconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.healthconnect.ui.theme.Dimensions
import com.nova.healthconnect.ui.theme.NovaBackground
import com.nova.healthconnect.ui.theme.NovaMint
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTealDark
import com.nova.healthconnect.ui.theme.NovaTextMuted
import com.nova.healthconnect.ui.theme.NovaTextPrimary

@Composable
fun NovaTopBar(
    userName: String = "Elena Vance",
    userStatus: String = "EQUILIBRIUM ACTIVE",
    onOpenSettings: () -> Unit,
    onLockTerminal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NovaBackground)
            .padding(horizontal = Dimensions.screenPaddingHorizontal, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nova Brand Mark
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NovaTeal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    color = NovaMint,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NOVA",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(NovaMint, RoundedCornerShape(9999.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = userStatus,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NovaTealDark
                        )
                    }
                }
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextMuted
                )
            }

            if (onLockTerminal != null) {
                IconButton(onClick = onLockTerminal) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Lock Terminal",
                        tint = NovaTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = NovaTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
