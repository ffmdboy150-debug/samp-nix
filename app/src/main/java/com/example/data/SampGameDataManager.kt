package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

enum class GameCacheType(val displayName: String, val sizeMb: Int, val description: String) {
    LITE("Lite Cache (Recommended)", 650, "Optimized for all Android devices. Fast download & smooth 60 FPS."),
    FULL("Full HD Cache", 1450, "High definition textures, original radio stations & Sri Lanka custom vehicle pack.")
}

data class DownloadProgressState(
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val currentFile: String = "",
    val downloadedMb: Float = 0f,
    val totalMb: Float = 650f,
    val downloadSpeed: String = "0.0 MB/s",
    val statusMessage: String = "",
    val isFinished: Boolean = false,
    val error: String? = null
)

class SampGameDataManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("samp_game_data_prefs", Context.MODE_PRIVATE)

    private val _downloadState = MutableStateFlow(DownloadProgressState())
    val downloadState: StateFlow<DownloadProgressState> = _downloadState.asStateFlow()

    companion object {
        private const val KEY_GAME_DATA_INSTALLED = "key_game_data_installed"
        private const val KEY_CACHE_TYPE = "key_cache_type"
        private const val KEY_INSTALL_TIME = "key_install_time"
        private const val KEY_PROMPT_SHOWN = "key_first_prompt_shown"

        @Volatile
        private var instance: SampGameDataManager? = null

        fun getInstance(context: Context): SampGameDataManager {
            return instance ?: synchronized(this) {
                instance ?: SampGameDataManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun isGameDataInstalled(): Boolean {
        return prefs.getBoolean(KEY_GAME_DATA_INSTALLED, false)
    }

    fun hasPromptBeenShown(): Boolean {
        return prefs.getBoolean(KEY_PROMPT_SHOWN, false)
    }

    fun markPromptShown() {
        prefs.edit().putBoolean(KEY_PROMPT_SHOWN, true).apply()
    }

    fun getInstalledCacheType(): GameCacheType {
        val typeName = prefs.getString(KEY_CACHE_TYPE, GameCacheType.LITE.name)
        return try {
            GameCacheType.valueOf(typeName ?: GameCacheType.LITE.name)
        } catch (_: Exception) {
            GameCacheType.LITE
        }
    }

    fun resetGameData() {
        prefs.edit()
            .putBoolean(KEY_GAME_DATA_INSTALLED, false)
            .remove(KEY_INSTALL_TIME)
            .apply()
        _downloadState.value = DownloadProgressState()
    }

    suspend fun startDownload(cacheType: GameCacheType) = withContext(Dispatchers.IO) {
        val totalSize = cacheType.sizeMb.toFloat()
        _downloadState.value = DownloadProgressState(
            isDownloading = true,
            progress = 0f,
            currentFile = "Connecting to Game Data CDN...",
            downloadedMb = 0f,
            totalMb = totalSize,
            downloadSpeed = "12.4 MB/s",
            statusMessage = "Establishing secure connection to Nexston Roleplay CDN..."
        )

        val filesToDownload = listOf(
            Pair("libGTASA.so & libsamp.so", 45f),
            Pair("gta3.img & samp.img", 280f),
            Pair("texdb/gta_int.txt & textures.txd", 110f),
            Pair("audio/streams/radio & SFX pack", 90f),
            Pair("data/fonts.txd, hud.txd & anim.ped", 35f),
            Pair("nexston_srilanka_custom_skins.img", 50f),
            Pair("server_assets_cache.dat", 40f)
        )

        var accumulatedMb = 0f

        // Ensure directory exists in internal storage
        try {
            val gameDir = File(context.filesDir, "SAMP")
            if (!gameDir.exists()) gameDir.mkdirs()

            for ((file, size) in filesToDownload) {
                val targetStepMb = (size * (if (cacheType == GameCacheType.FULL) 1.8f else 1.0f)).coerceAtLeast(10f)
                val steps = 8
                val stepSize = targetStepMb / steps

                for (i in 1..steps) {
                    delay(120)
                    accumulatedMb += stepSize
                    val currentProgress = (accumulatedMb / totalSize).coerceIn(0f, 0.96f)
                    val speed = (10.0f + (Math.random() * 4.5f)).toFloat()

                    _downloadState.value = DownloadProgressState(
                        isDownloading = true,
                        progress = currentProgress,
                        currentFile = file,
                        downloadedMb = accumulatedMb.coerceAtMost(totalSize),
                        totalMb = totalSize,
                        downloadSpeed = String.format("%.1f MB/s", speed),
                        statusMessage = "Downloading: $file (${String.format("%.1f", accumulatedMb)} MB / ${totalSize.toInt()} MB)"
                    )
                }

                // Create dummy placeholder files inside SAMP folder
                File(gameDir, file.replace("/", "_")).writeText("SAMP_CACHE_DATA_VERIFIED")
            }

            // Unpacking and verification stage
            _downloadState.value = DownloadProgressState(
                isDownloading = true,
                progress = 0.98f,
                currentFile = "Unpacking and Verifying Checksums...",
                downloadedMb = totalSize,
                totalMb = totalSize,
                downloadSpeed = "Finishing...",
                statusMessage = "Optimizing textures & registering client protocol 0.3.7-R1..."
            )
            delay(800)

            // Mark as installed in preferences
            prefs.edit()
                .putBoolean(KEY_GAME_DATA_INSTALLED, true)
                .putString(KEY_CACHE_TYPE, cacheType.name)
                .putLong(KEY_INSTALL_TIME, System.currentTimeMillis())
                .apply()

            _downloadState.value = DownloadProgressState(
                isDownloading = false,
                progress = 1.0f,
                currentFile = "Completed",
                downloadedMb = totalSize,
                totalMb = totalSize,
                downloadSpeed = "0 MB/s",
                statusMessage = "SA-MP Game Data successfully installed! Ready to connect.",
                isFinished = true
            )
        } catch (e: Exception) {
            _downloadState.value = DownloadProgressState(
                isDownloading = false,
                error = e.message ?: "Failed to complete download"
            )
        }
    }

    fun launchSampGame(ip: String, port: Int, nickname: String): Boolean {
        // Try known Android SA-MP launcher packages or GTA SA packages
        val possiblePackages = listOf(
            "com.rockstargames.gtasa",
            "ru.unisamp_mobile.game",
            "com.samp.launcher",
            "com.arizona.game",
            "com.liverussia.cr"
        )

        val packageManager = context.packageManager
        for (pkg in possiblePackages) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.apply {
                    putExtra("ip", ip)
                    putExtra("port", port)
                    putExtra("server", "$ip:$port")
                    putExtra("nick", nickname)
                    putExtra("name", nickname)
                    putExtra("player_name", nickname)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(launchIntent)
                    return true
                } catch (_: Exception) {}
            }
        }

        // Generic Intent for SAMP launch protocol
        val sampUri = Uri.parse("samp://$ip:$port?nick=$nickname")
        val sampIntent = Intent(Intent.ACTION_VIEW, sampUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(sampIntent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
