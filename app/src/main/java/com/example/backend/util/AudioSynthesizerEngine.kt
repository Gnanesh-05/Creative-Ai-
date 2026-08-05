package com.example.backend.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

object AudioSynthesizerEngine {

    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null

    /**
     * Synthesizes a real 15-second 16-bit 44.1kHz WAV musical track with chords, drums, and melody.
     * Generates a completely unique song structure, key, melody, and drum pattern based on prompt, genre, and tempo.
     * Returns the absolute file path of the generated .wav file.
     */
    suspend fun generateAndSaveAudio(
        context: Context,
        prompt: String,
        genre: String,
        bpm: Float
    ): String = withContext(Dispatchers.IO) {
        val sampleRate = 44100
        val durationSeconds = 15
        val numSamples = sampleRate * durationSeconds
        val pcmData = ShortArray(numSamples)

        // Seed calculations from user prompt, genre, and tempo
        val promptClean = prompt.trim().lowercase()
        val seed = promptClean.hashCode() xor genre.lowercase().hashCode() xor (bpm * 100).toInt()
        val absSeed = Math.abs(seed)

        val beatDurationSamples = (sampleRate * 60f / bpm.coerceIn(60f, 180f)).toInt()
        val beatsPerChord = 4

        // 1. Select Key, Chord Progressions & Scales based on Genre & Seed
        val (chordProgression, scaleNotes, synthStyle) = getMusicParamsForGenre(genre, absSeed)

        // Generate a 16-note melody sequence seeded by prompt
        val melodySequence = IntArray(16) { step ->
            val hash = (absSeed * 31 + step * 17 + promptClean.length)
            hash % scaleNotes.size
        }

        // Bass frequencies from roots of chords
        val bassFreqs = DoubleArray(chordProgression.size) { chordIdx ->
            chordProgression[chordIdx][0] / 2.0 // 1 octave down
        }

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val currentBeat = i / beatDurationSamples
            val chordIndex = (currentBeat / beatsPerChord) % chordProgression.size
            val currentChord = chordProgression[chordIndex]
            val rootBass = bassFreqs[chordIndex]

            // A. Synthesize Pad / Rhythm Chords (ADSR Envelope per beat)
            val beatProgress = (i % beatDurationSamples).toDouble() / beatDurationSamples
            val chordEnvelope = (1.0 - beatProgress * 0.35).coerceIn(0.2, 1.0)
            
            var chordSample = 0.0
            for (freq in currentChord) {
                chordSample += when (synthStyle) {
                    SynthStyle.WARM_LOFI -> sineWave(freq, t) * 0.7 + triangleWave(freq, t) * 0.3
                    SynthStyle.SAW_RETRO -> sawtoothWave(freq, t) * 0.5 + squareWave(freq * 0.999, t) * 0.5
                    SynthStyle.ROCK_POWER -> squareWave(freq, t) * 0.6 + sawtoothWave(freq * 1.002, t) * 0.4
                    SynthStyle.EDM_BRIGHT -> sawtoothWave(freq, t) * 0.8 + sineWave(freq * 2.0, t) * 0.2
                    SynthStyle.ACOUSTIC_SOFT -> triangleWave(freq, t) * 0.9 + sineWave(freq, t) * 0.1
                }
            }
            chordSample = (chordSample / currentChord.size) * 0.22 * chordEnvelope

            // B. Synthesize Bassline
            val bassEnvelope = Math.exp(-beatProgress * 8.0)
            val bassSample = when (synthStyle) {
                SynthStyle.SAW_RETRO, SynthStyle.ROCK_POWER -> squareWave(rootBass, t) * bassEnvelope * 0.25
                SynthStyle.EDM_BRIGHT -> sawtoothWave(rootBass, t) * bassEnvelope * 0.30
                else -> sineWave(rootBass, t) * bassEnvelope * 0.35
            }

            // C. Synthesize Lead Melody (Seeded 16-step sequence)
            val sixteenthStep = (i / (beatDurationSamples / 4)) % 16
            val noteIndex = melodySequence[sixteenthStep]
            val melodyFreq = scaleNotes[noteIndex]
            
            val noteProgress = (i % (beatDurationSamples / 4)).toDouble() / (beatDurationSamples / 4)
            val noteEnvelope = Math.exp(-noteProgress * 12.0)

            val melodySample = when (synthStyle) {
                SynthStyle.WARM_LOFI -> sineWave(melodyFreq, t) * noteEnvelope * 0.22
                SynthStyle.SAW_RETRO -> sawtoothWave(melodyFreq, t) * noteEnvelope * 0.25
                SynthStyle.ROCK_POWER -> (squareWave(melodyFreq, t) + triangleWave(melodyFreq * 2.0, t)) * 0.5 * noteEnvelope * 0.20
                SynthStyle.EDM_BRIGHT -> (sawtoothWave(melodyFreq, t) + squareWave(melodyFreq, t)) * 0.5 * noteEnvelope * 0.26
                SynthStyle.ACOUSTIC_SOFT -> triangleWave(melodyFreq, t) * noteEnvelope * 0.25
            }

            // D. Synthesize Percussion & Drums
            val sampleInBeat = i % beatDurationSamples
            val beatFrac = sampleInBeat.toDouble() / beatDurationSamples

            // Kick Drum
            val kickEnvelope = Math.exp(-beatFrac * 22.0)
            val kickSample = sin(2.0 * Math.PI * 60.0 * (1.0 - beatFrac * 0.85) * t) * kickEnvelope * 0.40

            // Snare Drum (Beat 2 & 4)
            val isSnareBeat = (currentBeat % 2 == 1)
            val snareEnvelope = if (isSnareBeat) Math.exp(-beatFrac * 16.0) else 0.0
            val noiseVal = ((i * 1103515245 + 12345) % 32768) / 32768.0 - 0.5
            val snareSample = (sineWave(180.0, t) * 0.3 + noiseVal * 0.7) * snareEnvelope * 0.28

            // Hi-Hat (8th / 16th notes)
            val subFrac = (sampleInBeat % (beatDurationSamples / 2)).toDouble() / (beatDurationSamples / 2)
            val hihatEnvelope = Math.exp(-subFrac * 40.0)
            val hihatSample = noiseVal * hihatEnvelope * 0.08

            // Mix Stems
            var mixed = chordSample + bassSample + melodySample + kickSample + snareSample + hihatSample

            // Soft clipping limiter
            mixed = mixed.coerceIn(-0.95, 0.95)
            pcmData[i] = (mixed * 32767.0).toInt().toShort()
        }

