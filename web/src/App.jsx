import React, { useState, useEffect } from 'react';
import { auth, db } from './services/firebase';
import { doc, getDoc } from 'firebase/firestore';

// Views
import LoginView from './views/LoginView';
import EmailVerificationView from './views/EmailVerificationView';
import Navbar from './components/Navbar';
import HomeView from './views/HomeView';
import ChatView from './views/ChatView';
import VoiceView from './views/VoiceView';
import StudioView from './views/StudioView';
import GameCenterView from './views/GameCenterView';
import ProfileView from './views/ProfileView';

export default function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [profileData, setProfileData] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Verification states
  const [verificationPending, setVerificationPending] = useState(false);
  const [verificationCreds, setVerificationCreds] = useState({ email: '', password: '' });

  // Tab navigation
  const [activeTab, setActiveTab] = useState('home');
  const [chatInputQuery, setChatInputQuery] = useState('');

  // Listen to Auth State
  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged(async (firebaseUser) => {
      setLoading(true);
      if (firebaseUser) {
        if (firebaseUser.emailVerified) {
          setCurrentUser(firebaseUser);
          setVerificationPending(false);
          // Load Profile data
          await fetchProfile(firebaseUser.uid, firebaseUser.email);
        } else {
          // If signed in but not verified (e.g. from Google or previous sessions), log out
          auth.signOut();
          setCurrentUser(null);
          setProfileData(null);
        }
      } else {
        setCurrentUser(null);
        setProfileData(null);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const fetchProfile = async (uid, email) => {
    try {
      const docRef = doc(db, 'users', uid);
      const snapshot = await getDoc(docRef);
      if (snapshot.exists()) {
        setProfileData(snapshot.data());
      } else {
        // Default structure
        setProfileData({
          uid,
          email,
          name: email.split('@')[0],
          premium: false,
          profileImage: '👩‍💻',
          upiId: ''
        });
      }
    } catch (err) {
      console.error("Error reading Firestore profile:", err);
    }
  };

  const handleLoginSuccess = async (user) => {
    setCurrentUser(user);
    setVerificationPending(false);
    await fetchProfile(user.uid, user.email);
  };

  const handleVerifyEmailNeeded = (email, password) => {
    setVerificationCreds({ email, password });
    setVerificationPending(true);
  };

  const handleProfileUpdated = (nextData) => {
    setProfileData(nextData);
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: 'var(--bg-deep)' }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '40px', marginBottom: '16px' }} className="animate-spin">🌀</div>
          <div style={{ fontSize: '13px', fontWeight: 'bold', color: 'var(--text-secondary)', letterSpacing: '1px' }}>
            SYNCHRONIZING NEURAL NODE...
          </div>
        </div>
      </div>
    );
  }

  // Auth Guard
  if (verificationPending) {
    return (
      <div className="iridescent-bg" style={{ minHeight: '100vh' }}>
        <EmailVerificationView 
          email={verificationCreds.email}
          password={verificationCreds.password}
          onVerified={handleLoginSuccess}
          onCancel={() => setVerificationPending(false)}
        />
      </div>
    );
  }

  if (!currentUser || !profileData) {
    return (
      <div className="iridescent-bg" style={{ minHeight: '100vh' }}>
        <LoginView 
          onLoginSuccess={handleLoginSuccess}
          onVerifyEmailNeeded={handleVerifyEmailNeeded}
        />
      </div>
    );
  }

  return (
    <div className="app-container iridescent-bg">
      {/* Navbar / Sidebar */}
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Main Content View Frame */}
      <main className="main-content">
        {activeTab === 'home' && (
          <HomeView 
            user={profileData} 
            setActiveTab={setActiveTab}
            setChatInputQuery={setChatInputQuery}
          />
        )}
        {activeTab === 'chat' && (
          <ChatView 
            user={profileData}
            inputQuery={chatInputQuery}
            clearInputQuery={() => setChatInputQuery('')}
          />
        )}
        {activeTab === 'voice' && (
          <VoiceView user={profileData} />
        )}
        {activeTab === 'studio' && (
          <StudioView user={profileData} />
        )}
        {activeTab === 'games' && (
          <GameCenterView />
        )}
        {activeTab === 'profile' && (
          <ProfileView 
            user={profileData} 
            onProfileUpdated={handleProfileUpdated}
          />
        )}
      </main>
    </div>
  );
}
