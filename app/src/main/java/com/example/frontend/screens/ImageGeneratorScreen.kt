package com.example.frontend.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.backend.model.ImageResultDomain
import com.example.frontend.viewmodel.ImageViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImageGeneratorScreen(
    imageViewModel: ImageViewModel
) {
    val uiState by imageViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showAdvancedOptions by remember { mutableStateOf(false) }

    val styles = listOf(
        "Photorealistic",
        "Cinematic",
        "Professional photography",
        "Product photography",
        "Landscape photography",
        "Portrait photography",
        "Architecture",
        "Wildlife",
        "Fantasy",
        "Illustration",
        "Artistic styles"
    )

    val aspectRatios = listOf("1:1", "16:9", "9:16", "4:3", "3:4")
    val resolutions = listOf("1024x1024", "1920x1080", "2048x2048")
    val samplePrompts = listOf(
        "A realistic photo of a modern house beside a lake at sunset",
        "A photorealistic portrait of an old craftsman in his workshop",
        "A realistic street scene in Tokyo during rain with neon lights",
        "A professional product photograph of a luxury watch on black marble",
        "A realistic wildlife photograph of a tiger in a dense forest"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0C20))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("image_generator_screen_container")
    ) {
        // Header Banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4FD8EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    tint = Color(0xFF0F0C20),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Realistic AI Image Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Photorealistic Imagen 3 & Gemini Vision Engine",
                    fontSize = 12.sp,
                    color = Color(0xFF4FD8EB)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sample Prompt Quick Chips
        Text(
            text = "Try Realistic Inspiration:",
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(samplePrompts) { prompt ->
                SuggestionChip(
                    onClick = { imageViewModel.updatePrompt(prompt) },
                    label = { Text(prompt.take(28) + "...", fontSize = 11.sp, color = Color.White) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFF1E1938)
                    ),
                    modifier = Modifier.testTag("sample_prompt_chip")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Prompt Input Box
        OutlinedTextField(
            value = uiState.prompt,
            onValueChange = { imageViewModel.updatePrompt(it) },
            label = { Text("Natural Language Prompt") },
            placeholder = { Text("Describe a photorealistic scene, lighting, subject...") },
            minLines = 3,
            maxLines = 5,
            trailingIcon = {
                if (uiState.prompt.isNotEmpty()) {
                    IconButton(onClick = { imageViewModel.updatePrompt("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4FD8EB),
                unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                focusedLabelColor = Color(0xFF4FD8EB),
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("image_prompt_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Prompt Enhancement Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { imageViewModel.enhancePrompt() },
                enabled = uiState.prompt.isNotBlank() && !uiState.isEnhancingPrompt,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0BCFF)),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD0BCFF))
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("enhance_prompt_button")
            ) {
                if (uiState.isEnhancingPrompt) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFD0BCFF), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enhancing with Gemini...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Enhance Prompt with AI", fontSize = 12.sp)
                }
            }

            TextButton(
                onClick = { showAdvancedOptions = !showAdvancedOptions },
                modifier = Modifier.testTag("toggle_advanced_options")
            ) {
                Text(
                    text = if (showAdvancedOptions) "Hide Details" else "Advanced Settings",
                    color = Color(0xFF4FD8EB),
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = if (showAdvancedOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF4FD8EB),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Enhanced Prompt Card
        if (uiState.isPromptEnhanced && uiState.enhancedPrompt != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF231B42)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enhanced_prompt_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFD0BCFF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Enhanced Photography Prompt", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF))
                        }
                        IconButton(
                            onClick = { imageViewModel.clearEnhancedPrompt() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.enhancedPrompt ?: "",
                        onValueChange = { imageViewModel.updateEnhancedPrompt(it) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = Color(0xFFD0BCFF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("enhanced_prompt_input")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Style Selector Section
        Text(
            text = "Style Preset",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(styles) { style ->
                val isSelected = uiState.selectedStyle == style
                FilterChip(
                    selected = isSelected,
                    onClick = { imageViewModel.updateStyle(style) },
                    label = { Text(style, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4FD8EB),
                        selectedLabelColor = Color(0xFF0F0C20),
                        containerColor = Color(0xFF1E1938),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("style_chip_$style")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aspect Ratio Section
        Text(
            text = "Aspect Ratio",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            aspectRatios.forEach { ratio ->
                val isSelected = uiState.selectedAspectRatio == ratio
                FilterChip(
                    selected = isSelected,
                    onClick = { imageViewModel.updateAspectRatio(ratio) },
                    label = { Text(ratio, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD0BCFF),
                        selectedLabelColor = Color(0xFF0F0C20),
                        containerColor = Color(0xFF1E1938),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("aspect_ratio_$ratio")
                )
            }
        }

        // Advanced Options Collapsible Section
        AnimatedVisibility(visible = showAdvancedOptions) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                // Resolution Selector
                Text(text = "Resolution", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    resolutions.forEach { res ->
                        FilterChip(
                            selected = uiState.selectedResolution == res,
                            onClick = { imageViewModel.updateResolution(res) },
                            label = { Text(res, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF6750A4),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E1938),
                                labelColor = Color.LightGray
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Negative Prompt
                OutlinedTextField(
                    value = uiState.negativePrompt,
                    onValueChange = { imageViewModel.updateNegativePrompt(it) },
                    label = { Text("Negative Prompt (Exclude details)") },
                    placeholder = { Text("e.g. blur, low quality, distorted, extra limbs") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFB4AB),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("negative_prompt_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Progress & Status Bar
        if (uiState.isGenerating) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("generation_progress_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Synthesizing Photorealistic Render...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4FD8EB)
                        )
                        Text(
                            text = "${uiState.generationProgress}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { uiState.generationProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4FD8EB),
                        trackColor = Color(0xFF2A1B54)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { imageViewModel.cancelActiveJob() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB4AB)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFB4AB))
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("cancel_generation_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel Job", fontSize = 12.sp)
                    }
                }
            }
        }

        // Generate Action Button
        Button(
            onClick = { imageViewModel.generateImage() },
            enabled = uiState.prompt.isNotBlank() && !uiState.isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4FD8EB),
                disabledContainerColor = Color(0xFF2A1B54)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("image_generate_button")
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF0F0C20),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Generate Realistic Image",
                color = Color(0xFF0F0C20),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Error Banner
        uiState.errorMessage?.let { err ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color(0xFF93000A),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = err,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result Gallery Section
        if (uiState.results.isNotEmpty()) {
            Text(
                text = "Latest Renders",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.results.forEach { result ->
                    ResultImageCard(
                        result = result,
                        onView = { imageViewModel.selectImageForViewer(result) },
                        onShare = { imageViewModel.shareImage(context, result) },
                        onDownload = { imageViewModel.downloadImage(context, result) },
                        onDelete = { imageViewModel.deleteImageFromHistory(result.id) },
                        onRegenerate = {
                            imageViewModel.updatePrompt(result.prompt)
                            imageViewModel.generateImage()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History Gallery Section
        if (uiState.history.isNotEmpty()) {
            Text(
                text = "Creation History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.history) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
                        modifier = Modifier
                            .width(140.dp)
                            .clickable { imageViewModel.selectImageForViewer(item) }
                            .testTag("history_item_${item.id}")
                    ) {
                        Column {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                            )
                            Text(
                                text = item.prompt,
                                fontSize = 11.sp,
                                color = Color.White,
                                maxLines = 2,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Screen Image Viewer Modal
    uiState.selectedImageForViewer?.let { img ->
        Dialog(
            onDismissRequest = { imageViewModel.selectImageForViewer(null) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Full High-Res View",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { imageViewModel.selectImageForViewer(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AsyncImage(
                        model = img.imageUrl,
                        contentDescription = img.prompt,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .testTag("fullscreen_image_view")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = img.prompt,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { imageViewModel.shareImage(context, img) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FD8EB))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF0F0C20))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", color = Color(0xFF0F0C20))
                        }

                        Button(
                            onClick = { imageViewModel.downloadImage(context, img) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0BCFF))
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0F0C20))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", color = Color(0xFF0F0C20))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultImageCard(
    result: ImageResultDomain,
    onView: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onRegenerate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1938)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("result_card_${result.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onView() }
            ) {
                AsyncImage(
                    model = result.imageUrl,
                    contentDescription = result.prompt,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = result.stylePreset,
                        color = Color(0xFF4FD8EB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.prompt,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2
            )

            if (!result.enhancedPrompt.isNullByBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enhanced: ${result.enhancedPrompt}",
                    fontSize = 11.sp,
                    color = Color(0xFFD0BCFF),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp).testTag("share_image_button")) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF4FD8EB), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDownload, modifier = Modifier.size(36.dp).testTag("download_image_button")) {
                        Icon(Icons.Default.Download, contentDescription = "Save", tint = Color(0xFFD0BCFF), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onRegenerate, modifier = Modifier.size(36.dp).testTag("regenerate_image_button")) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp).testTag("delete_image_button")) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFFFB4AB), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun String?.isNullByBlank(): Boolean = this == null || this.isBlank()
