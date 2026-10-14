package com.example.data.remote

import android.util.Log
import com.example.data.model.Memory
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreService {

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    suspend fun saveMemory(memory: Memory): Result<Unit> {
        return try {
            val userMemoriesCol = firestore.collection("users")
                .document(memory.userId.ifBlank { "anonymous" })
                .collection("memories")

            val data = hashMapOf(
                "id" to memory.id,
                "userId" to memory.userId,
                "title" to memory.title,
                "date" to memory.date,
                "category" to memory.category,
                "customCategory" to memory.customCategory,
                "description" to memory.description,
                "imageUri" to memory.imageUri,
                "cutoutImageUri" to memory.cutoutImageUri,
                "location" to memory.location,
                "people" to memory.people,
                "tags" to memory.tags,
                "isFavorite" to memory.isFavorite,
                "reminderEnabled" to memory.reminderEnabled,
                "yearlyAnniversary" to memory.yearlyAnniversary,
                "reminderTime" to memory.reminderTime,
                "advanceReminderDays" to memory.advanceReminderDays,
                "createdAt" to memory.createdAt,
                "updatedAt" to memory.updatedAt
            )

            userMemoriesCol.document(memory.id).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreService", "saveMemory failed (offline or unauthenticated): ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun fetchMemories(userId: String): Result<List<Memory>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId.ifBlank { "anonymous" })
                .collection("memories")
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    Memory(
                        id = doc.getString("id") ?: doc.id,
                        userId = doc.getString("userId") ?: userId,
                        title = doc.getString("title") ?: "",
                        date = doc.getString("date") ?: "",
                        category = doc.getString("category") ?: "Everyday",
                        customCategory = doc.getString("customCategory"),
                        description = doc.getString("description") ?: "",
                        imageUri = doc.getString("imageUri"),
                        cutoutImageUri = doc.getString("cutoutImageUri"),
                        location = doc.getString("location"),
                        people = doc.getString("people"),
                        tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                        isFavorite = doc.getBoolean("isFavorite") ?: false,
                        reminderEnabled = doc.getBoolean("reminderEnabled") ?: true,
                        yearlyAnniversary = doc.getBoolean("yearlyAnniversary") ?: true,
                        reminderTime = doc.getString("reminderTime") ?: "09:00",
                        advanceReminderDays = (doc.getLong("advanceReminderDays") ?: 0L).toInt(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                        isSynced = true
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.w("FirestoreService", "fetchMemories failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteMemory(userId: String, memoryId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId.ifBlank { "anonymous" })
                .collection("memories")
                .document(memoryId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreService", "deleteMemory failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val data = hashMapOf(
                "uid" to profile.uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "photoUrl" to profile.photoUrl,
                "bio" to profile.bio,
                "gender" to profile.gender,
                "age" to profile.age,
                "location" to profile.location,
                "joinedDate" to profile.joinedDate,
                "onboardingCompleted" to profile.onboardingCompleted,
                "themeName" to profile.themeName,
                "backgroundStyle" to profile.backgroundStyle,
                "fontStyle" to profile.fontStyle,
                "notificationsEnabled" to profile.notificationsEnabled,
                "callReminderEnabled" to profile.callReminderEnabled,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(profile.uid.ifBlank { "anonymous" })
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreService", "saveUserProfile failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(userId: String): Result<UserProfile?> {
        return try {
            val doc = firestore.collection("users")
                .document(userId.ifBlank { "anonymous" })
                .get()
                .await()

            if (!doc.exists()) {
                return Result.success(null)
            }

            val profile = UserProfile(
                uid = doc.getString("uid") ?: userId,
                displayName = doc.getString("displayName") ?: "Rahul Shah",
                email = doc.getString("email") ?: "",
                photoUrl = doc.getString("photoUrl"),
                bio = doc.getString("bio") ?: "Collecting moments, not things.",
                gender = doc.getString("gender") ?: "Male",
                age = doc.getString("age") ?: "28 years",
                location = doc.getString("location") ?: "Kathmandu, Nepal",
                joinedDate = doc.getString("joinedDate") ?: "Oct 15, 2025",
                onboardingCompleted = doc.getBoolean("onboardingCompleted") ?: false,
                themeName = doc.getString("themeName") ?: "Lavender Dream",
                backgroundStyle = doc.getString("backgroundStyle") ?: "Pastel",
                fontStyle = doc.getString("fontStyle") ?: "Default",
                notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
                callReminderEnabled = doc.getBoolean("callReminderEnabled") ?: true,
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
            )
            Result.success(profile)
        } catch (e: Exception) {
            Log.w("FirestoreService", "fetchUserProfile failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteUserData(userId: String): Result<Unit> {
        return try {
            val memories = firestore.collection("users")
                .document(userId)
                .collection("memories")
                .get()
                .await()

            for (doc in memories.documents) {
                doc.reference.delete().await()
            }

            firestore.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreService", "deleteUserData failed: ${e.message}")
            Result.failure(e)
        }
    }
}
