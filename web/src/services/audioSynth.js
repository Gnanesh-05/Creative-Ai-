// Audio Synthesizer Engine for Nexus AI Web App
// Replicates the exact synthesizer code in AudioSynthesizerEngine.kt

const SAMPLE_RATE = 44100;
const DURATION_SECONDS = 15;

const sineWave = (freq, t) => Math.sin(2.0 * Math.PI * freq * t);

const triangleWave = (freq, t) => {
  const cycle = (t * freq) % 1.0;
  return cycle < 0.5 ? 4.0 * cycle - 1.0 : 3.0 - 4.0 * cycle;
};

const sawtoothWave = (freq, t) => {
  const cycle = (t * freq) % 1.0;
  return 2.0 * cycle - 1.0;
};

const squareWave = (freq, t) => {
  const cycle = (t * freq) % 1.0;
  return cycle < 0.5 ? 0.8 : -0.8;
};

const getMusicParamsForGenre = (genre, seed) => {
  const genreLower = genre.toLowerCase();
  const keyOffset = Math.abs(seed % 5); // Key variations

  if (genreLower.includes("lofi") || genreLower.includes("lo-fi") || genreLower.includes("chill") || genreLower.includes("ambient")) {
    const chords = [
      [146.83 + keyOffset * 10, 174.61 + keyOffset * 10, 220.00 + keyOffset * 10, 261.63 + keyOffset * 10], // Dm7
      [196.00 + keyOffset * 10, 246.94 + keyOffset * 10, 293.66 + keyOffset * 10, 349.23 + keyOffset * 10], // G7
      [130.81 + keyOffset * 10, 164.81 + keyOffset * 10, 196.00 + keyOffset * 10, 246.94 + keyOffset * 10], // Cmaj7
      [220.00 + keyOffset * 10, 261.63 + keyOffset * 10, 329.63 + keyOffset * 10, 392.00 + keyOffset * 10]  // Am7
    ];
    const scale = [261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25]; // Pentatonic C Major
    return { chords, scale, synthStyle: "WARM_LOFI" };
  } 
  
  if (genreLower.includes("synth") || genreLower.includes("retro") || genreLower.includes("cyber") || genreLower.includes("80s")) {
    const chords = [
      [220.00, 261.63, 329.63], // Am
      [174.61, 220.00, 261.63], // F
      [261.63, 329.63, 392.00], // C
      [196.00, 246.94, 293.66]  // G
    ];
    const scale = [440.00, 523.25, 587.33, 659.25, 783.99, 880.00, 1046.50]; // Minor Pentatonic
    return { chords, scale, synthStyle: "SAW_RETRO" };
  }
  
  if (genreLower.includes("rock") || genreLower.includes("metal") || genreLower.includes("punk") || genreLower.includes("guitar")) {
    const chords = [
      [164.81, 246.94, 329.63], // E5 Power Chord
      [196.00, 293.66, 392.00], // G5
      [130.81, 196.00, 261.63], // C5
      [146.83, 220.00, 293.66]  // D5
    ];
    const scale = [329.63, 392.00, 440.00, 466.16, 493.88, 587.33, 659.25]; // Blues Scale
    return { chords, scale, synthStyle: "ROCK_POWER" };
  }
  
  if (genreLower.includes("edm") || genreLower.includes("dance") || genreLower.includes("pop") || genreLower.includes("house")) {
    const chords = [
      [164.81, 196.00, 246.94], // Em
      [130.81, 164.81, 196.00], // C
      [196.00, 246.94, 293.66], // G
      [146.83, 185.00, 220.00]  // D
    ];
    const scale = [523.25, 587.33, 659.25, 783.99, 880.00, 987.77, 1046.50]; // Bright Pop/EDM Scale
    return { chords, scale, synthStyle: "EDM_BRIGHT" };
  }

  // Acoustic / Jazz / Default
  const chords = [
    [261.63, 329.63, 392.00], // C
    [220.00, 261.63, 329.63], // Am
    [174.61, 220.00, 261.63], // F
    [196.00, 246.94, 293.66]  // G
  ];
  const scale = [261.63, 293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25]; // C Major Scale
  return { chords, scale, synthStyle: "ACOUSTIC_SOFT" };
};

