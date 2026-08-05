import React from 'react';
import { Home, Mic, MessageSquare, Palette, Gamepad2, User, LogOut } from 'lucide-react';
import { logoutUser } from '../services/firebase';

export default function Navbar({ activeTab, setActiveTab }) {
  
  const navItems = [
    { id: 'home', label: 'Home', icon: Home },
    { id: 'voice', label: 'Voice', icon: Mic },
    { id: 'chat', label: 'Smart Chat', icon: MessageSquare },
    { id: 'studio', label: 'Studio', icon: Palette },
    { id: 'games', label: 'Games', icon: Gamepad2 },
    { id: 'profile', label: 'Profile', icon: User }
  ];

  const handleLogout = async () => {
    try {
      await logoutUser();
    } catch (err) {
      console.error("Logout failed:", err);
    }
  };

  return (
    <>
      {/* Bottom Nav Bar (Mobile View) */}
      <nav style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        height: '70px',
        background: 'rgba(15, 12, 27, 0.95)',
        backdropFilter: 'blur(10px)',
        borderTop: '1px solid var(--border-glass)',
        display: 'flex',
        justifyContent: 'space-around',
        alignItems: 'center',
        zIndex: 100,
        paddingBottom: 'safe-area-inset-bottom'
      }} className="mobile-only-nav">
        {navItems.map(item => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              style={{
                background: 'none',
                border: 'none',
                color: isActive ? 'var(--nexus-magenta)' : 'var(--text-secondary)',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '4px',
                cursor: 'pointer',
                fontSize: '10px',
                fontWeight: isActive ? 'bold' : '500',
                transition: 'color 0.2s',
                flex: 1
              }}
            >
              <Icon size={20} style={{ transform: isActive ? 'scale(1.1)' : 'none', transition: 'transform 0.2s' }} />
              <span>{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* Sidebar Nav (Desktop View) */}
      <aside style={{
        position: 'fixed',
        top: 0,
        left: 0,
        bottom: 0,
        width: '260px',
        background: 'linear-gradient(180deg, rgba(15,12,27,0.95) 0%, rgba(8,6,13,0.98) 100%)',
        borderRight: '1px solid var(--border-glass)',
        padding: '32px 24px',
        display: 'flex',
        flexDirection: 'column',
        gap: '40px',
        zIndex: 100
      }} className="desktop-only-sidebar">
        
        {/* Title */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span style={{ fontSize: '28px' }}>🔮</span>
          <div>
            <div style={{ fontFamily: 'var(--font-brand)', fontSize: '18px', fontWeight: '800', tracking: '-0.5px' }} className="gradient-text">
              NEXUS OS
            </div>
            <div style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 'bold', letterSpacing: '1px' }}>
              V3.5 WEB ACTIVE
            </div>
          </div>
        </div>

        {/* Links */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1 }}>
          {navItems.map(item => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                style={{
                  background: isActive ? 'var(--bg-card-hover)' : 'transparent',
                  border: 'none',
                  color: isActive ? 'white' : 'var(--text-secondary)',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '16px',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: isActive ? 'bold' : '500',
                  padding: '14px 20px',
                  borderRadius: '14px',
                  width: '100%',
                  textAlign: 'left',
                  transition: 'all 0.2s',
                  boxShadow: isActive ? 'inset 0 0 10px rgba(255,255,255,0.02)' : 'none',
                  borderLeft: isActive ? '3px solid var(--nexus-magenta)' : '3px solid transparent'
                }}
              >
                <Icon size={18} style={{ color: isActive ? 'var(--nexus-magenta)' : 'var(--text-muted)' }} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>

        {/* Logout */}
        <button
          onClick={handleLogout}
          style={{
            background: 'rgba(239,68,68,0.06)',
            border: '1px solid rgba(239,68,68,0.15)',
            color: 'var(--error)',
            display: 'flex',
            alignItems: 'center',
            gap: '16px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: '600',
            padding: '14px 20px',
            borderRadius: '14px',
            width: '100%',
            transition: 'all 0.2s'
          }}
        >
          <LogOut size={18} />
          <span>Disconnect OS</span>
        </button>

      </aside>

      {/* CSS style injectors for layout hides */}
      <style>{`
        .mobile-only-nav { display: flex; }
        .desktop-only-sidebar { display: none; }
        
        @media (min-width: 768px) {
          .mobile-only-nav { display: none !important; }
          .desktop-only-sidebar { display: flex !important; }
        }
      `}</style>
    </>
  );
}
