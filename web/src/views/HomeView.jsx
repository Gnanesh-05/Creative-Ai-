import React, { useState } from 'react';
import { Mic, MessageSquare, Paintbrush, Music, Gamepad2, UserCheck, Search, Sparkles } from 'lucide-react';

export default function HomeView({ user, setActiveTab, setChatInputQuery }) {
  const [query, setQuery] = useState('');

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (query.trim()) {
      setChatInputQuery(query);
      setActiveTab('chat');
    }
  };

  const actionCards = [
    { id: 'chat', title: 'Smart Chat', desc: 'Converse with Gemini 3.5 AI models', icon: MessageSquare, color: '#3b82f6' },
    { id: 'voice', title: 'Voice Assistant', desc: 'Interactive real-time talk bot', icon: Mic, color: '#10b981' },
    { id: 'studio', title: 'Image Studio', desc: 'Neural graphics generators', icon: Paintbrush, color: '#f59e0b', subtab: 0 },
    { id: 'studio', title: 'Music & Lyrics', desc: 'Synthesizes audio tracks locally', icon: Music, color: '#ec4899', subtab: 1 },
    { id: 'games', title: 'Game Center', desc: 'Play Chess, Tic-Tac-Toe, and Maze', icon: Gamepad2, color: '#8b5cf6' },
    { id: 'profile', title: 'OS Settings', desc: 'Modify UPI parameters & profile details', icon: UserCheck, color: '#64748b' }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '32px', padding: '24px 0' }}>
      
      {/* Top Welcome Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '24px', fontWeight: '800', fontFamily: 'var(--font-brand)' }}>
            Welcome to <span className="gradient-text">Nexus OS</span>
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px' }}>
            Core node synchronized. Accessing local user session: <strong style={{ color: 'white' }}>{user.name || user.email.split('@')[0]}</strong>
          </p>
        </div>
        {user.premium && (
          <span style={{
            background: 'linear-gradient(135deg, #f59e0b, #d97706)',
            color: 'white',
            fontWeight: 'bold',
            fontSize: '11px',
            padding: '5px 12px',
            borderRadius: '20px',
            boxShadow: '0 0 10px rgba(245, 158, 11, 0.4)'
          }}>
            👑 PREMIUM ACTIVE
          </span>
        )}
      </div>

      {/* Floating Orb Section */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', margin: '20px 0', gap: '16px' }}>
        <div className="orb-container">
          <div className="orb-element"></div>
          <div className="orb-ring"></div>
          <div className="orb-ring-inner"></div>
          <div className="orb-core"></div>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 'bold', letterSpacing: '2px', textTransform: 'uppercase' }}>
            OS Neural Core Online
          </div>
        </div>
      </div>

      {/* Central Input Search Bar */}
      <form onSubmit={handleSearchSubmit} style={{ width: '100%', maxWidth: '640px', margin: '0 auto' }}>
        <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Ask Nexus AI OS anything..."
            className="glass-input"
            style={{
              width: '100%',
              height: '56px',
              paddingLeft: '56px',
              paddingRight: '64px',
              fontSize: '16px',
              borderRadius: '28px',
              borderWidth: '1.5px'
            }}
          />
          <Search size={20} style={{ position: 'absolute', left: '20px', color: 'var(--text-muted)' }} />
          <button
            type="submit"
            style={{
              position: 'absolute',
              right: '8px',
              width: '40px',
              height: '40px',
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #8b5cf6, #ec4899)',
              border: 'none',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: 'white'
            }}
          >
            <Sparkles size={16} />
          </button>
        </div>
      </form>

      {/* Actions Grid */}
      <div style={{ marginTop: '16px' }}>
        <h3 style={{ fontSize: '14px', fontWeight: 'bold', color: 'var(--text-secondary)', letterSpacing: '1px', marginBottom: '16px', textTransform: 'uppercase' }}>
          SPECIALIZED OS DELEGATES
        </h3>
        
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
          gap: '20px'
        }}>
          {actionCards.map((card, i) => {
            const Icon = card.icon;
            return (
              <div
                key={i}
                onClick={() => {
                  if (card.subtab !== undefined) {
                    window.sessionStorage.setItem('active_studio_tab', card.subtab);
                  }
                  setActiveTab(card.id);
                }}
                className="glass-panel"
                style={{
                  padding: '24px',
                  borderRadius: '20px',
                  cursor: 'pointer',
                  display: 'flex',
                  gap: '16px',
                  alignItems: 'flex-start',
                  transition: 'transform 0.2s, border-color 0.2s'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-4px)';
                  e.currentTarget.style.borderColor = 'rgba(255,255,255,0.15)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.borderColor = 'var(--border-glass)';
                }}
              >
                <div style={{
                  background: `${card.color}15`,
                  border: `1.5px solid ${card.color}35`,
                  padding: '12px',
                  borderRadius: '14px',
                  color: card.color,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  <Icon size={24} />
                </div>
                <div>
                  <h4 style={{ fontSize: '16px', fontWeight: '700', color: 'white', marginBottom: '4px' }}>
                    {card.title}
                  </h4>
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: '1.4' }}>
                    {card.desc}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

    </div>
  );
}