/**
 * Generates a WAV PCM file, writes WAV headers, and returns a Blob URL.
 */
export const generateAudioTrack = async (prompt, genre, bpm, mood) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const numSamples = SAMPLE_RATE * DURATION_SECONDS;
      const pcmBuffer = new Int16Array(numSamples);

      // Seed calculations from prompt, genre, and bpm
      const promptClean = prompt.trim().toLowerCase();
      let seed = 0;
      for (let i = 0; i < promptClean.length; i++) {
        seed = (seed * 31 + promptClean.charCodeAt(i)) | 0;
      }
      for (let i = 0; i < genre.length; i++) {
        seed = (seed * 31 + genre.charCodeAt(i)) | 0;
      }
      seed = seed ^ Math.floor(bpm * 100);
      const absSeed = Math.abs(seed);

      const beatDurationSamples = Math.floor((SAMPLE_RATE * 60) / Math.max(60, Math.min(180, bpm)));
      const beatsPerChord = 4;

      const { chords, scale, synthStyle } = getMusicParamsForGenre(genre, absSeed);

      // Generate a 16-note melody sequence
      const melodySequence = Array.from({ length: 16 }, (_, step) => {
        const hash = (absSeed * 31 + step * 17 + promptClean.length) | 0;
        return Math.abs(hash) % scale.length;
      });

      // Bass frequencies
      const bassFreqs = chords.map(chord => chord[0] / 2.0);

      // Generate PCM samples
      for (let i = 0; i < numSamples; i++) {
        const t = i / SAMPLE_RATE;
        const currentBeat = Math.floor(i / beatDurationSamples);
        const chordIndex = Math.floor(currentBeat / beatsPerChord) % chords.length;
        const currentChord = chords[chordIndex];
        const rootBass = bassFreqs[chordIndex];

        // A. Synthesize Pad / Rhythm Chords (ADSR Envelope per beat)
        const beatProgress = (i % beatDurationSamples) / beatDurationSamples;
        const chordEnvelope = Math.max(0.2, 1.0 - beatProgress * 0.35);

        let chordSample = 0.0;
        for (let freq of currentChord) {
          switch (synthStyle) {
            case "WARM_LOFI":
              chordSample += sineWave(freq, t) * 0.7 + triangleWave(freq, t) * 0.3;
              break;
            case "SAW_RETRO":
              chordSample += sawtoothWave(freq, t) * 0.5 + squareWave(freq * 0.999, t) * 0.5;
              break;
            case "ROCK_POWER":
              chordSample += squareWave(freq, t) * 0.6 + sawtoothWave(freq * 1.002, t) * 0.4;
              break;
            case "EDM_BRIGHT":
              chordSample += sawtoothWave(freq, t) * 0.8 + sineWave(freq * 2.0, t) * 0.2;
              break;
            default: // ACOUSTIC_SOFT
              chordSample += triangleWave(freq, t) * 0.9 + sineWave(freq, t) * 0.1;
              break;
          }
        }
        chordSample = (chordSample / currentChord.length) * 0.22 * chordEnvelope;

        // B. Bassline
        const bassEnvelope = Math.exp(-beatProgress * 8.0);
        let bassSample = 0;
        if (synthStyle === "SAW_RETRO" || synthStyle === "ROCK_POWER") {
          bassSample = squareWave(rootBass, t) * bassEnvelope * 0.25;
        } else if (synthStyle === "EDM_BRIGHT") {
          bassSample = sawtoothWave(rootBass, t) * bassEnvelope * 0.30;
        } else {
          bassSample = sineWave(rootBass, t) * bassEnvelope * 0.35;
        }

        // C. Lead Melody (Seeded 16-step sequence)
        const sixteenthStep = Math.floor(i / (beatDurationSamples / 4)) % 16;
        const noteIndex = melodySequence[sixteenthStep];
        const melodyFreq = scale[noteIndex];

        const noteProgress = (i % Math.floor(beatDurationSamples / 4)) / Math.floor(beatDurationSamples / 4);
        const noteEnvelope = Math.exp(-noteProgress * 12.0);

        let melodySample = 0;
        switch (synthStyle) {
          case "WARM_LOFI":
            melodySample = sineWave(melodyFreq, t) * noteEnvelope * 0.22;
            break;
          case "SAW_RETRO":
            melodySample = sawtoothWave(melodyFreq, t) * noteEnvelope * 0.25;
            break;
          case "ROCK_POWER":
            melodySample = (squareWave(melodyFreq, t) + triangleWave(melodyFreq * 2.0, t)) * 0.5 * noteEnvelope * 0.20;
            break;
          case "EDM_BRIGHT":
            melodySample = (sawtoothWave(melodyFreq, t) + squareWave(melodyFreq, t)) * 0.5 * noteEnvelope * 0.26;
            break;
          default: // ACOUSTIC_SOFT
            melodySample = triangleWave(melodyFreq, t) * noteEnvelope * 0.25;
            break;
        }

        // D. Percussion & Drums
        const sampleInBeat = i % beatDurationSamples;
        const beatFrac = sampleInBeat / beatDurationSamples;

        // Kick Drum
        const kickEnvelope = Math.exp(-beatFrac * 22.0);
        const kickSample = Math.sin(2.0 * Math.PI * 60.0 * (1.0 - beatFrac * 0.85) * t) * kickEnvelope * 0.40;

        // Snare Drum (Beat 2 & 4)
        const isSnareBeat = (currentBeat % 2 === 1);
        const snareEnvelope = isSnareBeat ? Math.exp(-beatFrac * 16.0) : 0.0;
        // White noise
        const noiseVal = Math.random() - 0.5;
        const snareSample = (sineWave(180.0, t) * 0.3 + noiseVal * 0.7) * snareEnvelope * 0.28;

        // Hi-Hat (8th / 16th notes)
        const subFrac = (sampleInBeat % Math.floor(beatDurationSamples / 2)) / Math.floor(beatDurationSamples / 2);
        const hihatEnvelope = Math.exp(-subFrac * 40.0);
        const hihatSample = noiseVal * hihatEnvelope * 0.08;

        // Mix Stems
        let mixed = chordSample + bassSample + melodySample + kickSample + snareSample + hihatSample;

        // Soft clipping limiter
        mixed = Math.max(-0.95, Math.min(0.95, mixed));
        pcmBuffer[i] = Math.floor(mixed * 32767.0);
      }

      // Write WAV file array buffer
      const wavBuffer = createWavBuffer(pcmBuffer, SAMPLE_RATE);
      const blob = new Blob([wavBuffer], { type: "audio/wav" });
      const audioUrl = URL.createObjectURL(blob);
      resolve(audioUrl);
    }, 100);
  });
};

