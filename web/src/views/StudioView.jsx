import React, { useState, useEffect } from 'react';
import { 
  saveGeneratedImage, 
  saveGeneratedMusic, 
  listenToFeatureData 
} from '../services/firebase';
import { generateAudioTrack } from '../services/audioSynth';
import { 
  Image as ImageIcon, 
  Music as MusicIcon, 
  Sparkles, 
  Play, 
  Pause, 
  Download, 
  Clock, 
  Sliders, 
  CheckSquare, 
  Square 
} from 'lucide-react';

const STYLES = ["Photography", "3D Render", "Anime", "Cyberpunk", "Watercolor", "Minimalist"];
const GENRES = ["Lo-fi", "Synthwave", "Rock", "Pop/EDM", "Acoustic/Jazz"];
const MOODS = ["Chill & Relaxing", "Energetic & Pumped", "Dark & Cyber", "Uplifting & Happy"];
const AVAILABLE_INSTRUMENTS = ["Piano", "Synth", "Drums", "Guitar", "Bass"];

export default function StudioView({ user }) {
  // 0: Image Studio, 1: Music & Lyrics
  const [activeTab, setActiveTab] = useState(() => {
    const saved = window.sessionStorage.getItem('active_studio_tab');
    if (saved !== null) {
      window.sessionStorage.removeItem('active_studio_tab');
      return parseInt(saved, 10);
    }
    return 0;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      
      {/* Top Segmented Control */}
      <div className="glass-panel" style={{ 
        display: 'flex', 
        padding: '4px', 
        borderRadius: '24px', 
        maxWidth: '420px', 
        margin: '0 auto', 
        width: '100%',
        border: '1px solid var(--border-glass)'
      }}>
        <button
          onClick={() => setActiveTab(0)}
          style={{
            flex: 1,
            background: activeTab === 0 ? 'linear-gradient(135deg, var(--nexus-violet), var(--nexus-magenta))' : 'transparent',
            border: 'none',
            color: 'white',
            padding: '12px',
            borderRadius: '20px',
            fontSize: '13px',
            fontWeight: 'bold',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            transition: 'all 0.3s'
          }}
        >
          <ImageIcon size={16} />
          Image Studio
        </button>
        <button
          onClick={() => setActiveTab(1)}
          style={{
            flex: 1,
            background: activeTab === 1 ? 'linear-gradient(135deg, var(--nexus-violet), var(--nexus-magenta))' : 'transparent',
            border: 'none',
            color: 'white',
            padding: '12px',
            borderRadius: '20px',
            fontSize: '13px',
            fontWeight: 'bold',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '8px',
            transition: 'all 0.3s'
          }}
        >
          <MusicIcon size={16} />
          Music & Lyrics
        </button>
      </div>

      {/* Render Sub View */}
      {activeTab === 0 ? (
        <ImageStudio user={user} />
      ) : (
        <MusicStudio user={user} />
      )}

    </div>
  );
}

// -----------------------------------------------------------------------------
// IMAGE STUDIO VIEW
// -----------------------------------------------------------------------------
function ImageStudio({ user }) {
  const [prompt, setPrompt] = useState('');
  const [selectedStyle, setSelectedStyle] = useState('Photography');
  const [aspectRatio, setAspectRatio] = useState('1:1');
  const [isGenerating, setIsGenerating] = useState(false);
  const [stepText, setStepText] = useState('⚡ Initializing Latents...');
  const [history, setHistory] = useState([]);

  useEffect(() => {
    const unsubscribe = listenToFeatureData('generated_images', user.uid, (data) => {
      setHistory(data);
    });
    return () => unsubscribe();
  }, [user.uid]);

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!prompt.trim() || isGenerating) return;

    setIsGenerating(true);
    setStepText('⚡ Initializing Latents...');
    await new Promise(r => setTimeout(r, 600));
    setStepText('🎨 Processing 100% Precision Match...');
    await new Promise(r => setTimeout(r, 800));
    setStepText('✨ Finalizing High-Res Visual Asset...');

    // Determine dimensions from ratio
    let w = 1024, h = 1024;
    if (aspectRatio === '16:9') { w = 1280; h = 720; }
    else if (aspectRatio === '9:16') { w = 720; h = 1280; }

    const seed = Math.floor(Math.random() * 1000000);
    const fullPrompt = `a high quality ${selectedStyle} image depicting: ${prompt}`;
    const imageUrl = `https://image.pollinations.ai/prompt/${encodeURIComponent(fullPrompt)}?width=${w}&height=${h}&nologo=true&seed=${seed}`;

    try {
      // Validate image exists by pre-loading it
      await new Promise((resolve, reject) => {
        const img = new Image();
        img.src = imageUrl;
        img.onload = resolve;
        img.onerror = reject;
      });

      // Save to Firebase
      await saveGeneratedImage(user.uid, prompt, aspectRatio, imageUrl);
    } catch (err) {
      console.error("Failed to load generated image:", err);
      // Save anyway as fallback
      await saveGeneratedImage(user.uid, prompt, aspectRatio, imageUrl);
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Settings Panel */}
      <form onSubmit={handleGenerate} className="glass-panel" style={{ padding: '24px', borderRadius: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        
        <div className="form-group">
          <label>STYLE PRESETS</label>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            {STYLES.map(style => (
              <button
                key={style}
                type="button"
                onClick={() => setSelectedStyle(style)}
                style={{
                  background: selectedStyle === style ? 'var(--nexus-magenta)' : 'rgba(255,255,255,0.05)',
                  border: 'none',
                  color: 'white',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  padding: '8px 16px',
                  borderRadius: '12px',
                  cursor: 'pointer',
                  transition: 'background 0.2s'
                }}
              >
                {style}
              </button>
            ))}
          </div>
        </div>

        <div className="form-group">
          <label>ASPECT RATIO</label>
          <div style={{ display: 'flex', gap: '8px' }}>
            {["1:1", "16:9", "9:16"].map(ratio => (
              <button
                key={ratio}
                type="button"
                onClick={() => setAspectRatio(ratio)}
                style={{
                  background: aspectRatio === ratio ? 'var(--nexus-violet)' : 'rgba(255,255,255,0.05)',
                  border: 'none',
                  color: 'white',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  padding: '8px 16px',
                  borderRadius: '12px',
                  cursor: 'pointer',
                  transition: 'background 0.2s',
                  minWidth: '60px'
                }}
              >
                {ratio}
              </button>
            ))}
          </div>
        </div>

        <div className="form-group">
          <label>PROMPT INPUT</label>
          <textarea
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="A futuristic cybernetic library with floating screens and holograms..."
            className="glass-input"
            style={{ width: '100%', height: '80px', resize: 'none' }}
            required
          />
        </div>

        <button 
          type="submit" 
          className="btn btn-primary" 
          disabled={isGenerating || !prompt.trim()}
          style={{ width: '100%', height: '48px', gap: '10px' }}
        >
          <Sparkles size={18} />
          {isGenerating ? stepText : "Generate Graphic Asset"}
        </button>

      </form>

      {/* History Grid */}
      <div>
        <h3 style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-secondary)', letterSpacing: '1px', textTransform: 'uppercase', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Clock size={16} /> GENERATION HISTORY
        </h3>

        {history.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            No graphics generated yet. Enter a prompt above to compile your first neural design!
          </div>
        ) : (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
            gap: '20px'
          }}>
            {history.map(img => (
              <div 
                key={img.id} 
                className="glass-panel" 
                style={{ 
                  borderRadius: '16px', 
                  overflow: 'hidden', 
                  display: 'flex', 
                  flexDirection: 'column', 
                  transition: 'transform 0.2s'
                }}
                onMouseEnter={e => e.currentTarget.style.transform = 'scale(1.02)'}
                onMouseLeave={e => e.currentTarget.style.transform = 'scale(1)'}
              >
                <div style={{ width: '100%', aspectRatio: img.aspect_ratio === '16:9' ? '16/9' : img.aspect_ratio === '9:16' ? '9/16' : '1' }}>
                  <img 
                    src={img.image_url} 
                    alt={img.prompt} 
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    loading="lazy"
                  />
                </div>
                <div style={{ padding: '12px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <div style={{ fontSize: '12px', color: 'white', fontWeight: 'bold', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {img.prompt}
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                      Ratio: {img.aspect_ratio}
                    </span>
                    <a 
                      href={img.image_url} 
                      target="_blank" 
                      rel="noopener noreferrer" 
                      download={`image_${img.id}.jpg`} 
                      style={{ color: 'var(--nexus-magenta)', display: 'flex', alignItems: 'center', textDecoration: 'none' }}
                    >
                      <Download size={14} />
                    </a>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
}

// -----------------------------------------------------------------------------
// MUSIC & LYRICS STUDIO
// -----------------------------------------------------------------------------
function MusicStudio({ user }) {
  const [prompt, setPrompt] = useState('A relaxing lofi beat for studying on a rainy afternoon');
  const [selectedGenre, setSelectedGenre] = useState('Lo-fi');
  const [selectedMood, setSelectedMood] = useState('Chill & Relaxing');
  const [tempo, setTempo] = useState(85);
  const [instruments, setInstruments] = useState(["Piano", "Synth", "Drums"]);
  
  const [isGenerating, setIsGenerating] = useState(false);
  const [stepText, setStepText] = useState('⚡ Synthesizing Stem Harmonics...');
  const [history, setHistory] = useState([]);
  
  const [activeAudio, setActiveAudio] = useState(null);
  const [playingId, setPlayingId] = useState(null);

  useEffect(() => {
    const unsubscribe = listenToFeatureData('generated_music', user.uid, (data) => {
      setHistory(data);
    });
    return () => {
      unsubscribe();
      if (activeAudio) {
        activeAudio.pause();
      }
    };
  }, [user.uid, activeAudio]);

  const toggleInstrument = (inst) => {
    setInstruments(prev => 
      prev.includes(inst) ? prev.filter(i => i !== inst) : [...prev, inst]
    );
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!prompt.trim() || isGenerating) return;

    setIsGenerating(true);
    setStepText('⚡ Synthesizing Stem Harmonics...');
    await new Promise(r => setTimeout(r, 600));
    setStepText('✍️ Writing Verse & Chorus Lyrics...');
    await new Promise(r => setTimeout(r, 800));
    setStepText('🎼 Aligning Chord Progressions...');
    await new Promise(r => setTimeout(r, 600));

    try {
      // 1. Generate local WAV blob URL via PCM synthesis
      const audioUrl = await generateAudioTrack(prompt, selectedGenre, tempo, selectedMood);

      // 2. Generate lyrics markdown to store as metadata
      const lyrics = generateDynamicLyricsText(prompt, selectedGenre, selectedMood, tempo);

      // 3. Save to Firestore
      await saveGeneratedMusic(user.uid, prompt, selectedGenre, { audioUrl, lyrics });
      
    } catch (err) {
      console.error("Audio generation failed:", err);
    } finally {
      setIsGenerating(false);
    }
  };

  const playMusic = (musicItem) => {
    // Extract metadata
    const audioUrl = musicItem.genre?.audioUrl || musicItem.genre; // Firebase model compatibility
    if (!audioUrl) return;

    if (playingId === musicItem.id) {
      activeAudio.pause();
      setPlayingId(null);
    } else {
      if (activeAudio) activeAudio.pause();
      
      const audio = new Audio(audioUrl);
      audio.play();
      audio.onended = () => {
        setPlayingId(null);
      };
      
      setActiveAudio(audio);
      setPlayingId(musicItem.id);
    }
  };

  // Helper to extract lyrics to show
  const getLyricsForDisplay = (item) => {
    return item.genre?.lyrics || `Genre: ${item.genre} | Mood: ${selectedMood} | BPM: ${tempo}
Chords: Dm7 - G7 - Cmaj7
[Verse]
Ambient chords starting to rise...`;
  };

  // Simple lyrics generator
  const generateDynamicLyricsText = (prompt, genre, mood, bpm) => {
    const topic = prompt.trim();
    const keyAndChords = genre.toLowerCase().includes("lofi") 
      ? "Key: D Minor | Chords: Dm7 - G7 - Cmaj7 - Am7"
      : "Key: C Major | Chords: C - Am - F - G";

    return `Genre: ${genre} | Mood: ${mood} | Tempo: ${bpm} BPM
${keyAndChords}

[Verse 1]
Step into the flow of ${topic},
Echoes in the air, feeling pure and hypnotic.
Rhythm holding steady with a ${mood} design,
Every single moment coming into line.

[Chorus]
Oh, shining in the light,
Turn the volume up as we take off in flight.
Driven by the ${genre} beat, moving in harmony,
Living in this moment, wild and free!

[Outro]
Soft resonance fading into the sky... forever.`;
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Settings Form */}
      <form onSubmit={handleGenerate} className="glass-panel" style={{ padding: '24px', borderRadius: '20px', display: 'flex', flexDirection: 'column', gap: '18px' }}>
        
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }} className="grid-mobile-1">
          <div className="form-group">
            <label>GENRE</label>
            <select 
              value={selectedGenre} 
              onChange={e => setSelectedGenre(e.target.value)}
              style={{
                background: 'rgba(0,0,0,0.25)',
                border: '1px solid var(--border-glass)',
                color: 'white',
                padding: '10px 14px',
                borderRadius: '12px',
                fontSize: '14px',
                outline: 'none',
                cursor: 'pointer'
              }}
            >
              {GENRES.map(g => <option key={g} value={g}>{g}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>MOOD PROFILE</label>
            <select 
              value={selectedMood} 
              onChange={e => setSelectedMood(e.target.value)}
              style={{
                background: 'rgba(0,0,0,0.25)',
                border: '1px solid var(--border-glass)',
                color: 'white',
                padding: '10px 14px',
                borderRadius: '12px',
                fontSize: '14px',
                outline: 'none',
                cursor: 'pointer'
              }}
            >
              {MOODS.map(m => <option key={m} value={m}>{m}</option>)}
            </select>
          </div>
        </div>

        <div className="form-group">
          <label style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>TEMPO (BPM)</span>
            <strong style={{ color: 'var(--nexus-magenta)' }}>{tempo} BPM</strong>
          </label>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <Sliders size={16} style={{ color: 'var(--text-muted)' }} />
            <input 
              type="range" 
              min="60" 
              max="180" 
              value={tempo} 
              onChange={e => setTempo(parseInt(e.target.value, 10))}
              style={{ 
                flex: 1, 
                accentColor: 'var(--nexus-magenta)',
                background: 'rgba(255,255,255,0.1)',
                height: '6px',
                borderRadius: '3px',
                outline: 'none'
              }}
            />
          </div>
        </div>

        <div className="form-group">
          <label>INSTRUMENT STEMS</label>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px' }}>
            {AVAILABLE_INSTRUMENTS.map(inst => {
              const isSelected = instruments.includes(inst);
              return (
                <button
                  key={inst}
                  type="button"
                  onClick={() => toggleInstrument(inst)}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    cursor: 'pointer',
                    fontSize: '13px'
                  }}
                >
                  {isSelected ? (
                    <CheckSquare size={16} style={{ color: 'var(--nexus-magenta)' }} />
                  ) : (
                    <Square size={16} style={{ color: 'var(--text-muted)' }} />
                  )}
                  <span>{inst}</span>
                </button>
              );
            })}
          </div>
        </div>

        <div className="form-group">
          <label>SONG THEME / PROMPT</label>
          <input
            type="text"
            value={prompt}
            onChange={e => setPrompt(e.target.value)}
            placeholder="A calming rhythm about code compilation..."
            className="glass-input"
            style={{ width: '100%' }}
            required
          />
        </div>

        <button 
          type="submit" 
          className="btn btn-primary" 
          disabled={isGenerating || !prompt.trim()}
          style={{ width: '100%', height: '48px', gap: '10px' }}
        >
          <Sparkles size={18} />
          {isGenerating ? stepText : "Synthesize WAV Audio"}
        </button>

      </form>

      {/* History & Lyrics Panel */}
      <div>
        <h3 style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-secondary)', letterSpacing: '1px', textTransform: 'uppercase', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Clock size={16} /> AUDIO TRACK COMPILATION
        </h3>

        {history.length === 0 ? (
          <div className="glass-panel" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            No tracks synthesized yet. Configure and generate one above to hear custom waves!
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {history.map(item => {
              const isPlaying = playingId === item.id;
              const lyricText = getLyricsForDisplay(item);
              return (
                <div key={item.id} className="glass-panel" style={{ padding: '20px', borderRadius: '18px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  
                  {/* Row Controls */}
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '10px' }}>
                    <div>
                      <h4 style={{ fontSize: '15px', fontWeight: 'bold' }}>{item.prompt}</h4>
                      <p style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                        Genre: {item.genre?.lyrics ? item.genre.lyrics.split('\n')[0].split('|')[0].split(':')[1].trim() : item.genre} | 15s PCM Stem
                      </p>
                    </div>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button
                        onClick={() => playMusic(item)}
                        style={{
                          width: '38px',
                          height: '38px',
                          borderRadius: '50%',
                          background: isPlaying ? 'var(--nexus-magenta)' : 'var(--nexus-violet)',
                          border: 'none',
                          color: 'white',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          cursor: 'pointer'
                        }}
                      >
                        {isPlaying ? <Pause size={16} /> : <Play size={16} />}
                      </button>
                      <a
                        href={item.genre?.audioUrl || item.genre}
                        download={`song_${item.id}.wav`}
                        style={{
                          width: '38px',
                          height: '38px',
                          borderRadius: '50%',
                          background: 'rgba(255,255,255,0.06)',
                          border: '1px solid var(--border-glass)',
                          color: 'white',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          textDecoration: 'none'
                        }}
                      >
                        <Download size={16} />
                      </a>
                    </div>
                  </div>

                  {/* Lyrics Display Scroll Container */}
                  <div style={{
                    background: 'rgba(0,0,0,0.2)',
                    border: '1px solid var(--border-glass)',
                    padding: '14px',
                    borderRadius: '12px',
                    maxHeight: '120px',
                    overflowY: 'auto',
                    fontSize: '12px',
                    lineHeight: '1.6',
                    fontFamily: 'var(--font-body)',
                    color: 'var(--text-secondary)',
                    whiteSpace: 'pre-wrap'
                  }}>
                    {lyricText}
                  </div>

                </div>
              );
            })}
          </div>
        )}
      </div>

      <style>{`
        @media(max-width: 480px) {
          .grid-mobile-1 { grid-template-columns: 1fr !important; }
        }
      `}</style>

    </div>
  );
}
