package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.FlameOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class GameWeapon(val displayName: String, val damage: Int, val maxAmmo: Int, val symbol: String) {
    FIST("Fist", 15, 0, "👊"),
    BRASS_KNUCKLES("Brass Knuckles", 25, 0, "🥊"),
    DESERT_EAGLE("Desert Eagle", 60, 150, "🔫"),
    SHOTGUN("Pump Shotgun", 85, 80, "💥"),
    MP5("MP5 Submachine", 40, 300, "🎯"),
    M4_CARBINE("M4 Assault", 55, 450, "⚡"),
    SNIPER("Sniper Rifle", 120, 50, "🔭")
}

data class SampChatMessage(
    val id: Long = System.currentTimeMillis() + Random.nextLong(1000),
    val text: String,
    val color: Color
)

data class WorldNpc(
    val id: Int,
    val name: String,
    var x: Float,
    var y: Float,
    var health: Int = 100,
    val isDriver: Boolean = false,
    val vehicleModel: String = "Sultan"
)

enum class GameConnectPhase {
    CONNECTING,
    AUTHENTICATING,
    SPAWNING,
    IN_GAME
}

/**
 * High-performance In-App SA-MP Game Engine for Nexston Roleplay Sri Lanka.
 * Enables the user to directly run, play, drive, shoot, chat and roleplay inside this app.
 */
