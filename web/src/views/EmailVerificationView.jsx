import React, { useState, useEffect } from 'react';
import { auth, loginUser } from '../services/firebase';
import { sendEmailVerification } from 'firebase/auth';
import { Mail, RefreshCw, LogOut, ShieldCheck } from 'lucide-react';

export default function EmailVerificationView({ email, password, onVerified, onCancel }) {
  const [checking, setChecking] = useState(false);
  const [resending, setResending] = useState(false);
  const [statusMsg, setStatusMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const checkStatus = async () => {
    setChecking(true);
    setErrorMsg('');
    setStatusMsg('Checking status...');
    try {
      // Authenticate temporary session to check verification status
      const user = await loginUser(email, password);
      // If it succeeds, it means they are verified (loginUser throws if not verified)
      setStatusMsg('Email verified successfully! Loading OS...');
      setTimeout(() => {
        onVerified(user);
      }, 1000);
    } catch (err) {
      console.error(err);
      setErrorMsg(err.message || "Still not verified. Please check your inbox and click the verification link.");
      setStatusMsg('');
    } finally {
      setChecking(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    setErrorMsg('');
    setStatusMsg('');
    try {
      // Temporary sign in to resend
      const credential = await auth.signInWithEmailAndPassword(email, password);
      const user = credential.user;
      await sendEmailVerification(user);
      await auth.signOut(); // logout again
      setStatusMsg('Verification email resent! Please check your email.');
    } catch (err) {
      console.error(err);
      setErrorMsg(err.message || 'Failed to resend verification email.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '20px' }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '440px', padding: '40px', display: 'flex', flexDirection: 'column', gap: '24px', textAlign: 'center' }}>
        
        <div style={{ fontSize: '48px', marginBottom: '8px' }}>✉️</div>
        
        <h2 style={{ fontSize: '22px', fontWeight: '800', fontFamily: 'var(--font-brand)' }}>
          Verify Your Email
        </h2>
        
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px', lineHeight: '1.6' }}>
          We sent a verification link to <strong style={{ color: 'white' }}>{email}</strong>.<br/>
          Please click that link to activate your Nexus account.
        </p>

        {statusMsg && (
          <div style={{ padding: '12px', background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.2)', borderRadius: '12px', color: 'var(--success)', fontSize: '13px' }}>
            {statusMsg}
          </div>
        )}

        {errorMsg && (
          <div style={{ padding: '12px', background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: '12px', color: 'var(--error)', fontSize: '13px' }}>
            {errorMsg}
          </div>
        )}

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '8px' }}>
          
          <button 
            onClick={checkStatus} 
            className="btn btn-primary" 
            style={{ height: '48px', gap: '10px' }}
            disabled={checking || resending}
          >
            <ShieldCheck size={18} />
            {checking ? 'Analyzing...' : 'I have verified (Log In)'}
          </button>

          <button 
            onClick={handleResend} 
            className="btn btn-secondary" 
            style={{ height: '48px', gap: '10px' }}
            disabled={checking || resending}
          >
            <RefreshCw size={18} className={resending ? 'animate-spin' : ''} />
            {resending ? 'Synthesizing email...' : 'Resend Verification Email'}
          </button>

          <button 
            onClick={onCancel} 
            className="btn btn-secondary" 
            style={{ height: '48px', gap: '10px', background: 'transparent', borderColor: 'transparent' }}
            disabled={checking || resending}
          >
            <LogOut size={18} />
            Cancel & Back
          </button>

        </div>

      </div>
    </div>
  );
}