/**
 * Creates WAV format headers and packs the PCM 16-bit data.
 */
function createWavBuffer(pcmShorts, sampleRate) {
  const byteLen = pcmShorts.length * 2;
  const buffer = new ArrayBuffer(44 + byteLen);
  const view = new DataView(buffer);

  // RIFF identifier
  writeString(view, 0, "RIFF");
  // File length minus RIFF header
  view.setUint32(4, 36 + byteLen, true);
  // WAVE identifier
  writeString(view, 8, "WAVE");
  // fmt chunk identifier
  writeString(view, 12, "fmt ");
  // Chunk size
  view.setUint32(16, 16, true);
  // Audio format (1 = PCM 16-bit)
  view.setUint16(20, 1, true);
  // Channels (1 = Mono)
  view.setUint16(22, 1, true);
  // Sample rate
  view.setUint32(24, sampleRate, true);
  // Byte rate (sampleRate * channels * bytesPerSample)
  view.setUint32(28, sampleRate * 1 * 2, true);
  // Block align (channels * bytesPerSample)
  view.setUint16(32, 2, true);
  // Bits per sample (16)
  view.setUint16(34, 16, true);
  // data chunk identifier
  writeString(view, 36, "data");
  // Chunk size (number of bytes of PCM audio)
  view.setUint32(40, byteLen, true);

  // Write short samples
  let offset = 44;
  for (let i = 0; i < pcmShorts.length; i++) {
    view.setInt16(offset, pcmShorts[i], true);
    offset += 2;
  }

  return buffer;
}

function writeString(view, offset, string) {
  for (let i = 0; i < string.length; i++) {
    view.setUint8(offset + i, string.charCodeAt(i));
  }
}