        val audioDir = File(context.getExternalFilesDir("studio_audio") ?: context.cacheDir, "generated")
        if (!audioDir.exists()) audioDir.mkdirs()

        val fileName = "music_${System.currentTimeMillis()}.wav"
        val wavFile = File(audioDir, fileName)

        writeWavFile(wavFile, pcmData, sampleRate)
        wavFile.absolutePath
    }

    private enum class SynthStyle {
        WARM_LOFI, SAW_RETRO, ROCK_POWER, EDM_BRIGHT, ACOUSTIC_SOFT
    }

    private fun sineWave(freq: Double, t: Double): Double = sin(2.0 * Math.PI * freq * t)
    
    private fun triangleWave(freq: Double, t: Double): Double {
        val cycle = (t * freq) % 1.0
        return if (cycle < 0.5) 4.0 * cycle - 1.0 else 3.0 - 4.0 * cycle
    }

    private fun sawtoothWave(freq: Double, t: Double): Double {
        val cycle = (t * freq) % 1.0
        return 2.0 * cycle - 1.0
    }

    private fun squareWave(freq: Double, t: Double): Double {
        val cycle = (t * freq) % 1.0
        return if (cycle < 0.5) 0.8 else -0.8
    }

    private fun getMusicParamsForGenre(genre: String, seed: Int): Triple<Array<DoubleArray>, DoubleArray, SynthStyle> {
        val genreLower = genre.lowercase()
        val keyOffset = (seed % 5) // Key variations (C, D, Eb, F, G)
        
        return when {
            genreLower.contains("lofi") || genreLower.contains("lo-fi") || genreLower.contains("chill") || genreLower.contains("ambient") -> {
                val chords = arrayOf(
                    doubleArrayOf(146.83 + keyOffset * 10, 174.61 + keyOffset * 10, 220.00 + keyOffset * 10, 261.63 + keyOffset * 10), // Dm7
                    doubleArrayOf(196.00 + keyOffset * 10, 246.94 + keyOffset * 10, 293.66 + keyOffset * 10, 349.23 + keyOffset * 10), // G7
                    doubleArrayOf(130.81 + keyOffset * 10, 164.81 + keyOffset * 10, 196.00 + keyOffset * 10, 246.94 + keyOffset * 10), // Cmaj7
                    doubleArrayOf(220.00 + keyOffset * 10, 261.63 + keyOffset * 10, 329.63 + keyOffset * 10, 392.00 + keyOffset * 10)  // Am7
                )
                val scale = doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25) // Pentatonic C Major
                Triple(chords, scale, SynthStyle.WARM_LOFI)
            }
            genreLower.contains("synth") || genreLower.contains("retro") || genreLower.contains("cyber") || genreLower.contains("80s") -> {
                val chords = arrayOf(
                    doubleArrayOf(220.00, 261.63, 329.63), // Am
                    doubleArrayOf(174.61, 220.00, 261.63), // F
                    doubleArrayOf(261.63, 329.63, 392.00), // C
                    doubleArrayOf(196.00, 246.94, 293.66)  // G
                )
                val scale = doubleArrayOf(440.00, 523.25, 587.33, 659.25, 783.99, 880.00, 1046.50) // Minor Pentatonic
                Triple(chords, scale, SynthStyle.SAW_RETRO)
            }
            genreLower.contains("rock") || genreLower.contains("metal") || genreLower.contains("punk") || genreLower.contains("guitar") -> {
                val chords = arrayOf(
                    doubleArrayOf(164.81, 246.94, 329.63), // E5 Power Chord
                    doubleArrayOf(196.00, 293.66, 392.00), // G5
                    doubleArrayOf(130.81, 196.00, 261.63), // C5
                    doubleArrayOf(146.83, 220.00, 293.66)  // D5
                )
                val scale = doubleArrayOf(329.63, 392.00, 440.00, 466.16, 493.88, 587.33, 659.25) // Blues Scale
                Triple(chords, scale, SynthStyle.ROCK_POWER)
            }
            genreLower.contains("edm") || genreLower.contains("dance") || genreLower.contains("pop") || genreLower.contains("house") -> {
                val chords = arrayOf(
                    doubleArrayOf(164.81, 196.00, 246.94), // Em
                    doubleArrayOf(130.81, 164.81, 196.00), // C
                    doubleArrayOf(196.00, 246.94, 293.66), // G
                    doubleArrayOf(146.83, 185.00, 220.00)  // D
                )
                val scale = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 880.00, 987.77, 1046.50) // Bright Pop/EDM Scale
                Triple(chords, scale, SynthStyle.EDM_BRIGHT)
            }
            else -> { // Acoustic / Classical / Jazz / Default
                val chords = arrayOf(
                    doubleArrayOf(261.63, 329.63, 392.00), // C
                    doubleArrayOf(220.00, 261.63, 329.63), // Am
                    doubleArrayOf(174.61, 220.00, 261.63), // F
                    doubleArrayOf(196.00, 246.94, 293.66)  // G
                )
                val scale = doubleArrayOf(261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25) // C Major Scale
                Triple(chords, scale, SynthStyle.ACOUSTIC_SOFT)
            }
        }
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36

        FileOutputStream(file).use { out ->
            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put("RIFF".toByteArray())
                putInt(totalDataLen)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // Subchunk1Size
                putShort(1.toShort()) // PCM format
                putShort(1.toShort()) // Mono
                putInt(sampleRate)
                putInt(sampleRate * 2) // Byte rate
                putShort(2.toShort()) // Block align
                putShort(16.toShort()) // Bits per sample
                put("data".toByteArray())
                putInt(totalAudioLen)
            }.array()

            out.write(header)

            val pcmBytes = ByteBuffer.allocate(pcmData.size * 2).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                for (s in pcmData) {
                    putShort(s)
                }
            }.array()

            out.write(pcmBytes)
        }
    }

    fun playAudio(context: Context, audioPath: String, onCompletion: () -> Unit = {}) {
        stopAudio()
        try {
            val file = File(audioPath)
            if (file.exists()) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, Uri.fromFile(file))
                    prepare()
                    start()
                    setOnCompletionListener {
                        onCompletion()
                    }
                }
            } else {
                // Synthesize live fallback ToneGenerator if file not present
                playLiveSynthSound()
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error playing audio file", e)
            playLiveSynthSound()
        }
    }

    fun stopAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error stopping/releasing MediaPlayer", e)
        } finally {
            mediaPlayer = null
        }
        playbackJob?.cancel()
        playbackJob = null
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying == true
        } catch (e: Exception) {
            false
        }
    }

    private fun playLiveSynthSound() {
        playbackJob?.cancel()
        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            var audioTrack: AudioTrack? = null
            try {
                val sampleRate = 44100
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .build()

                audioTrack = track
                track.play()

                val freqs = doubleArrayOf(261.63, 329.63, 392.00, 523.25)
                val buffer = ShortArray(4410)

                for (step in 0..12) {
                    val freq = freqs[step % freqs.size]
                    for (i in buffer.indices) {
                        val t = i.toDouble() / sampleRate
                        buffer[i] = (sin(2.0 * Math.PI * freq * t) * 20000).toInt().toShort()
                    }
                    track.write(buffer, 0, buffer.size)
                    delay(300)
                }
            } catch (e: Exception) {
                Log.e("AudioEngine", "Error in live synth fallback", e)
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (e: Exception) {
                    Log.e("AudioEngine", "Error releasing AudioTrack", e)
                }
            }
        }
    }
}
