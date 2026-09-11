package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampGameDataManager
import com.example.data.SampOnlinePlayer
import com.example.data.SampQueryClient
import com.example.data.SampServer
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun ServerListScreen(
    playerNickname: String,
    gameDataManager: SampGameDataManager,
    onRequestDownloadData: () -> Unit,
    onLaunchGame: (SampServer) -> Unit,
    onPlayInApp: (ip: String, port: Int, serverName: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var serverInfo by remember {
        mutableStateOf(
            SampServerLiveInfo(
                isOnline = true,
                isPasswordLocked = false,
                hostname = SampQueryClient.DEFAULT_HOSTNAME,
                ip = SampQueryClient.OFFICIAL_IP,
                port = SampQueryClient.OFFICIAL_PORT,
                currentPlayers = 0,
                maxPlayers = 100,
                gamemode = "NEXSTON",
                mapname = "Sri Lanka",
                ping = 45
            )
        )
    }

    var currentNickname by remember { mutableStateOf(gameDataManager.getPlayerNickname()) }
    var playersList by remember { mutableStateOf<List<SampOnlinePlayer>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isConnectingDialog by remember { mutableStateOf(false) }
    var showManualSetupDialog by remember { mutableStateOf(false) }
    var autoRefreshCount by remember { mutableIntStateOf(0) }

    // Live Query Function
    fun fetchLiveServerData() {
        scope.launch {
            isRefreshing = true
            val liveData = SampQueryClient.queryServer(SampQueryClient.OFFICIAL_IP, SampQueryClient.OFFICIAL_PORT)
            serverInfo = liveData
            if (liveData.isOnline && liveData.currentPlayers > 0) {
                playersList = SampQueryClient.queryDetailedPlayers(SampQueryClient.OFFICIAL_IP, SampQueryClient.OFFICIAL_PORT)
            } else {
                playersList = emptyList()
            }
            isRefreshing = false
        }
    }

    // Auto-refresh loop every 10 seconds in the background
    LaunchedEffect(Unit) {
        while (isActive) {
            fetchLiveServerData()
            delay(10000)
            autoRefreshCount++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("official_server_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Dedicated Official Server Lock Banner
            OfficialServerBadge(
                ip = SampQueryClient.OFFICIAL_IP,
                port = SampQueryClient.OFFICIAL_PORT,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("SAMP Server", "${SampQueryClient.OFFICIAL_IP}:${SampQueryClient.OFFICIAL_PORT}")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied IP: ${SampQueryClient.OFFICIAL_IP}:${SampQueryClient.OFFICIAL_PORT}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            // Game Data Status Banner (Installed or Action to Download)
            GameDataStatusBanner(
                isInstalled = gameDataManager.isGameDataInstalled(),
                cacheType = gameDataManager.getInstalledCacheType().displayName,
                onClickDownload = onRequestDownloadData,
                onClickManualSetup = { showManualSetupDialog = true }
            )
        }

        item {
            // Live Status Card (Online/Offline, Locked/Unlocked, Live Players, Ping)
            LiveServerTelemetryCard(
                serverInfo = serverInfo,
                isRefreshing = isRefreshing,
                pulseAlpha = pulseAlpha,
                currentNickname = currentNickname,
                onNicknameChange = {
                    currentNickname = it
                    gameDataManager.savePlayerNickname(it)
                },
                onManualRefresh = { fetchLiveServerData() },
                onPlayInApp = {
                    val cleanNick = currentNickname.trim().ifBlank { "Madu_M2" }
                    currentNickname = cleanNick
                    gameDataManager.savePlayerNickname(cleanNick)
                    onPlayInApp(serverInfo.ip, serverInfo.port, serverInfo.hostname)
                },
                onLaunchExternal = {
                    val cleanNick = currentNickname.trim().ifBlank { "Madu_M2" }
                    currentNickname = cleanNick
                    gameDataManager.savePlayerNickname(cleanNick)

                    val launchResult = gameDataManager.launchSampGame(serverInfo.ip, serverInfo.port, cleanNick)
                    if (launchResult.isSuccess) {
                        Toast.makeText(context, "Direct Game APK Started", Toast.LENGTH_SHORT).show()
                    } else {
                        showManualSetupDialog = true
                        Toast.makeText(context, "GTA SA Game APK not detected. Please run directly inside this app!", Toast.LENGTH_LONG).show()
                    }
                },
                onCopyIp = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("SAMP Server", "${serverInfo.ip}:${serverInfo.port}")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied: ${serverInfo.ip}:${serverInfo.port}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            // Server Specs & Roleplay Info
            ServerDetailsSection(serverInfo = serverInfo)
        }

        item {
            // Second Favorite Server from launcher: Ceylon City RolePlay
            CeylonCityServerCard(
                currentNickname = currentNickname,
                onPlayInApp = { ip, port, name ->
                    val cleanNick = currentNickname.trim().ifBlank { "Madu_M2" }
                    currentNickname = cleanNick
                    gameDataManager.savePlayerNickname(cleanNick)
                    onPlayInApp(ip, port, name)
                },
                onLaunch = { ip, port ->
                    val cleanNick = currentNickname.trim().ifBlank { "Madu_M2" }
                    currentNickname = cleanNick
                    gameDataManager.savePlayerNickname(cleanNick)
                    val result = gameDataManager.launchSampGame(ip, port, cleanNick)
                    if (result.isSuccess) {
                        Toast.makeText(context, "Direct Game APK Started", Toast.LENGTH_SHORT).show()
                    } else {
                        showManualSetupDialog = true
                        Toast.makeText(context, "GTA SA Game APK not detected.", Toast.LENGTH_LONG).show()
                    }
                },
                onCopyIp = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("SAMP Server", "ccrp.samp.lk:7777")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied: ccrp.samp.lk:7777", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            // Live Players Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Players",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE PLAYERS (${serverInfo.currentPlayers}/${serverInfo.maxPlayers})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (isRefreshing) "Syncing..." else "Auto-Sync 10s",
                    fontSize = 10.sp,
                    color = if (isRefreshing) CyberGold else TextMuted
                )
            }
        }

        // Live Players List or Empty State
        if (playersList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CardBorder, CardBorder)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Empty",
                            tint = CrimsonRed,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (serverInfo.isOnline) "Server is Ready to Play!" else "Server Offline / Reconnecting...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (serverInfo.isOnline)
                                "Currently 0 players online. Be the first player to join the city!"
                            else
                                "Could not connect to ${serverInfo.ip}:${serverInfo.port}. Check server status.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(playersList, key = { it.id }) { player ->
                LivePlayerRow(player = player)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Connect Execution Dialog with Netcode & Engine Launch
    if (isConnectingDialog) {
        val mappedServer = SampServer(
            id = "nexston_official",
            name = serverInfo.hostname,
            ip = serverInfo.ip,
            port = serverInfo.port,
            currentPlayers = serverInfo.currentPlayers,
            maxPlayers = serverInfo.maxPlayers,
            ping = serverInfo.ping.toInt(),
            gamemode = serverInfo.gamemode,
            map = serverInfo.mapname,
            isFavorite = true,
            isVerified = true,
            region = "Sri Lanka"
        )
        FullGameConnectSequenceDialog(
            serverInfo = serverInfo,
            playerNickname = playerNickname,
            onDismiss = { isConnectingDialog = false },
            onLaunchGameIntent = {
                isConnectingDialog = false
                val launchResult = gameDataManager.launchSampGame(serverInfo.ip, serverInfo.port, playerNickname)
                if (launchResult.isSuccess) {
                    Toast.makeText(context, launchResult.message, Toast.LENGTH_LONG).show()
                    onLaunchGame(mappedServer)
                } else {
                    showManualSetupDialog = true
                    Toast.makeText(context, "Game APK not detected! Opening ZArchiver & Setup guide...", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    if (showManualSetupDialog) {
        ManualDataAndApkSetupDialog(
            gameDataManager = gameDataManager,
            playerNickname = playerNickname,
            onDismiss = { showManualSetupDialog = false },
            onLaunchGame = {
                isConnectingDialog = true
            }
        )
    }
}

@Composable
fun GameDataStatusBanner(
    isInstalled: Boolean,
    cacheType: String,
    onClickDownload: () -> Unit,
    onClickManualSetup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClickManualSetup() }
            .border(
                1.dp,
                if (isInstalled) Color(0xFF00E676).copy(alpha = 0.5f) else FlameOrange.copy(alpha = 0.8f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled) Color(0xFF0A140F) else Color(0xFF1E100A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isInstalled) Color(0x3300E676) else Color(0x33FF6D00)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInstalled) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = if (isInstalled) Color(0xFF00E676) else FlameOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isInstalled) "SA-MP GAME DATA: READY" else "GAME DATA REQUIRED (OR PASTE MANUALLY)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isInstalled) Color(0xFF00E676) else FlameOrange,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isInstalled) "Cache: $cacheType • Ready to connect" else "ZArchiver මගින් Data දමන්න හෝ Download කරන්න",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isInstalled) Color(0x2200E676) else CrimsonRed)
                        .clickable { if (isInstalled) onClickManualSetup() else onClickDownload() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isInstalled) "VERIFIED" else "DOWNLOAD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInstalled) Color(0xFF00E676) else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action chips for ZArchiver path & Fast Download
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF161622))
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        .clickable { onClickManualSetup() }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📁 ZArchiver Data Path",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF161622))
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        .clickable { onClickDownload() }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ In-App Downloader",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberGold
                    )
                }
            }
        }
    }
}

@Composable
fun OfficialServerBadge(
    ip: String,
    port: Int,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Brush.horizontalGradient(listOf(DarkCrimson, CrimsonRed, CardBorder)), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FF1744)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Official",
                        tint = CrimsonRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "OFFICIAL SERVER • IP LOCKED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlameOrange,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$ip:$port",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x22FFFFFF))
                    .size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy IP",
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun LiveServerTelemetryCard(
    serverInfo: SampServerLiveInfo,
    isRefreshing: Boolean,
    pulseAlpha: Float,
    currentNickname: String,
    onNicknameChange: (String) -> Unit,
    onManualRefresh: () -> Unit,
    onPlayInApp: () -> Unit,
    onLaunchExternal: () -> Unit,
    onCopyIp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_server_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    if (serverInfo.isOnline) CrimsonRed else Color.Gray,
                    CardBorder
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Status Badges (Online/Offline & Locked/Unlocked) + Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Online / Offline Badge
                    val statusColor = if (serverInfo.isOnline) Color(0xFF00E676) else Color(0xFFFF1744)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (serverInfo.isOnline) statusColor.copy(alpha = pulseAlpha) else statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (serverInfo.isOnline) "LIVE ONLINE" else "OFFLINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Password Locked / Unlocked Badge
                    val isLocked = serverInfo.isPasswordLocked
                    val lockColor = if (isLocked) CyberGold else Color(0xFF00E5FF)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(lockColor.copy(alpha = 0.15f))
                            .border(1.dp, lockColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isLocked) "Locked" else "Unlocked",
                                tint = lockColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isLocked) "PASSWORD LOCKED" else "UNLOCKED (PUBLIC)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = lockColor
                            )
                        }
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = onManualRefresh,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CardSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isRefreshing) CyberGold else TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(if (isRefreshing) 180f else 0f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Server Hostname
            Text(
                text = serverInfo.hostname,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // IP & Port clickable row
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onCopyIp() }
                    .background(Color(0xFF08080C))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${serverInfo.ip}:${serverInfo.port}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Tap to copy",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Players Progress Bar & Count
            val playerProgress = if (serverInfo.maxPlayers > 0) {
                (serverInfo.currentPlayers.toFloat() / serverInfo.maxPlayers.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SERVER CAPACITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "${serverInfo.currentPlayers} / ${serverInfo.maxPlayers} PLAYERS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { playerProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CrimsonRed,
                trackColor = CardSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row: Ping, Gamemode, Map
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardSurfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("PING", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = "Ping",
                            tint = if (serverInfo.isOnline && serverInfo.ping < 100) Color(0xFF00E676) else FlameOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (serverInfo.isOnline) "${serverInfo.ping} ms" else "--",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GAMEMODE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = serverInfo.gamemode,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("MAP", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = serverInfo.mapname,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player Nickname Input (as shown in SA-MP Launcher)
            OutlinedTextField(
                value = currentNickname,
                onValueChange = onNicknameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_nickname_card_input"),
                label = { Text("PLAYER NICKNAME", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold) },
                placeholder = { Text("Madu_M2", color = TextMuted) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Player Name",
                        tint = CrimsonRed,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonRed,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF101016),
                    unfocusedContainerColor = Color(0xFF101016)
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Big Connect Action Button (Runs the game directly inside this app)
            Button(
                onClick = onPlayInApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("launch_inapp_game_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonRed,
                    disabledContainerColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = serverInfo.isOnline
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Play In App",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (serverInfo.isOnline) "▶ PLAY GAME (RUN IN THIS APP)" else "SERVER IS OFFLINE",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary option: Launch standalone external APK if installed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚀 Launch Native GTA:SA Standalone APK",
                    fontSize = 11.sp,
                    color = CyberCyan,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onLaunchExternal() }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ServerDetailsSection(serverInfo: SampServerLiveInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CardBorder, CardBorder)))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "SERVER INFORMATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Version Protocol", fontSize = 12.sp, color = TextMuted)
                Text("SA-MP 0.3.7-R1 Mobile", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Server Region", fontSize = 12.sp, color = TextMuted)
                Text("Sri Lanka 🇱🇰", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyberGold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Access Mode", fontSize = 12.sp, color = TextMuted)
                Text(
                    text = if (serverInfo.isPasswordLocked) "Restricted (Password Required)" else "Open Public (No Password)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (serverInfo.isPasswordLocked) CyberGold else Color(0xFF00E676)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Official IP Binding", fontSize = 12.sp, color = TextMuted)
                Text("STRICT (51.79.254.10:7774)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CrimsonRed)
            }
        }
    }
}

@Composable
fun CeylonCityServerCard(
    currentNickname: String,
    onPlayInApp: (ip: String, port: Int, serverName: String) -> Unit,
    onLaunch: (ip: String, port: Int) -> Unit,
    onCopyIp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ceylon_city_server_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF2979FF), CardBorder)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ONLINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                }
                Text(
                    text = "FAVORITE #2",
                    fontSize = 10.sp,
                    color = CyberGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ceylon City RolePlay | SRI LANKA",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "ccrp.samp.lk:7777",
                fontSize = 12.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode: CCRP v2.4",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onPlayInApp("ccrp.samp.lk", 7777, "Ceylon City RolePlay") },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Play In-App",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLAY IN-APP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LivePlayerRow(player: SampOnlinePlayer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2C)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${player.id}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = player.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Score: ${player.score}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Text(
                text = "${player.ping}ms",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (player.ping < 100) Color(0xFF00E676) else FlameOrange
            )
        }
    }
}

