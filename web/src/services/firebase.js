import { initializeApp } from 'firebase/app';
import { 
  getAuth, 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  sendEmailVerification,
  GoogleAuthProvider,
  signInWithCredential
} from 'firebase/auth';
import { 
  getFirestore, 
  doc, 
  setDoc, 
  addDoc, 
  collection, 
  query, 
  where, 
  orderBy, 
  onSnapshot 
} from 'firebase/firestore';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);

// --- Auth Helpers ---

export const registerUser = async (email, password, displayName) => {
  const userCredential = await createUserWithEmailAndPassword(auth, email, password);
  const user = userCredential.user;
  // Send email verification
  await sendEmailVerification(user);
  // Sign out immediately as per mobile rules: don't auto-login unverified accounts
  await signOut(auth);
  return user;
};

export const loginUser = async (email, password) => {
  const userCredential = await signInWithEmailAndPassword(auth, email, password);
  const user = userCredential.user;
  // Check email verification
  if (!user.emailVerified) {
    await signOut(auth);
    throw new Error("Email is not verified yet. Please check your inbox.");
  }
  // Sync profile
  await syncUserProfile(user.uid, user.displayName || email.split('@')[0], user.email, true);
  return user;
};

export const logoutUser = () => signOut(auth);

// --- Profile Sync ---

export const syncUserProfile = async (uid, name, email, emailVerified, extra = {}) => {
  const userRef = doc(db, 'users', uid);
  const payload = {
    uid,
    name,
    email,
    emailVerified,
    phone: extra.phone || null,
    profileImage: extra.profileImage || null,
    premium: extra.premium || false,
    updatedAt: Date.now(),
    ...extra
  };
  await setDoc(userRef, payload, { merge: true });
};

// --- Feature Data Persistence ---

export const saveFeatureData = async (tableName, userUid, dataPayload) => {
  const colRef = collection(db, tableName);
  const payload = {
    user_id: userUid,
    created_at: Date.now(),
    ...dataPayload
  };
  return await addDoc(colRef, payload);
};

export const saveChatMessage = (userUid, message, role) => {
  return saveFeatureData('chats', userUid, { message, role });
};

export const saveGeneratedImage = (userUid, prompt, aspectRatio, imageUrl) => {
  return saveFeatureData('generated_images', userUid, { prompt, aspect_ratio: aspectRatio, image_url: imageUrl });
};

export const saveGeneratedMusic = (userUid, prompt, genre, audioUrl) => {
  return saveFeatureData('generated_music', userUid, { prompt, genre, audio_url: audioUrl });
};

export const saveGameScore = (userUid, gameName, score, level) => {
  return saveFeatureData('game_scores', userUid, { game_name: gameName, score, level });
};

// --- Realtime Data Listeners ---

export const listenToFeatureData = (tableName, userUid, onDataChanged) => {
  const colRef = collection(db, tableName);
  const q = query(
    colRef, 
    where('user_id', '==', userUid), 
    orderBy('created_at', 'desc')
  );
  
  return onSnapshot(q, (snapshot) => {
    const items = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
    onDataChanged(items);
  }, (error) => {
    console.error(`Error listening to ${tableName}:`, error);
  });
};
