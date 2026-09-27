package com.nova.healthconnect.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.healthconnect.NovaApplication
import com.nova.healthconnect.auth.AuthState
import com.nova.healthconnect.data.api.RetrofitClient
import com.nova.healthconnect.ui.components.NovaButton
import com.nova.healthconnect.ui.components.NovaButtonVariant
import com.nova.healthconnect.ui.components.NovaCard
import com.nova.healthconnect.ui.components.NovaSectionHeader
import com.nova.healthconnect.ui.theme.Dimensions
import com.nova.healthconnect.ui.theme.NovaBackground
import com.nova.healthconnect.ui.theme.NovaBorder
import com.nova.healthconnect.ui.theme.NovaMint
import com.nova.healthconnect.ui.theme.NovaRed
import com.nova.healthconnect.ui.theme.NovaTeal
import com.nova.healthconnect.ui.theme.NovaTealDark
import com.nova.healthconnect.ui.theme.NovaTextMuted
import com.nova.healthconnect.ui.theme.NovaTextPrimary

@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authManager = NovaApplication.instance.authManager
    val syncManager = NovaApplication.instance.healthSyncManager
    val authState by authManager.authState.collectAsState()

    var backendUrl by remember { mutableStateOf(RetrofitClient.getBaseUrl(context)) }
    var backgroundSyncEnabled by remember { mutableStateOf(true) }
    val lastSyncTime by syncManager.lastSyncTime.collectAsState()

    val scrollState = rememberScrollState()

    val currentUser = (authState as? AuthState.Authenticated)?.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = Dimensions.ScreenPadding, vertical = 20.dp)
    ) {
        Text(
            text = "System Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = NovaTextPrimary
        )
        Text(
            text = "Terminal configurations, synchronization & network",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = NovaTextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. User Profile Card
        NovaCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(NovaTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "User",
                        tint = NovaMint,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.displayName ?: "NOVA Explorer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = NovaTextPrimary
                    )
                    Text(
                        text = currentUser?.email ?: "explorer@nova.internal",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NovaButton(
                text = "Sign Out",
                onClick = {
                    authManager.logout()
                    onLogout()
                },
                icon = Icons.Rounded.ExitToApp,
                variant = NovaButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Backend URL Configuration
        NovaSectionHeader(title = "Backend Service Endpoint")
        NovaCard {
            Text(
                text = "Specify the address of your existing NOVA Node/Express backend server.",
                style = MaterialTheme.typography.bodySmall,
                color = NovaTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = backendUrl,
                onValueChange = { backendUrl = it },
                label = { Text("Base API URL") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Dns,
                        contentDescription = "URL",
                        tint = NovaTeal
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaTeal,
                    unfocusedBorderColor = NovaBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NovaButton(
                    text = "Save Endpoint",
                    onClick = {
                        RetrofitClient.saveBaseUrl(context, backendUrl)
                        Toast.makeText(context, "Backend endpoint updated", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )

                NovaButton(
                    text = "Reset Default",
                    onClick = {
                        val defaultUrl = "http://10.0.2.2:5000/api/"
                        backendUrl = defaultUrl
                        RetrofitClient.saveBaseUrl(context, defaultUrl)
                    },
                    variant = NovaButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Background Sync Configuration
        NovaSectionHeader(title = "Autonomous Background Sync")
        NovaCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WorkManager Hourly Sync",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = NovaTextPrimary
                    )
                    Text(
                        text = "Synchronize Health Connect biometrics in the background when connected to network.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NovaTextMuted
                    )
                }

                Switch(
                    checked = backgroundSyncEnabled,
                    onCheckedChange = { enabled ->
                        backgroundSyncEnabled = enabled
                        if (enabled) {
                            syncManager.schedulePeriodicSync()
                        } else {
                            syncManager.cancelPeriodicSync()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NovaTeal,
                        checkedTrackColor = NovaMint
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Last sync execution: ${lastSyncTime ?: "Pending"}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = NovaTealDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. About App
        NovaCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = "About",
                    tint = NovaTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NOVA - Health Connect",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = NovaTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Version 1.0.0 • Native Android Architecture\nKotlin 1.9 • Jetpack Compose • Health Connect SDK • Retrofit • WorkManager",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = NovaTextMuted
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