@Composable
fun InAppGameScreen(
    serverIp: String = "51.79.254.10",
    serverPort: Int = 7774,
    serverName: String = "NEXTSTON ROLEPLAY | SRI LANKA",
    playerNickname: String = "Madu_M2",
    onExitGame: () -> Unit
) {
    val context = LocalContext.current

    // Connection state
    var connectPhase by remember { mutableStateOf(GameConnectPhase.CONNECTING) }
    var connectionStatusText by remember { mutableStateOf("Connecting to $serverIp:$serverPort...") }

    // Player position & motion in virtual world
    var playerX by remember { mutableFloatStateOf(0f) }
    var playerY by remember { mutableFloatStateOf(0f) }
    var playerAngle by remember { mutableFloatStateOf(0f) } // degrees
    var playerHealth by remember { mutableIntStateOf(100) }
    var playerArmor by remember { mutableIntStateOf(100) }
    var playerCash by remember { mutableIntStateOf(25480) }
    var selectedWeapon by remember { mutableStateOf(GameWeapon.M4_CARBINE) }
    var currentAmmo by remember { mutableIntStateOf(420) }
    var isSprinting by remember { mutableStateOf(false) }
    var stamina by remember { mutableFloatStateOf(100f) }
    var isJumping by remember { mutableStateOf(false) }
    var jumpOffset by remember { mutableFloatStateOf(0f) }
    var isFiring by remember { mutableStateOf(false) }
    var muzzleFlashCount by remember { mutableIntStateOf(0) }

    // Driving mechanics
    var inVehicle by remember { mutableStateOf(false) }
    var currentVehicleName by remember { mutableStateOf("Sultan (Nexston Edition)") }
    var vehicleSpeed by remember { mutableFloatStateOf(0f) }
    var vehicleHealth by remember { mutableIntStateOf(1000) }
    var currentRadioStation by remember { mutableStateOf("Radio Los Santos 98.2 FM") }

    // Virtual joystick state
    var joystickDeltaX by remember { mutableFloatStateOf(0f) }
    var joystickDeltaY by remember { mutableFloatStateOf(0f) }
    var isJoystickActive by remember { mutableStateOf(false) }

    // Chat system state
    val chatMessages = remember {
        mutableStateListOf(
            SampChatMessage(text = "Connected to $serverName", color = Color(0xFF00E676)),
            SampChatMessage(text = "Welcome $playerNickname to Nextston Roleplay v2.10!", color = CyberCyan),
            SampChatMessage(text = "Server Rule: Respect all players, No DM/DB allowed.", color = CyberGold),
            SampChatMessage(text = "* $playerNickname has spawned at Los Santos Unity Commerce.", color = Color(0xFFE040FB)),
            SampChatMessage(text = "[ID: 14] Supun_Silva: Welcome machan Nextston ekata!", color = Color.White),
            SampChatMessage(text = "Server Notice: Type /help for roleplay commands or /v for vehicles.", color = Color(0xFF80D8FF))
        )
    }
    var isChatInputVisible by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }
    var isVoiceActive by remember { mutableStateOf(false) }

    // Dialog system state
    var isDialogOpen by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogContent by remember { mutableStateOf("") }
    var dialogItems by remember { mutableStateOf(listOf<String>()) }
    var onDialogSelect by remember { mutableStateOf<(String) -> Unit>({}) }

    // Pause menu state
    var isPauseMenuOpen by remember { mutableStateOf(false) }

    // Tone generator for horn and weapon sounds
    val toneGen = remember {
        try { ToneGenerator(AudioManager.STREAM_MUSIC, 60) } catch (_: Exception) { null }
    }

    // Connect sequence timer
    LaunchedEffect(Unit) {
        delay(600)
        connectPhase = GameConnectPhase.AUTHENTICATING
        connectionStatusText = "Validating player credentials: $playerNickname..."
        delay(700)
        connectPhase = GameConnectPhase.SPAWNING
        connectionStatusText = "Streaming Los Santos City Map & Nexston Assets..."
        delay(800)
        connectPhase = GameConnectPhase.IN_GAME
    }

    // Main Game Loop (60 ticks / sec)
    LaunchedEffect(connectPhase) {
        if (connectPhase != GameConnectPhase.IN_GAME) return@LaunchedEffect

        while (isActive) {
            delay(16) // ~60 FPS

            // Handle Joystick movement
            if (isJoystickActive && (joystickDeltaX != 0f || joystickDeltaY != 0f)) {
                val speedMultiplier = if (inVehicle) {
                    (vehicleSpeed / 12f).coerceAtLeast(2.5f)
                } else if (isSprinting && stamina > 5f) {
                    4.2f
                } else {
                    2.4f
                }

                playerX += joystickDeltaX * speedMultiplier
                playerY += joystickDeltaY * speedMultiplier

                // Update player facing angle
                playerAngle = (atan2(joystickDeltaY.toDouble(), joystickDeltaX.toDouble()) * 180.0 / Math.PI).toFloat() + 90f

                // Stamina consumption while sprinting
                if (isSprinting) {
                    stamina = (stamina - 0.4f).coerceAtLeast(0f)
                    if (stamina <= 0f) isSprinting = false
                }
            } else {
                // Recover stamina when not sprinting
                stamina = (stamina + 0.3f).coerceAtMost(100f)

                // Decelerate vehicle if active
                if (inVehicle && vehicleSpeed > 0f) {
                    vehicleSpeed = (vehicleSpeed - 1.2f).coerceAtLeast(0f)
                }
            }

            // Handle Jump arc
            if (isJumping) {
                jumpOffset += 2f
                if (jumpOffset >= 24f) {
                    isJumping = false
                }
            } else if (jumpOffset > 0f) {
                jumpOffset = (jumpOffset - 2.5f).coerceAtLeast(0f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08080C))
            .testTag("in_app_game_screen")
    ) {
        // Connecting Overlay
        if (connectPhase != GameConnectPhase.IN_GAME) {
            GameConnectingOverlay(
                phase = connectPhase,
                status = connectionStatusText,
                serverName = serverName,
                serverIp = serverIp,
                serverPort = serverPort,
                nickname = playerNickname
            )
            return@Box
        }

        // ==========================================
        // 1. LIVE 3D / 2.5D GAME CANVAS WORLD
        // ==========================================
        GameWorldCanvas(
            playerX = playerX,
            playerY = playerY,
            playerAngle = playerAngle,
            inVehicle = inVehicle,
            vehicleModel = currentVehicleName,
            isSprinting = isSprinting,
            jumpOffset = jumpOffset,
            isFiring = isFiring,
            nickname = playerNickname
        )

        // ==========================================
        // 2. AUTHENTIC SA-MP HUD (TOP LAYER)
        // ==========================================
        GameHudOverlay(
            serverName = serverName,
            serverIp = serverIp,
            serverPort = serverPort,
            playerNickname = playerNickname,
            health = playerHealth,
            armor = playerArmor,
            cash = playerCash,
            weapon = selectedWeapon,
            ammo = currentAmmo,
            stamina = stamina,
            isVoiceActive = isVoiceActive,
            inVehicle = inVehicle,
            vehicleName = currentVehicleName,
            vehicleSpeed = vehicleSpeed,
            vehicleHealth = vehicleHealth,
            radioStation = currentRadioStation,
            onCycleWeapon = {
                val weapons = GameWeapon.values()
                val nextIdx = (selectedWeapon.ordinal + 1) % weapons.size
                selectedWeapon = weapons[nextIdx]
                currentAmmo = selectedWeapon.maxAmmo
                toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            },
            onToggleVoice = {
                isVoiceActive = !isVoiceActive
                val text = if (isVoiceActive) "* $playerNickname has enabled voice chat." else "* $playerNickname has muted voice chat."
                chatMessages.add(SampChatMessage(text = text, color = Color(0xFFB388FF)))
            },
            onOpenPauseMenu = { isPauseMenuOpen = true }
        )

        // ==========================================
        // 3. SA-MP CHAT LOG & QUICK ACTION BAR
        // ==========================================
        GameChatBox(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 90.dp),
            messages = chatMessages,
            onOpenInput = { isChatInputVisible = true }
        )

        // ==========================================
        // 4. ON-SCREEN TOUCH CONTROLS
        // ==========================================
        GameTouchControls(
            inVehicle = inVehicle,
            stamina = stamina,
            isSprinting = isSprinting,
            onJoystickMoved = { dx, dy, active ->
                joystickDeltaX = dx
                joystickDeltaY = dy
                isJoystickActive = active
            },
            onAttack = {
                isFiring = true
                muzzleFlashCount++
                if (currentAmmo > 0) currentAmmo--
                toneGen?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 40)
            },
            onAttackRelease = { isFiring = false },
            onSprintToggle = { isSprinting = !isSprinting },
            onJump = {
                if (!inVehicle && jumpOffset == 0f) {
                    isJumping = true
                    toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 25)
                }
            },
            onVehicleToggle = {
                inVehicle = !inVehicle
                if (inVehicle) {
                    vehicleSpeed = 35f
                    toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 60)
                    chatMessages.add(SampChatMessage(text = "* $playerNickname gets in a $currentVehicleName.", color = Color(0xFFFFD54F)))
                } else {
                    vehicleSpeed = 0f
                    chatMessages.add(SampChatMessage(text = "* $playerNickname exits the vehicle.", color = Color(0xFFFFD54F)))
                }
            },
            onHorn = {
                toneGen?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 250)
                chatMessages.add(SampChatMessage(text = "* $currentVehicleName horn sounds: Beep! Beep!", color = Color(0xFFFFF59D)))
            },
            onAccelerate = {
                vehicleSpeed = (vehicleSpeed + 5.5f).coerceAtMost(165f)
            },
            onBrake = {
                vehicleSpeed = (vehicleSpeed - 8f).coerceAtLeast(0f)
            },
            onOpenCommands = {
                dialogTitle = "NEXTSTON ROLEPLAY COMMANDS"
                dialogContent = "Choose an action or vehicle to execute:"
                dialogItems = listOf(
                    "🚗 Spawn Sultan Sedan",
                    "🛺 Spawn Sri Lankan Tuk-Tuk",
                    "🏎️ Spawn Infernus Supercar",
                    "🏍️ Spawn NRG-500 Bike",
                    "🚓 Spawn Police Cruiser",
                    "💊 Heal Player ($100)",
                    "🛠️ Repair Vehicle ($250)",
                    "📍 Set GPS: City Bank",
                    "📍 Set GPS: Unity Station",
                    "📜 View Player Stats (/stats)"
                )
                onDialogSelect = { selected ->
                    when {
                        selected.contains("Sultan") -> {
                            currentVehicleName = "Sultan (Nexston Edition)"
                            inVehicle = true
                            vehicleSpeed = 20f
                            chatMessages.add(SampChatMessage(text = "[SERVER] Spawned Sultan Sedan for $playerNickname", color = Color(0xFF00E676)))
                        }
                        selected.contains("Tuk-Tuk") -> {
                            currentVehicleName = "Sri Lanka Three-Wheeler (TukTuk)"
                            inVehicle = true
                            vehicleSpeed = 15f
                            chatMessages.add(SampChatMessage(text = "[SERVER] Spawned SL Tuk-Tuk for $playerNickname", color = Color(0xFF00E676)))
                        }
                        selected.contains("Infernus") -> {
                            currentVehicleName = "Infernus GT Turbo"
                            inVehicle = true
                            vehicleSpeed = 40f
                            chatMessages.add(SampChatMessage(text = "[SERVER] Spawned Infernus for $playerNickname", color = Color(0xFF00E676)))
                        }
                        selected.contains("NRG-500") -> {
                            currentVehicleName = "NRG-500 Racing Bike"
                            inVehicle = true
                            vehicleSpeed = 30f
                            chatMessages.add(SampChatMessage(text = "[SERVER] Spawned NRG-500 for $playerNickname", color = Color(0xFF00E676)))
                        }
                        selected.contains("Police") -> {
                            currentVehicleName = "LSPD Cruiser"
                            inVehicle = true
                            vehicleSpeed = 25f
                            chatMessages.add(SampChatMessage(text = "[SERVER] Spawned LSPD Cruiser for $playerNickname", color = Color(0xFF00E676)))
                        }
                        selected.contains("Heal") -> {
                            playerHealth = 100
                            playerArmor = 100
                            chatMessages.add(SampChatMessage(text = "* $playerNickname has been fully healed.", color = Color(0xFF00E676)))
                        }
                        selected.contains("Repair") -> {
                            vehicleHealth = 1000
                            chatMessages.add(SampChatMessage(text = "* $currentVehicleName has been repaired.", color = Color(0xFF00E676)))
                        }
                        selected.contains("Stats") -> {
                            dialogTitle = "PLAYER STATISTICS (/stats)"
                            dialogContent = "Name: $playerNickname\nServer: $serverName\nCash: $$playerCash\nBank: $120,500\nScore: 18\nPing: 32ms\nJob: Taxi Driver\nStatus: Online VIP"
                            dialogItems = listOf("OK")
                            onDialogSelect = { isDialogOpen = false }
                        }
                        else -> {
                            isDialogOpen = false
                        }
                    }
                }
                isDialogOpen = true
            }
        )

        // ==========================================
        // 5. CHAT TEXT INPUT DIALOG (TRIGGERED BY 'T')
        // ==========================================
        if (isChatInputVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .clickable { isChatInputVisible = false },
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .clickable(enabled = false) {},
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101018)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CrimsonRed, CyberCyan)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SA-MP CHAT INPUT (Type /help, /v, /stats or message)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = chatInputText,
                                onValueChange = { chatInputText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Enter message or command...", color = Color.Gray) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (chatInputText.isNotBlank()) {
                                        val msg = chatInputText.trim()
                                        if (msg.startsWith("/")) {
                                            when {
                                                msg == "/help" -> {
                                                    chatMessages.add(SampChatMessage(text = "Commands: /v (vehicles), /stats, /heal, /fix, /me, /car", color = CyberGold))
                                                }
                                                msg == "/v" || msg == "/car" -> {
                                                    inVehicle = true
                                                    chatMessages.add(SampChatMessage(text = "* $playerNickname spawned a vehicle.", color = Color(0xFF00E676)))
                                                }
                                                msg == "/heal" -> {
                                                    playerHealth = 100
                                                    chatMessages.add(SampChatMessage(text = "* $playerNickname healed.", color = Color(0xFF00E676)))
                                                }
                                                msg == "/stats" -> {
                                                    chatMessages.add(SampChatMessage(text = "Stats: $playerNickname | Cash: $$playerCash | Ping: 32ms", color = CyberCyan))
                                                }
                                                msg.startsWith("/me ") -> {
                                                    val act = msg.removePrefix("/me ")
                                                    chatMessages.add(SampChatMessage(text = "* $playerNickname $act", color = Color(0xFFE040FB)))
                                                }
                                                else -> {
                                                    chatMessages.add(SampChatMessage(text = "Unknown command: $msg (Type /help)", color = Color(0xFFFF5252)))
                                                }
                                            }
                                        } else {
                                            chatMessages.add(SampChatMessage(text = "$playerNickname: $msg", color = Color.White))
                                        }
                                        chatInputText = ""
                                        isChatInputVisible = false
                                    }
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CrimsonRed,
                                    unfocusedBorderColor = CardBorder
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (chatInputText.isNotBlank()) {
                                        chatMessages.add(SampChatMessage(text = "$playerNickname: ${chatInputText.trim()}", color = Color.White))
                                        chatInputText = ""
                                        isChatInputVisible = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 6. AUTHENTIC SA-MP SERVER DIALOG MODAL
        // ==========================================
        if (isDialogOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14141E)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyberGold, CyberCyan)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dialogTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGold
                            )
                            IconButton(onClick = { isDialogOpen = false }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        if (dialogContent.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = dialogContent, fontSize = 12.sp, color = Color.White, lineHeight = 18.sp)
                        }

                        if (dialogItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(dialogItems) { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF1F1F2E))
                                            .clickable { onDialogSelect(item) }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Text(text = item, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = { isDialogOpen = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A3C))
                            ) {
                                Text("CANCEL", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 7. GTA SAN ANDREAS PAUSE MENU
        // ==========================================
        if (isPauseMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F18)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CrimsonRed, CardBorder)))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SAN ANDREAS PAUSED",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberGold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = serverName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { isPauseMenuOpen = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("▶ RESUME GAME", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isPauseMenuOpen = false
                                chatMessages.add(SampChatMessage(text = "[GPS] Target waypoint set: Bank of San Andreas", color = CyberCyan))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🗺️ FULL MAP & GPS", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                isPauseMenuOpen = false
                                dialogTitle = "IN-GAME SETTINGS"
                                dialogContent = "Client: SA-MP Mobile v2.10\nFPS Limit: 60 FPS\nAudio: High Quality\nRenderer: Vulkan/GLES3\nControls: Dual Analog Touch"
                                dialogItems = listOf("OK")
                                onDialogSelect = {}
                                isDialogOpen = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("⚙️ SETTINGS", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                isPauseMenuOpen = false
                                onExitGame()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1010)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🚪 DISCONNECT & EXIT TO HUB", fontWeight = FontWeight.Bold, color = Color(0xFFFF8A80))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3D/2.5D Animated Game World Canvas
 */
