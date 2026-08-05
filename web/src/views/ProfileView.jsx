import React, { useState, useEffect } from 'react';
import { 
  syncUserProfile, 
  saveFeatureData, 
  listenToFeatureData,
  db
} from '../services/firebase';
import { doc, deleteDoc } from 'firebase/firestore';
import { User, CreditCard, Phone, Crown, Trash2, Plus, Star } from 'lucide-react';

const AVATARS = [
  "👩‍💻", "👨‍💻", "🤖", "🔮", "👽", "🦄", "🐼", "🦊", "🦁"
];

export default function ProfileView({ user, onProfileUpdated }) {
  const [name, setName] = useState(user.name || '');
  const [upiId, setUpiId] = useState(user.upiId || '');
  const [phone, setPhone] = useState(user.phone || '');
  const [avatar, setAvatar] = useState(user.profileImage || '👩‍💻');
  const [isPremium, setIsPremium] = useState(user.premium || false);
  const [isUpdating, setIsUpdating] = useState(false);
  
  // Memories
  const [memories, setMemories] = useState([]);
  const [newCategory, setNewCategory] = useState('Preference');
  const [newFact, setNewFact] = useState('');
  const [isPinned, setIsPinned] = useState(false);
  const [savingMemory, setSavingMemory] = useState(false);

  useEffect(() => {
    // Listen to user's saved memory facts
    const unsubscribe = listenToFeatureData('memory_facts', user.uid, (data) => {
      setMemories(data);
    });
    return () => unsubscribe();
  }, [user.uid]);

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setIsUpdating(true);
    try {
      const extra = {
        upiId,
        phone,
        profileImage: avatar,
        premium: isPremium
      };
      await syncUserProfile(user.uid, name, user.email, true, extra);
      onProfileUpdated({ ...user, name, ...extra });
      alert("Profile nodes updated successfully!");
    } catch (err) {
      console.error(err);
      alert("Error syncing profile properties: " + err.message);
    } finally {
      setIsUpdating(false);
    }
  };

  const handleAddMemory = async (e) => {
    e.preventDefault();
    if (!newFact.trim()) return;

    setSavingMemory(true);
    try {
      await saveFeatureData('memory_facts', user.uid, {
        category: newCategory,
        fact: newFact,
        isPinned: isPinned
      });
      setNewFact('');
      setIsPinned(false);
    } catch (err) {
      console.error(err);
      alert("Failed to write memory property: " + err.message);
    } finally {
      setSavingMemory(false);
    }
  };

  const handleDeleteMemory = async (id) => {
    try {
      const docRef = doc(db, 'memory_facts', id);
      await deleteDoc(docRef);
    } catch (err) {
      console.error(err);
      alert("Failed to delete memory node: " + err.message);
    }
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '32px' }} className="profile-grid-mobile">
      
      {/* Settings Form */}
      <div className="glass-panel" style={{ padding: '32px', borderRadius: '24px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        
        <div>
          <h3 style={{ fontSize: '18px', fontWeight: '800', fontFamily: 'var(--font-brand)', marginBottom: '4px' }}>
            OS Node Configuration
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>
            Modify your user details and premium status settings
          </p>
        </div>

        {/* Avatar Neon Selector */}
        <div className="form-group" style={{ alignItems: 'center' }}>
          <label style={{ alignSelf: 'flex-start' }}>CHOOSE AVATAR</label>
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginTop: '8px', justifyContent: 'center' }}>
            {AVATARS.map(av => (
              <button
                key={av}
                type="button"
                onClick={() => setAvatar(av)}
                style={{
                  fontSize: '28px',
                  width: '54px',
                  height: '54px',
                  borderRadius: '50%',
                  background: avatar === av ? 'var(--bg-card-hover)' : 'transparent',
                  border: avatar === av ? '2px solid var(--nexus-magenta)' : '1px solid var(--border-glass)',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'all 0.2s',
                  boxShadow: avatar === av ? '0 0 12px var(--magenta-glow)' : 'none'
                }}
              >
                {av}
              </button>
            ))}
          </div>
        </div>

        <form onSubmit={handleProfileSave} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          <div className="form-group">
            <label>DISPLAY NAME</label>
            <div style={{ position: 'relative' }}>
              <input 
                type="text" 
                value={name} 
                onChange={e => setName(e.target.value)} 
                placeholder="Name" 
                className="glass-input" 
                style={{ width: '100%', paddingLeft: '40px' }}
                required
              />
              <User size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
            </div>
          </div>

          <div className="form-group">
            <label>UPI ID (FOR PAYMENT ROUTING)</label>
            <div style={{ position: 'relative' }}>
              <input 
                type="text" 
                value={upiId} 
                onChange={e => setUpiId(e.target.value)} 
                placeholder="name@upi" 
                className="glass-input" 
                style={{ width: '100%', paddingLeft: '40px' }}
              />
              <CreditCard size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
            </div>
          </div>

          <div className="form-group">
            <label>PHONE NUMBER</label>
            <div style={{ position: 'relative' }}>
              <input 
                type="tel" 
                value={phone} 
                onChange={e => setPhone(e.target.value)} 
                placeholder="+91 98765 43210" 
                className="glass-input" 
                style={{ width: '100%', paddingLeft: '40px' }}
              />
              <Phone size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
            </div>
          </div>

          {/* Premium Selector Card */}
          <div 
            onClick={() => setIsPremium(!isPremium)}
            className="glass-panel" 
            style={{ 
              padding: '16px 20px', 
              borderRadius: '16px', 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'space-between',
              cursor: 'pointer',
              border: isPremium ? '1.5px solid #f59e0b' : '1px solid var(--border-glass)',
              background: isPremium ? 'rgba(245,158,11,0.06)' : 'transparent',
              boxShadow: isPremium ? '0 0 15px rgba(245,158,11,0.25)' : 'none',
              transition: 'all 0.3s'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
              <div style={{
                background: isPremium ? '#f59e0b25' : 'rgba(255,255,255,0.05)',
                padding: '10px',
                borderRadius: '12px',
                color: isPremium ? '#f59e0b' : 'var(--text-muted)'
              }}>
                <Crown size={20} />
              </div>
              <div>
                <h4 style={{ fontSize: '14px', color: 'white', fontWeight: 'bold' }}>Premium Status Badge</h4>
                <p style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>Unlocks elite model limits & custom styles</p>
              </div>
            </div>
            <input 
              type="checkbox" 
              checked={isPremium} 
              onChange={() => {}} // Handled by div click
              style={{ accentColor: '#f59e0b', width: '16px', height: '16px', cursor: 'pointer' }} 
            />
          </div>

          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%', height: '48px', marginTop: '12px' }}
            disabled={isUpdating}
          >
            {isUpdating ? "Syncing configs..." : "Save Configuration Node"}
          </button>

        </form>

      </div>

      {/* Memory Manager */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
        
        {/* Add Memory Panel */}
        <div className="glass-panel" style={{ padding: '24px', borderRadius: '20px' }}>
          <h3 style={{ fontSize: '16px', fontWeight: '800', fontFamily: 'var(--font-brand)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Star size={16} style={{ color: 'var(--nexus-magenta)' }} />
            Neural Memory Manager
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '12px', marginBottom: '16px' }}>
            Teach Nexus AI facts about you to personalize response models
          </p>

          <form onSubmit={handleAddMemory} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div className="form-group">
              <label>CATEGORY</label>
              <select 
                value={newCategory} 
                onChange={e => setNewCategory(e.target.value)}
                style={{
                  background: 'rgba(0,0,0,0.25)',
                  border: '1px solid var(--border-glass)',
                  color: 'white',
                  padding: '8px 12px',
                  borderRadius: '10px',
                  fontSize: '13px',
                  outline: 'none',
                  cursor: 'pointer'
                }}
              >
                <option value="Preference">Preference</option>
                <option value="Fact">Personal Fact</option>
                <option value="Hobby">Hobby / Interest</option>
                <option value="Rule">Behavior Rule</option>
              </select>
            </div>

            <div className="form-group">
              <label>FACT CONTENT</label>
              <input 
                type="text" 
                value={newFact} 
                onChange={e => setNewFact(e.target.value)} 
                placeholder="Remember that my favorite programming language is Python." 
                className="glass-input" 
                style={{ width: '100%' }}
                required
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input 
                type="checkbox" 
                id="pin-memory"
                checked={isPinned}
                onChange={e => setIsPinned(e.target.checked)}
                style={{ accentColor: 'var(--nexus-magenta)' }}
              />
              <label htmlFor="pin-memory" style={{ fontSize: '12px', fontWeight: 'bold', color: 'var(--text-secondary)', cursor: 'pointer' }}>
                Pin to Active Context Box
              </label>
            </div>

            <button 
              type="submit" 
              className="btn btn-secondary" 
              style={{ width: '100%', padding: '10px', gap: '6px' }}
              disabled={savingMemory || !newFact.trim()}
            >
              <Plus size={16} />
              Add Memory Node
            </button>
          </form>
        </div>

        {/* Memories List */}
        <div className="glass-panel" style={{ padding: '24px', borderRadius: '20px', flex: 1, display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <h4 style={{ fontSize: '13px', fontWeight: 'bold', color: 'var(--text-secondary)', letterSpacing: '0.5px' }}>
            ACTIVE CONTEXT FACTS ({memories.length})
          </h4>

          <div style={{ 
            display: 'flex', 
            flexDirection: 'column', 
            gap: '10px', 
            overflowY: 'auto', 
            maxHeight: '260px' 
          }}>
            {memories.length === 0 ? (
              <div style={{ padding: '20px 0', textAlign: 'center', color: 'var(--text-muted)', fontSize: '12px' }}>
                No memory facts recorded. Add a fact above!
              </div>
            ) : (
              memories.map(mem => (
                <div 
                  key={mem.id} 
                  style={{
                    background: 'rgba(255,255,255,0.02)',
                    border: '1px solid var(--border-glass)',
                    padding: '12px 16px',
                    borderRadius: '12px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: '10px'
                  }}
                >
                  <div style={{ overflow: 'hidden' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ 
                        fontSize: '9px', 
                        fontWeight: 'bold', 
                        background: 'rgba(236,72,153,0.15)', 
                        color: 'var(--nexus-magenta)',
                        padding: '2px 6px',
                        borderRadius: '4px'
                      }}>
                        {mem.category.toUpperCase()}
                      </span>
                      {mem.isPinned && (
                        <span style={{ fontSize: '10px' }}>📌</span>
                      )}
                    </div>
                    <div style={{ fontSize: '13px', color: 'white', marginTop: '6px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                      {mem.fact}
                    </div>
                  </div>
                  <button
                    onClick={() => handleDeleteMemory(mem.id)}
                    style={{
                      background: 'transparent',
                      border: 'none',
                      color: 'var(--text-muted)',
                      cursor: 'pointer',
                      transition: 'color 0.2s',
                      display: 'flex',
                      alignItems: 'center',
                      padding: '4px'
                    }}
                    onMouseEnter={e => e.currentTarget.style.color = 'var(--error)'}
                    onMouseLeave={e => e.currentTarget.style.color = 'var(--text-muted)'}
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ))
            )}
          </div>
        </div>

      </div>

      <style>{`
        @media(max-width: 768px) {
          .profile-grid-mobile { grid-template-columns: 1fr !important; }
        }
      `}</style>

    </div>
  );
}
