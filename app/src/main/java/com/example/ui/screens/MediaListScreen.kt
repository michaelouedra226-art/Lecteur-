package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import com.example.data.entity.MediaEntity
import com.example.data.entity.PlaylistEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.components.getPremiumAudioCover
import com.example.ui.viewmodel.MediaPlayerViewModel
import com.example.ui.viewmodel.PlayerTab
import com.example.ui.viewmodel.SortType
import com.example.ui.viewmodel.PremiumThemeAccent
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaListScreen(
    viewModel: MediaPlayerViewModel,
    onOpenVideoPlayer: (MediaEntity) -> Unit,
    onOpenAudioPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val premiumTheme by viewModel.premiumTheme.collectAsState()
    val NeonCyan = Color(premiumTheme.primaryCyanHex)
    val NeonPink = Color(premiumTheme.secondaryPinkHex)

    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val mediaList by viewModel.mediaList.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val currentPlayingItem by viewModel.currentPlayingItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var showAddToPlaylistDialog by remember { mutableStateOf<MediaEntity?>(null) }
    var showSortDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassObsidian,
                        Color(0xFF0F121C)
                    )
                )
            )
            .padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title Header & Theme Customizer / Scanner Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Lecteur Premium",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Cinéma & Son Ultra HD",
                    fontSize = 12.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Circular Palette icon dropdown Menu
                var showThemeMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showThemeMenu = true },
                        modifier = Modifier
                            .background(Color.White.copy(0.08f), CircleShape)
                            .testTag("theme_palette_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Thème",
                            tint = NeonCyan
                        )
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false },
                        modifier = Modifier.background(Color(0xFF131722))
                    ) {
                        PremiumThemeAccent.values().forEach { accent ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color(accent.primaryCyanHex), CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color(accent.secondaryPinkHex), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(accent.nameFr, color = TextLight, fontSize = 13.sp)
                                    }
                                },
                                onClick = {
                                    viewModel.changeThemeAccent(accent)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.scanMediaFiles()
                        Toast.makeText(context, "Numérisation des médias en cours...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .background(Color.White.copy(0.08f), CircleShape)
                        .testTag("scan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Frosted Glass Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Rechercher vos vidéos & audios...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("search_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextLight,
                    unfocusedTextColor = TextLight,
                    focusedContainerColor = Color.White.copy(0.05f),
                    unfocusedContainerColor = Color.White.copy(0.05f),
                    focusedBorderColor = NeonCyan.copy(0.5f),
                    unfocusedBorderColor = Color.White.copy(0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Sort Dropdown button
            Box {
                IconButton(
                    onClick = { showSortDropdown = true },
                    modifier = Modifier.background(Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.SortByAlpha, contentDescription = "Sort", tint = NeonCyan)
                }

                DropdownMenu(
                    expanded = showSortDropdown,
                    onDismissRequest = { showSortDropdown = false },
                    modifier = Modifier.background(Color(0xFF131722))
                ) {
                    DropdownMenuItem(
                        text = { Text("Nom (A-Z)", color = TextLight) },
                        onClick = { viewModel.setSortType(SortType.NAME); showSortDropdown = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Date (Plus récent)", color = TextLight) },
                        onClick = { viewModel.setSortType(SortType.DATE); showSortDropdown = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Taille", color = TextLight) },
                        onClick = { viewModel.setSortType(SortType.SIZE); showSortDropdown = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Durée", color = TextLight) },
                        onClick = { viewModel.setSortType(SortType.DURATION); showSortDropdown = false }
                    )
                }
            }
        }

        // Apple-style Glass Tab Indicator Scroll Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(0.03f), RoundedCornerShape(24.dp))
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                PlayerTab.VIDEOS to Icons.Default.VideoLibrary,
                PlayerTab.AUDIOS to Icons.Default.MusicNote,
                PlayerTab.IPTV to Icons.Default.Tv,
                PlayerTab.PLAYLISTS to Icons.AutoMirrored.Filled.PlaylistPlay,
                PlayerTab.FAVORITES to Icons.Default.Favorite,
                PlayerTab.HISTORY to Icons.Default.History
            )

            tabs.forEach { (tab, icon) ->
                val selected = currentTab == tab
                val bgBrush = if (selected) {
                    Brush.linearGradient(colors = listOf(NeonCyan, NeonPink))
                } else {
                    Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush = bgBrush)
                        .clickable { viewModel.selectTab(tab) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.name,
                            tint = if (selected) Color.Black else TextLight.copy(0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (tab) {
                                PlayerTab.VIDEOS -> "Vidéos"
                                PlayerTab.AUDIOS -> "Audios"
                                PlayerTab.IPTV -> "IPTV"
                                PlayerTab.PLAYLISTS -> "Listes"
                                PlayerTab.FAVORITES -> "Favoris"
                                PlayerTab.HISTORY -> "Historique"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (selected) Color.Black else TextLight.copy(0.7f)
                        )
                    }
                }
            }
        }

        // Back action bar if nested inside a specific playlist list view
        if (currentTab == PlayerTab.PLAYLISTS && selectedPlaylist != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playlist: ${selectedPlaylist?.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPink
                )
                TextButton(onClick = { viewModel.selectPlaylist(null) }) {
                    Text("<- Retour aux Playlists", color = NeonCyan)
                }
            }
        }

        // Empty State Check
        if (mediaList.isEmpty() && currentTab != PlayerTab.PLAYLISTS && currentTab != PlayerTab.IPTV) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Empty",
                    tint = TextGray.copy(0.4f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Aucun élément trouvé", color = TextGray, fontSize = 16.sp)
                Text(
                    "Scannez le stockage pour importer vos fichiers",
                    color = TextGray.copy(0.7f),
                    fontSize = 12.sp
                )
            }
        } else {
            // Screen grid / list renderer based on tab
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    PlayerTab.VIDEOS -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = 68.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(mediaList) { item ->
                                VideoCardItem(
                                    item = item,
                                    onClick = { onOpenVideoPlayer(item) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(item) },
                                    onAddToPlaylist = { showAddToPlaylistDialog = item }
                                )
                            }
                        }
                    }

                    PlayerTab.PLAYLISTS -> {
                        if (selectedPlaylist != null) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 68.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(mediaList) { item ->
                                    AudioPlaylistItemLine(
                                        item = item,
                                        onPlay = {
                                            if (item.isAudio) {
                                                viewModel.playMedia(item)
                                                onOpenAudioPlayer()
                                            } else {
                                                onOpenVideoPlayer(item)
                                            }
                                        },
                                        onRemove = {
                                            selectedPlaylist?.let {
                                                viewModel.removeMediaFromPlaylist(it.name, item.path)
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            // Show custom playlists list
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Vos collections audio & vidéo", color = TextGray, fontSize = 14.sp)
                                    Button(
                                        onClick = { showCreatePlaylistDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink.copy(0.2f)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Nouvelle", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(playlists) { playlist ->
                                        PlaylistCard(
                                            playlist = playlist,
                                            onClick = { viewModel.selectPlaylist(playlist) },
                                            onDelete = { viewModel.removePlaylist(playlist.name) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PlayerTab.IPTV -> {
                        IptvScreenContent(
                            viewModel = viewModel,
                            onOpenVideoPlayer = onOpenVideoPlayer
                        )
                    }

                    else -> { // Audios, Favorites, History
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 68.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(mediaList) { item ->
                                AudioListItemLine(
                                    item = item,
                                    isPlayingThis = currentPlayingItem?.path == item.path,
                                    onClick = {
                                        viewModel.playMedia(item)
                                        if (item.isAudio) {
                                            onOpenAudioPlayer()
                                        } else {
                                            onOpenVideoPlayer(item)
                                        }
                                    },
                                    onFavoriteToggle = { viewModel.toggleFavorite(item) },
                                    onAddToPlaylist = { showAddToPlaylistDialog = item }
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM COMPACT CONTROLLER / MINI PLAYER PILL (For Audio in background)
        AnimatedVisibility(
            visible = currentPlayingItem != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            currentPlayingItem?.let { item ->
                InteractiveGlassCard(
                    onClick = onOpenAudioPlayer,
                    cornerRadius = 24.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .testTag("mini_player_pill")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAudioPlayer() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icon image preview (with glowing overlay)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isAudio) {
                                AsyncImage(
                                    model = getPremiumAudioCover(item.title),
                                    contentDescription = "Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Audiotrack, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = NeonPink, modifier = Modifier.size(24.dp))
                            }
                        }

                        // Title & Metadata
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = TextLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (item.isAudio) "Audio - En lecture" else "Vidéo en cours",
                                color = TextGray,
                                fontSize = 11.sp
                            )

                            // Clean glass timeline tracking bar
                            if (duration > 0) {
                                LinearProgressIndicator(
                                    progress = { (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .height(2.dp),
                                    color = NeonCyan,
                                    trackColor = Color.White.copy(0.1f),
                                )
                            }
                        }

                        // Playing Action Controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { viewModel.playPrevious() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipPrevious,
                                    contentDescription = "Précédent",
                                    tint = TextLight.copy(0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.playNext() }) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Suivant",
                                    tint = TextLight.copy(0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Create Playlist
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = Color(0xFF161924),
            title = { Text("Créer une Playlist", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nom de la playlist", color = NeonCyan) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = NeonCyan
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                            Toast.makeText(context, "Playlist créée !", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Valider", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Annuler", color = TextGray)
                }
            }
        )
    }

    // Dialog: Add to Playlist selection
    if (showAddToPlaylistDialog != null) {
        val selectedMedia = showAddToPlaylistDialog!!
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = null },
            containerColor = Color(0xFF161924),
            title = { Text("Ajouter à une collection", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (playlists.isEmpty()) {
                        Text("Aucune playlist disponible.", color = TextGray)
                    } else {
                        playlists.forEach { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addMediaToPlaylist(playlist.name, selectedMedia.path)
                                        showAddToPlaylistDialog = null
                                        Toast
                                            .makeText(
                                                context,
                                                "Ajouté à ${playlist.name} !",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.04f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = NeonCyan)
                                    Text(playlist.name, color = TextLight)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistDialog = null }) {
                    Text("Fermer", color = TextGray)
                }
            }
        )
    }
}

@Composable
fun VideoCardItem(
    item: MediaEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.04f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column {
            // Video Thumbnail Poster
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(Color.Black.copy(0.4f))
            ) {
                // Background video helper visual (loads actual local frames dynamically)
                VideoThumbnail(
                    path = item.path,
                    modifier = Modifier.fillMaxSize()
                )

                // Glass overlay duration indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(item.durationText, color = TextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Play triangle vector decoration center
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                }
            }

            // Info texts
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    color = TextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.sizeText, color = TextGray, fontSize = 10.sp)

                    // Control actions
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Fav",
                                tint = if (item.isFavorite) NeonPink else TextGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = onAddToPlaylist, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                                contentDescription = "Add Playlist",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioListItemLine(
    item: MediaEntity,
    isPlayingThis: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayingThis) Color.White.copy(0.08f) else Color.White.copy(0.03f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Visual circle note
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = getPremiumAudioCover(item.title),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isAudio) Icons.Default.MusicNote else Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = if (isPlayingThis) NeonCyan else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Info details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = if (isPlayingThis) NeonCyan else TextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(item.durationText, color = TextGray, fontSize = 11.sp)
                    Text(item.sizeText, color = TextGray, fontSize = 11.sp)
                }
            }

            // Actions row
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Fav",
                        tint = if (item.isFavorite) NeonPink else TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onAddToPlaylist, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = "Playlist",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AudioPlaylistItemLine(
    item: MediaEntity,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.03f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.05f))
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonCyan)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(item.durationText, color = TextGray, fontSize = 11.sp)
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = NeonPink.copy(0.7f))
            }
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: PlaylistEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.04f))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(NeonCyan.copy(0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                }

                // Delete custom playlists option
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }

            Column {
                Text(
                    text = playlist.name,
                    color = TextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Collection Audio",
                    color = TextGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun VideoThumbnail(
    path: String,
    modifier: Modifier = Modifier
) {
    var thumbnailBitmap by remember(path) { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    val fastImageUrl = remember(path) {
        when {
            path.contains("BigBuckBunny.mp4") -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg"
            path.contains("ElephantsDream.mp4") -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg"
            path.contains("TearsOfSteel.mp4") -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg"
            path.contains("ForBiggerBlazes.mp4") -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg"
            else -> null
        }
    }

    LaunchedEffect(path) {
        if (fastImageUrl == null) {
            withContext(Dispatchers.IO) {
                var retriever: MediaMetadataRetriever? = null
                try {
                    retriever = MediaMetadataRetriever()
                    if (path.startsWith("http")) {
                        retriever.setDataSource(path, HashMap())
                    } else if (path.startsWith("content://")) {
                        retriever.setDataSource(context, Uri.parse(path))
                    } else {
                        retriever.setDataSource(path)
                    }
                    val bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                    thumbnailBitmap = bitmap
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        retriever?.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    if (fastImageUrl != null) {
        AsyncImage(
            model = fastImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else if (thumbnailBitmap != null) {
        Image(
            bitmap = thumbnailBitmap!!.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Modern premium gradient fallback with video indicator
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1F2335), Color(0xFF161924))
                )
            ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = null,
                tint = Color.White.copy(0.3f),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun IptvScreenContent(
    viewModel: MediaPlayerViewModel,
    onOpenVideoPlayer: (MediaEntity) -> Unit
) {
    var showImportDialog by remember { mutableStateOf(false) }
    var m3uInputText by remember { mutableStateOf("") }
    var iptvUrlText by remember { mutableStateOf("") }
    var importTabActive by remember { mutableStateOf(0) } // 0 = URL, 1 = Local File, 2 = Raw Text
    var isImportingUrl by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val channels by viewModel.iptvChannels.collectAsState()
    val selectedGroup by viewModel.selectedIptvGroup.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val text = inputStream.bufferedReader().use { it.readText() }
                    val success = viewModel.saveCustomIptvM3u(text)
                    if (success) {
                        Toast.makeText(context, "Playlist M3U importée avec succès !", Toast.LENGTH_SHORT).show()
                        showImportDialog = false
                    } else {
                        Toast.makeText(context, "Erreur: format M3U non valide ou vide", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur de chargement: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Compute categories dynamically
    val groups = remember(channels) {
        listOf("Tout") + channels.mapNotNull { it.group }.distinct().sorted()
    }

    // Filter channels based on selected group
    val filteredChannels = remember(channels, selectedGroup) {
        if (selectedGroup == "Tout") {
            channels
        } else {
            channels.filter { it.group == selectedGroup }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top action bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "IPTV Pro en direct",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Pulsing Red Live Dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
                Text(
                    text = "${channels.size} Chaînes disponibles",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showImportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importer IPTV", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = {
                        viewModel.refreshIptvChannels(
                            onStarted = { isRefreshing = true },
                            onFinished = { success, msg ->
                                isRefreshing = false
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = !isRefreshing,
                    modifier = Modifier.background(Color.White.copy(0.04f), RoundedCornerShape(10.dp))
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Réactualiser", tint = TextLight)
                    }
                }

                if (channels.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            viewModel.clearIptvChannels()
                            Toast.makeText(context, "IPTV vidé ! Vous pouvez re-configurer.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.background(Color.White.copy(0.04f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Vider", tint = Color.Red.copy(0.8f))
                    }
                }
            }
        }

        // Horizontal Category sliding row
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(groups) { group ->
                val isSelected = group == selectedGroup
                val borderAlpha = if (isSelected) 0.6f else 0.1f
                val txtColor = if (isSelected) NeonCyan else TextLight.copy(0.8f)
                val bgColor = if (isSelected) NeonCyan.copy(0.1f) else Color.White.copy(0.03f)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgColor)
                        .border(1.dp, NeonCyan.copy(alpha = borderAlpha), RoundedCornerShape(14.dp))
                        .clickable { viewModel.selectIptvGroup(group) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = group,
                        color = txtColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Channels List
        if (channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(0.04f), RoundedCornerShape(24.dp))
                        .border(1.dp, NeonGreen.copy(0.25f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(56.dp)
                    )
                    
                    Text(
                        text = "Configurer votre IPTV / M3U Pro",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Collez le lien URL .m3u de votre abonnement ou fournisseur pour charger vos flux en direct avec votre connexion internet.",
                        color = TextGray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    var directUrlText by remember { mutableStateOf("") }
                    var isDirectImporting by remember { mutableStateOf(false) }
                    
                    OutlinedTextField(
                        value = directUrlText,
                        onValueChange = { directUrlText = it },
                        placeholder = { Text("https://exemple.com/playlist.m3u", color = TextGray.copy(0.35f), fontSize = 12.sp) },
                        singleLine = true,
                        enabled = !isDirectImporting,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color.White.copy(0.12f),
                            focusedContainerColor = Color.Black.copy(0.2f),
                            unfocusedContainerColor = Color.Black.copy(0.2f)
                        )
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (directUrlText.trim().isNotEmpty()) {
                                    isDirectImporting = true
                                    viewModel.saveCustomIptvUrl(
                                        url = directUrlText.trim(),
                                        onSuccess = { count ->
                                            isDirectImporting = false
                                            Toast.makeText(context, "$count chaînes chargées avec succès !", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            isDirectImporting = false
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "Saisissez d'abord un lien URL", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isDirectImporting,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            if (isDirectImporting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                            } else {
                                Text("Charger URL", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            enabled = !isDirectImporting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fichier", color = TextLight, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = {
                                viewModel.resetIptvToDefaults()
                                Toast.makeText(context, "Flux de démonstration chargés !", Toast.LENGTH_SHORT).show()
                            },
                            enabled = !isDirectImporting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text("Démo", color = TextLight, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucune chaîne trouvée dans cette catégorie", color = TextGray, fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredChannels) { channel ->
                    InteractiveGlassCard(
                        onClick = {
                            val mediaItem = MediaEntity(
                                path = channel.url,
                                title = channel.name,
                                mimeType = "video/mp4", // Video stream treatment
                                size = 0L,
                                duration = 0L,
                                dateAdded = System.currentTimeMillis(),
                                isAudio = false
                            )
                            viewModel.playMedia(mediaItem)
                            onOpenVideoPlayer(mediaItem)
                        },
                        cornerRadius = 14.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.1f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Logo display with live indicator overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (channel.logo != null) {
                                    AsyncImage(
                                        model = channel.logo,
                                        contentDescription = channel.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize().padding(10.dp)
                                    )
                                } else {
                                    // Custom abstract TV logo procedural shape
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = null,
                                            tint = NeonCyan.copy(0.35f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                // Breathing red "DIRECT / LIVE" label overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Red.copy(0.85f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Title name labels
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = channel.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = channel.group ?: "Général",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue with tabs to input URL link OR paste raw M3U playlist text
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isImportingUrl) showImportDialog = false },
            containerColor = Color(0xFF1A1D29),
            title = {
                Text(
                    text = "Importer IPTV / M3U Pro",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Custom tab switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(0.3f))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (importTabActive == 0) NeonGreen.copy(0.12f) else Color.Transparent)
                                .clickable(enabled = !isImportingUrl) { importTabActive = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Lien URL",
                                color = if (importTabActive == 0) NeonGreen else TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.3f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (importTabActive == 1) NeonGreen.copy(0.12f) else Color.Transparent)
                                .clickable(enabled = !isImportingUrl) { importTabActive = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Fichier local",
                                color = if (importTabActive == 1) NeonGreen else TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (importTabActive == 2) NeonGreen.copy(0.12f) else Color.Transparent)
                                .clickable(enabled = !isImportingUrl) { importTabActive = 2 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Texte brut",
                                color = if (importTabActive == 2) NeonGreen else TextGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (importTabActive == 0) {
                        Text(
                            text = "Entrez l'URL d'un flux ou fichier .m3u / .m3u8 distant. Cela fonctionnera de manière dynamique avec votre connexion internet.",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                        OutlinedTextField(
                            value = iptvUrlText,
                            onValueChange = { iptvUrlText = it },
                            placeholder = { Text("https://example.com/playlist.m3u", color = TextGray.copy(0.35f), fontSize = 12.sp) },
                            singleLine = true,
                            enabled = !isImportingUrl,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.White.copy(0.12f),
                                focusedContainerColor = Color.Black.copy(0.2f),
                                unfocusedContainerColor = Color.Black.copy(0.2f)
                            )
                        )
                    } else if (importTabActive == 1) {
                        Text(
                            text = "Sélectionnez un fichier .m3u local stocké sur votre appareil pour charger vos chaînes IPTV.",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(0.03f), RoundedCornerShape(12.dp))
                                .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(12.dp))
                                .clickable {
                                    filePickerLauncher.launch("*/*")
                                }
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "Parcourir les fichiers...",
                                    color = NeonGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Collez le contenu textuel brut du fichier m3u contenant vos chaînes.",
                            color = TextGray,
                            fontSize = 11.sp
                        )
                        OutlinedTextField(
                            value = m3uInputText,
                            onValueChange = { m3uInputText = it },
                            placeholder = { Text("#EXTM3U\n#EXTINF:-1 tvg-logo=\"logo_url\" group-title=\"Groupe\",Chaîne\nhttp://ip:port/stream", color = TextGray.copy(0.35f), fontSize = 11.sp) },
                            enabled = !isImportingUrl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color.White.copy(0.12f),
                                focusedContainerColor = Color.Black.copy(0.2f),
                                unfocusedContainerColor = Color.Black.copy(0.2f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                if (importTabActive == 1) {
                    // File selection handles imports automatically
                } else if (isImportingUrl) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NeonGreen, strokeWidth = 2.dp)
                } else {
                    Button(
                        onClick = {
                            if (importTabActive == 0) {
                                if (iptvUrlText.trim().isNotEmpty()) {
                                    isImportingUrl = true
                                    viewModel.saveCustomIptvUrl(
                                        url = iptvUrlText.trim(),
                                        onSuccess = { count ->
                                            isImportingUrl = false
                                            Toast.makeText(context, "Succès: $count chaînes chargées avec connexion !", Toast.LENGTH_SHORT).show()
                                            showImportDialog = false
                                        },
                                        onError = { error ->
                                            isImportingUrl = false
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "Veuillez saisir un lien URL valide", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (m3uInputText.trim().isNotEmpty()) {
                                    val success = viewModel.saveCustomIptvM3u(m3uInputText)
                                    if (success) {
                                        Toast.makeText(context, "Playlist M3U importée avec succès !", Toast.LENGTH_SHORT).show()
                                        showImportDialog = false
                                    } else {
                                        Toast.makeText(context, "Erreur: format M3U non valide ou vide", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Le texte est vide", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Importer", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isImportingUrl) {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Annuler", color = TextGray)
                    }
                }
            }
        )
    }
}