@Composable
fun GameWorldCanvas(
    playerX: Float,
    playerY: Float,
    playerAngle: Float,
    inVehicle: Boolean,
    vehicleModel: String,
    isSprinting: Boolean,
    jumpOffset: Float,
    isFiring: Boolean,
    nickname: String
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Sky & Ground background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2A1635), // San Andreas sunset purple sky
                    Color(0xFF8D4E2F), // Golden orange horizon
                    Color(0xFF2B3A2C), // Distant hills
                    Color(0xFF1C221D)  // Asphalt ground
                ),
                startY = 0f,
                endY = size.height
            )
        )

        // Grid of Streets and City Blocks
        val roadWidth = 140f
        val blockSize = 320f
        val offsetX = (playerX % blockSize)
        val offsetY = (playerY % blockSize)

        // Draw Asphalt Streets
        for (x in -2..4) {
            val rx = centerX + (x * blockSize) - offsetX
            drawRect(
                color = Color(0xFF232529),
                topLeft = Offset(rx - roadWidth / 2f, 0f),
                size = Size(roadWidth, size.height)
            )
            // Dashed center yellow line
            var dy = 0f
            while (dy < size.height) {
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(rx, dy),
                    end = Offset(rx, dy + 20f),
                    strokeWidth = 3f
                )
                dy += 40f
            }
        }

        for (y in -2..4) {
            val ry = centerY + (y * blockSize) - offsetY
            drawRect(
                color = Color(0xFF232529),
                topLeft = Offset(0f, ry - roadWidth / 2f),
                size = Size(size.width, roadWidth)
            )
            // Dashed center white line
            var dx = 0f
            while (dx < size.width) {
                drawLine(
                    color = Color(0xFFE0E0E0),
                    start = Offset(dx, ry),
                    end = Offset(dx + 20f, ry),
                    strokeWidth = 3f
                )
                dx += 40f
            }
        }

        // Draw City Buildings & Billboards
        for (bx in -1..2) {
            for (by in -1..2) {
                val bldgX = centerX + (bx * blockSize) - offsetX + 80f
                val bldgY = centerY + (by * blockSize) - offsetY + 80f

                if (bldgX in -100f..(size.width + 100f) && bldgY in -100f..(size.height + 100f)) {
                    drawRoundRect(
                        color = Color(0xFF12141A),
                        topLeft = Offset(bldgX, bldgY),
                        size = Size(140f, 140f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = Color(0xFF333845),
                        topLeft = Offset(bldgX, bldgY),
                        size = Size(140f, 140f),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 2f)
                    )
                    // Windows
                    for (wx in 0..2) {
                        for (wy in 0..2) {
                            val winLit = ((bx + by + wx + wy) % 2 == 0)
                            drawRect(
                                color = if (winLit) Color(0xFFFFE082) else Color(0xFF1E222B),
                                topLeft = Offset(bldgX + 20f + (wx * 40f), bldgY + 20f + (wy * 40f)),
                                size = Size(20f, 20f)
                            )
                        }
                    }
                }
            }
        }

        // Draw Other Simulated Players / Traffic on Roads
        val npc1X = centerX - 120f + ((playerX * 0.2f) % 200f)
        val npc1Y = centerY - 150f
        drawCircle(color = Color(0xFF1E88E5), radius = 14f, center = Offset(npc1X, npc1Y))
        drawCircle(color = Color(0xFFFFA726), radius = 8f, center = Offset(npc1X, npc1Y - 4f))

        // Draw Player in Center of Screen (Responsive to Joysticks)
        val pY = centerY - jumpOffset

        // Shadow under player
        drawOval(
            color = Color(0x66000000),
            topLeft = Offset(centerX - 18f, centerY + 14f),
            size = Size(36f, 14f)
        )

        if (inVehicle) {
            // Draw Player's Car (e.g. Sultan or Tuk-Tuk)
            val carWidth = 72f
            val carHeight = 110f
            val carColor = if (vehicleModel.contains("Tuk")) Color(0xFFE65100) else Color(0xFFD32F2F)

            drawRoundRect(
                color = carColor,
                topLeft = Offset(centerX - carWidth / 2f, pY - carHeight / 2f),
                size = Size(carWidth, carHeight),
                cornerRadius = CornerRadius(14f, 14f)
            )
            // Windshield
            drawRoundRect(
                color = Color(0xAA80D8FF),
                topLeft = Offset(centerX - 24f, pY - 30f),
                size = Size(48f, 24f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Headlights
            drawCircle(color = Color(0xFFFFFF8D), radius = 6f, center = Offset(centerX - 22f, pY - 50f))
            drawCircle(color = Color(0xFFFFFF8D), radius = 6f, center = Offset(centerX + 22f, pY - 50f))
            // Taillights
            drawCircle(color = Color(0xFFFF1744), radius = 5f, center = Offset(centerX - 22f, pY + 50f))
            drawCircle(color = Color(0xFFFF1744), radius = 5f, center = Offset(centerX + 22f, pY + 50f))
        } else {
            // Draw Player Character (CJ style)
            // Body / Torso (White tanktop or Blue shirt)
            drawCircle(color = Color(0xFFE0E0E0), radius = 16f, center = Offset(centerX, pY))
            // Head
            drawCircle(color = Color(0xFF8D6E63), radius = 10f, center = Offset(centerX, pY - 6f))
            // Blue Jeans
            drawRect(
                color = Color(0xFF1565C0),
                topLeft = Offset(centerX - 10f, pY + 10f),
                size = Size(20f, 16f)
            )

            // Attack Muzzle flash & tracer
            if (isFiring) {
                drawLine(
                    color = Color(0xFFFFF176),
                    start = Offset(centerX + 12f, pY),
                    end = Offset(centerX + 80f, pY - 40f),
                    strokeWidth = 3f
                )
                drawCircle(color = Color(0xFFFF6D00), radius = 12f, center = Offset(centerX + 20f, pY - 8f))
                drawCircle(color = Color(0xFFFFFF00), radius = 6f, center = Offset(centerX + 20f, pY - 8f))
            }
        }
    }
}

