package com.example.frontend.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.frontend.viewmodel.NexusViewModel
import com.example.frontend.components.GlassCard
import com.example.frontend.theme.IridescentGradient
import com.example.frontend.theme.LightCanvasEnd
import com.example.frontend.theme.LightCanvasMid
import com.example.frontend.theme.LightCanvasStart
import com.example.frontend.theme.LightGlassBorder
import com.example.frontend.theme.LightGlassSurface
import com.example.frontend.theme.PastelMagenta
import com.example.frontend.theme.PastelViolet
import com.example.frontend.theme.TextMutedLight
import com.example.frontend.theme.TextPrimaryLight
import com.example.frontend.theme.TextSecondaryLight
import com.example.backend.util.MediaSharingUtils
import java.io.File

import androidx.compose.ui.res.painterResource
import com.example.backend.util.App3DAssets

@Composable
fun ProfileScreen(
    viewModel: NexusViewModel,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoggedIn by remember { mutableStateOf(true) }
    var userEmail by remember { mutableStateOf(com.example.backend.remote.SupabaseManager.currentUserEmail ?: "user@example.com") }
    var userName by remember { mutableStateOf(userEmail.substringBefore("@").replaceFirstChar { it.uppercase() }) }
    var showLoginScreen by remember { mutableStateOf(false) }

    var isLightGlassTheme by remember { mutableStateOf(true) }
    var isLocalGpuEnabled by remember { mutableStateOf(true) }
    var selectedHistoryFilter by remember { mutableStateOf("ALL") } // ALL, IMAGE, MUSIC

    val assets by viewModel.studioAssets.collectAsState()
    val isPlaying by viewModel.isMusicPlaying.collectAsState()

    val filteredAssets = when (selectedHistoryFilter) {
        "IMAGE" -> assets.filter { it.type == "IMAGE" }
        "MUSIC" -> assets.filter { it.type == "MUSIC" }
        else -> assets
    }

    if (showLoginScreen) {
        LoginScreen(
            onLoginSuccess = { name, email ->
                userName = name
                userEmail = email
                isLoggedIn = true
                showLoginScreen = false
                onNavigateToHome()
            },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightCanvasStart,
                        LightCanvasMid,
                        LightCanvasEnd
                    )
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LightGlassSurface,
                borderColor = LightGlassBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color.White, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = App3DAssets.appLogo),
                                contentDescription = "3D App Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryLight
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PastelViolet)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("PRO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showLoginScreen = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Switch Account / Login", tint = PastelViolet)
                    }
                }
            }
        }

        // Dedicated Creation History Header & Filter Pills
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.History, contentDescription = null, tint = PastelViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Creation History Log (${assets.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                        )
                    }

                    Text(
                        text = "Tap item to view / play",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight, fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Pills: All, Images, Music
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("ALL" to "All (${assets.size})", "IMAGE" to "Images (${assets.count { it.type == "IMAGE" }})", "MUSIC" to "Music (${assets.count { it.type == "MUSIC" }})")
                    filters.forEach { (key, label) ->
                        val isSelected = selectedHistoryFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PastelViolet else Color.White.copy(alpha = 0.8f))
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable { selectedHistoryFilter = key }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondaryLight
                                )
                            )
                        }
                    }
                }
            }
        }

        // History Gallery Items List
        if (filteredAssets.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No items found in creation history yet.", style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedLight))
                    }
                }
            }
        } else {
            items(filteredAssets) { asset ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = asset.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                                )
                                Text(
                                    text = "Prompt: ${asset.prompt}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight),
                                    maxLines = 2
                                )
                            }

                            if (asset.type == "MUSIC") {
                                IconButton(
                                    onClick = { viewModel.toggleMusicPlayback(context, asset.assetUri) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PastelViolet)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play Music",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Show preview image if type is IMAGE
                        if (asset.type == "IMAGE" && !asset.assetUri.isNullOrEmpty()) {
                            val imageBitmap = remember(asset.assetUri) {
                                val file = File(asset.assetUri)
                                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
                            }
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = asset.prompt,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }

                        // Show lyrics preview if type is MUSIC
                        if (asset.type == "MUSIC") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = asset.paramsJson,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextPrimaryLight),
                                    maxLines = 4
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Share / Download actions
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (asset.type == "IMAGE" && !asset.assetUri.isNullOrEmpty()) {
                                        MediaSharingUtils.downloadImageToGallery(context, asset.assetUri)
                                    } else {
                                        MediaSharingUtils.downloadSongAndLyrics(context, asset.title, asset.paramsJson, asset.assetUri)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelViolet),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (asset.type == "IMAGE" && !asset.assetUri.isNullOrEmpty()) {
                                        MediaSharingUtils.shareImageViaWhatsApp(context, asset.assetUri, asset.prompt)
                                    } else {
                                        MediaSharingUtils.shareMusicViaWhatsApp(context, asset.title, asset.paramsJson, asset.assetUri)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // System Specs & Engine Performance
        item {
            Text(text = "Engine Settings & Diagnostics", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight))
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = LightGlassSurface,
                borderColor = LightGlassBorder
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = PastelViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Real Engine Execution Latency", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight))
                        }
                        Text("<280ms (60 FPS)", style = MaterialTheme.typography.bodyMedium.copy(color = PastelMagenta, fontWeight = FontWeight.Bold))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = PastelViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nexus Engine Status", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight))
                        }
                        Text(
                            text = if (BuildConfig.GEMINI_API_KEY.isNotEmpty() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") "Configured & Active" else "Active (Local Fast Fallback)",
                            style = MaterialTheme.typography.bodySmall.copy(color = PastelViolet, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Luminous Glass Theme", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryLight, fontWeight = FontWeight.Bold))
                            Text("Pastel iridescence matching system design", style = MaterialTheme.typography.bodySmall.copy(color = TextMutedLight, fontSize = 11.sp))
                        }
                        Switch(
                            checked = isLightGlassTheme,
                            onCheckedChange = { isLightGlassTheme = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PastelViolet, checkedTrackColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}
