package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.SampSampleData
import com.example.data.SampServer
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
fun ServerListScreen(
    playerNickname: String,
    onLaunchGame: (SampServer) -> Unit
) {
    val context = LocalContext.current
    val servers = remember { mutableStateListOf<SampServer>().apply { addAll(SampSampleData.defaultServers) } }
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var connectingServer by remember { mutableStateOf<SampServer?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Filter logic
    val filteredServers = servers.filter { server ->
        val matchesFilter = when (selectedFilter) {
            "Favorites" -> server.isFavorite
            "Roleplay" -> server.gamemode.contains("RP", ignoreCase = true) || server.gamemode.contains("Roleplay", ignoreCase = true)
            "DM / Gang" -> server.gamemode.contains("DM", ignoreCase = true) || server.gamemode.contains("Gang", ignoreCase = true) || server.gamemode.contains("War", ignoreCase = true)
            "Drift" -> server.gamemode.contains("Drift", ignoreCase = true) || server.gamemode.contains("Stunt", ignoreCase = true)
            else -> true
        }
        val matchesSearch = server.name.contains(searchQuery, ignoreCase = true) ||
                server.ip.contains(searchQuery, ignoreCase = true) ||
                server.gamemode.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_search_input"),
                placeholder = { Text("Search servers by name or IP...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CrimsonRed)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                delay(600)
                                isRefreshing = false
                                Toast.makeText(context, "Servers refreshed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = if (isRefreshing) CyberGold else TextSecondary
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonRed,
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Favorites", "Roleplay", "DM / Gang", "Drift").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White,
                            containerColor = CardSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = CardBorder,
                            selectedBorderColor = DarkCrimson
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Server Count & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AVAILABLE SERVERS (${filteredServers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "CONNECT AS: $playerNickname",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Server List
            if (filteredServers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No servers found matching '$searchQuery'",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredServers, key = { it.id }) { server ->
                        ServerCard(
                            server = server,
                            onConnect = { connectingServer = server },
                            onToggleFavorite = {
                                val idx = servers.indexOfFirst { it.id == server.id }
                                if (idx >= 0) {
                                    servers[idx] = server.copy(isFavorite = !server.isFavorite)
                                }
                            },
                            onCopyIp = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SAMP Server IP", "${server.ip}:${server.port}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "IP Copied: ${server.ip}:${server.port}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Custom Server
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_custom_server_fab"),
            containerColor = CrimsonRed,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Server")
        }
    }

    // Add Custom Server Dialog
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newIp by remember { mutableStateOf("") }
        var newPort by remember { mutableStateOf("7777") }
        var newMode by remember { mutableStateOf("Roleplay") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text("Add Custom SA-MP Server", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Server Name") },
                        placeholder = { Text("e.g. My Private SAMP Server") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newIp,
                        onValueChange = { newIp = it },
                        label = { Text("IP Address / Domain") },
                        placeholder = { Text("e.g. 127.0.0.1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPort,
                        onValueChange = { newPort = it },
                        label = { Text("Port") },
                        placeholder = { Text("7777") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMode,
                        onValueChange = { newMode = it },
                        label = { Text("Game Mode") },
                        placeholder = { Text("Roleplay / TDM / Stunt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newIp.isNotBlank()) {
                            val parsedPort = newPort.toIntOrNull() ?: 7777
                            val newServer = SampServer(
                                id = "custom_${System.currentTimeMillis()}",
                                name = "⭐ $newName",
                                ip = newIp.trim(),
                                port = parsedPort,
                                currentPlayers = 1,
                                maxPlayers = 500,
                                ping = 38,
                                gamemode = newMode.ifBlank { "Custom Mod" },
                                map = "San Andreas",
                                isFavorite = true
                            )
                            servers.add(0, newServer)
                            showAddDialog = false
                            Toast.makeText(context, "Server Added!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("Add Server")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CardSurfaceVariant
        )
    }

    // Connect Simulation Dialog
    connectingServer?.let { server ->
        ConnectServerDialog(
            server = server,
            playerNickname = playerNickname,
            onDismiss = { connectingServer = null },
            onConnected = {
                connectingServer = null
                onLaunchGame(server)
            }
        )
    }
}

@Composable
fun ServerCard(
    server: SampServer,
    onConnect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopyIp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${server.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CardBorder, CardBorder)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Status, Name, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (server.currentPlayers > 0) Color(0xFF00E676) else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = server.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (server.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (server.isFavorite) CrimsonRed else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // IP & Port Row + Copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${server.ip}:${server.port}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = CyberCyan,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(
                    onClick = onCopyIp,
                    modifier = Modifier.size(28.dp).padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy IP",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Region / Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x33FF1744))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = server.region,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = FlameOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meta Info: Gamemode, Players, Ping
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "MODE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(text = server.gamemode, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium, maxLines = 1)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PLAYERS", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${server.currentPlayers}/${server.maxPlayers}",
                        fontSize = 12.sp,
                        color = CyberGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PING", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = "Ping",
                            tint = if (server.ping < 50) Color(0xFF00E676) else FlameOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${server.ping}ms",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Join Button
            Button(
                onClick = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("connect_btn_${server.id}"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CONNECT TO SERVER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun ConnectServerDialog(
    server: SampServer,
    playerNickname: String,
    onDismiss: () -> Unit,
    onConnected: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val logs = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        logs.add("Connecting to ${server.ip}:${server.port}...")
        delay(400)
        logs.add("Sending handshake query [SAMP 0.3.7-R1]...")
        delay(450)
        logs.add("Server: ${server.name}")
        logs.add("Gamemode: ${server.gamemode}")
        delay(400)
        logs.add("Registering player nickname: '$playerNickname'...")
        delay(500)
        logs.add("Player authenticated! Status: ONLINE")
        logs.add("Spawning player at Los Santos Idlewood...")
        step = 1
        delay(800)
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
                    text = if (step == 0) "CONNECTING TO SA-MP..." else "CONNECTED!",
                    fontSize = 16.sp,
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
                        color = if (log.contains("ONLINE")) CyberGold else Color.LightGray,
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