/**
 * Authentic SA-MP HUD Overlay (Health, Armor, Money, Wanted Stars, Radar, NetStats)
 */
@Composable
fun GameHudOverlay(
    serverName: String,
    serverIp: String,
    serverPort: Int,
    playerNickname: String,
    health: Int,
    armor: Int,
    cash: Int,
    weapon: GameWeapon,
    ammo: Int,
    stamina: Float,
    isVoiceActive: Boolean,
    inVehicle: Boolean,
    vehicleName: String,
    vehicleSpeed: Float,
    vehicleHealth: Int,
    radioStation: String,
    onCycleWeapon: () -> Unit,
    onToggleVoice: () -> Unit,
    onOpenPauseMenu: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // TOP LEFT: Server & NetStats + Voice Chat
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 14.dp, top = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$serverName ($serverIp:$serverPort)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Player: $playerNickname | Ping: 32ms | FPS: 60 | Loss: 0.0%",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFB0BEC5)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Push-To-Talk Voice button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isVoiceActive) Color(0xFF00E676) else Color(0x44FFFFFF))
                    .clickable { onToggleVoice() }
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = if (isVoiceActive) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // TOP RIGHT: Authentic GTA SA Weapon, Health, Armor, Money HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 14.dp, top = 26.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pause Menu button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x66000000))
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        .clickable { onOpenPauseMenu() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MENU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Weapon Icon & Ammo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xCC0D0D14))
                        .border(1.5.dp, CyberCyan, RoundedCornerShape(8.dp))
                        .clickable { onCycleWeapon() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = weapon.symbol, fontSize = 20.sp)
                        Text(
                            text = if (weapon.maxAmmo > 0) "$ammo" else "∞",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Health Bar (Classic Red/Coral)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("HP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                Spacer(modifier = Modifier.width(4.dp))
                LinearProgressIndicator(
                    progress = { health / 100f },
                    modifier = Modifier
                        .width(90.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFFF1744),
                    trackColor = Color(0x66400000)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Armor Bar (Classic White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("AP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                LinearProgressIndicator(
                    progress = { armor / 100f },
                    modifier = Modifier
                        .width(90.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color(0x66444444)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Money Counter ($00,025,480) in green
            Text(
                text = String.format("$%08d", cash),
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF00E676),
                letterSpacing = 1.sp
            )
        }

        // BOTTOM LEFT: Authentic Radar / Minimap
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xCC090C10))
                .border(2.5.dp, Color(0xFF37474F), CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                // Radar grid lines
                drawLine(Color(0x3300E5FF), Offset(0f, center.y), Offset(size.width, center.y), 1.5f)
                drawLine(Color(0x3300E5FF), Offset(center.x, 0f), Offset(center.x, size.height), 1.5f)
                drawCircle(Color(0x2200E5FF), radius = 24f, center = center, style = Stroke(1.5f))

                // North arrow
                drawLine(Color(0xFFFF1744), Offset(center.x, 6f), Offset(center.x, 16f), 3f)

                // Player arrow in center
                val arrowPath = Path().apply {
                    moveTo(center.x, center.y - 8f)
                    lineTo(center.x + 6f, center.y + 6f)
                    lineTo(center.x, center.y + 3f)
                    lineTo(center.x - 6f, center.y + 6f)
                    close()
                }
                drawPath(arrowPath, Color(0xFF00E5FF), style = Fill)
            }
            Text(
                text = "COMMERCE",
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            )
        }

        // VEHICLE DASHBOARD & SPEEDOMETER (Visible when driving)
        if (inVehicle) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xDD0D0D14)),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(FlameOrange, CrimsonRed)))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.0f", vehicleSpeed),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = FlameOrange
                        )
                        Text("KM/H", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(text = vehicleName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "📻 $radioStation", fontSize = 8.sp, color = CyberCyan)
                    }
                }
            }
        }
    }
}

