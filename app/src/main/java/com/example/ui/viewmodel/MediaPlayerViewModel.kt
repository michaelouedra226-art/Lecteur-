package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.Equalizer
import android.os.Build
import android.net.Uri
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.database.PremiumDatabase
import com.example.data.entity.MediaEntity
import com.example.data.entity.PlaylistEntity
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PlayerTab {
    VIDEOS, AUDIOS, IPTV, PLAYLISTS, FAVORITES, HISTORY
}

data class IptvChannel(
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String? = null
)

enum class SortType {
    NAME, DATE, SIZE, DURATION
}

enum class PremiumThemeAccent(
    val nameFr: String,
    val primaryCyanHex: Long,
    val secondaryPinkHex: Long
) {
    CYBERPUNK("Cyberpunk Néo", 0xFF00E5FF, 0xFFFF2D55),
    GOLD_LOUNGE("Luxe Doré", 0xFFFFD700, 0xFFFF5722),
    EMERALD_MATRIX("Acid Emerald", 0xFF39FF14, 0xFF0083FF),
    MIDNIGHT_PURPLE("Crépuscule Violet", 0xFFD0BCFF, 0xFF9D4EDD)
}

class MediaPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PremiumDatabase.getDatabase(application)
    private val repository = MediaRepository(application, database.mediaDao())

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
        return false
    }

    // --- IPTV / M3U Professional State and Logic ---
    private val _iptvChannels = MutableStateFlow<List<IptvChannel>>(emptyList())
    val iptvChannels: StateFlow<List<IptvChannel>> = _iptvChannels.asStateFlow()

    private val _selectedIptvGroup = MutableStateFlow("Tout")
    val selectedIptvGroup: StateFlow<String> = _selectedIptvGroup.asStateFlow()

    private val defaultIptvChannels = listOf(
        IptvChannel(
            name = "France 24 (Français Direct)",
            url = "https://static.france24.com/live/F24_FR_LO_HLS/live_tv.m3u8",
            logo = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=100&q=80",
            group = "Actualités"
        ),
        IptvChannel(
            name = "France 24 (English Live)",
            url = "https://static.france24.com/live/F24_EN_LO_HLS/live_tv.m3u8",
            logo = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=100&q=80",
            group = "Actualités"
        ),
        IptvChannel(
            name = "NHK World Japan (English)",
            url = "https://nhkwlive-x.akamaized.net/hls/live/2003450/nhkwlive/hls_1500.m3u8",
            logo = "https://images.unsplash.com/photo-1493612276216-ee3925520721?w=100&q=80",
            group = "Actualités"
        ),
        IptvChannel(
            name = "Red Bull Live Action TV",
            url = "https://rbmn-live.akamaized.net/hls/live/590964/sports/sports_3.m3u8",
            logo = "https://images.unsplash.com/photo-1560089000-7433a4ebbd64?w=100&q=80",
            group = "Sports & Loisirs"
        ),
        IptvChannel(
            name = "Big Buck Bunny Stream",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            logo = "https://images.unsplash.com/photo-1473448912268-2022ce9509d8?w=100&q=80",
            group = "Documentaires"
        )
    )

    fun loadIptvSettings() {
        val prefs = getApplication<Application>().getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
        val m3uRaw = prefs.getString("custom_m3u_raw", "") ?: ""
        if (m3uRaw.isNotEmpty()) {
            _iptvChannels.value = parseM3uContents(m3uRaw)
        } else {
            _iptvChannels.value = defaultIptvChannels
        }
    }

    fun saveCustomIptvM3u(m3uText: String): Boolean {
        val parsed = parseM3uContents(m3uText)
        if (parsed.isEmpty()) return false
        
        val prefs = getApplication<Application>().getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("custom_m3u_raw", m3uText)
            .remove("custom_m3u_url") // Clear URL if text imported manually
            .apply()
        _iptvChannels.value = parsed
        _selectedIptvGroup.value = "Tout"
        return true
    }

    fun saveCustomIptvUrl(url: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 12000
                connection.readTimeout = 18000
                connection.requestMethod = "GET"
                connection.useCaches = false
                
                val code = connection.responseCode
                if (code == 200) {
                    val content = connection.inputStream.bufferedReader().use { it.readText() }
                    val parsed = parseM3uContents(content)
                    if (parsed.isNotEmpty()) {
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            val prefs = getApplication<Application>().getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("custom_m3u_url", url)
                                .putString("custom_m3u_raw", content)
                                .apply()
                            _iptvChannels.value = parsed
                            _selectedIptvGroup.value = "Tout"
                            onSuccess(parsed.size)
                        }
                    } else {
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            onError("Aucun canal de diffusion IPTV valide détecté.")
                        }
                    }
                } else {
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        onError("Erreur serveur HTTP: $code")
                    }
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    val errorMsg = e.localizedMessage ?: "Échec de la connexion réseau"
                    onError("Échec d'importation: $errorMsg")
                }
            }
        }
    }

    fun refreshIptvChannels(onStarted: () -> Unit, onFinished: (Boolean, String) -> Unit) {
        val prefs = getApplication<Application>().getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
        val url = prefs.getString("custom_m3u_url", "") ?: ""
        if (url.isEmpty()) {
            onFinished(false, "Aucune URL distante n'est actuellement configurée. Veuillez d'abord importer via un lien.")
            return
        }
        onStarted()
        saveCustomIptvUrl(
            url = url,
            onSuccess = { count -> onFinished(true, "Mise à jour réussie : $count chaînes chargées !") },
            onError = { err -> onFinished(false, err) }
        )
    }

    fun resetIptvToDefaults() {
        val prefs = getApplication<Application>().getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("custom_m3u_raw")
            .remove("custom_m3u_url")
            .apply()
        _iptvChannels.value = defaultIptvChannels
        _selectedIptvGroup.value = "Tout"
    }

    fun clearIptvChannels() {
        val prefs = getApplication<Application>().getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("custom_m3u_raw")
            .remove("custom_m3u_url")
            .apply()
        _iptvChannels.value = emptyList()
        _selectedIptvGroup.value = "Tout"
    }

    fun selectIptvGroup(group: String) {
        _selectedIptvGroup.value = group
    }

    private fun parseM3uContents(content: String): List<IptvChannel> {
        val channels = mutableListOf<IptvChannel>()
        val lines = content.lines()
        var currentName = ""
        var currentLogo: String? = null
        var currentGroup: String? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                val commaIndex = trimmed.lastIndexOf(',')
                currentName = if (commaIndex != -1) {
                    trimmed.substring(commaIndex + 1).trim()
                } else {
                    ""
                }
                
                val logoRegex = """tvg-logo=["']([^"']+)["']""".toRegex()
                currentLogo = logoRegex.find(trimmed)?.groupValues?.get(1)

                val groupRegex = """group-title=["']([^"']+)["']""".toRegex()
                currentGroup = groupRegex.find(trimmed)?.groupValues?.get(1)
            } else if (trimmed.startsWith("http")) {
                val name = if (currentName.isNotEmpty()) currentName else trimmed.substringAfterLast('/').substringBefore('?')
                channels.add(
                    IptvChannel(
                        name = name,
                        url = trimmed,
                        logo = currentLogo,
                        group = currentGroup ?: "Général"
                    )
                )
                currentName = ""
                currentLogo = null
                currentGroup = null
            }
        }
        return channels
    }

    // Dynamic Theme Customization Accent
    private val _premiumTheme = MutableStateFlow(PremiumThemeAccent.CYBERPUNK)
    val premiumTheme: StateFlow<PremiumThemeAccent> = _premiumTheme.asStateFlow()

    fun changeThemeAccent(accent: PremiumThemeAccent) {
        _premiumTheme.value = accent
    }

    // UI Navigation & Filters
    private val _currentTab = MutableStateFlow(PlayerTab.VIDEOS)
    val currentTab: StateFlow<PlayerTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.NAME)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist: StateFlow<PlaylistEntity?> = _selectedPlaylist.asStateFlow()

    // Database Flows
    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackHistory: StateFlow<List<MediaEntity>> = repository.playbackHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class PlaybackFilters(
        val tab: PlayerTab,
        val query: String,
        val sort: SortType,
        val playlist: PlaylistEntity?
    )

    // Medias list matching current tab with filters applied
    val mediaList: StateFlow<List<MediaEntity>> = combine(
        combine(_currentTab, _searchQuery, _sortType, _selectedPlaylist) { tab, query, sort, playlist ->
            PlaybackFilters(tab, query, sort, playlist)
        },
        repository.allMedia,
        repository.playbackHistory
    ) { filters, all, history ->
        var list = when (filters.tab) {
            PlayerTab.VIDEOS -> all.filter { !it.isAudio }
            PlayerTab.AUDIOS -> all.filter { it.isAudio }
            PlayerTab.IPTV -> emptyList()
            PlayerTab.FAVORITES -> all.filter { it.isFavorite }
            PlayerTab.HISTORY -> history
            PlayerTab.PLAYLISTS -> {
                if (filters.playlist != null) {
                    repository.getPlaylistMedia(filters.playlist.name).first()
                } else {
                    emptyList()
                }
            }
        }

        // Apply Search
        if (filters.query.isNotEmpty()) {
            list = list.filter { it.title.contains(filters.query, ignoreCase = true) }
        }

        // Apply Sorting
        list = when (filters.sort) {
            SortType.NAME -> list.sortedBy { it.title }
            SortType.DATE -> list.sortedByDescending { it.dateAdded }
            SortType.SIZE -> list.sortedByDescending { it.size }
            SortType.DURATION -> list.sortedByDescending { it.duration }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Core Player Engine (ExoPlayer)
    private val _player = ExoPlayer.Builder(application).build()
    val player: ExoPlayer get() = _player

    private val _currentPlayingItem = MutableStateFlow<MediaEntity?>(null)
    val currentPlayingItem: StateFlow<MediaEntity?> = _currentPlayingItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _shuffleActive = MutableStateFlow(false)
    val shuffleActive: StateFlow<Boolean> = _shuffleActive.asStateFlow()

    // Fullscreen, screen lock, and brightness overlay UI states
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    private val _screenBrightness = MutableStateFlow(0.7f) // Default brightness
    val screenBrightness: StateFlow<Float> = _screenBrightness.asStateFlow()

    private val _audioVolume = MutableStateFlow(0.8f) // Default volume percentage
    val audioVolume: StateFlow<Float> = _audioVolume.asStateFlow()

    // Equalizer Bands UI sliders state (-15dB to +15dB)
    private val _eqBands = MutableStateFlow(listOf(0, 0, 0, 0, 0)) // 5 Bands: 60Hz, 230Hz, 910Hz, 4KHz, 14KHz
    val eqBands: StateFlow<List<Int>> = _eqBands.asStateFlow()

    private var androidEqualizer: Equalizer? = null
    private var progressTrackingJob: Job? = null

    private val mediaNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.PLAY_PAUSE" -> {
                    togglePlayPause()
                }
                "com.example.PREV" -> {
                    playPrevious()
                }
                "com.example.NEXT" -> {
                    playNext()
                }
                "com.example.CLOSE" -> {
                    pause()
                    val stopIntent = Intent(context, com.example.data.service.AudioNotificationService::class.java)
                    context?.stopService(stopIntent)
                }
            }
        }
    }

    init {
        loadIptvSettings()
        // Register receiver for media playback controls safely
        val filter = IntentFilter().apply {
            addAction("com.example.PLAY_PAUSE")
            addAction("com.example.PREV")
            addAction("com.example.NEXT")
            addAction("com.example.CLOSE")
        }
        val receiverFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Context.RECEIVER_NOT_EXPORTED
        } else {
            0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(mediaNotificationReceiver, filter, receiverFlag)
        } else {
            application.registerReceiver(mediaNotificationReceiver, filter)
        }

        // Prepare listener for player state
        _player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) {
                    startTrackingProgress()
                } else {
                    stopTrackingProgress()
                }
                updateNotification()
            }

            override fun onPlaybackStateChanged(state: Int) {
                _duration.value = _player.duration.coerceAtLeast(0L)
                if (state == Player.STATE_READY) {
                    _playbackSpeed.value = _player.playbackParameters.speed
                } else if (state == Player.STATE_ENDED) {
                    handlePlaybackEnded()
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                super.onAudioSessionIdChanged(audioSessionId)
                setupAndroidEqualizer(audioSessionId)
            }
        })

        // Initial background media scanning
        viewModelScope.launch(Dispatchers.IO) {
            repository.scanStorage()
        }
    }

    private fun handlePlaybackEnded() {
        viewModelScope.launch {
            val item = _currentPlayingItem.value ?: return@launch
            // Save completion status (resetting playback position back to 0)
            repository.savePlaybackPosition(item.path, 0L)

            if (_repeatMode.value == Player.REPEAT_MODE_ONE) {
                playMedia(item)
            } else {
                val mediaArr = mediaList.value
                if (mediaArr.isNotEmpty()) {
                    val index = mediaArr.indexOfFirst { it.path == item.path }
                    if (index != -1 && index < mediaArr.size - 1) {
                        playNext()
                    } else if (_repeatMode.value == Player.REPEAT_MODE_ALL) {
                        playNext()
                    } else {
                        // REPEAT_MODE_OFF at end of playlist: stop/pause playback gracefully
                        pause()
                    }
                }
            }
        }
    }

    private fun setupAndroidEqualizer(audioSessionId: Int) {
        if (audioSessionId != androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) {
            try {
                if (androidEqualizer != null) {
                    androidEqualizer?.release()
                }
                androidEqualizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
                // Apply existing band settings from UI state immediately
                applyEqualizerBandsToHardware()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyEqualizerBandsToHardware() {
        val eq = androidEqualizer ?: return
        val bandsCount = eq.numberOfBands
        val uiBands = _eqBands.value
        for (i in 0 until minOf(uiBands.size, bandsCount.toInt())) {
            val millibels = (uiBands[i] * 100).toShort() // Convert dB to millibels
            val range = eq.bandLevelRange
            if (range != null && range.size >= 2) {
                val coercedLevel = millibels.coerceIn(range[0], range[1])
                try {
                    eq.setBandLevel(i.toShort(), coercedLevel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setEqualizerBand(bandIndex: Int, value: Int) {
        val current = _eqBands.value.toMutableList()
        if (bandIndex in current.indices) {
            current[bandIndex] = value.coerceIn(-15, 15)
            _eqBands.value = current
            applyEqualizerBandsToHardware()
        }
    }

    fun scanMediaFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.scanStorage()
        }
    }

    // Tab control
    fun selectTab(tab: PlayerTab) {
        _currentTab.value = tab
        _selectedPlaylist.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortType(type: SortType) {
        _sortType.value = type
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            _currentTab.value = PlayerTab.PLAYLISTS
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun removePlaylist(name: String) {
        viewModelScope.launch {
            if (_selectedPlaylist.value?.name == name) {
                _selectedPlaylist.value = null
                _currentTab.value = PlayerTab.PLAYLISTS
            }
            repository.deletePlaylist(name)
        }
    }

    fun addMediaToPlaylist(playlistName: String, mediaPath: String) {
        viewModelScope.launch {
            repository.addMediaToPlaylist(playlistName, mediaPath)
        }
    }

    fun removeMediaFromPlaylist(playlistName: String, mediaPath: String) {
        viewModelScope.launch {
            repository.removeMediaFromPlaylist(playlistName, mediaPath)
        }
    }

    fun toggleFavorite(item: MediaEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.path, !item.isFavorite)
            if (_currentPlayingItem.value?.path == item.path) {
                _currentPlayingItem.value = item.copy(isFavorite = !item.isFavorite)
            }
        }
    }

    // Media Playback Controls
    fun playMedia(item: MediaEntity) {
        if (item.path.startsWith("http")) {
            if (!isNetworkAvailable()) {
                Toast.makeText(
                    getApplication(),
                    "Pas de connexion internet. Impossible de diffuser ce flux de direct (IPTV).",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }
        viewModelScope.launch {
            val positionToResume = item.playbackPosition
            _currentPlayingItem.value = item

            _player.stop()
            _player.clearMediaItems()

            val mediaItem = if (item.path.startsWith("http") && (item.path.contains(".m3u8") || item.path.contains(".m3u") || item.path.contains("live") || item.path.contains("live_tv"))) {
                MediaItem.Builder()
                    .setUri(Uri.parse(item.path))
                    .setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
                    .build()
            } else {
                MediaItem.fromUri(Uri.parse(item.path))
            }
            _player.setMediaItem(mediaItem, positionToResume)
            _player.prepare()
            _player.playWhenReady = true

            // Update database history
            repository.addToHistory(item.path)
            
            updateNotification()
        }
    }

    fun playNext() {
        val current = _currentPlayingItem.value ?: return
        val list = mediaList.value
        if (list.isNotEmpty()) {
            if (_shuffleActive.value) {
                val nextItem = if (list.size > 1) {
                    list.filter { it.path != current.path }.random()
                } else {
                    list[0]
                }
                playMedia(nextItem)
            } else {
                val index = list.indexOfFirst { it.path == current.path }
                if (index != -1) {
                    if (index < list.size - 1) {
                        playMedia(list[index + 1])
                    } else {
                        // Loop to first
                        playMedia(list[0])
                    }
                } else {
                    // Current item not in current list (possibly list changed), play first available
                    playMedia(list[0])
                }
            }
        }
    }

    fun playPrevious() {
        val current = _currentPlayingItem.value ?: return
        val list = mediaList.value
        if (list.isNotEmpty()) {
            if (_shuffleActive.value) {
                val prevItem = if (list.size > 1) {
                    list.filter { it.path != current.path }.random()
                } else {
                    list[0]
                }
                playMedia(prevItem)
            } else {
                val index = list.indexOfFirst { it.path == current.path }
                if (index != -1) {
                    if (index > 0) {
                        playMedia(list[index - 1])
                    } else {
                        // Loop to last
                        playMedia(list.last())
                    }
                } else {
                    playMedia(list[0])
                }
            }
        }
    }

    fun pause() {
        _player.pause()
    }

    fun resume() {
        _player.play()
    }

    fun togglePlayPause() {
        if (_player.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun seekTo(position: Long) {
        _player.seekTo(position.coerceIn(0L, _player.duration.coerceAtLeast(0L)))
        _currentPosition.value = position
        savePlaybackState()
    }

    fun changeSpeed(speed: Float) {
        val verifiedSpeed = speed.coerceIn(0.25f, 4.0f)
        _playbackSpeed.value = verifiedSpeed
        _player.setPlaybackSpeed(verifiedSpeed)
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        _player.repeatMode = nextMode
    }

    fun toggleShuffle() {
        val active = !_shuffleActive.value
        _shuffleActive.value = active
        _player.shuffleModeEnabled = active
    }

    fun skipForward() {
        seekTo(_player.currentPosition + 10000L) // +10s
    }

    fun skipBackward() {
        seekTo(_player.currentPosition - 10000L) // -10s
    }

    fun setLockState(locked: Boolean) {
        _isLocked.value = locked
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun setScreenBrightness(level: Float) {
        _screenBrightness.value = level.coerceIn(0.1f, 1.0f)
    }

    fun setAudioVolume(level: Float) {
        val verified = level.coerceIn(0f, 1.0f)
        _audioVolume.value = verified
        _player.volume = verified
    }

    // Sleep Timer (Minuteur de mise en veille)
    private var sleepTimerJob: Job? = null
    private val _sleepTimerMinutesLeft = MutableStateFlow(0)
    val sleepTimerMinutesLeft: StateFlow<Int> = _sleepTimerMinutesLeft.asStateFlow()

    private val _sleepTimerSecondsLeft = MutableStateFlow(0)
    val sleepTimerSecondsLeft: StateFlow<Int> = _sleepTimerSecondsLeft.asStateFlow()

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerMinutesLeft.value = 0
            _sleepTimerSecondsLeft.value = 0
            return
        }
        _sleepTimerMinutesLeft.value = minutes
        _sleepTimerSecondsLeft.value = 0
        
        sleepTimerJob = viewModelScope.launch {
            var totalSeconds = minutes * 60
            while (totalSeconds > 0) {
                delay(1000)
                totalSeconds--
                _sleepTimerMinutesLeft.value = totalSeconds / 60
                _sleepTimerSecondsLeft.value = totalSeconds % 60
            }
            // Timer finished, safely halt playback with modern state updates
            _player.pause()
            _sleepTimerMinutesLeft.value = 0
            _sleepTimerSecondsLeft.value = 0
        }
    }

    private fun startTrackingProgress() {
        progressTrackingJob?.cancel()
        progressTrackingJob = viewModelScope.launch {
            while (true) {
                _currentPosition.value = _player.currentPosition
                delay(1000)
                savePlaybackState()
            }
        }
    }

    private fun stopTrackingProgress() {
        progressTrackingJob?.cancel()
        savePlaybackState()
    }

    private fun savePlaybackState() {
        val item = _currentPlayingItem.value ?: return
        val currentPos = _player.currentPosition
        viewModelScope.launch {
            repository.savePlaybackPosition(item.path, currentPos)
        }
    }

    private fun updateNotification() {
        val item = _currentPlayingItem.value
        val context = getApplication<Application>().applicationContext
        if (item != null) {
            val intent = Intent(context, com.example.data.service.AudioNotificationService::class.java).apply {
                putExtra("EXTRA_TITLE", item.title)
                putExtra("EXTRA_IS_AUDIO", item.isAudio)
                putExtra("EXTRA_IS_PLAYING", _isPlaying.value)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            val intent = Intent(context, com.example.data.service.AudioNotificationService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(mediaNotificationReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val context = getApplication<Application>().applicationContext
        val stopIntent = Intent(context, com.example.data.service.AudioNotificationService::class.java)
        context.stopService(stopIntent)
        progressTrackingJob?.cancel()
        androidEqualizer?.release()
        _player.release()
    }
}
