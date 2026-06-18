package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import coil.compose.AsyncImage
import com.example.data.entity.MediaEntity
import com.example.data.entity.PlaylistEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.viewmodel.MediaPlayerViewModel
import com.example.ui.viewmodel.PlayerTab
import com.example.ui.viewmodel.SortType
import com.example.ui.viewmodel.PremiumThemeAccent
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
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
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val tabs = listOf(
                PlayerTab.VIDEOS to Icons.Default.VideoLibrary,
                PlayerTab.AUDIOS to Icons.Default.MusicNote,
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
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.name,
                            tint = if (selected) Color.Black else TextLight.copy(0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        if (selected) {
                            Text(
                                text = when (tab) {
                                    PlayerTab.VIDEOS -> "Vidéos"
                                    PlayerTab.AUDIOS -> "Audios"
                                    PlayerTab.PLAYLISTS -> "Listes"
                                    PlayerTab.FAVORITES -> "Favoris"
                                    PlayerTab.HISTORY -> "Historique"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
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
        if (mediaList.isEmpty() && currentTab != PlayerTab.PLAYLISTS) {
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
                                    model = "https://picsum.photos/seed/${item.title.hashCode()}/200",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(28.dp)
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
                // Background video placeholder visual
                AsyncImage(
                    model = "https://picsum.photos/seed/${item.title.hashCode()}/320/200",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
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
                    model = "https://picsum.photos/seed/${item.title.hashCode()}/120",
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
