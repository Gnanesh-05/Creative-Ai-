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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush as CanvasBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ImageStudioScreen(
    viewModel: NexusViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prompt by viewModel.imagePrompt.collectAsState()
    val selectedStyle by viewModel.imageStyle.collectAsState()
    val selectedRatio by viewModel.imageAspectRatio.collectAsState()
    val isGenerating by viewModel.isGeneratingImage.collectAsState()
    val stepText by viewModel.generationStepText.collectAsState()
    val assets by viewModel.studioAssets.collectAsState()

    val imageAssets = assets.filter { it.type == "IMAGE" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = CanvasBrush.verticalGradient(
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
        // Prompt Input Field
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Image Prompt Assistant",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextPrimaryLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    // Intent Preservation Engine Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PastelViolet.copy(alpha = 0.15f))
                            .border(1.dp, PastelViolet.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✨ 100% Intent Preservation Engine",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PastelViolet,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { viewModel.setImagePrompt(it) },
                    placeholder = {
                        Text(text = "Describe your vision e.g. A cute golden retriever puppy in an astronaut helmet...", color = TextMutedLight)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.85f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.75f),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = TextPrimaryLight,
                        unfocusedTextColor = TextPrimaryLight
                    ),
                    minLines = 2
                )
            }
        }

        // Art Styles Selector
        item {
            Column {
                Text(
                    text = "Art & Photography Style",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextPrimaryLight, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val styles = listOf("Photography", "Digital Art", "Anime / Ghibli", "Cinematic 8K", "Oil Painting", "3D Render", "Watercolor", "Minimalist")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(styles) { style ->
                        val isSelected = style == selectedStyle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PastelViolet else Color.White.copy(alpha = 0.8f))
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setImageStyle(style) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = style,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondaryLight
                                )
                            )
                        }
                    }
                }
            }
        }

        // Aspect Ratio Selector
        item {
            Column {
                Text(
                    text = "Aspect Ratio",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextPrimaryLight, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                val ratios = listOf("1:1", "16:9", "9:16", "4:3")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ratios.forEach { ratio ->
                        val isSelected = ratio == selectedRatio
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) PastelViolet else Color.White.copy(alpha = 0.8f))
                                .border(1.dp, Color.White, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setImageAspectRatio(ratio) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = ratio,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextSecondaryLight
                                )
                            )
                        }
                    }
                }
            }
        }

        // Generate Button
        item {
            Button(
                onClick = { viewModel.generateImageAsset(context) },
                enabled = prompt.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PastelViolet,
                    disabledContainerColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stepText, color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Visual Image", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Studio Gallery showing REAL RENDERED IMAGES
        item {
            Text(
                text = "Generated Studio Gallery (${imageAssets.size})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight)
            )
        }

        if (imageAssets.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No images generated yet. Tap a quick prompt above!", style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedLight))
                    }
                }
            }
        } else {
            items(imageAssets) { asset ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = LightGlassSurface,
                    borderColor = LightGlassBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title & Prompt
                        Text(
                            text = asset.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimaryLight)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prompt: ${asset.prompt}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryLight)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // EXACT RENDERED IMAGE PREVIEW
                        val imageBitmap = remember(asset.assetUri) {
                            if (!asset.assetUri.isNullOrEmpty()) {
                                val file = File(asset.assetUri)
                                if (file.exists()) {
                                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                                } else null
                            } else null
                        }

                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = asset.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
                            )
                        } else {
                            // Fallback rendering
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(IridescentGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎨 Generating Image Canvas...", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Download & Share Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Download Button
                            Button(
                                onClick = {
                                    if (!asset.assetUri.isNullOrEmpty()) {
                                        MediaSharingUtils.downloadImageToGallery(context, asset.assetUri)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PastelViolet),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Share via WhatsApp Button
                            Button(
                                onClick = {
                                    if (!asset.assetUri.isNullOrEmpty()) {
                                        MediaSharingUtils.shareImageViaWhatsApp(context, asset.assetUri, asset.prompt)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
