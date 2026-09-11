package com.example.data

data class SampServer(
    val id: String,
    val name: String,
    val ip: String,
    val port: Int,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val ping: Int,
    val gamemode: String,
    val map: String,
    val isFavorite: Boolean = false,
    val isVerified: Boolean = false,
    val region: String = "Global"
)

data class SampItem(
    val id: Int,
    val name: String,
    val category: String,
    val description: String
)

data class SampCommand(
    val command: String,
    val description: String,
    val syntax: String
)

object SampSampleData {
    val defaultServers = listOf(
        SampServer(
            id = "s1",
            name = "⚡ Los Santos Roleplay [SIN/ENG]",
            ip = "194.87.147.65",
            port = 7777,
            currentPlayers = 894,
            maxPlayers = 1000,
            ping = 32,
            gamemode = "LSRP v4.2 - Sri Lanka",
            map = "San Andreas",
            isFavorite = true,
            isVerified = true,
            region = "Asia / SL"
        ),
        SampServer(
            id = "s2",
            name = "🔥 Sri Lanka Gang Wars & DM",
            ip = "51.89.152.12",
            port = 7777,
            currentPlayers = 412,
            maxPlayers = 500,
            ping = 45,
            gamemode = "TDM / Turf War v2.0",
            map = "Los Santos & LV",
            isFavorite = true,
            isVerified = true,
            region = "Asia / SL"
        ),
        SampServer(
            id = "s3",
            name = "🏁 Las Venturas Drift & Stunt Kings",
            ip = "185.125.230.90",
            port = 7777,
            currentPlayers = 278,
            maxPlayers = 400,
            ping = 58,
            gamemode = "Drift / Mega Ramps",
            map = "Las Venturas",
            isFavorite = false,
            isVerified = true,
            region = "Global"
        ),
        SampServer(
            id = "s4",
            name = "👮 San Fierro Police vs Robbers",
            ip = "176.31.229.18",
            port = 7777,
            currentPlayers = 635,
            maxPlayers = 800,
            ping = 64,
            gamemode = "CnR Tactical v6.1",
            map = "San Fierro",
            isFavorite = false,
            isVerified = true,
            region = "Global"
        ),
        SampServer(
            id = "s5",
            name = "🧟 Red County Zombie Apocalypse",
            ip = "144.76.118.43",
            port = 7777,
            currentPlayers = 189,
            maxPlayers = 300,
            ping = 78,
            gamemode = "Survival DayZ Mod",
            map = "Red County Woods",
            isFavorite = false,
            isVerified = false,
            region = "EU"
        )
    )

    val popularVehicles = listOf(
        SampItem(411, "Infernus", "Super Car", "Top speed: 221 km/h - Fastest street car"),
        SampItem(541, "Bullet", "Super Car", "Top speed: 203 km/h - Excellent handling"),
        SampItem(415, "Cheetah", "Super Car", "Top speed: 192 km/h - High traction"),
        SampItem(522, "NRG-500", "Bike", "Top speed: 219 km/h - Racing superbike"),
        SampItem(520, "Hydra", "Aircraft", "Military fighter jet with heat-seeking missiles"),
        SampItem(425, "Hunter", "Aircraft", "Military attack helicopter with miniguns"),
        SampItem(560, "Sultan", "Tuner Car", "Top 4-door street tuner, custom mods"),
        SampItem(562, "Elegy", "Drift Car", "The premier drift vehicle in San Andreas"),
        SampItem(432, "Rhino", "Heavy", "Heavy armored military battle tank")
    )

    val popularWeapons = listOf(
        SampItem(24, "Desert Eagle", "Handgun", "Damage: 46 HP - Heavy stopping power"),
        SampItem(31, "M4 Carbine", "Assault Rifle", "Damage: 30 HP - Rapid fire accuracy"),
        SampItem(30, "AK-47", "Assault Rifle", "Damage: 30 HP - Standard street rifle"),
        SampItem(34, "Sniper Rifle", "Long Range", "Damage: 82 HP - High-zoom headshot weapon"),
        SampItem(25, "Shotgun", "Shotgun", "Damage: 45 HP - Deadly in close quarters"),
        SampItem(29, "MP5", "SMG", "Damage: 25 HP - Drive-by capable submachine gun"),
        SampItem(8, "Katana", "Melee", "Damage: 20 HP - Traditional samurai blade")
    )

    val commonCommands = listOf(
        SampCommand("/help", "View all server help and commands menu", "/help [topic]"),
        SampCommand("/stats", "Check your player statistics, money, and level", "/stats"),
        SampCommand("/me", "Perform a roleplay action in third person", "/me [action text]"),
        SampCommand("/do", "Describe an environmental condition or state", "/do [description]"),
        SampCommand("/b", "Speak in local out-of-character (OOC) chat", "/b [message]"),
        SampCommand("/pm", "Send private message to another player ID", "/pm [player_id] [text]"),
        SampCommand("/car", "Spawn or manage your personal vehicle", "/car [lock/park/engine]"),
        SampCommand("/pay", "Transfer cash to nearby player", "/pay [player_id] [amount]")
    )
}
