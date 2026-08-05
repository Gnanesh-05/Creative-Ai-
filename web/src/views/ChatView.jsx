import React, { useState, useEffect, useRef } from 'react';
import { 
  auth, 
  saveChatMessage, 
  listenToFeatureData 
} from '../services/firebase';
import { generateGeminiContent } from '../services/gemini';
import { Send, Sparkles, BrainCircuit, Bot, User, Cpu, ChevronDown } from 'lucide-react';

const AGENTS = [
  { id: 'PLANNER', title: 'Planner Agent', capability: 'General cognitive reasoning' },
  { id: 'CODING', title: 'Coding Agent', capability: 'Write and audit code segments' },
  { id: 'RESEARCH', title: 'Research Agent', capability: 'Analyze deep queries' },
  { id: 'MEMORY', title: 'Memory Manager', capability: 'Update persistent user preferences' }
];

const MODELS = [
  { id: 'gemini-3.5-flash', name: 'Gemini 3.5 Flash (Default)' },
  { id: 'gemini-3.1-pro-preview', name: 'Gemini 3.1 Pro (Preview)' },
  { id: 'gemini-2.5-flash-image', name: 'Gemini 2.5 Flash Image' },
  { id: 'ollama-local', name: 'Ollama Local (Offline)' }
];

const PROMPT_SUGGESTIONS = [
  "Write a compose layout for a profile card",
  "Explain quantum computing in simple terms",
  "Check my memories tab",
  "Give me ideas for a Sci-Fi story"
];

