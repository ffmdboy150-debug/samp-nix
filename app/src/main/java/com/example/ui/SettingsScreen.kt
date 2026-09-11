package com.example.ui

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Bolt
import com.example.data.SampGameDataManager
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentNickname: String,
    gameDataManager: SampGameDataManager,
    onDownloadData: () -> Unit,
    onSaveNickname: (String) -> Unit,
    onReplayLoading: () -> Unit
) {
    val context = LocalContext.current
    var nicknameInput by remember { mutableStateOf(currentNickname) }
    var fpsLimit by remember { mutableStateOf("60 FPS") }
    var graphicsQuality by remember { mutableStateOf("Ultra HD") }
    var fastConnect by remember { mutableStateOf(true) }
    var soundFx by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    var wideAspect by remember { mutableStateOf(true) }
    var isDataInstalled by remember { mutableStateOf(gameDataManager.isGameDataInstalled()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag("settings_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Replay Loading Screen Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onReplayLoading() },
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CrimsonRed))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FF1744)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay",
                        tint = CrimsonRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TEST LOADING SCREEN (0-100%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Replay the Oni mask 100% progress animation",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = onReplayLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Player Profile Card
        Text(
            text = "PLAYER IDENTITY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    label = { Text("In-Game Nickname") },
                    placeholder = { Text("Name_Surname") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Name", tint = CrimsonRed)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("nickname_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonRed,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = CardSurfaceVariant,
                        unfocusedContainerColor = CardSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (nicknameInput.isNotBlank()) {
                            onSaveNickname(nicknameInput.trim())
                            Toast.makeText(context, "Nickname updated: $nicknameInput", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_nickname_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVE NICKNAME", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Performance & Graphics
        Text(
            text = "GRAPHICS & PERFORMANCE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // FPS Limit Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = "FPS", tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Target Frame Rate", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            Text("Current: $fpsLimit", fontSize = 11.sp, color = CyberGold)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("30", "60", "90").forEach { fps ->
                            val isSelected = fpsLimit.startsWith(fps)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CrimsonRed else CardSurfaceVariant)
                                    .clickable { fpsLimit = "$fps FPS" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = fps,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Graphics Quality
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Graphics", tint = FlameOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Graphics Shaders", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            Text(graphicsQuality, fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Low", "Ultra").forEach { g ->
                            val isSelected = graphicsQuality.contains(g)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) FlameOrange else CardSurfaceVariant)
                                    .clickable { graphicsQuality = "$g HD" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Fast Connect Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = "Fast Connect", tint = CyberGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Fast Connect (Skip Slot Wait)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            Text("Reconnects within 500ms when server is full", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Switch(
                        checked = fastConnect,
                        onCheckedChange = { fastConnect = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio & Controls
        Text(
            text = "CONTROLS & DISPLAY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Orientation Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ScreenRotation, contentDescription = "Orientation", tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Display Orientation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            Text("Locked to Portrait (දික් අතට)", fontSize = 11.sp, color = CyberCyan)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x3300E5FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }
                }

                // Sound FX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Audio", tint = FlameOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Game Sounds & Audio", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Switch(
                        checked = soundFx,
                        onCheckedChange = { soundFx = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonRed)
                    )
                }

                // Vibration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = "Vibration", tint = CrimsonRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Haptic Feedback on Taps", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                    }

                    Switch(
                        checked = vibration,
                        onCheckedChange = { vibration = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SAMP Game Cache Management
        Text(
            text = "SA-MP GAME DATA & CACHE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(14.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDataInstalled) Icons.Default.DownloadDone else Icons.Default.CloudDownload,
                            contentDescription = "Game Data",
                            tint = if (isDataInstalled) Color(0xFF00E676) else FlameOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isDataInstalled) "Game Files Installed" else "No Game Files Downloaded",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isDataInstalled) "GTA SA Cache: ~650 MB (Lite Edition)" else "Download required to play",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Button(
                        onClick = { onDownloadData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDataInstalled) CardSurfaceVariant else CrimsonRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isDataInstalled) "Re-download" else "Download",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDataInstalled) CyberCyan else Color.White
                        )
                    }
                }

                if (isDataInstalled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                gameDataManager.resetGameData()
                                isDataInstalled = false
                                Toast.makeText(context, "Game cache reset.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF1744)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear",
                                tint = CrimsonRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Game Cache", fontSize = 11.sp, color = CrimsonRed)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
