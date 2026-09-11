package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.data.SampGameDataManager
import com.example.ui.GameDataDownloaderDialog
import com.example.ui.GameDataPromptDialog
import com.example.ui.GameDatabaseScreen
import com.example.ui.LoadingScreen
import com.example.ui.ServerListScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SampMainApp()
            }
        }
    }
}

@Composable
fun SampMainApp() {
    val context = LocalContext.current
    val gameDataManager = remember { SampGameDataManager.getInstance(context) }

    var isLoadingScreen by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var playerNickname by remember { mutableStateOf("Player_Carl") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDataPromptDialog by remember { mutableStateOf(false) }
    var showDataDownloaderDialog by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = isLoadingScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "AppScreenTransition"
    ) { loading ->
        if (loading) {
            // Fullscreen Game Loading screen (0% to 100%) with custom Oni Mask background
            LoadingScreen(
                onFinished = {
                    isLoadingScreen = false
                    // Once loading finishes, ask user to download SAMP game data if not already installed
                    if (!gameDataManager.isGameDataInstalled()) {
                        showDataPromptDialog = true
                    }
                }
            )
        } else {
            // Main SAMP Game Hub & Launcher
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color(0xFF0A0A0E),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    GameHeader(
                        playerNickname = playerNickname,
                        onReplayIntro = { isLoadingScreen = true }
                    )
                },
                bottomBar = {
                    GameBottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Subtle themed background wallpaper
                    Image(
                        painter = painterResource(id = R.drawable.img_bg_character),
                        contentDescription = "Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.22f
                    )

                    // Dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xEE0A0A0E),
                                        Color(0xF50D0D14),
                                        Color(0xFA0A0A0E)
                                    )
                                )
                            )
                    )

                    // Content per tab
                    when (selectedTab) {
                        0 -> ServerListScreen(
                            playerNickname = playerNickname,
                            gameDataManager = gameDataManager,
                            onRequestDownloadData = { showDataDownloaderDialog = true },
                            onLaunchGame = { server ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Connecting to ${server.name}...")
                                }
                            }
                        )
                        1 -> GameDatabaseScreen()
                        2 -> SettingsScreen(
                            currentNickname = playerNickname,
                            gameDataManager = gameDataManager,
                            onDownloadData = { showDataDownloaderDialog = true },
                            onSaveNickname = { playerNickname = it },
                            onReplayLoading = { isLoadingScreen = true }
                        )
                    }

                    // Automatic Game Data Prompt Dialog
                    if (showDataPromptDialog) {
                        GameDataPromptDialog(
                            onAccept = {
                                showDataPromptDialog = false
                                showDataDownloaderDialog = true
                            },
                            onDismiss = {
                                showDataPromptDialog = false
                                gameDataManager.markPromptShown()
                            }
                        )
                    }

                    // Interactive Game Data Downloader Dialog
                    if (showDataDownloaderDialog) {
                        GameDataDownloaderDialog(
                            gameDataManager = gameDataManager,
                            onDismiss = { showDataDownloaderDialog = false },
                            onDownloadFinished = {
                                showDataDownloaderDialog = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("SA-MP Game Data verified! You can now enter the server.")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameHeader(
    playerNickname: String,
    onReplayIntro: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color(0xFF0F0F16))
            .border(1.dp, CardBorder)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("game_header")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Profile info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(DarkCrimson, CrimsonRed))
                        )
                        .border(1.5.dp, FlameOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_launcher_foreground),
                        contentDescription = "Avatar",
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = playerNickname,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                    }

                    Text(
                        text = "51.79.254.10:7774 • NEXSTON RP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Quick Replay Loading Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onReplayIntro,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .size(36.dp)
                        .testTag("header_replay_loading")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Replay Intro",
                        tint = CrimsonRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0D0D14),
        contentColor = TextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier.border(1.dp, CardBorder)
    ) {
        val navItems = listOf(
            Triple(0, "Live Server", Icons.Default.Dns),
            Triple(1, "Database", Icons.Default.Storage),
            Triple(2, "Settings", Icons.Default.Settings)
        )

        navItems.forEach { (index, title, icon) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) CrimsonRed else TextMuted
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) CrimsonRed else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CrimsonRed,
                    unselectedIconColor = TextMuted,
                    indicatorColor = Color(0x33FF1744)
                )
            )
        }
    }
}
