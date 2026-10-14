package com.example.data.repository

import com.example.data.local.PreferencesDataStore
import com.example.data.model.UserProfile
import com.example.data.remote.FirebaseAuthService
import com.example.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirestoreService,
    private val preferencesDataStore: PreferencesDataStore
) {
    val currentUser: FirebaseUser?
        get() = authService.currentUser

    val isUserSignedIn: Boolean
        get() = authService.isUserSignedIn

    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        val res = authService.signInWithGoogle()
        if (res.isSuccess) {
            val user = res.getOrNull()
            if (user != null) {
                syncProfileAfterLogin(user)
            }
        }
        return res
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val res = authService.signInWithEmail(email, pass)
        if (res.isSuccess) {
            val user = res.getOrNull()
            if (user != null) {
                syncProfileAfterLogin(user)
            }
        }
        return res
    }

    suspend fun registerWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val res = authService.registerWithEmail(email, pass)
        if (res.isSuccess) {
            val user = res.getOrNull()
            if (user != null) {
                val profile = UserProfile(
                    uid = user.uid,
                    displayName = user.displayName ?: email.substringBefore("@"),
                    email = user.email ?: email,
                    joinedDate = "Oct 15, 2025"
                )
                firestoreService.saveUserProfile(profile)
            }
        }
        return res
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return authService.sendPasswordReset(email)
    }

    fun signOut() {
        authService.signOut()
    }

    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = authService.currentUser?.uid
        if (uid != null) {
            firestoreService.deleteUserData(uid)
        }
        authService.deleteAccount()
    }

    private suspend fun syncProfileAfterLogin(user: FirebaseUser) {
        val cloudProfileResult = firestoreService.fetchUserProfile(user.uid)
        if (cloudProfileResult.isSuccess && cloudProfileResult.getOrNull() != null) {
            val p = cloudProfileResult.getOrNull()!!
            preferencesDataStore.updateUserProfile(
                name = p.displayName,
                email = p.email,
                bio = p.bio,
                location = p.location,
                age = p.age,
                gender = p.gender
            )
            preferencesDataStore.setTheme(p.themeName)
            preferencesDataStore.setBackground(p.backgroundStyle)
            preferencesDataStore.setFont(p.fontStyle)
            preferencesDataStore.setNotifications(p.notificationsEnabled)
            preferencesDataStore.setCallReminder(p.callReminderEnabled)
            if (p.onboardingCompleted) {
                preferencesDataStore.setOnboardingDone(true)
            }
        } else {
            // First time profile creation in Firestore
            val defaultName = user.displayName ?: preferencesDataStore.userNameFlow.first()
            val defaultEmail = user.email ?: preferencesDataStore.userEmailFlow.first()
            val initialProfile = UserProfile(
                uid = user.uid,
                displayName = defaultName,
                email = defaultEmail,
                joinedDate = "Oct 15, 2025"
            )
            firestoreService.saveUserProfile(initialProfile)
        }
    }
}
