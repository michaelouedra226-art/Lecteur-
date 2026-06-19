package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.example.data.entity.MediaEntity
import com.example.ui.viewmodel.MediaPlayerViewModel
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextLight
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
    viewModel: MediaPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val premiumTheme by viewModel.premiumTheme.collectAsState()
    val NeonCyan = Color(premiumTheme.primaryCyanHex)
    val NeonPink = Color(premiumTheme.secondaryPinkHex)

    val currentItem by viewModel.currentPlayingItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val speed by viewModel.playbackSpeed.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val brightness by viewModel.screenBrightness.collectAsState()
    val volume by viewModel.audioVolume.collectAsState()

    var showControls by remember { mutableStateOf(true) }
    var speedMenuOpen by remember { mutableStateOf(false) }

    // HUD overlays for gestures
    var activeGestureType by remember { mutableStateOf<String?>(null) } // "volume" or "brightness"
    var activeGestureValue by remember { mutableStateOf(0f) }

    val activity = remember(context) { context.findActivity() }

    // Handle full screen screen bright layouts on dynamic updates
    LaunchedEffect(brightness) {
        activity?.apply {
            val params = window.attributes
            params.screenBrightness = brightness
            window.attributes = params
        }
    }

    // Auto-hide controls after delay
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // Keep screen turned ON during video playback screen
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Restore portrait default on exits
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Back button press releases locks or returns
    BackHandler {
        if (isLocked) {
            viewModel.setLockState(false)
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_box")
            // Gesture triggers
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 2) {
                            viewModel.seekTo((currentPosition - 10000L).coerceAtLeast(0L))
                        } else {
                            viewModel.seekTo((currentPosition + 10000L).coerceAtMost(duration))
                        }
                    },
                    onTap = { showControls = !showControls }
                )
            }
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        // Determine left (brightness) vs right (volume)
                        val screenWidth = size.width
                        activeGestureType = if (offset.x < screenWidth / 2) "brightness" else "volume"
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val sensitivity = 500f
                        if (activeGestureType == "volume") {
                            val nextVol = volume - (dragAmount.y / sensitivity)
                            viewModel.setAudioVolume(nextVol)
                            activeGestureValue = nextVol.coerceIn(0f, 1f)
                        } else if (activeGestureType == "brightness") {
                            val nextBright = brightness - (dragAmount.y / sensitivity)
                            viewModel.setScreenBrightness(nextBright)
                            activeGestureValue = nextBright.coerceIn(0.1f, 1f)
                        }
                    },
                    onDragEnd = {
                        activeGestureType = null
                    },
                    onDragCancel = {
                        activeGestureType = null
                    }
                )
            }
    ) {
        // ExoPlayer Render View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false // Custom overlays in Compose!
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    player = viewModel.player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Overlay Gestures HUD HUD feedback
        AnimatedVisibility(
            visible = activeGestureType != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            GestureHUDOverlay(
                type = activeGestureType ?: "volume",
                value = activeGestureValue
            )
        }

        // FULL CONTROL OVERLAYS BAR SYSTEM
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.45f))
            ) {
                // LOCK OVERLAY STATE
                if (isLocked) {
                    IconButton(
                        onClick = { viewModel.setLockState(false) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .background(Color.Black.copy(0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = NeonCyan, modifier = Modifier.size(28.dp))
                    }
                } else {
                    // Standard Controls header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = TextLight)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentItem?.title ?: "Lecture Vidéo",
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                        }

                        // Top right quick configs
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Picture-in-picture mode
                            IconButton(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val aspect = Rational(16, 9)
                                    try {
                                        val params = PictureInPictureParams.Builder()
                                            .setAspectRatio(aspect)
                                            .build()
                                        activity?.enterPictureInPictureMode(params)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", tint = TextLight)
                            }

                            // Dynamic hardware rotation toggle
                            IconButton(onClick = {
                                val currentOrient = activity?.requestedOrientation
                                if (currentOrient == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                }
                            }) {
                                Icon(Icons.Default.ScreenRotation, contentDescription = "Rotation", tint = TextLight)
                            }

                            // Screenguard Locking
                            IconButton(onClick = { viewModel.setLockState(true) }) {
                                Icon(Icons.Default.LockOpen, contentDescription = "Lock Controls", tint = TextLight)
                            }
                        }
                    }

                    // Center Media Action controls (Play, Pause, Seek fast, Skip tracks)
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.playPrevious() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Précédent", tint = TextLight, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = { viewModel.skipBackward() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "-10s", tint = NeonCyan, modifier = Modifier.size(22.dp))
                        }

                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Black.copy(0.6f), CircleShape)
                                .testTag("play_pause_video")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.skipForward() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Black.copy(0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "+10s", tint = NeonCyan, modifier = Modifier.size(22.dp))
                        }

                        IconButton(
                            onClick = { viewModel.playNext() },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Suivant", tint = TextLight, modifier = Modifier.size(20.dp))
                        }
                    }

                    // Bottom Timeline Controls section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Slider tracking bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(formatTime(currentPosition), color = TextLight, fontSize = 12.sp)

                            Slider(
                                value = if (duration > 0) currentPosition.toFloat() else 0f,
                                onValueChange = { viewModel.seekTo(it.toLong()) },
                                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timeline_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan,
                                    inactiveTrackColor = Color.White.copy(0.2f)
                                )
                            )

                            Text(formatTime(duration), color = TextLight, fontSize = 12.sp)
                        }

                        // Speed multiplier button faders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(0.5f))
                                        .clickable { speedMenuOpen = !speedMenuOpen }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Text("Vitesse: ${speed}x", color = TextLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (speedMenuOpen) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(bottom = 36.dp)
                                            .background(Color(0xFF131722), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                                            .padding(4.dp)
                                    ) {
                                        val speedsChoices = listOf(0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f)
                                        speedsChoices.forEach { rate ->
                                            Text(
                                                text = "${rate}x",
                                                color = if (speed == rate) NeonCyan else TextLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable {
                                                        viewModel.changeSpeed(rate)
                                                        speedMenuOpen = false
                                                    }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GestureHUDOverlay(
    type: String,
    value: Float
) {
    Box(
        modifier = Modifier
            .size(120.dp, 60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (type == "volume") Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.BrightnessHigh,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(value * 100).toInt()}%",
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// Convert long time ms into human format
fun formatTime(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    return String.format("%02d:%02d", mins, secs)
}

// Find Activity context accessor
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
