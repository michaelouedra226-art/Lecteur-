package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.InteractiveGlassCard
import com.example.ui.viewmodel.MediaPlayerViewModel
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextLight
import androidx.media3.common.Player

@Composable
fun AudioPlayerScreen(
    viewModel: MediaPlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentItem by viewModel.currentPlayingItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val shuffleActive by viewModel.shuffleActive.collectAsState()
    val eqBands by viewModel.eqBands.collectAsState()

    val premiumTheme by viewModel.premiumTheme.collectAsState()
    val NeonCyan = Color(premiumTheme.primaryCyanHex)
    val NeonPink = Color(premiumTheme.secondaryPinkHex)

    var showEqPanel by remember { mutableStateOf(false) }

    // Vinyl album spin rotational animation
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing)
        )
    )

    // Animated Phase for dynamic sound equalizer waves
    val phaseAngleScroll by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassObsidian,
                        Color(0xFF151928)
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper back bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextLight)
            }
            Text(
                if (showEqPanel) "Égaliseur Premium HD" else "Lecteur Audio Premium",
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleep Timer Trigger Button
                var showTimerMenu by remember { mutableStateOf(false) }
                val sleepMinutesLeft by viewModel.sleepTimerMinutesLeft.collectAsState()
                val sleepSecondsLeft by viewModel.sleepTimerSecondsLeft.collectAsState()

                Box {
                    IconButton(onClick = { showTimerMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Veille",
                            tint = if (sleepMinutesLeft > 0) NeonPink else TextLight
                        )
                    }

                    DropdownMenu(
                        expanded = showTimerMenu,
                        onDismissRequest = { showTimerMenu = false },
                        modifier = Modifier.background(Color(0xFF131722))
                    ) {
                        listOf(0, 5, 15, 30, 45, 60).forEach { mins ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (mins == 0) "Désactiver" else "$mins minutes",
                                        color = TextLight,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    viewModel.setSleepTimer(mins)
                                    showTimerMenu = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = { showEqPanel = !showEqPanel }) {
                    Icon(Icons.Default.Equalizer, contentDescription = "EQ", tint = if (showEqPanel) NeonCyan else TextLight)
                }
            }
        }

        // Live Sleep Timer Remaining Pill Badge
        val sleepMinutesLeft by viewModel.sleepTimerMinutesLeft.collectAsState()
        val sleepSecondsLeft by viewModel.sleepTimerSecondsLeft.collectAsState()

        if (sleepMinutesLeft > 0 || sleepSecondsLeft > 0) {
            val formattedSeconds = String.format("%02d", sleepSecondsLeft)
            Text(
                text = "Mise en veille : $sleepMinutesLeft:$formattedSeconds",
                color = NeonPink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(NeonPink.copy(0.12f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        if (showEqPanel) {
            // HIGH FIDELITY EQUALIZER CONTROLS LAYER
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("eq_panel_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Ajustement Multi-bande",
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // 5 interactive equalizer channels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1F)
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val frequencies = listOf("60 Hz", "230 Hz", "910 Hz", "4 kHz", "14 kHz")
                        frequencies.forEachIndexed { index, freq ->
                            val level = eqBands[index]
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(52.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (level > 0) "+${level}dB" else "${level}dB",
                                    color = if (level != 0) NeonCyan else TextGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Vertical Slider Custom rendering
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Custom visual volume notch vertical line fader
                                    Slider(
                                        value = level.toFloat(),
                                        onValueChange = { viewModel.setEqualizerBand(index, it.toInt()) },
                                        valueRange = -15f..15f,
                                        modifier = Modifier
                                            .rotate(-90f)
                                            .height(30.dp)
                                            .width(140.dp)
                                            .testTag("eq_slider_$index"),
                                        colors = SliderDefaults.colors(
                                            thumbColor = NeonCyan,
                                            activeTrackColor = NeonCyan,
                                            inactiveTrackColor = Color.White.copy(0.15f)
                                        )
                                    )
                                }

                                Text(
                                    text = freq,
                                    color = TextLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Predefined Equalizer presets selection row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val presets = listOf("Flat", "Bass+", "Vocal", "Electronic")
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(0.06f))
                                    .clickable {
                                        when (preset) {
                                            "Flat" -> List(5) { 0 }
                                            "Bass+" -> listOf(10, 6, 2, 0, -2)
                                            "Vocal" -> listOf(-3, 1, 6, 8, 3)
                                            "Electronic" -> listOf(8, 4, 0, 4, 9)
                                            else -> List(5) { 0 }
                                        }.forEachIndexed { i, vol -> viewModel.setEqualizerBand(i, vol) }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp)
                            ) {
                                Text(preset, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // GORGEOUS MEDIA COVER ART PLAYER (Main Screen) with live spectrum
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spinning album cover disk
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .rotate(if (isPlaying) rotationAngle else 0f)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(4.dp, Color.White.copy(0.12f), CircleShape)
                        .border(8.dp, Color.Black, CircleShape)
                ) {
                    // Seeded internet placeholder album image
                    AsyncImage(
                        model = "https://picsum.photos/seed/${currentItem?.title.hashCode()}/320",
                        contentDescription = "Cd Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Disk groove vector overlays
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.Black.copy(0.6f),
                            radius = size.width / 4,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                        drawCircle(
                            color = Color.Black,
                            radius = size.width / 12,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // GORGEOUS LIVE SPECTRUM EQUALIZER BARS
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    val barCount = 18
                    val barWidth = 6.dp.toPx()
                    val barSpacing = 6.dp.toPx()
                    val totalWidth = barCount * barWidth + (barCount - 1) * barSpacing
                    val startX = (size.width - totalWidth) / 2

                    for (i in 0 until barCount) {
                        val heightFactor = if (isPlaying) {
                            val wave1 = Math.sin((phaseAngleScroll + i * 0.45).toDouble()).toFloat() * 0.45f + 0.5f
                            val wave2 = Math.cos((phaseAngleScroll * 1.5 + i * 0.9).toDouble()).toFloat() * 0.35f + 0.35f
                            (wave1 + wave2).coerceIn(0.15f, 1.0f)
                        } else {
                            0.12f
                        }

                        val barHeight = heightFactor * size.height
                        val x = startX + i * (barWidth + barSpacing)
                        val y = (size.height - barHeight) / 2

                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonPink, NeonCyan)
                            ),
                            start = Offset(x, size.height - y),
                            end = Offset(x, y),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Title and heart trigger favorite
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentItem?.title ?: "Titre Audio",
                color = TextLight,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (currentItem?.isAudio == true) "Format Haute Fidélité (AAC/FLAC)" else "Vidéo Active",
                color = NeonCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Slider timeline progression seeker
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Slider(
                value = if (duration > 0) currentPosition.toFloat() else 0f,
                onValueChange = { viewModel.seekTo(it.toLong()) },
                valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                modifier = Modifier.fillMaxWidth().testTag("audio_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = NeonPink,
                    activeTrackColor = NeonPink,
                    inactiveTrackColor = Color.White.copy(0.15f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatTime(currentPosition), color = TextGray, fontSize = 11.sp)
                Text(formatTime(duration), color = TextGray, fontSize = 11.sp)
            }
        }

        // Dynamic Playbacks triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lecture aléatoire (Shuffle)
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleActive) NeonCyan else TextLight.copy(0.4f)
                )
            }

            // Skip previous
            IconButton(onClick = { viewModel.seekTo(0L) }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = TextLight, modifier = Modifier.size(28.dp))
            }

            // Big Center Play pauses
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(0.08f), CircleShape)
                    .border(1.dp, Color.White.copy(0.2f), CircleShape)
                    .testTag("play_pause_audio")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Skip Next
            IconButton(onClick = { viewModel.skipForward() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = TextLight, modifier = Modifier.size(28.dp))
            }

            // Lecture répétition (Repeat Mode cycling)
            IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                val cycleIcon = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                    Player.REPEAT_MODE_ALL -> Icons.Default.Repeat
                    else -> Icons.Default.Repeat
                }
                Icon(
                    imageVector = cycleIcon,
                    contentDescription = "Repeat",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) NeonPink else TextLight.copy(0.4f)
                )
            }
        }

        // Adding beautiful secondary bottom indicator and volume slider fader HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Speed control button
            var showSpeedMenu by remember { mutableStateOf(false) }
            val currentPlaybackSpeed by viewModel.playbackSpeed.collectAsState()
            
            Box {
                Row(
                    modifier = Modifier
                        .clickable { showSpeedMenu = true }
                        .background(Color.White.copy(0.06f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed, 
                        contentDescription = "Vitesse", 
                        tint = NeonCyan, 
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${currentPlaybackSpeed}x",
                        color = TextLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = { showSpeedMenu = false },
                    modifier = Modifier.background(Color(0xFF131722))
                ) {
                    listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${speed}x" + (if (speed == 1.0f) " (Normal)" else ""),
                                    color = if (currentPlaybackSpeed == speed) NeonCyan else TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = if (currentPlaybackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.changeSpeed(speed)
                                showSpeedMenu = false
                            }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                Text(
                    "Dolby Atmos activé",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
