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
