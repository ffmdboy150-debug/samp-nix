package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameCacheType
import com.example.data.SampGameDataManager
import com.example.data.SampServerLiveInfo
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceVariant
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameDataPromptDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("game_data_prompt_dialog"),
        containerColor = CardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FF1744)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = "Download Data",
                        tint = CrimsonRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GAME DATA REQUIRED",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "SA-MP Mobile Game Cache",
                        fontSize = 11.sp,
                        color = FlameOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "සර්වරයට Connect වී සෙල්ලම් කිරීමට අවශ්‍ය SA-MP 0.3.7 Game Data Files (Audio, Models, Textures, Scripts) බාගත කර Install කරන්නද?",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Nexston RP Vehicles & Map", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Optimized 60 FPS Mobile Audio & Textures", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Client Libraries (libGTASA.so & libsamp.so)", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("prompt_accept_btn")
            ) {
                Text("YES, DOWNLOAD NOW", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("prompt_dismiss_btn")
            ) {
                Text("LATER", color = TextSecondary, fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun GameDataDownloaderDialog(
    gameDataManager: SampGameDataManager,
    onDismiss: () -> Unit,
    onDownloadFinished: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val downloadState by gameDataManager.downloadState.collectAsState()
    var selectedCache by remember { mutableStateOf(GameCacheType.LITE) }

    AlertDialog(
        onDismissRequest = {
            if (!downloadState.isDownloading) onDismiss()
        },
        modifier = Modifier.testTag("game_data_downloader_dialog"),
        containerColor = CardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (downloadState.isFinished) Icons.Default.DownloadDone else Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = if (downloadState.isFinished) Color(0xFF00E676) else CrimsonRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (downloadState.isFinished) "GAME DATA INSTALLED!" else "DOWNLOADING GAME DATA",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!downloadState.isDownloading && !downloadState.isFinished) {
                    Text(
                        text = "Select your preferred Game Cache package:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    // Cache Selection Radio Cards
                    GameCacheType.values().forEach { cache ->
                        val isSelected = selectedCache == cache
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCache = cache }
                                .border(
                                    1.dp,
                                    if (isSelected) CrimsonRed else CardBorder,
                                    RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CardSurfaceVariant else Color(0xFF101018)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedCache = cache },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CrimsonRed,
                                        unselectedColor = TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cache.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${cache.sizeMb} MB",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = CyberGold
                                        )
                                    }
                                    Text(
                                        text = cache.description,
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (downloadState.isDownloading) {
                    // Downloading Active Progress View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C0C12))
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "FILE: ${downloadState.currentFile}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = "${(downloadState.progress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FlameOrange
                            )
                        }

                        LinearProgressIndicator(
                            progress = { downloadState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = CrimsonRed,
                            trackColor = CardSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${String.format("%.1f", downloadState.downloadedMb)} MB / ${downloadState.totalMb.toInt()} MB",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = downloadState.downloadSpeed,
                                fontSize = 11.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = downloadState.statusMessage,
                            fontSize = 10.sp,
                            color = TextMuted,
                            lineHeight = 13.sp
                        )
                    }
                } else if (downloadState.isFinished) {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0E1A14))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "GAME DATA VERIFIED & READY!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                        Text(
                            text = "All game files (Textures, Audio, Map & SA-MP Client) have been installed into internal storage. You can now connect to the server!",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!downloadState.isDownloading && !downloadState.isFinished) {
                Button(
                    onClick = {
                        scope.launch {
                            gameDataManager.startDownload(selectedCache)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("start_download_btn")
                ) {
                    Text("START DOWNLOAD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else if (downloadState.isFinished) {
                Button(
                    onClick = onDownloadFinished,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("finish_download_btn")
                ) {
                    Text("CLOSE & PLAY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            if (!downloadState.isDownloading && !downloadState.isFinished) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
fun FullGameConnectSequenceDialog(
    serverInfo: SampServerLiveInfo,
    playerNickname: String,
    onDismiss: () -> Unit,
    onLaunchGameIntent: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val logs = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        logs.add("[ENGINE] Initializing San Andreas Game Engine v2.10...")
        delay(350)
        logs.add("[STORAGE] Verifying cache: gta3.img, samp.img, textdb [OK]")
        delay(350)
        logs.add("[CLIENT] Loading libGTASA.so & libsamp.so binaries...")
        delay(400)
        logs.add("[NETWORK] Resolving official host: ${serverInfo.ip}:${serverInfo.port}...")
        delay(450)
        logs.add("[SECURITY] Security check: ${if (serverInfo.isPasswordLocked) "PASSWORD PROTECTED" else "UNLOCKED / PUBLIC ACCESS"}")
        delay(350)
        logs.add("[SA-MP] Authenticating player: '$playerNickname'...")
        delay(400)
        logs.add("[NETCODE] Connecting to server at ping ${serverInfo.ping}ms...")
        delay(450)
        logs.add("[CONNECTED] Welcome to ${serverInfo.hostname}!")
        logs.add("[GAMEPLAY] Spawning character in Los Santos / Sri Lanka...")
        step = 1
        delay(600)
        onLaunchGameIntent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("game_connect_sequence_dialog"),
        containerColor = CardSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (step == 0) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = CrimsonRed,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Connected",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (step == 0) "ENTERING CITY..." else "CONNECTED TO SERVER!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${serverInfo.ip}:${serverInfo.port}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF08080C))
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                logs.forEach { log ->
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = when {
                            log.contains("[CONNECTED]") -> Color(0xFF00E676)
                            log.contains("[GAMEPLAY]") -> CyberGold
                            log.contains("[SECURITY]") -> FlameOrange
                            else -> Color.LightGray
                        },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (step == 0) "Abort" else "Close Console")
            }
        }
    )
}