/**
 * On-Screen Translucent Chat Box (8 lines)
 */
@Composable
fun GameChatBox(
    modifier: Modifier = Modifier,
    messages: List<SampChatMessage>,
    onOpenInput: () -> Unit
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x990A0A10))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💬 SA-MP CHAT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberGold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x44FFFFFF))
                    .clickable { onOpenInput() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("TAP 'T' TO CHAT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.height(110.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(messages.takeLast(8)) { msg ->
                Text(
                    text = msg.text,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    color = msg.color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Modern On-Screen Touch Controls (Virtual Thumbstick, Attack, Sprint, Jump, Vehicle)
 */
@Composable
fun GameTouchControls(
    inVehicle: Boolean,
    stamina: Float,
    isSprinting: Boolean,
    onJoystickMoved: (Float, Float, Boolean) -> Unit,
    onAttack: () -> Unit,
    onAttackRelease: () -> Unit,
    onSprintToggle: () -> Unit,
    onJump: () -> Unit,
    onVehicleToggle: () -> Unit,
    onHorn: () -> Unit,
    onAccelerate: () -> Unit,
    onBrake: () -> Unit,
    onOpenCommands: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // LEFT: Floating Virtual Analog Joystick
        var thumbOffsetX by remember { mutableFloatStateOf(0f) }
        var thumbOffsetY by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 120.dp, bottom = 20.dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .border(2.dp, Color(0x66FFFFFF), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val dx = (offset.x - 55f).coerceIn(-40f, 40f)
                            val dy = (offset.y - 55f).coerceIn(-40f, 40f)
                            thumbOffsetX = dx
                            thumbOffsetY = dy
                            onJoystickMoved(dx / 40f, dy / 40f, true)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            thumbOffsetX = (thumbOffsetX + dragAmount.x).coerceIn(-40f, 40f)
                            thumbOffsetY = (thumbOffsetY + dragAmount.y).coerceIn(-40f, 40f)
                            onJoystickMoved(thumbOffsetX / 40f, thumbOffsetY / 40f, true)
                        },
                        onDragEnd = {
                            thumbOffsetX = 0f
                            thumbOffsetY = 0f
                            onJoystickMoved(0f, 0f, false)
                        },
                        onDragCancel = {
                            thumbOffsetX = 0f
                            thumbOffsetY = 0f
                            onJoystickMoved(0f, 0f, false)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Knob
            Box(
                modifier = Modifier
                    .offset { IntOffset(thumbOffsetX.toInt(), thumbOffsetY.toInt()) }
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC00E5FF))
                    .border(2.dp, Color.White, CircleShape)
            )
        }

        // RIGHT: Action Controls Pad
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Vehicle button & Dialog Command Menu
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Command Menu (Spawn / Teleport / Stats)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xDD0D0D14))
                        .border(1.5.dp, CyberGold, CircleShape)
                        .clickable { onOpenCommands() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡ /V", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberGold)
                }

                // Enter / Exit Vehicle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (inVehicle) FlameOrange else Color(0xDD0D0D14))
                        .border(1.5.dp, FlameOrange, CircleShape)
                        .clickable { onVehicleToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Car",
                        tint = if (inVehicle) Color.Black else FlameOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Row 2: Driving or On-Foot specific buttons
            if (inVehicle) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Horn
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD0D0D14))
                            .border(1.5.dp, Color(0xFFFFF59D), CircleShape)
                            .clickable { onHorn() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Horn", tint = Color(0xFFFFF59D), modifier = Modifier.size(22.dp))
                    }

                    // Brake Pedal
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD990000))
                            .border(2.dp, CrimsonRed, CircleShape)
                            .clickable { onBrake() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("BRAKE", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }

                    // Gas Pedal
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD008040))
                            .border(2.dp, Color(0xFF00E676), CircleShape)
                            .clickable { onAccelerate() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GAS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Sprint Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSprinting) CrimsonRed else Color(0xDD0D0D14))
                            .border(1.5.dp, CrimsonRed, CircleShape)
                            .clickable { onSprintToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FlashOn, contentDescription = "Sprint", tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(String.format("%.0f%%", stamina), fontSize = 7.sp, color = Color.White)
                        }
                    }

                    // Jump Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD0D0D14))
                            .border(1.5.dp, CyberCyan, CircleShape)
                            .clickable { onJump() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("JUMP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }

                    // Attack / Punch / Fire Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xDD990000))
                            .border(2.dp, CrimsonRed, CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { onAttack() },
                                    onDrag = { change, _ -> change.consume() },
                                    onDragEnd = { onAttackRelease() },
                                    onDragCancel = { onAttackRelease() }
                                )
                            }
                            .clickable {
                                onAttack()
                                onAttackRelease()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("FIRE", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Connecting Overlay
 */
@Composable
fun GameConnectingOverlay(
    phase: GameConnectPhase,
    status: String,
    serverName: String,
    serverIp: String,
    serverPort: Int,
    nickname: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "SA-MP MOBILE CLIENT ENGINE",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberGold,
                letterSpacing = 1.sp
            )

            Text(
                text = serverName,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "$serverIp:$serverPort",
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = CyberCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CrimsonRed,
                trackColor = Color(0xFF222230)
            )

            Text(
                text = status,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Connecting Player: $nickname",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}
