package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ClipdropService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Key supplied by user in master PRD
    private val apiKey = "pk_9b2962312ae257cd03daf4c14c69a681"

    /**
     * Removes the background from an image Uri using Clipdrop remove-background API.
     * Returns the local File Uri of the cutout PNG, or null if failed.
     */
    suspend fun removeBackground(imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // Read and compress bitmap to reasonable resolution (< 2048px)
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return@withContext null

            // Scale down if larger than 1200px for speed and quota efficiency
            val maxDimension = 1200
            val scaledBitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                val targetWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                val targetHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image_file",
                    "photo.jpg",
                    imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url("https://clipdrop-api.co/remove-background/v1")
                .addHeader("x-api-key", apiKey)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBytes = response.body?.bytes()
                if (responseBytes != null && responseBytes.isNotEmpty()) {
                    val outputFile = File(context.filesDir, "cutout_${System.currentTimeMillis()}.png")
                    FileOutputStream(outputFile).use { fos ->
                        fos.write(responseBytes)
                    }
                    return@withContext Uri.fromFile(outputFile).toString()
                }
            } else {
                val errBody = response.body?.string() ?: "Unknown error"
                Log.e("ClipdropService", "Background removal failed: ${response.code} $errBody")
            }
        } catch (e: Exception) {
            Log.e("ClipdropService", "Error during removeBackground", e)
        }
        return@withContext null
    }
}
