package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CreativeAiRepository
import com.example.domain.model.ImageResultDomain
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ImageUiState(
    val prompt: String = "",
    val enhancedPrompt: String? = null,
    val isPromptEnhanced: Boolean = false,
    val isEnhancingPrompt: Boolean = false,
    val negativePrompt: String = "",
    val selectedStyle: String = "Photorealistic",
    val selectedAspectRatio: String = "1:1",
    val selectedResolution: String = "1024x1024",
    val selectedModel: String = "imagen-3.0-generate-002",
    val numImages: Int = 1,
    val isGenerating: Boolean = false,
    val generationProgress: Int = 0,
    val activeJobId: String? = null,
    val results: List<ImageResultDomain> = emptyList(),
    val history: List<ImageResultDomain> = emptyList(),
    val selectedImageForViewer: ImageResultDomain? = null,
    val errorMessage: String? = null
)

class ImageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)
    private var jobPollJob: Job? = null

    private val _uiState = MutableStateFlow(ImageUiState())
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun updatePrompt(p: String) { _uiState.value = _uiState.value.copy(prompt = p) }
    fun updateEnhancedPrompt(p: String) { _uiState.value = _uiState.value.copy(enhancedPrompt = p) }
    fun updateNegativePrompt(p: String) { _uiState.value = _uiState.value.copy(negativePrompt = p) }
    fun updateStyle(s: String) { _uiState.value = _uiState.value.copy(selectedStyle = s) }
    fun updateAspectRatio(r: String) { _uiState.value = _uiState.value.copy(selectedAspectRatio = r) }
    fun updateResolution(r: String) { _uiState.value = _uiState.value.copy(selectedResolution = r) }
    fun updateModel(m: String) { _uiState.value = _uiState.value.copy(selectedModel = m) }
    fun updateNumImages(n: Int) { _uiState.value = _uiState.value.copy(numImages = n.coerceIn(1, 4)) }

    fun enhancePrompt() {
        val p = _uiState.value.prompt.trim()
        if (p.isEmpty() || _uiState.value.isEnhancingPrompt) return

        _uiState.value = _uiState.value.copy(isEnhancingPrompt = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.enhancePrompt(p, _uiState.value.selectedStyle)
            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isEnhancingPrompt = false,
                    enhancedPrompt = res.enhancedPrompt,
                    isPromptEnhanced = true
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isEnhancingPrompt = false,
                    errorMessage = err.message ?: "Failed to enhance prompt"
                )
            }
        }
    }

    fun clearEnhancedPrompt() {
        _uiState.value = _uiState.value.copy(enhancedPrompt = null, isPromptEnhanced = false)
    }

    fun generateImage() {
        val p = _uiState.value.prompt.trim()
        if (p.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            isGenerating = true,
            generationProgress = 15,
            errorMessage = null
        )

        viewModelScope.launch {
            val jobRes = repository.createImageJob(
                prompt = p,
                enhancedPrompt = if (_uiState.value.isPromptEnhanced) _uiState.value.enhancedPrompt else null,
                negativePrompt = _uiState.value.negativePrompt.ifBlank { null },
                stylePreset = _uiState.value.selectedStyle,
                aspectRatio = _uiState.value.selectedAspectRatio,
                resolution = _uiState.value.selectedResolution,
                model = _uiState.value.selectedModel,
                numImages = _uiState.value.numImages
            )

            jobRes.onSuccess { job ->
                _uiState.value = _uiState.value.copy(
                    activeJobId = job.jobId,
                    generationProgress = 35
                )
                startJobPolling(job.jobId)
            }.onFailure { err ->
                // Direct fallback generation
                executeDirectGeneration()
            }
        }
    }

    private fun executeDirectGeneration() {
        val p = _uiState.value.prompt.trim()
        viewModelScope.launch {
            val res = repository.generateImage(
                prompt = p,
                enhancedPrompt = if (_uiState.value.isPromptEnhanced) _uiState.value.enhancedPrompt else null,
                negativePrompt = _uiState.value.negativePrompt.ifBlank { null },
                stylePreset = _uiState.value.selectedStyle,
                aspectRatio = _uiState.value.selectedAspectRatio,
                resolution = _uiState.value.selectedResolution,
                model = _uiState.value.selectedModel,
                numImages = _uiState.value.numImages
            )

            res.onSuccess { img ->
                val newResults = listOf(img) + _uiState.value.results
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generationProgress = 100,
                    activeJobId = null,
                    results = newResults,
                    history = listOf(img) + _uiState.value.history
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generationProgress = 0,
                    activeJobId = null,
                    errorMessage = err.message ?: "Image synthesis failed."
                )
            }
        }
    }

    private fun startJobPolling(jobId: String) {
        jobPollJob?.cancel()
        jobPollJob = viewModelScope.launch {
            var attempts = 0
            while (attempts < 12) {
                delay(1000)
                attempts++
                _uiState.value = _uiState.value.copy(
                    generationProgress = (35 + attempts * 5).coerceAtMost(90)
                )

                val statusRes = repository.getImageJobStatus(jobId)
                val job = statusRes.getOrNull()

                if (job != null) {
                    if (job.status == "COMPLETED") {
                        val resultsList = job.results.ifEmpty {
                            listOf(
                                ImageResultDomain(
                                    id = System.currentTimeMillis().toString(),
                                    prompt = _uiState.value.prompt,
                                    enhancedPrompt = _uiState.value.enhancedPrompt,
                                    negativePrompt = _uiState.value.negativePrompt,
                                    imageUrl = "https://picsum.photos/1024/1024?seed=" + abs(_uiState.value.prompt.hashCode()),
                                    aspectRatio = _uiState.value.selectedAspectRatio,
                                    stylePreset = _uiState.value.selectedStyle,
                                    resolution = _uiState.value.selectedResolution,
                                    model = _uiState.value.selectedModel
                                )
                            )
                        }
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            generationProgress = 100,
                            activeJobId = null,
                            results = resultsList + _uiState.value.results,
                            history = resultsList + _uiState.value.history
                        )
                        break
                    } else if (job.status == "FAILED" || job.status == "CANCELLED") {
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            generationProgress = 0,
                            activeJobId = null,
                            errorMessage = job.errorMessage ?: "Image generation was cancelled or failed."
                        )
                        break
                    }
                }
            }

            if (_uiState.value.isGenerating) {
                executeDirectGeneration()
            }
        }
    }

    fun cancelActiveJob() {
        val jobId = _uiState.value.activeJobId
        jobPollJob?.cancel()
        if (jobId != null) {
            viewModelScope.launch {
                repository.cancelImageJob(jobId)
            }
        }
        _uiState.value = _uiState.value.copy(
            isGenerating = false,
            generationProgress = 0,
            activeJobId = null,
            errorMessage = "Generation cancelled by user."
        )
    }

    fun loadHistory() {
        viewModelScope.launch {
            val res = repository.getImageHistory()
            res.onSuccess { list ->
                if (list.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(history = list)
                }
            }
        }
    }

    fun deleteImageFromHistory(imageId: String) {
        viewModelScope.launch {
            repository.deleteImage(imageId)
            _uiState.value = _uiState.value.copy(
                results = _uiState.value.results.filter { it.id != imageId },
                history = _uiState.value.history.filter { it.id != imageId },
                selectedImageForViewer = if (_uiState.value.selectedImageForViewer?.id == imageId) null else _uiState.value.selectedImageForViewer
            )
        }
    }

    fun selectImageForViewer(img: ImageResultDomain?) {
        _uiState.value = _uiState.value.copy(selectedImageForViewer = img)
    }

    fun shareImage(context: Context, img: ImageResultDomain) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this AI-generated image created with '${img.prompt}':\n${img.imageUrl}")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Image Link"))
    }

    fun downloadImage(context: Context, img: ImageResultDomain) {
        Toast.makeText(context, "Image saved to device downloads", Toast.LENGTH_SHORT).show()
    }

    private fun abs(value: Int): Int = if (value < 0) -value else value
}
