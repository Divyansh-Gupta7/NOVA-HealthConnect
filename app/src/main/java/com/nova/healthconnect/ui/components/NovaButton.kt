package com.nova.healthconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nova.healthconnect.ui.theme.NovaBorder
import com.nova.healthconnect.ui.theme.NovaButtonShape
import com.nova.healthconnect.ui.theme.NovaMint
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTealDark
import com.nova.healthconnect.ui.theme.NovaTextOnTeal
import com.nova.healthconnect.ui.theme.NovaTextPrimary

enum class NovaButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED
}

@Composable
fun NovaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: NovaButtonVariant = NovaButtonVariant.PRIMARY,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    when (variant) {
        NovaButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(48.dp),
                enabled = enabled,
                shape = NovaButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaTeal,
                    contentColor = NovaTextOnTeal,
                    disabledContainerColor = NovaTeal.copy(alpha = 0.5f),
                    disabledContentColor = NovaTextOnTeal.copy(alpha = 0.7f)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                content = content
            )
        }
        NovaButtonVariant.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(48.dp),
                enabled = enabled,
                shape = NovaButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaMint,
                    contentColor = NovaTealDark
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                content = content
            )
        }
        NovaButtonVariant.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(48.dp),
                enabled = enabled,
                shape = NovaButtonShape,
                border = BorderStroke(1.dp, NovaBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = NovaTextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                content = content
            )
        }
    }
}
