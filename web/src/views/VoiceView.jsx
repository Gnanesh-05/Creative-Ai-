import React, { useState, useEffect, useRef } from 'react';
import { generateGeminiContent } from '../services/gemini';
import { listenToFeatureData } from '../services/firebase';
import { Mic, MicOff, Volume2, VolumeX, Sparkles, BrainCircuit } from 'lucide-react';

export default function VoiceView({ user }) {
  const [isListening, setIsListening] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [isThinking, setIsThinking] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [aiText, setAiText] = useState('');
  const [memories, setMemories] = useState([]);
  
  const canvasRef = useRef(null);
  const recognitionRef = useRef(null);
  const synthesisUtteranceRef = useRef(null);
  const animationFrameRef = useRef(null);

  // Sync memories
  useEffect(() => {
    const unsubscribe = listenToFeatureData('memory_facts', user.uid, (data) => {
      setMemories(data);
    });
    return () => unsubscribe();
  }, [user.uid]);

  // Set up Speech Recognition (Speech-to-Text)
  useEffect(() => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (SpeechRecognition) {
      const rec = new SpeechRecognition();
      rec.continuous = false;
      rec.interimResults = false;
      rec.lang = 'en-US';

      rec.onstart = () => {
        setIsListening(true);
        setTranscript('Listening...');
        setAiText('');
        // Stop any currently speaking TTS
        window.speechSynthesis.cancel();
        setIsSpeaking(false);
      };

      rec.onresult = async (event) => {
        const text = event.results[0][0].transcript;
        setTranscript(`You: "${text}"`);
        setIsListening(false);
        await processVoiceInput(text);
      };

      rec.onerror = (e) => {
        console.error("Speech Recognition Error:", e);
        setTranscript(`Recognition Error: ${e.error}`);
        setIsListening(false);
      };

      rec.onend = () => {
        setIsListening(false);
      };

      recognitionRef.current = rec;
    } else {
      setTranscript("Web Speech Recognition API is not supported in this browser. Try Chrome or Edge.");
    }

    return () => {
      window.speechSynthesis.cancel();
      cancelAnimationFrame(animationFrameRef.current);
    };
  }, [memories]);

  // Process input and vocalize output
  const processVoiceInput = async (text) => {
    setIsThinking(true);
    try {
      const response = await generateGeminiContent(
        text, 
        'gemini-3.5-flash', 
        { title: 'Voice Assistant', capability: 'Voice response synthesis' }, 
        memories
      );

      // Strip markdown for speech synthesis
      const cleanResponse = response.replace(/\*\*|\*|#/g, '');
      setAiText(response);
      speakResponse(cleanResponse);
    } catch (err) {
      console.error(err);
      setAiText(`Error processing request: ${err.message}`);
      speakResponse(`Error processing request: ${err.message}`);
    } finally {
      setIsThinking(false);
    }
  };

  const speakResponse = (text) => {
    if (!window.speechSynthesis) return;

    window.speechSynthesis.cancel(); // Stop current speech
    
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 1.0;
    utterance.pitch = 1.0;
    
    utterance.onstart = () => {
      setIsSpeaking(true);
    };

    utterance.onend = () => {
      setIsSpeaking(false);
    };

    utterance.onerror = (e) => {
      console.error("Speech Synthesis Error:", e);
      setIsSpeaking(false);
    };

    synthesisUtteranceRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  };

  const toggleListening = () => {
    if (isListening) {
      recognitionRef.current?.stop();
    } else {
      try {
        recognitionRef.current?.start();
      } catch (err) {
        console.warn("Failed to start speech recognition:", err);
      }
    }
  };

  const stopSpeaking = () => {
    window.speechSynthesis.cancel();
    setIsSpeaking(false);
  };

  // Canvas waveform generator animation
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    
    // Set internal dimensions
    const resizeCanvas = () => {
      canvas.width = canvas.parentElement.clientWidth;
      canvas.height = 160;
    };
    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);

    let phase = 0;
    const draw = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      
      const width = canvas.width;
      const height = canvas.height;
      const centerY = height / 2;

      // Base settings based on assistant state
      let numWaves = 3;
      let amplitude = 12;
      let speed = 0.08;
      let frequency = 0.015;

      if (isListening) {
        amplitude = 35;
        speed = 0.15;
        frequency = 0.025;
        numWaves = 4;
      } else if (isSpeaking) {
        amplitude = 25;
        speed = 0.12;
        frequency = 0.02;
        numWaves = 3;
      } else if (isThinking) {
        amplitude = 8;
        speed = 0.25;
        frequency = 0.04;
        numWaves = 2;
      }

      ctx.lineWidth = 2.5;

      const colors = [
        'rgba(236, 72, 153, 0.45)', // Magenta
        'rgba(129, 116, 158, 0.6)',  // Violet
        'rgba(6, 182, 212, 0.4)'    // Cyan
      ];

      for (let w = 0; w < numWaves; w++) {
        ctx.beginPath();
        const waveOffset = w * Math.PI / 4;
        
        ctx.strokeStyle = colors[w % colors.length];

        for (let x = 0; x < width; x++) {
          // Fade amplitude at edges
          const edgeDecay = Math.sin((x / width) * Math.PI);
          const y = centerY + Math.sin(x * frequency + phase + waveOffset) * amplitude * edgeDecay;
          
          if (x === 0) {
            ctx.moveTo(x, y);
          } else {
            ctx.lineTo(x, y);
          }
        }
        ctx.stroke();
      }

      phase += speed;
      animationFrameRef.current = requestAnimationFrame(draw);
    };

    draw();

    return () => {
      window.removeEventListener('resize', resizeCanvas);
      cancelAnimationFrame(animationFrameRef.current);
    };
  }, [isListening, isSpeaking, isThinking]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: 'calc(100vh - 120px)', padding: '24px 0', gap: '30px' }}>
      
      {/* Wave Visualizer Canvas */}
      <div className="glass-panel" style={{ width: '100%', maxWidth: '640px', padding: '20px', borderRadius: '24px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 'bold', letterSpacing: '1px', textTransform: 'uppercase', marginBottom: '8px' }}>
          Assistant Neural Waveform
        </div>
        <canvas ref={canvasRef} style={{ width: '100%', height: '160px', display: 'block' }} />
      </div>

      {/* Main Microphone Button */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
        <button
          onClick={toggleListening}
          style={{
            width: '100px',
            height: '100px',
            borderRadius: '50%',
            background: isListening 
              ? 'linear-gradient(135deg, #ef4444, #f87171)' 
              : 'linear-gradient(135deg, #8b5cf6, #ec4899)',
            border: 'none',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            color: 'white',
            boxShadow: isListening 
              ? '0 0 30px rgba(239, 68, 68, 0.6)' 
              : '0 0 25px var(--violet-glow)',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)'
          }}
          className={isListening ? 'animate-pulse' : ''}
        >
          {isListening ? <MicOff size={36} /> : <Mic size={36} />}
        </button>

        <div style={{ textAlign: 'center' }}>
          <h3 style={{ fontSize: '18px', fontWeight: 'bold' }}>
            {isListening ? 'Listening...' : isThinking ? 'Solving request...' : isSpeaking ? 'Speaking...' : 'Vocal Interface Standby'}
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '13px', marginTop: '6px', maxWidth: '320px', lineHeight: '1.4' }}>
            {isListening ? 'Nexus OS is auditing your voice...' : isSpeaking ? 'Press mute below to interrupt vocalizer' : 'Tap the mic and speak natural instructions'}
          </p>
        </div>
      </div>

      {/* Transcript Text and Speech Interrupter */}
      <div className="glass-panel" style={{ width: '100%', maxWidth: '580px', padding: '24px', borderRadius: '18px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
        
        <div style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 'bold' }}>TRANSCRIPT:</div>
        <div style={{ color: 'white', fontSize: '14px', fontStyle: 'italic', minHeight: '20px' }}>
          {transcript || '"Awaiting voice input..."'}
        </div>

        {aiText && (
          <>
            <div style={{ borderTop: '1px solid var(--border-glass)', paddingTop: '12px', marginTop: '6px' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 'bold', marginBottom: '6px' }}>NEXUS AI ANSWER:</div>
              <div style={{ color: 'white', fontSize: '14px', lineHeight: '1.5' }}>{aiText}</div>
            </div>

            {isSpeaking && (
              <button 
                onClick={stopSpeaking} 
                className="btn btn-secondary" 
                style={{ 
                  marginTop: '12px', 
                  alignSelf: 'center', 
                  gap: '8px', 
                  padding: '8px 16px',
                  borderColor: 'rgba(239,68,68,0.3)',
                  color: 'var(--error)',
                  fontSize: '12px'
                }}
              >
                <VolumeX size={14} /> Stop Speech
              </button>
            )}
          </>
        )}
      </div>

    </div>
  );
}
