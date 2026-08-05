package com.example.ui.viewmodels

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.MusicComposeRequest
import com.example.data.repository.CreativeAiRepository
import com.example.domain.model.MusicResultDomain
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 30000,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isBuffering: Boolean = false
)

data class MusicUiState(
    val prompt: String = "Relaxing lofi beats for late night coding with soft piano and vinyl crackle",
    val enhancedPrompt: String? = null,
    val isEnhancing: Boolean = false,
    val genre: String = "Lo-Fi Beats",
    val mood: String = "Relaxing",
    val tempoBpm: Int = 90,
    val durationSeconds: Int = 30,
    val keySignature: String = "C Major",
    val instruments: String = "Grand Piano, Strings, Soft Synth",
    val energyLevel: String = "Medium",
    val isInstrumental: Boolean = true,
    val lyrics: String = "",
    val selectedModel: String = "musicgen-stereo-large",
    val isGenerating: Boolean = false,
    val generationProgress: Int = 0,
    val activeJobId: String? = null,
    val currentTrack: MusicResultDomain? = null,
    val history: List<MusicResultDomain> = emptyList(),
    val savedTracks: List<MusicResultDomain> = emptyList(),
    val playerState: AudioPlayerState = AudioPlayerState(),
    val activeTab: Int = 0, // 0 = Composer, 1 = Recent History, 2 = Bookmarks
    val errorMessage: String? = null
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var progressTickerJob: Job? = null
    private var pollingJob: Job? = null

    init {
        loadHistory()
    }

    fun updatePrompt(p: String) { _uiState.value = _uiState.value.copy(prompt = p) }
    fun updateGenre(g: String) { _uiState.value = _uiState.value.copy(genre = g) }
    fun updateMood(m: String) { _uiState.value = _uiState.value.copy(mood = m) }
    fun updateTempo(t: Int) { _uiState.value = _uiState.value.copy(tempoBpm = t) }
    fun updateDuration(d: Int) { _uiState.value = _uiState.value.copy(durationSeconds = d) }
    fun updateKeySignature(k: String) { _uiState.value = _uiState.value.copy(keySignature = k) }
    fun updateInstruments(i: String) { _uiState.value = _uiState.value.copy(instruments = i) }
    fun updateEnergyLevel(e: String) { _uiState.value = _uiState.value.copy(energyLevel = e) }
    fun updateIsInstrumental(inst: Boolean) { _uiState.value = _uiState.value.copy(isInstrumental = inst) }
    fun updateLyrics(l: String) { _uiState.value = _uiState.value.copy(lyrics = l) }
    fun updateModel(m: String) { _uiState.value = _uiState.value.copy(selectedModel = m) }
    fun setActiveTab(tab: Int) { _uiState.value = _uiState.value.copy(activeTab = tab) }

    fun enhancePrompt() {
        val p = _uiState.value.prompt.trim()
        if (p.isEmpty()) return

        _uiState.value = _uiState.value.copy(isEnhancing = true, errorMessage = null)
        viewModelScope.launch {
            val res = repository.enhanceMusicPrompt(p, _uiState.value.genre, _uiState.value.mood)
            res.onSuccess { domain ->
                _uiState.value = _uiState.value.copy(
                    isEnhancing = false,
                    enhancedPrompt = domain.enhancedPrompt
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isEnhancing = false,
                    errorMessage = "Enhancement failed: ${err.message}"
                )
            }
        }
    }

    fun clearEnhancedPrompt() {
        _uiState.value = _uiState.value.copy(enhancedPrompt = null)
    }

    fun generateMusic() {
        val promptText = _uiState.value.enhancedPrompt ?: _uiState.value.prompt.trim()
        if (promptText.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            isGenerating = true,
            generationProgress = 10,
            errorMessage = null
        )

        viewModelScope.launch {
            val jobRes = repository.createMusicJob(
                prompt = _uiState.value.prompt,
                enhancedPrompt = _uiState.value.enhancedPrompt,
                genre = _uiState.value.genre,
                mood = _uiState.value.mood,
                tempoBpm = _uiState.value.tempoBpm,
                durationSeconds = _uiState.value.durationSeconds,
                keySignature = _uiState.value.keySignature,
                instruments = _uiState.value.instruments,
                energyLevel = _uiState.value.energyLevel,
                isInstrumental = _uiState.value.isInstrumental,
                lyrics = if (_uiState.value.isInstrumental) null else _uiState.value.lyrics,
                model = _uiState.value.selectedModel
            )

            jobRes.onSuccess { job ->
                _uiState.value = _uiState.value.copy(activeJobId = job.jobId)
                startPollingJobStatus(job.jobId)
            }.onFailure {
                // Direct sync generation fallback
                performSyncGeneration()
            }
        }
    }

    private fun startPollingJobStatus(jobId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            var completed = false
            var attempts = 0
            while (!completed && attempts < 20) {
                delay(1200)
                attempts++
                val res = repository.getMusicJobStatus(jobId)
                res.onSuccess { job ->
                    _uiState.value = _uiState.value.copy(generationProgress = job.progress)
                    if (job.status == "COMPLETED") {
                        completed = true
                        val track = job.results.firstOrNull()
                        if (track != null) {
                            onTrackGenerated(track)
                        } else {
                            performSyncGeneration()
                        }
                    } else if (job.status == "FAILED" || job.status == "CANCELLED") {
                        completed = true
                        if (job.status == "FAILED") {
                            _uiState.value = _uiState.value.copy(
                                isGenerating = false,
                                errorMessage = job.errorMessage ?: "Music generation failed"
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(isGenerating = false)
                        }
                    }
                }
            }
            if (!completed) {
                performSyncGeneration()
            }
        }
    }

    private fun performSyncGeneration() {
        viewModelScope.launch {
            val res = repository.generateMusic(
                prompt = _uiState.value.prompt,
                enhancedPrompt = _uiState.value.enhancedPrompt,
                genre = _uiState.value.genre,
                mood = _uiState.value.mood,
                tempoBpm = _uiState.value.tempoBpm,
                durationSeconds = _uiState.value.durationSeconds,
                keySignature = _uiState.value.keySignature,
                instruments = _uiState.value.instruments,
                energyLevel = _uiState.value.energyLevel,
                isInstrumental = _uiState.value.isInstrumental,
                lyrics = if (_uiState.value.isInstrumental) null else _uiState.value.lyrics,
                model = _uiState.value.selectedModel
            )

            res.onSuccess { track ->
                onTrackGenerated(track)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = err.message ?: "Music generation failed."
                )
            }
        }
    }

    private fun onTrackGenerated(track: MusicResultDomain) {
        _uiState.value = _uiState.value.copy(
            isGenerating = false,
            generationProgress = 100,
            activeJobId = null,
            currentTrack = track,
            history = listOf(track) + _uiState.value.history.filter { it.id != track.id }
        )
        playTrack(track)
    }

    fun cancelActiveJob() {
        val jobId = _uiState.value.activeJobId
        pollingJob?.cancel()
        if (jobId != null) {
            viewModelScope.launch {
                repository.cancelMusicJob(jobId)
            }
        }
        _uiState.value = _uiState.value.copy(isGenerating = false, activeJobId = null, generationProgress = 0)
    }

    fun composeMusic() {
        generateMusic()
    }

    // --- Audio Player Logic ---
    fun playTrack(track: MusicResultDomain) {
        _uiState.value = _uiState.value.copy(currentTrack = track)
        prepareAndPlayAudio(track.audioUrl)
    }

    private fun prepareAndPlayAudio(url: String) {
        try {
            stopAudio()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { mp ->
                    mp.start()
                    val dur = mp.duration.coerceAtLeast(1000)
                    _uiState.value = _uiState.value.copy(
                        playerState = _uiState.value.playerState.copy(
                            isPlaying = true,
                            durationMs = dur,
                            isBuffering = false
                        )
                    )
                    startProgressTicker()
                }
                setOnCompletionListener {
                    _uiState.value = _uiState.value.copy(
                        playerState = _uiState.value.playerState.copy(
                            isPlaying = false,
                            currentPositionMs = 0
                        )
                    )
                    progressTickerJob?.cancel()
                }
                setOnErrorListener { _, _, _ ->
                    _uiState.value = _uiState.value.copy(
                        playerState = _uiState.value.playerState.copy(isPlaying = false, isBuffering = false)
                    )
                    true
                }
                prepareAsync()
            }
            _uiState.value = _uiState.value.copy(
                playerState = _uiState.value.playerState.copy(isBuffering = true)
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                playerState = _uiState.value.playerState.copy(isPlaying = false, isBuffering = false)
            )
        }
    }

    fun togglePlayback() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _uiState.value = _uiState.value.copy(
                    playerState = _uiState.value.playerState.copy(isPlaying = false)
                )
                progressTickerJob?.cancel()
            } else {
                mp.start()
                _uiState.value = _uiState.value.copy(
                    playerState = _uiState.value.playerState.copy(isPlaying = true)
                )
                startProgressTicker()
            }
        } ?: run {
            _uiState.value.currentTrack?.let { playTrack(it) }
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { mp ->
            mp.seekTo(positionMs)
            _uiState.value = _uiState.value.copy(
                playerState = _uiState.value.playerState.copy(currentPositionMs = positionMs)
            )
        }
    }

    fun setVolume(vol: Float) {
        mediaPlayer?.setVolume(vol, vol)
        _uiState.value = _uiState.value.copy(
            playerState = _uiState.value.playerState.copy(
                volume = vol,
                isMuted = vol == 0f
            )
        )
    }

    fun toggleMute() {
        val curState = _uiState.value.playerState
        if (curState.isMuted) {
            setVolume(1.0f)
        } else {
            setVolume(0.0f)
        }
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = viewModelScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                val pos = mediaPlayer?.currentPosition ?: 0
                _uiState.value = _uiState.value.copy(
                    playerState = _uiState.value.playerState.copy(currentPositionMs = pos)
                )
                delay(300)
            }
        }
    }

    private fun stopAudio() {
        progressTickerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // --- Actions ---
    fun toggleBookmark(track: MusicResultDomain) {
        viewModelScope.launch {
            repository.toggleSaveMusicTrack(track.id)
            val updatedHistory = _uiState.value.history.map {
                if (it.id == track.id) it.copy(isSaved = !it.isSaved) else it
            }
            val updatedSaved = updatedHistory.filter { it.isSaved }
            val updatedCurrent = if (_uiState.value.currentTrack?.id == track.id) {
                _uiState.value.currentTrack?.copy(isSaved = !_uiState.value.currentTrack!!.isSaved)
            } else _uiState.value.currentTrack

            _uiState.value = _uiState.value.copy(
                history = updatedHistory,
                savedTracks = updatedSaved,
                currentTrack = updatedCurrent
            )
        }
    }

    fun deleteTrack(track: MusicResultDomain) {
        viewModelScope.launch {
            repository.deleteMusicTrack(track.id)
            if (_uiState.value.currentTrack?.id == track.id) {
                stopAudio()
            }
            val updatedHistory = _uiState.value.history.filter { it.id != track.id }
            val updatedSaved = _uiState.value.savedTracks.filter { it.id != track.id }
            val nextTrack = if (_uiState.value.currentTrack?.id == track.id) updatedHistory.firstOrNull() else _uiState.value.currentTrack

            _uiState.value = _uiState.value.copy(
                history = updatedHistory,
                savedTracks = updatedSaved,
                currentTrack = nextTrack
            )
        }
    }

    fun createVariation(track: MusicResultDomain) {
        _uiState.value = _uiState.value.copy(
            prompt = "Variation of ${track.prompt}",
            genre = track.genre,
            mood = track.mood,
            tempoBpm = track.tempoBpm,
            durationSeconds = track.durationSeconds,
            keySignature = track.keySignature,
            activeTab = 0
        )
        generateMusic()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val res = repository.getMusicHistory()
            res.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    history = list,
                    savedTracks = list.filter { it.isSaved },
                    currentTrack = _uiState.value.currentTrack ?: list.firstOrNull()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        pollingJob?.cancel()
    }
}
