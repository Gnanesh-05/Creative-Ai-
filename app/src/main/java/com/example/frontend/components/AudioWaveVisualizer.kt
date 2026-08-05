package com.example.frontend.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.frontend.theme.NexusCyan
import com.example.frontend.theme.NexusIndigo
import com.example.frontend.theme.NexusPink

@Composable
fun AudioWaveVisualizer(
    isPlaying: Boolean = true,
    barsCount: Int = 20,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until barsCount) {
            WaveBar(index = index, isPlaying = isPlaying)
        }
    }
}

@Composable
private fun WaveBar(
    index: Int,
    isPlaying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_$index")
    val duration = 400 + (index % 5) * 120
    val delay = index * 40

    val heightFactor by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (isPlaying) 0.9f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, delayMillis = delay, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar_$index"
    )

    Box(
        modifier = Modifier
            .width(4.dp)
            .height((36 * heightFactor).dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(NexusPink, NexusCyan, NexusIndigo)
                )
            )
    )
}