export default function ChatView({ user, inputQuery, clearInputQuery }) {
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');
  const [selectedModel, setSelectedModel] = useState('gemini-3.5-flash');
  const [selectedAgent, setSelectedAgent] = useState(AGENTS[0]);
  const [isThinking, setIsThinking] = useState(false);
  const [thinkingStatusText, setThinkingStatusText] = useState('Analyzing intent...');
  const [memories, setMemories] = useState([]);
  
  const chatBottomRef = useRef(null);

  // Sync messages in real-time
  useEffect(() => {
    const unsubscribeChats = listenToFeatureData('chats', user.uid, (data) => {
      // Sort ascending by timestamp (Firestore snapshot query does it, but double check)
      const sorted = [...data].sort((a, b) => a.created_at - b.created_at);
      setMessages(sorted);
    });

    const unsubscribeMemories = listenToFeatureData('users', user.uid, (data) => {
      // Users collection doesn't store sub-arrays, but let's check profile memories
      // Fallback: we can load from a "memories" collection if needed, or profile
    });

    // Also listen to dedicated 'memories' collection if any
    const unsubscribeMemoryFacts = listenToFeatureData('memory_facts', user.uid, (data) => {
      setMemories(data);
    });

    return () => {
      unsubscribeChats();
      unsubscribeMemories();
      unsubscribeMemoryFacts();
    };
  }, [user.uid]);

  // Handle incoming query from search bar
  useEffect(() => {
    if (inputQuery) {
      setInputText(inputQuery);
      clearInputQuery();
    }
  }, [inputQuery]);

  // Scroll to bottom
  useEffect(() => {
    chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isThinking]);

  const handleSendMessage = async (textToSend) => {
    const text = textToSend || inputText;
    if (!text.trim()) return;

    if (!textToSend) setInputText('');

    try {
      // 1. Save user message to Firestore
      await saveChatMessage(user.uid, text, 'user');

      // 2. Set thinking states
      setIsThinking(true);
      setThinkingStatusText('Analyzing intent...');
      await new Promise(r => setTimeout(r, 400));
      
      setThinkingStatusText('Searching memory & context...');
      await new Promise(r => setTimeout(r, 400));

      setThinkingStatusText('Routing query...');
      await new Promise(r => setTimeout(r, 300));
      
      setThinkingStatusText('Gathering response...');

      // 3. Request AI response from Gemini service
      const aiResponse = await generateGeminiContent(
        text, 
        selectedModel, 
        selectedAgent, 
        memories
      );

      // 4. Save AI response to Firestore
      await saveChatMessage(user.uid, aiResponse, 'model');

    } catch (err) {
      console.error(err);
      await saveChatMessage(user.uid, `[Error: ${err.message}]`, 'model');
    } finally {
      setIsThinking(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 120px)', padding: '10px 0' }}>
      
      {/* Top Selectors Bar */}
      <div style={{ 
        display: 'flex', 
        flexWrap: 'wrap', 
        gap: '12px', 
        paddingBottom: '16px', 
        borderBottom: '1px solid var(--border-glass)',
        marginBottom: '16px',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <BrainCircuit size={18} style={{ color: 'var(--nexus-magenta)' }} />
          <span style={{ fontSize: '13px', fontWeight: 'bold', color: 'var(--text-secondary)' }}>DELEGATE:</span>
          <select 
            value={selectedAgent.id} 
            onChange={(e) => setSelectedAgent(AGENTS.find(a => a.id === e.target.value))}
            style={{
              background: 'rgba(0, 0, 0, 0.25)',
              border: '1px solid var(--border-glass)',
              color: 'white',
              padding: '6px 12px',
              borderRadius: '8px',
              outline: 'none',
              fontSize: '13px',
              fontFamily: 'var(--font-body)',
              cursor: 'pointer'
            }}
          >
            {AGENTS.map(agent => (
              <option key={agent.id} value={agent.id}>{agent.title}</option>
            ))}
          </select>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Cpu size={18} style={{ color: 'var(--nexus-violet)' }} />
          <span style={{ fontSize: '13px', fontWeight: 'bold', color: 'var(--text-secondary)' }}>MODEL:</span>
          <select 
            value={selectedModel} 
            onChange={(e) => setSelectedModel(e.target.value)}
            style={{
              background: 'rgba(0, 0, 0, 0.25)',
              border: '1px solid var(--border-glass)',
              color: 'white',
              padding: '6px 12px',
              borderRadius: '8px',
              outline: 'none',
              fontSize: '13px',
              fontFamily: 'var(--font-body)',
              cursor: 'pointer'
            }}
          >
            {MODELS.map(model => (
              <option key={model.id} value={model.id}>{model.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Message List */}
      <div style={{ 
        flex: 1, 
        overflowY: 'auto', 
        paddingRight: '8px', 
        display: 'flex', 
        flexDirection: 'column', 
        gap: '16px',
        marginBottom: '16px'
      }}>
        {messages.length === 0 ? (
          <div style={{ 
            height: '100%', 
            display: 'flex', 
            flexDirection: 'column', 
            justifyContent: 'center', 
            alignItems: 'center',
            gap: '12px',
            textAlign: 'center',
            opacity: 0.8
          }}>
            <Bot size={48} style={{ color: 'var(--nexus-violet)' }} />
            <h3 style={{ fontSize: '18px', fontWeight: '700' }}>Start your chat with Nexus AI</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '13px', maxWidth: '360px', lineHeight: '1.5' }}>
              Choose a custom agent or switch models. Ask coding questions, request math help, or try one of the suggestions below:
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', width: '100%', maxWidth: '380px', marginTop: '12px' }}>
              {PROMPT_SUGGESTIONS.map((s, idx) => (
                <button
                  key={idx}
                  onClick={() => handleSendMessage(s)}
                  style={{
                    background: 'rgba(255,255,255,0.04)',
                    border: '1px solid var(--border-glass)',
                    color: 'white',
                    padding: '10px 14px',
                    borderRadius: '10px',
                    fontSize: '12px',
                    cursor: 'pointer',
                    textAlign: 'left',
                    transition: 'background 0.2s'
                  }}
                  onMouseEnter={e => e.currentTarget.style.background = 'var(--bg-card-hover)'}
                  onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.04)'}
                >
                  💡 "{s}"
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg, idx) => {
            const isUser = msg.role === 'user';
            return (
              <div 
                key={msg.id || idx} 
                style={{ 
                  display: 'flex', 
                  gap: '12px', 
                  flexDirection: isUser ? 'row-reverse' : 'row',
                  alignItems: 'flex-start'
                }}
              >
                <div style={{
                  background: isUser ? 'rgba(129, 116, 158, 0.25)' : 'rgba(236, 72, 153, 0.15)',
                  border: isUser ? '1px solid rgba(129, 116, 158, 0.4)' : '1px solid rgba(236, 72, 153, 0.3)',
                  padding: '8px',
                  borderRadius: '10px',
                  color: isUser ? 'var(--nexus-violet)' : 'var(--nexus-magenta)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  {isUser ? <User size={16} /> : <Bot size={16} />}
                </div>

                <div className="glass-panel" style={{ 
                  padding: '12px 18px', 
                  borderRadius: isUser ? '18px 2px 18px 18px' : '2px 18px 18px 18px',
                  maxWidth: '75%',
                  fontSize: '14px',
                  lineHeight: '1.6',
                  whiteSpace: 'pre-wrap',
                  wordBreak: 'break-word',
                  color: 'white'
                }}>
                  {msg.message}
                </div>
              </div>
            );
          })
        )}

        {/* Thinking Indicator */}
        {isThinking && (
          <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
            <div style={{
              background: 'rgba(236, 72, 153, 0.15)',
              border: '1px solid rgba(236, 72, 153, 0.3)',
              padding: '8px',
              borderRadius: '10px',
              color: 'var(--nexus-magenta)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <Bot size={16} className="animate-pulse" />
            </div>

            <div className="glass-panel" style={{ 
              padding: '14px 20px', 
              borderRadius: '2px 18px 18px 18px',
              fontSize: '13px',
              color: 'var(--text-secondary)',
              display: 'flex',
              alignItems: 'center',
              gap: '12px'
            }}>
              <div style={{ display: 'flex', gap: '3px' }}>
                <span className="wave-bar" style={{ animationDuration: '0.6s', width: '3px', height: '10px' }} />
                <span className="wave-bar" style={{ animationDuration: '0.6s', animationDelay: '0.15s', width: '3px', height: '10px' }} />
                <span className="wave-bar" style={{ animationDuration: '0.6s', animationDelay: '0.3s', width: '3px', height: '10px' }} />
              </div>
              <span>{thinkingStatusText}</span>
            </div>
          </div>
        )}
        <div ref={chatBottomRef} />
      </div>

      {/* Input Form Box */}
      <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
        <textarea
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask Nexus AI... (Press Enter to Send)"
          className="glass-input"
          style={{
            flex: 1,
            height: '50px',
            resize: 'none',
            borderRadius: '16px',
            fontSize: '14px',
            lineHeight: '1.4',
            padding: '14px 18px'
          }}
          disabled={isThinking}
        />
        <button
          onClick={() => handleSendMessage()}
          className="gradient-btn btn"
          style={{
            width: '50px',
            height: '50px',
            borderRadius: '16px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: 0
          }}
          disabled={!inputText.trim() || isThinking}
        >
          <Send size={18} />
        </button>
      </div>

    </div>
  );
}