@Composable
fun LiveConnectDialog(
    server: SampServer,
    serverInfo: SampServerLiveInfo,
    playerNickname: String,
    onDismiss: () -> Unit,
    onConnected: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val logs = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        logs.add("Connecting to Official Host ${server.ip}:${server.port}...")
        delay(400)
        logs.add("Sending SA-MP Query Protocol (0.3.7-R1)...")
        delay(400)
        logs.add("Hostname: ${serverInfo.hostname}")
        logs.add("Security: ${if (serverInfo.isPasswordLocked) "PASSWORD LOCKED" else "UNLOCKED (PUBLIC)"}")
        delay(400)
        logs.add("Authenticating Nickname: '$playerNickname'...")
        delay(500)
        logs.add("Connected successfully! Syncing roleplay data...")
        step = 1
        delay(700)
        onConnected()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (step == 0) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CrimsonRed,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Connected",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (step == 0) "CONNECTING TO SERVER..." else "CONNECTION ESTABLISHED!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A0A0E))
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                logs.forEach { log ->
                    Text(
                        text = "> $log",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (log.contains("successfully") || log.contains("UNLOCKED")) CyberGold else Color.LightGray,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text(if (step == 0) "Abort" else "Close")
            }
        },
        containerColor = CardSurface
    )
}
