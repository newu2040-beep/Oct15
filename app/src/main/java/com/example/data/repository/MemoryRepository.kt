package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.MemoryDao
import com.example.data.local.MemoryEntity
import com.example.data.model.Memory
import com.example.data.receiver.ReminderScheduler
import com.example.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MemoryRepository(
    private val memoryDao: MemoryDao,
    private val firestoreService: FirestoreService,
    private val context: Context
) {
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"

    val memoriesFlow: Flow<List<Memory>> = memoryDao.getAllMemoriesFlow().map { entities ->
        entities.map { it.toMemory() }
    }

    suspend fun getMemoryById(id: String): Memory? = withContext(Dispatchers.IO) {
        memoryDao.getMemoryById(id)?.toMemory()
    }

    suspend fun saveMemory(memory: Memory): Result<Unit> = withContext(Dispatchers.IO) {
        val memoryToSave = if (memory.id.isBlank()) {
            memory.copy(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            memory.copy(
                userId = currentUserId,
                updatedAt = System.currentTimeMillis()
            )
        }

        // 1. Save to local Room DB immediately
        memoryDao.insertMemory(MemoryEntity.fromMemory(memoryToSave.copy(isSynced = false)))

        // 2. Schedule local reminder alarm
        ReminderScheduler.scheduleReminder(context, memoryToSave)

        // 3. Sync to Cloud Firestore asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = firestoreService.saveMemory(memoryToSave)
                if (result.isSuccess) {
                    memoryDao.updateMemory(MemoryEntity.fromMemory(memoryToSave.copy(isSynced = true)))
                }
            } catch (e: Exception) {
                Log.w("MemoryRepository", "Async cloud sync failed: ${e.message}")
            }
        }

        Result.success(Unit)
    }

    suspend fun deleteMemory(memoryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Cancel reminder
        ReminderScheduler.cancelReminder(context, memoryId)

        // Delete from local DB
        memoryDao.deleteMemoryById(memoryId)

        // Delete from Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestoreService.deleteMemory(currentUserId, memoryId)
            } catch (e: Exception) {
                Log.w("MemoryRepository", "Cloud delete failed: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    suspend fun toggleFavorite(memoryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = memoryDao.getMemoryById(memoryId)?.toMemory() ?: return@withContext Result.failure(Exception("Not found"))
        val updated = existing.copy(isFavorite = !existing.isFavorite)
        saveMemory(updated)
    }

    suspend fun toggleReminder(memoryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = memoryDao.getMemoryById(memoryId)?.toMemory() ?: return@withContext Result.failure(Exception("Not found"))
        val updated = existing.copy(reminderEnabled = !existing.reminderEnabled)
        saveMemory(updated)
    }

    suspend fun syncWithCloud(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext Result.success(Unit)
        val cloudResult = firestoreService.fetchMemories(uid)
        if (cloudResult.isSuccess) {
            val cloudMemories = cloudResult.getOrNull() ?: emptyList()
            if (cloudMemories.isNotEmpty()) {
                val entities = cloudMemories.map { MemoryEntity.fromMemory(it.copy(isSynced = true)) }
                memoryDao.insertMemories(entities)
                cloudMemories.forEach { ReminderScheduler.scheduleReminder(context, it) }
            }
        }
        Result.success(Unit)
    }

    /**
     * Seeds initial showcase memories if database is empty, exactly matching the user showcase:
     * - First Love ❤️ (May 12, 2018)
     * - First Job 💼 (Mar 03, 2020)
     * - First Phone 📱 (Jun 21, 2021)
     * - Last Breakup 💔 (Jan 14, 2023)
     * - First Crush 🥰 (Aug 09, 2024)
     */
    suspend fun seedInitialShowcaseMemoriesIfEmpty() = withContext(Dispatchers.IO) {
        val initialSamples = listOf(
            Memory(
                id = "sample_first_love",
                userId = currentUserId,
                title = "First Love",
                date = "2018-05-12",
                category = "Love",
                description = "Walking through the park under the jacaranda blossoms. We talked for hours until the streetlights came on.",
                people = "Elena",
                location = "Central Park",
                tags = listOf("love", "park", "spring"),
                reminderEnabled = true,
                yearlyAnniversary = true,
                reminderTime = "09:00"
            ),
            Memory(
                id = "sample_first_job",
                userId = currentUserId,
                title = "First Job",
                date = "2020-03-03",
                category = "Career",
                description = "Received my first official offer letter as a junior software engineer. Celebrated with pizza and family.",
                location = "Downtown Tech Hub",
                tags = listOf("career", "code", "milestone"),
                reminderEnabled = true,
                yearlyAnniversary = true,
                reminderTime = "10:00"
            ),
            Memory(
                id = "sample_first_phone",
                userId = currentUserId,
                title = "First Phone",
                date = "2021-06-21",
                category = "Milestones",
                description = "Bought my very first smartphone with my own hard-earned savings. Felt so independent and proud.",
                location = "Electronics Galleria",
                tags = listOf("milestone", "savings"),
                reminderEnabled = true,
                yearlyAnniversary = true,
                reminderTime = "09:00"
            ),
            Memory(
                id = "sample_last_breakup",
                userId = currentUserId,
                title = "Last Breakup",
                date = "2023-01-14",
                category = "Personal growth",
                description = "A painful but necessary goodbye at the quiet coffee shop. It taught me what I truly value and how to love myself.",
                location = "Corner Cafe",
                tags = listOf("growth", "reflection"),
                reminderEnabled = false,
                yearlyAnniversary = true,
                reminderTime = "09:00"
            ),
            Memory(
                id = "sample_first_crush",
                userId = currentUserId,
                title = "First Crush",
                date = "2024-08-09",
                category = "Love",
                description = "Sitting across in the library sharing headphones listening to acoustic melodies. Butterflies that never left.",
                location = "City Library",
                tags = listOf("music", "butterflies"),
                reminderEnabled = true,
                yearlyAnniversary = true,
                reminderTime = "11:00"
            )
        )

        for (sample in initialSamples) {
            val existing = memoryDao.getMemoryById(sample.id)
            if (existing == null) {
                memoryDao.insertMemory(MemoryEntity.fromMemory(sample))
                ReminderScheduler.scheduleReminder(context, sample)
            }
        }
    }
}
