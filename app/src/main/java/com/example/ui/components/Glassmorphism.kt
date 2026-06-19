package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GlassObsidianBorder
import com.example.ui.theme.GlassObsidianCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(GlassObsidianCard)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
fun InteractiveGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(GlassObsidianCard)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.03f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier,
    playing: Boolean = true
) {
    val barCount = 12
    val animators = remember { List(barCount) { Animatable(0.2f) } }

    LaunchedEffect(playing) {
        if (playing) {
            animators.forEachIndexed { index, animatable ->
                val delayMs = (index * 80).toLong()
                val duration = 400 + (Math.random() * 400).toInt()
                animatable.animateTo(
                    targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = duration, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
        } else {
            animators.forEach { animatable ->
                animatable.animateTo(0.15f, tween(300))
            }
        }
    }

    Row(
        modifier = modifier
            .width(120.dp)
            .height(48.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        animators.forEachIndexed { index, animatable ->
            val color = if (index % 2 == 0) NeonCyan else NeonPink
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(animatable.value)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Returns a high-quality, professional, themed cover URL strictly mapped to the music title
 * to avoid random or generic image placement.
 */
fun getPremiumAudioCover(title: String): String {
    val lower = title.lowercase()
    return when {
        lower.contains("synthwave") || lower.contains("synth") || lower.contains("retro") || lower.contains("neon") -> 
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80" // Purple neon waves synth
        lower.contains("ambient") || lower.contains("echo") || lower.contains("space") || lower.contains("relax") -> 
            "https://images.unsplash.com/photo-1518241353330-0f7941c2d9b5?w=400&q=80" // Deep space / cozy lofi lights
        lower.contains("woods") || lower.contains("resonance") || lower.contains("forest") || lower.contains("nature") -> 
            "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=400&q=80" // Sunrays deep woods/nature
        lower.contains("acoustic") || lower.contains("whisper") || lower.contains("guitar") || lower.contains("voice") -> 
            "https://images.unsplash.com/photo-1485278537138-4e8911a13c02?w=400&q=80" // Acoustic instrument closeup
        lower.contains("jazz") || lower.contains("blues") || lower.contains("chill") -> 
            "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=400&q=80" // Saxophone lounge ambiance
        lower.contains("piano") || lower.contains("classic") || lower.contains("orchestra") -> 
            "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?w=400&q=80" // Grand piano ivory keys
        else -> {
            val index = Math.abs(title.hashCode()) % 6
            val artisticPlaceholders = listOf(
                "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=400&q=80", // Curved abstract glowing threads
                "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=400&q=80", // Dark slate fluid wave painting
                "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80", // Glowing neon stage lasers
                "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=400&q=80", // Analog vinyl turntable close detail
                "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=400&q=80", // Vintage cassette deck close detail
                "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=400&q=80"  // Deep premium abstract sound waves
            )
            artisticPlaceholders[index]
        }
    }
}
