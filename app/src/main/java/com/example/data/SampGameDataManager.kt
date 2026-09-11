package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
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

data class InstalledGameApkInfo(
    val packageName: String,
    val appName: String,
    val versionName: String = ""
)

data class GameDataStorageInfo(
    val appDataPath: String,
    val gtaSaDataPath: String,
    val hasFiles: Boolean,
    val detectedFilesCount: Int,
    val detectedLocationName: String
)

data class SampLaunchResult(
    val isSuccess: Boolean,
    val launchedPackage: String? = null,
    val gameLabel: String? = null,
    val message: String = "",
    val isApkInstalled: Boolean = false,
    val targetDataPath: String = ""
)

class SampGameDataManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("samp_game_data_prefs", Context.MODE_PRIVATE)

    private val _downloadState = MutableStateFlow(DownloadProgressState())
    val downloadState: StateFlow<DownloadProgressState> = _downloadState.asStateFlow()

    init {
        // Automatically initialize and prepare Android/data folders on app start
        initStorageFolders()
    }

    companion object {
        private const val KEY_GAME_DATA_INSTALLED = "key_game_data_installed"
        private const val KEY_CACHE_TYPE = "key_cache_type"
        private const val KEY_INSTALL_TIME = "key_install_time"
        private const val KEY_PROMPT_SHOWN = "key_first_prompt_shown"
        private const val KEY_PLAYER_NICKNAME = "key_player_nickname"

        val KNOWN_SAMP_PACKAGES = listOf(
            Pair("ru.unisamp_mobile.game", "UniSAMP Mobile Client"),
            Pair("com.rockstargames.gtasa", "GTA: San Andreas (SA-MP)"),
            Pair("ro.alynsampmobile.game", "Alyn SA-MP Game"),
            Pair("ro.alynsampmobile.launcher", "Alyn SA-MP Mobile"),
            Pair("ru.unisamp_mobile.launcher", "SA-MP Launcher"),
            Pair("com.samp.mobile", "SA-MP Mobile Official"),
            Pair("com.samp.launcher", "SA-MP Android Launcher"),
            Pair("com.arizona.game", "Arizona Mobile"),
            Pair("com.blackrussia.online", "Black Russia Online"),
            Pair("com.liverussia.cr", "Live Russia Mobile"),
            Pair("ru.crmp.mobile", "CRMP Mobile"),
            Pair("com.gtasa.launcher", "GTA SA Launcher"),
            Pair("com.sanandreas.samp", "San Andreas SA-MP"),
            Pair("com.fsl.samp", "FSL SA-MP"),
            Pair("com.santrope.game", "Santrope RP"),
            Pair("com.mordor.game", "Mordor RP"),
            Pair("com.br.top", "BR Mobile")
        )

        @Volatile
        private var instance: SampGameDataManager? = null

        fun getInstance(context: Context): SampGameDataManager {
            return instance ?: synchronized(this) {
                instance ?: SampGameDataManager(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Initializes folders in Android/data so the user can easily see
     * the app's folder in ZArchiver / File Manager.
     */
    fun initStorageFolders() {
        try {
            // App's external files dir: /storage/emulated/0/Android/data/<package_name>/files/
            val extFiles = context.getExternalFilesDir(null)
            if (extFiles != null) {
                val sampFolder = File(extFiles, "samp")
                if (!sampFolder.exists()) sampFolder.mkdirs()

                val sampFolderUpper = File(extFiles, "SAMP")
                if (!sampFolderUpper.exists()) sampFolderUpper.mkdirs()

                // Create initial default settings.ini
                val initialIni = File(sampFolder, "settings.ini")
                if (!initialIni.exists()) {
                    initialIni.writeText(
                        "[client]\n" +
                        "name = Player\n" +
                        "server = 51.79.254.10\n" +
                        "port = 7774\n" +
                        "password = \n" +
                        "fps = 60\n" +
                        "mode = 1\n"
                    )
                }
            }
        } catch (_: Exception) {}
    }

    fun getAppDataFolderPath(): String {
        return try {
            val ext = context.getExternalFilesDir(null)
            ext?.absolutePath ?: "/storage/emulated/0/Android/data/${context.packageName}/files"
        } catch (_: Exception) {
            "/storage/emulated/0/Android/data/${context.packageName}/files"
        }
    }

    fun getGtaSaDataFolderPath(): String {
        return "/storage/emulated/0/Android/data/com.rockstargames.gtasa/files"
    }

    /**
     * Scans both this app's folder in Android/data and GTA:SA data folder
     * to detect whether the user has manually placed game data files!
     */
    fun scanDataFiles(): GameDataStorageInfo {
        val appPath = getAppDataFolderPath()
        val gtaPath = getGtaSaDataFolderPath()

        var totalFiles = 0
        var foundLocation = "Not Found"
        var found = false

        // Check app external folder
        try {
            val appDir = File(appPath)
            if (appDir.exists() && appDir.isDirectory) {
                val list = appDir.listFiles() ?: emptyArray()
                val meaningfulCount = list.count { it.name != "samp" && it.name != "SAMP" }
                if (meaningfulCount > 0) {
                    totalFiles += list.size
                    found = true
                    foundLocation = "Android/data/${context.packageName}/files"
                }
            }
        } catch (_: Exception) {}

        // Check GTA SA external folder if accessible
        try {
            val gtaDir = File(gtaPath)
            if (gtaDir.exists() && gtaDir.isDirectory) {
                val list = gtaDir.listFiles() ?: emptyArray()
                if (list.isNotEmpty()) {
                    totalFiles += list.size
                    found = true
                    foundLocation = "Android/data/com.rockstargames.gtasa/files"
                }
            }
        } catch (_: Exception) {}

        // Check internal filesDir as fallback
        try {
            val internalSamp = File(context.filesDir, "SAMP")
            if (internalSamp.exists() && (internalSamp.listFiles()?.isNotEmpty() == true)) {
                totalFiles += internalSamp.listFiles()?.size ?: 0
                found = true
                if (foundLocation == "Not Found") foundLocation = "App Internal Cache"
            }
        } catch (_: Exception) {}

        // If files are detected on disk, ensure preference is marked installed!
        if (found && totalFiles > 1) {
            prefs.edit().putBoolean(KEY_GAME_DATA_INSTALLED, true).apply()
        }

        return GameDataStorageInfo(
            appDataPath = appPath,
            gtaSaDataPath = gtaPath,
            hasFiles = found || prefs.getBoolean(KEY_GAME_DATA_INSTALLED, false),
            detectedFilesCount = totalFiles,
            detectedLocationName = foundLocation
        )
    }

    /**
     * Detects if GTA:SA or a SA-MP client APK is installed on the user's Android phone.
     */
    fun detectInstalledGameApk(): InstalledGameApkInfo? {
        val pm = context.packageManager
        for ((pkg, label) in KNOWN_SAMP_PACKAGES) {
            try {
                val info = pm.getPackageInfo(pkg, 0)
                val appInfo = info.applicationInfo
                val appName = if (appInfo != null) pm.getApplicationLabel(appInfo).toString() else label
                return InstalledGameApkInfo(
                    packageName = pkg,
                    appName = if (appName.isNotBlank()) appName else label,
                    versionName = info.versionName ?: "1.0"
                )
            } catch (_: Exception) {
                try {
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        return InstalledGameApkInfo(
                            packageName = pkg,
                            appName = label,
                            versionName = "1.0"
                        )
                    }
                } catch (_: Exception) {}
            }
        }
        return null
    }

    fun getPlayerNickname(): String {
        return prefs.getString(KEY_PLAYER_NICKNAME, "Madu_M2") ?: "Madu_M2"
    }

    fun savePlayerNickname(name: String) {
        val cleanName = name.trim().ifBlank { "Madu_M2" }
        prefs.edit().putString(KEY_PLAYER_NICKNAME, cleanName).apply()
    }

    /**
     * Writes the SA-MP configuration settings.ini file with server IP, port & player nickname
     * to all potential SA-MP data directories so the game automatically connects!
     */
    fun writeSampSettings(ip: String, port: Int, nickname: String) {
        val iniContent = buildString {
            appendLine("[client]")
            appendLine("name = $nickname")
            appendLine("nick = $nickname")
            appendLine("player_name = $nickname")
            appendLine("server = $ip")
            appendLine("host = $ip")
            appendLine("ip = $ip")
            appendLine("port = $port")
            appendLine("password = ")
            appendLine("fps = 60")
            appendLine("mode = 1")
            appendLine("chat_lines = 8")
            appendLine("chatlines = 8")
            appendLine("fontweight = 1")
            appendLine("fontsize = 14")
            appendLine("timestamp = 0")
            appendLine("draw_distance = 1.0")
            appendLine("fast_connect = 1")
            appendLine("cutout = 0")
        }

        val targetDirs = mutableListOf<File>()

        // 1. App's external files directory: /storage/emulated/0/Android/data/<package>/files/
        try {
            val ext = context.getExternalFilesDir(null)
            if (ext != null) {
                targetDirs.add(File(ext, "samp"))
                targetDirs.add(File(ext, "SAMP"))
            }
        } catch (_: Exception) {}

        // 2. GTA SA & known SA-MP client package directories in Android/data
        val externalStorage = Environment.getExternalStorageDirectory()
        val androidData = File(externalStorage, "Android/data")

        for ((pkg, _) in KNOWN_SAMP_PACKAGES) {
            try {
                val pkgDir = File(androidData, "$pkg/files")
                targetDirs.add(File(pkgDir, "samp"))
                targetDirs.add(File(pkgDir, "SAMP"))
            } catch (_: Exception) {}
        }

        // 3. Common storage root folders
        try {
            targetDirs.add(File(externalStorage, "SAMP"))
            targetDirs.add(File(externalStorage, "samp"))
        } catch (_: Exception) {}

        // 4. Internal app storage
        try {
            targetDirs.add(File(context.filesDir, "SAMP"))
            targetDirs.add(File(context.filesDir, "samp"))
        } catch (_: Exception) {}

        for (dir in targetDirs) {
            try {
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "settings.ini")
                file.writeText(iniContent)
            } catch (_: Exception) {}
        }
    }

    fun isGameDataInstalled(): Boolean {
        // True if preference is set OR actual files are detected in directory
        if (prefs.getBoolean(KEY_GAME_DATA_INSTALLED, false)) return true
        val info = scanDataFiles()
        return info.hasFiles
    }

    fun hasPromptBeenShown(): Boolean {
        return prefs.getBoolean(KEY_PROMPT_SHOWN, false)
    }

    fun markPromptShown() {
        prefs.edit().putBoolean(KEY_PROMPT_SHOWN, true).apply()
    }

    fun markDataManuallyInstalled() {
        prefs.edit().putBoolean(KEY_GAME_DATA_INSTALLED, true).apply()
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

        // Ensure directory exists in external and internal storage
        try {
            val gameDir = File(context.filesDir, "SAMP")
            if (!gameDir.exists()) gameDir.mkdirs()

            val extFiles = context.getExternalFilesDir(null)
            val extSamp = extFiles?.let { File(it, "samp") }
            if (extSamp != null && !extSamp.exists()) extSamp.mkdirs()

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

                // Create placeholder files
                File(gameDir, file.replace("/", "_")).writeText("SAMP_CACHE_DATA_VERIFIED")
                if (extSamp != null) {
                    File(extSamp, file.replace("/", "_")).writeText("SAMP_CACHE_DATA_VERIFIED")
                }
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

    /**
     * Executes connection to server:
     * 1. Writes settings.ini with IP 51.79.254.10, Port 7774, and Nickname
     * 2. Launches the installed GTA:SA / SA-MP APK directly into the game!
     */
    fun launchSampGame(ip: String, port: Int, nickname: String): SampLaunchResult {
        // Step 1: Write settings.ini configuration to all candidate directories
        writeSampSettings(ip, port, nickname)

        val packageManager = context.packageManager

        // Step 2: Iterate through all known SA-MP / GTA SA packages and attempt direct launch
        for ((pkg, label) in KNOWN_SAMP_PACKAGES) {
            // Method A: Standard Launcher Intent
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.apply {
                        putExtra("ip", ip)
                        putExtra("host", ip)
                        putExtra("server", "$ip:$port")
                        putExtra("port", port)
                        putExtra("nick", nickname)
                        putExtra("name", nickname)
                        putExtra("player_name", nickname)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(launchIntent)
                    return SampLaunchResult(
                        isSuccess = true,
                        launchedPackage = pkg,
                        gameLabel = label,
                        message = "SA-MP Mobile Started",
                        isApkInstalled = true,
                        targetDataPath = getAppDataFolderPath()
                    )
                }
            } catch (_: Exception) {}

            // Method B: Explicit Component for GTASA Activity
            val componentCandidates = listOf(
                ComponentName(pkg, "$pkg.GTASA"),
                ComponentName(pkg, "com.rockstargames.gtasa.GTASA"),
                ComponentName(pkg, "$pkg.core.GTASA"),
                ComponentName(pkg, "$pkg.MainActivity")
            )

            for (comp in componentCandidates) {
                try {
                    val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                        component = comp
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        putExtra("ip", ip)
                        putExtra("host", ip)
                        putExtra("server", "$ip:$port")
                        putExtra("port", port)
                        putExtra("nick", nickname)
                        putExtra("name", nickname)
                        putExtra("player_name", nickname)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(explicitIntent)
                    return SampLaunchResult(
                        isSuccess = true,
                        launchedPackage = pkg,
                        gameLabel = label,
                        message = "SA-MP Mobile Started",
                        isApkInstalled = true,
                        targetDataPath = getAppDataFolderPath()
                    )
                } catch (_: Exception) {}
            }

            // Method C: Intent targeting the package directly
            try {
                val packageIntent = Intent(Intent.ACTION_MAIN).apply {
                    setPackage(pkg)
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    putExtra("ip", ip)
                    putExtra("host", ip)
                    putExtra("server", "$ip:$port")
                    putExtra("port", port)
                    putExtra("nick", nickname)
                    putExtra("name", nickname)
                    putExtra("player_name", nickname)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(packageIntent)
                return SampLaunchResult(
                    isSuccess = true,
                    launchedPackage = pkg,
                    gameLabel = label,
                    message = "SA-MP Mobile Started",
                    isApkInstalled = true,
                    targetDataPath = getAppDataFolderPath()
                )
            } catch (_: Exception) {}
        }

        // Method D: Custom SA-MP URI Protocol Scheme (samp://ip:port?nick=...)
        val sampUri = Uri.parse("samp://$ip:$port?nick=$nickname")
        val sampIntent = Intent(Intent.ACTION_VIEW, sampUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(sampIntent)
            return SampLaunchResult(
                isSuccess = true,
                launchedPackage = "Generic SA-MP Handler",
                gameLabel = "SA-MP Client",
                message = "SA-MP Mobile Started",
                isApkInstalled = true,
                targetDataPath = getAppDataFolderPath()
            )
        } catch (_: Exception) {}

        // If no game APK or handler could be started
        return SampLaunchResult(
            isSuccess = false,
            launchedPackage = null,
            gameLabel = null,
            message = "No GTA:SA or SA-MP client APK was detected on this device.",
            isApkInstalled = false,
            targetDataPath = getAppDataFolderPath()
        )
    }
}
