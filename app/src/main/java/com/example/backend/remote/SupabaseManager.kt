package com.example.backend.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SupabaseManager {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    companion object {
        var currentAccessToken: String? = "session_token_local"
        var currentUserId: String? = null
        var currentUserEmail: String? = null
        var currentUserPassword: String? = null
        var isCodeVerified: Boolean = false

        private val featureDataStore = mutableMapOf<String, MutableList<Map<String, Any?>>>()
    }

    // --- AUTHENTICATION (Firebase Auth Only) ---

    fun signUp(
        email: String,
        password: String,
        displayName: String = "",
        emailRedirectTo: String = "",
        onResult: (success: Boolean, userId: String?, message: String?) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()

        if (trimmedEmail.isEmpty() || trimmedPass.isEmpty()) {
            onResult(false, null, "Please enter both email and password.")
            return
        }

        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    currentUserEmail = trimmedEmail
                    currentUserPassword = trimmedPass
                    currentUserId = user?.uid

                    // Send Verification Email via Firebase Auth
                    user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                        // STRICT REQUIREMENT: Don't auto login after signup -> Sign out immediately!
                        auth.signOut()

                        if (emailTask.isSuccessful) {
                            Log.d("FirebaseAuth", "Verification email sent to $trimmedEmail")
                            onResult(
                                true,
                                user.uid,
                                "Check your email & verify, then login"
                            )
                        } else {
                            Log.e("FirebaseAuth", "Failed to send verification email", emailTask.exception)
                            onResult(
                                true,
                                user.uid,
                                "Account created! Check your email & verify, then login"
                            )
                        }
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Signup failed. Please try again."
                    Log.e("FirebaseAuth", "SignUp error: $errorMsg")
                    onResult(false, null, errorMsg)
                }
            }
    }

    fun signInWithGoogle(
        email: String,
        displayName: String = "",
        onResult: (success: Boolean, userId: String?, message: String?) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val name = displayName.ifBlank { trimmedEmail.substringBefore("@") }

        currentUserEmail = trimmedEmail
        val user = auth.currentUser
        val uid = user?.uid ?: ("google_" + System.currentTimeMillis())
        currentUserId = uid
        isCodeVerified = true

        syncUserProfile(
            uid = uid,
            name = name,
            email = trimmedEmail,
            emailVerified = true
        )

        onResult(true, uid, "Signed in with Google as $trimmedEmail")
    }

    fun signIn(
        email: String,
        password: String,
        onResult: (success: Boolean, userId: String?, emailVerified: Boolean, message: String?) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()

        if (trimmedEmail.isEmpty() || trimmedPass.isEmpty()) {
            onResult(false, null, false, "Please enter both email and password.")
            return
        }

        auth.signInWithEmailAndPassword(trimmedEmail, trimmedPass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Reload user state to obtain latest email verification status
                        user.reload().addOnCompleteListener { reloadTask ->
                            val isVerified = user.isEmailVerified
                            currentUserEmail = trimmedEmail
                            currentUserPassword = trimmedPass
                            currentUserId = user.uid

                            if (!isVerified) {
                                // STRICT REQUIREMENT: If email NOT verified on login -> BLOCK -> Sign out!
                                auth.signOut()
                                Log.w("FirebaseAuth", "Login blocked: Email $trimmedEmail is not verified yet.")
                                onResult(
                                    true,
                                    user.uid,
                                    false,
                                    "Check your email & verify, then login"
                                )
                            } else {
                                Log.d("FirebaseAuth", "Login successful for verified email $trimmedEmail")
                                onResult(
                                    true,
                                    user.uid,
                                    true,
                                    "Login successful!"
                                )
                            }
                        }
                    } else {
                        onResult(false, null, false, "User session invalid.")
                    }
                } else {
                    val errorMsg = task.exception?.localizedMessage ?: "Invalid email or password."
                    Log.e("FirebaseAuth", "SignIn error: $errorMsg")
                    onResult(false, null, false, errorMsg)
                }
            }
    }

    fun checkEmailVerification(
        accessToken: String? = currentAccessToken,
        onResult: (isVerified: Boolean) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener {
                val verified = user.isEmailVerified
                if (!verified) {
                    auth.signOut()
                }
                onResult(verified)
            }
        } else {
            val email = currentUserEmail
            val pass = currentUserPassword
            if (!email.isNullOrBlank() && !pass.isNullOrBlank()) {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val u = auth.currentUser
                        u?.reload()?.addOnCompleteListener {
                            val verified = u.isEmailVerified
                            if (!verified) {
                                auth.signOut()
                            }
                            onResult(verified)
                        }
                    } else {
                        onResult(false)
                    }
                }
            } else {
                onResult(false)
            }
        }
    }

    fun resendVerificationEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener { task ->
                auth.signOut()
                if (task.isSuccessful) {
                    onResult(true, "Verification email sent to $email!")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Failed to resend verification email.")
                }
            }
        } else {
            val pass = currentUserPassword
            if (!email.isBlank() && !pass.isNullOrBlank()) {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val u = auth.currentUser
                        u?.sendEmailVerification()?.addOnCompleteListener { sendTask ->
                            auth.signOut()
                            if (sendTask.isSuccessful) {
                                onResult(true, "Verification email sent to $email!")
                            } else {
                                onResult(false, sendTask.exception?.localizedMessage ?: "Failed to send email.")
                            }
                        }
                    } else {
                        onResult(false, task.exception?.localizedMessage ?: "Unable to authenticate to resend email.")
                    }
                }
            } else {
                onResult(false, "Verification email sent to $email!")
            }
        }
    }

    fun verifyEmailOtp(
        email: String,
        otpToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        checkEmailVerification { verified ->
            if (verified) {
                onResult(true, "Email verified successfully!")
            } else {
                onResult(false, "Email is not verified yet. Please check your email inbox and click the verification link, then login.")
            }
        }
    }

    // --- FIREBASE FIRESTORE DATA PERSISTENCE ---

    fun syncUserProfile(
        uid: String,
        name: String?,
        email: String?,
        emailVerified: Boolean,
        phone: String? = null,
        profileImage: String? = null,
        premium: Boolean = false,
        onComplete: (Boolean) -> Unit = {}
    ) {
        if (!email.isNullOrEmpty()) currentUserEmail = email
        val userData = hashMapOf<String, Any?>(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "emailVerified" to emailVerified,
            "phone" to phone,
            "profileImage" to profileImage,
            "premium" to premium,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d("FirebaseFirestore", "User profile synced to Firestore for UID $uid")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFirestore", "Error syncing user profile to Firestore", e)
                onComplete(false)
            }
    }

    fun saveFeatureData(
        tableName: String,
        userUid: String,
        dataPayload: Map<String, Any?>,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val entry = mutableMapOf<String, Any?>()
        entry["user_id"] = userUid
        entry["created_at"] = System.currentTimeMillis()
        entry.putAll(dataPayload)

        synchronized(featureDataStore) {
            val list = featureDataStore.getOrPut(tableName) { mutableListOf() }
            list.add(0, entry)
        }

        firestore.collection(tableName)
            .add(entry)
            .addOnSuccessListener { docRef ->
                Log.d("FirebaseFirestore", "Data saved to Firestore collection '$tableName' with ID: ${docRef.id}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseFirestore", "Error saving data to Firestore collection '$tableName'", e)
                onComplete(false)
            }
    }

    fun saveChatMessage(userUid: String, message: String, role: String, onComplete: (Boolean) -> Unit = {}) {
        saveFeatureData("chats", userUid, mapOf("message" to message, "role" to role), onComplete)
    }

    fun saveGeneratedImage(userUid: String, prompt: String, aspectRatio: String, imageUrl: String, onComplete: (Boolean) -> Unit = {}) {
        saveFeatureData("generated_images", userUid, mapOf("prompt" to prompt, "aspect_ratio" to aspectRatio, "image_url" to imageUrl), onComplete)
    }

    fun saveGeneratedMusic(userUid: String, prompt: String, genre: String, audioUrl: String, onComplete: (Boolean) -> Unit = {}) {
        saveFeatureData("generated_music", userUid, mapOf("prompt" to prompt, "genre" to genre, "audio_url" to audioUrl), onComplete)
    }

    fun saveGameScore(userUid: String, gameName: String, score: Int, level: Int, onComplete: (Boolean) -> Unit = {}) {
        saveFeatureData("game_scores", userUid, mapOf("game_name" to gameName, "score" to score, "level" to level), onComplete)
    }

    fun listenToFeatureData(
        tableName: String,
        userUid: String,
        onDataChanged: (List<Map<String, Any?>>) -> Unit
    ): Job {
        return CoroutineScope(Dispatchers.Main).launch {
            try {
                firestore.collection(tableName)
                    .whereEqualTo("user_id", userUid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("FirebaseFirestore", "Listen failed for collection '$tableName'", e)
                            val list = synchronized(featureDataStore) {
                                featureDataStore[tableName]?.toList() ?: emptyList()
                            }
                            onDataChanged(list)
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val items = snapshot.documents.mapNotNull { it.data }
                            synchronized(featureDataStore) {
                                featureDataStore[tableName] = items.toMutableList()
                            }
                            onDataChanged(items)
                        }
                    }
            } catch (ex: Exception) {
                Log.e("FirebaseFirestore", "Exception in listenToFeatureData", ex)
            }

            while (isActive) {
                val list = synchronized(featureDataStore) {
                    featureDataStore[tableName]?.toList() ?: emptyList()
                }
                onDataChanged(list)
                delay(3000)
            }
        }
    }
}
