package com.example.frontend.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend.model.AgentType
import com.example.backend.model.AiModelInfo
import com.example.backend.model.AiModelRegistry
import com.example.backend.model.ChatMessage
import com.example.backend.model.MemoryFact
import com.example.backend.model.StudioAsset
import com.example.backend.repository.AiRepository
import com.example.backend.util.AudioSynthesizerEngine
import com.example.backend.util.ImageGeneratorEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NexusViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AiRepository(application)

    // Session State
    private val _currentSessionId = MutableStateFlow(1L)
    val currentSessionId: StateFlow<Long> = _currentSessionId.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessage>> = repository.getMessagesForSession(1L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val memoryFacts: StateFlow<List<MemoryFact>> = repository.getAllMemoryFacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val studioAssets: StateFlow<List<StudioAsset>> = repository.getAllStudioAssets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active Settings
    private val _selectedModel = MutableStateFlow<AiModelInfo>(AiModelRegistry.GEMINI_3_5_FLASH)
    val selectedModel: StateFlow<AiModelInfo> = _selectedModel.asStateFlow()

    private val _selectedAgent = MutableStateFlow<AgentType>(AgentType.PLANNER)
    val selectedAgent: StateFlow<AgentType> = _selectedAgent.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _thinkingStatusText = MutableStateFlow("Thinking...")
    val thinkingStatusText: StateFlow<String> = _thinkingStatusText.asStateFlow()

    private val _isVoiceActive = MutableStateFlow(false)
    val isVoiceActive: StateFlow<Boolean> = _isVoiceActive.asStateFlow()

    // Image Studio States
    private val _imagePrompt = MutableStateFlow("")
    val imagePrompt: StateFlow<String> = _imagePrompt.asStateFlow()

    private val _imageStyle = MutableStateFlow("Photography")
    val imageStyle: StateFlow<String> = _imageStyle.asStateFlow()

    private val _imageAspectRatio = MutableStateFlow("1:1")
    val imageAspectRatio: StateFlow<String> = _imageAspectRatio.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage.asStateFlow()

    // Music Studio States
    private val _musicPrompt = MutableStateFlow("A relaxing lofi beat for studying on a rainy afternoon")
    val musicPrompt: StateFlow<String> = _musicPrompt.asStateFlow()

    private val _musicGenre = MutableStateFlow("Lo-fi")
    val musicGenre: StateFlow<String> = _musicGenre.asStateFlow()

    private val _musicMood = MutableStateFlow("Chill & Relaxing")
    val musicMood: StateFlow<String> = _musicMood.asStateFlow()

    private val _musicTempo = MutableStateFlow(85f) // BPM
    val musicTempo: StateFlow<Float> = _musicTempo.asStateFlow()

    private val _selectedInstruments = MutableStateFlow(listOf("Piano", "Synth", "Drums"))
    val selectedInstruments: StateFlow<List<String>> = _selectedInstruments.asStateFlow()

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _isGeneratingMusic = MutableStateFlow(false)
    val isGeneratingMusic: StateFlow<Boolean> = _isGeneratingMusic.asStateFlow()

    private val _generationStepText = MutableStateFlow("⚡ Fast Neural Pipeline Active...")
    val generationStepText: StateFlow<String> = _generationStepText.asStateFlow()

    init {
        // Empty initial state ready for user input
    }

    fun selectModel(model: AiModelInfo) {
        _selectedModel.value = model
    }

    fun selectAgent(agent: AgentType) {
        _selectedAgent.value = agent
    }

    fun toggleVoiceMode() {
        _isVoiceActive.value = !_isVoiceActive.value
    }

    fun sendMessage(text: String, imageUri: String? = null) {
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.saveUserMessage(_currentSessionId.value, text, imageUri)

            _isThinking.value = true
            _thinkingStatusText.value = "Analyzing intent..."
            delay(300)

            _thinkingStatusText.value = "Searching Memory & Context..."
            delay(400)

            _thinkingStatusText.value = "Routing query..."
            delay(400)

            _thinkingStatusText.value = "Gathering response..."

            repository.processUserQuery(
                sessionId = _currentSessionId.value,
                userQuery = text,
                selectedModelId = _selectedModel.value.id,
                selectedAgent = _selectedAgent.value
            )

            _isThinking.value = false
        }
    }

    fun addMemoryFact(category: String, fact: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.addMemoryFact(category, fact, isPinned)
        }
    }

    fun deleteMemoryFact(id: Long) {
        viewModelScope.launch {
            repository.deleteMemoryFact(id)
        }
    }

    fun togglePinFact(id: Long, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinMemoryFact(id, currentPinned)
        }
    }

    // Image Studio Actions
    fun setImagePrompt(prompt: String) { _imagePrompt.value = prompt }
    fun setImageStyle(style: String) { _imageStyle.value = style }
    fun setImageAspectRatio(ratio: String) { _imageAspectRatio.value = ratio }

    fun generateImageAsset(context: Context, promptOverride: String? = null) {
        val targetPrompt = promptOverride ?: _imagePrompt.value
        if (targetPrompt.isBlank()) return
        if (promptOverride != null) {
            _imagePrompt.value = promptOverride
        }
        viewModelScope.launch {
            _isGeneratingImage.value = true
            _generationStepText.value = "⚡ Initializing Fast Latents..."
            delay(400)
            _generationStepText.value = "🎨 Processing 100% Precision Match..."
            
            val imagePath = withContext(Dispatchers.IO) {
                ImageGeneratorEngine.generateAndSaveImage(
                    context = context,
                    prompt = targetPrompt,
                    style = _imageStyle.value,
                    aspectRatio = _imageAspectRatio.value
                )
            }

            _generationStepText.value = "✨ Finalizing High-Res Visual Asset..."
            delay(300)

            val title = if (targetPrompt.length > 30) targetPrompt.take(28) + "..." else targetPrompt
            val params = "Style: ${_imageStyle.value} | Aspect: ${_imageAspectRatio.value} | 2.0s Fast Engine"
            repository.saveStudioAsset("IMAGE", title, targetPrompt, params, imagePath)
            _isGeneratingImage.value = false
        }
    }

    // Music & Lyrics Studio Actions
    fun setMusicPrompt(prompt: String) { _musicPrompt.value = prompt }
    fun setMusicGenre(genre: String) { _musicGenre.value = genre }
    fun setMusicMood(mood: String) { _musicMood.value = mood }
    fun setMusicTempo(tempo: Float) { _musicTempo.value = tempo }
    fun toggleInstrument(inst: String) {
        val current = _selectedInstruments.value.toMutableList()
        if (current.contains(inst)) current.remove(inst) else current.add(inst)
        _selectedInstruments.value = current
    }

    fun toggleMusicPlayback(context: Context, audioPath: String? = null) {
        val currentlyPlaying = AudioSynthesizerEngine.isPlaying()
        if (currentlyPlaying) {
            AudioSynthesizerEngine.stopAudio()
            _isMusicPlaying.value = false
        } else {
            if (!audioPath.isNullOrEmpty()) {
                AudioSynthesizerEngine.playAudio(context, audioPath) {
                    _isMusicPlaying.value = false
                }
                _isMusicPlaying.value = true
            } else {
                AudioSynthesizerEngine.playAudio(context, "") {
                    _isMusicPlaying.value = false
                }
                _isMusicPlaying.value = true
            }
        }
    }

    fun generateMusicTrack(context: Context, promptOverride: String? = null) {
        val targetPrompt = promptOverride ?: _musicPrompt.value
        if (targetPrompt.isBlank()) return
        if (promptOverride != null) {
            _musicPrompt.value = promptOverride
        }
        viewModelScope.launch {
            _isGeneratingMusic.value = true
            _generationStepText.value = "⚡ Synthesizing Stem Harmonics..."
            delay(400)
            _generationStepText.value = "✍️ Writing Verse & Chorus Lyrics..."

            val audioPath = withContext(Dispatchers.IO) {
                AudioSynthesizerEngine.generateAndSaveAudio(
                    context = context,
                    prompt = targetPrompt,
                    genre = _musicGenre.value,
                    bpm = _musicTempo.value
                )
            }

            _generationStepText.value = "🎼 Aligning Chord Progressions..."
            delay(400)

            val title = if (targetPrompt.length > 28) targetPrompt.take(26) + "..." else targetPrompt
            val lyricsJson = generateDynamicLyrics(targetPrompt, _musicGenre.value, _musicMood.value, _musicTempo.value)
            repository.saveStudioAsset("MUSIC", title, targetPrompt, lyricsJson, audioPath)
            _isGeneratingMusic.value = false
            
            // Auto play the newly generated music
            AudioSynthesizerEngine.playAudio(context, audioPath) {
                _isMusicPlaying.value = false
            }
            _isMusicPlaying.value = true
        }
    }

    private fun generateDynamicLyrics(prompt: String, genre: String, mood: String, tempo: Float): String {
        val topic = prompt.trim()
            .replace(Regex("^[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF\\uFE0F]+\\s*"), "")
            .trim()
        val words = topic.split(Regex("\\s+")).filter { it.length > 2 }
        val mainSubject = words.firstOrNull { 
            !it.equals("about", ignoreCase = true) && 
            !it.equals("track", ignoreCase = true) &&
            !it.equals("song", ignoreCase = true) &&
            !it.equals("with", ignoreCase = true)
        } ?: topic.take(18)
        
        val keyAndChords = when {
            genre.contains("lofi", ignoreCase = true) || genre.contains("chill", ignoreCase = true) -> "Key: D Minor | Chords: Dm7 - G7 - Cmaj7 - Am7"
            genre.contains("synth", ignoreCase = true) || genre.contains("80s", ignoreCase = true) -> "Key: A Minor | Chords: Am - F - C - G"
            genre.contains("rock", ignoreCase = true) || genre.contains("metal", ignoreCase = true) -> "Key: E Minor | Chords: E5 - G5 - C5 - D5"
            genre.contains("edm", ignoreCase = true) || genre.contains("dance", ignoreCase = true) -> "Key: E Minor | Chords: Em - C - G - D"
            genre.contains("jazz", ignoreCase = true) -> "Key: F Major | Chords: Fmaj7 - Dm7 - Gm7 - C7"
            else -> "Key: C Major | Chords: C - Am - F - G"
        }

        val sanitizedSubject = mainSubject.lowercase().replaceFirstChar { it.uppercase() }

        return buildString {
            append("Genre: $genre | Mood: $mood | Tempo: ${tempo.toInt()} BPM\n")
            append("$keyAndChords\n\n")
            append("[Verse 1]\n")
            append("Step into the flow of $topic,\n")
            append("Echoes in the air, feeling pure and hypnotic.\n")
            append("Rhythm holding steady with a $mood design,\n")
            append("Every single moment coming into line.\n\n")
            append("[Chorus]\n")
            append("Oh, $sanitizedSubject shining in the light,\n")
            append("Turn the volume up as we take off in flight.\n")
            append("Driven by the $genre beat, moving in harmony,\n")
            append("Living in this moment, wild and free!\n\n")
            append("[Verse 2]\n")
            append("Fading out the noise, keeping focus crystal clear,\n")
            append("Nothing in the distance that we have to fear.\n")
            append("The melodies align with $sanitizedSubject in our mind,\n")
            append("One of a kind, the sweetest rhythm we could find.\n\n")
            append("[Outro]\n")
            append("Soft resonance fading into the sky... $sanitizedSubject forever.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        AudioSynthesizerEngine.stopAudio()
    }
}
