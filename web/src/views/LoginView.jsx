import React, { useState } from 'react';
import { auth, registerUser, loginUser, syncUserProfile } from '../services/firebase';
import { GoogleAuthProvider, signInWithPopup } from 'firebase/auth';
import { Eye, EyeOff, KeyRound, Mail, User, ShieldAlert, Award } from 'lucide-react';

export default function LoginView({ onLoginSuccess, onVerifyEmailNeeded }) {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [upiId, setUpiId] = useState('');
  const [phone, setPhone] = useState('');
  
  const [showPassword, setShowPassword] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');
    setLoading(true);

    try {
      if (isLogin) {
        // Login Flow
        const user = await loginUser(email, password);
        onLoginSuccess(user);
      } else {
        // Signup Flow
        if (password.length < 6) {
          throw new Error("Password must be at least 6 characters.");
        }
        await registerUser(email, password, name || email.split('@')[0]);
        // After register, user is signed out, profile synced, email verification sent
        // Let's first save profile details locally to sync in Firestore
        // (Wait, registerUser logs out immediately, so we sync on login or create temp entry)
        setSuccessMsg("Account created! Verification email sent. Please check your inbox, click the link, and then log in below.");
        setIsLogin(true);
      }
    } catch (err) {
      console.error(err);
      if (err.message.includes("is not verified")) {
        onVerifyEmailNeeded(email, password);
      } else {
        setErrorMsg(err.message || "An authentication error occurred.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setErrorMsg('');
    setLoading(true);
    const provider = new GoogleAuthProvider();
    try {
      const result = await signInWithPopup(auth, provider);
      const user = result.user;
      await syncUserProfile(user.uid, user.displayName, user.email, true);
      onLoginSuccess(user);
    } catch (err) {
      console.error(err);
      // Suppress the error if the user voluntarily closed the authentication window
      if (err.code !== 'auth/popup-closed-by-user' && !err.message?.includes('auth/popup-closed-by-user')) {
        setErrorMsg(err.message || "Google Sign-In failed.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '20px' }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '440px', padding: '40px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
        
        {/* Title */}
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '48px', marginBottom: '8px' }}>🔮</div>
          <h1 className="gradient-text" style={{ fontSize: '28px', fontWeight: '800', fontFamily: 'var(--font-brand)', letterSpacing: '-0.5px', marginBottom: '6px' }}>
            NEXUS AI OS
          </h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Advanced Artificial Intelligence Operating System
          </p>
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', background: 'rgba(0,0,0,0.25)', borderRadius: '12px', padding: '4px', border: '1px solid var(--border-glass)' }}>
          <button 
            type="button"
            onClick={() => { setIsLogin(true); setErrorMsg(''); setSuccessMsg(''); }}
            style={{
              flex: 1,
              background: isLogin ? 'var(--bg-card-hover)' : 'transparent',
              border: 'none',
              color: isLogin ? 'white' : 'var(--text-secondary)',
              padding: '10px',
              borderRadius: '8px',
              fontWeight: '600',
              cursor: 'pointer',
              transition: 'all 0.2s'
            }}
          >
            Log in
          </button>
          <button 
            type="button"
            onClick={() => { setIsLogin(false); setErrorMsg(''); setSuccessMsg(''); }}
            style={{
              flex: 1,
              background: !isLogin ? 'var(--bg-card-hover)' : 'transparent',
              border: 'none',
              color: !isLogin ? 'white' : 'var(--text-secondary)',
              padding: '10px',
              borderRadius: '8px',
              fontWeight: '600',
              cursor: 'pointer',
              transition: 'all 0.2s'
            }}
          >
            Sign up
          </button>
        </div>

        {/* Messages */}
        {errorMsg && (
          <div style={{ display: 'flex', gap: '8px', padding: '12px', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: '12px', color: 'var(--error)', fontSize: '13px', alignItems: 'center' }}>
            <ShieldAlert size={18} style={{ flexShrink: 0 }} />
            <span>{errorMsg}</span>
          </div>
        )}
        {successMsg && (
          <div style={{ display: 'flex', gap: '8px', padding: '12px', background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.2)', borderRadius: '12px', color: 'var(--success)', fontSize: '13px', alignItems: 'center' }}>
            <Award size={18} style={{ flexShrink: 0 }} />
            <span>{successMsg}</span>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          
          {!isLogin && (
            <div className="form-group">
              <label>Your name</label>
              <div style={{ position: 'relative' }}>
                <input 
                  type="text" 
                  value={name} 
                  onChange={(e) => setName(e.target.value)} 
                  placeholder="Karan" 
                  className="glass-input" 
                  style={{ width: '100%', paddingLeft: '40px' }}
                  required
                />
                <User size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
              </div>
            </div>
          )}

          <div className="form-group">
            <label>Email</label>
            <div style={{ position: 'relative' }}>
              <input 
                type="email" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                placeholder="you@example.com" 
                className="glass-input" 
                style={{ width: '100%', paddingLeft: '40px' }}
                required
              />
              <Mail size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
            </div>
          </div>

          <div className="form-group">
            <label>Password</label>
            <div style={{ position: 'relative' }}>
              <input 
                type={showPassword ? "text" : "password"} 
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                placeholder="At least 6 characters" 
                className="glass-input" 
                style={{ width: '100%', paddingLeft: '40px', paddingRight: '40px' }}
                required
              />
              <KeyRound size={16} style={{ position: 'absolute', left: '14px', top: '15px', color: 'var(--text-muted)' }} />
              <button 
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', position: 'absolute', right: '14px', top: '15px', color: 'var(--text-muted)' }}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          {!isLogin && (
            <>
              <div className="form-group">
                <label>UPI ID (Optional)</label>
                <input 
                  type="text" 
                  value={upiId} 
                  onChange={(e) => setUpiId(e.target.value)} 
                  placeholder="name@upi" 
                  className="glass-input" 
                  style={{ width: '100%' }}
                />
              </div>
              <div className="form-group">
                <label>Phone Number (Optional)</label>
                <input 
                  type="tel" 
                  value={phone} 
                  onChange={(e) => setPhone(e.target.value)} 
                  placeholder="+91 98765 43210" 
                  className="glass-input" 
                  style={{ width: '100%' }}
                />
              </div>
            </>
          )}

          <button 
            type="submit" 
            className="btn btn-primary" 
            style={{ width: '100%', height: '48px', marginTop: '8px' }}
            disabled={loading}
          >
            {loading ? "Vaporizing state..." : (isLogin ? "Log in" : "Create account")}
          </button>

        </form>

        {/* Divider */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ flex: 1, height: '1px', background: 'var(--border-glass)' }}></div>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 'bold' }}>OR</span>
          <div style={{ flex: 1, height: '1px', background: 'var(--border-glass)' }}></div>
        </div>

        {/* Google sign-in */}
        <button 
          onClick={handleGoogleSignIn} 
          className="btn btn-secondary" 
          style={{ width: '100%', height: '48px', gap: '10px' }}
          disabled={loading}
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
          </svg>
          Google Sign-In
        </button>

      </div>
    </div>
  );
}
