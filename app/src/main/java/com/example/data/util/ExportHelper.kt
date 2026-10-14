package com.example.data.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.Memory
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

object ExportHelper {

    private fun getShareUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun launchShareIntent(context: Context, uri: Uri, mimeType: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Exports a Memory as a formatted PDF invoice / certificate of memory.
     */
    fun exportToPdfInvoice(context: Context, memory: Memory, userName: String = "Rahul Shah") {
        try {
            val pdfDoc = PdfDocument()
            val pageWidth = 595 // A4 standard point width
            val pageHeight = 842 // A4 standard point height
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            // Background soft cream
            val bgPaint = Paint().apply { color = Color.parseColor("#FFFDF8") }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // Header Banner (Pastel Lavender)
            val headerPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, pageWidth.toFloat(), 130f,
                    Color.parseColor("#8B72D6"),
                    Color.parseColor("#A38CE6"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 130f, headerPaint)

            // Header text
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("OCT15 — MEMORY INVOICE", 40f, 55f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#F3E8FF")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("Official Certified Keepsake Archive • Your life, remembered.", 40f, 85f, subtitlePaint)
            canvas.drawText("Curator: $userName  •  Tagline: Made with love by Rahul Shah", 40f, 108f, subtitlePaint)

            // Outer decorative frame
            val framePaint = Paint().apply {
                color = Color.parseColor("#E0D6F5")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(RectF(25f, 150f, pageWidth - 25f, pageHeight - 40f), 16f, 16f, framePaint)

            var yOffset = 190f
            val labelPaint = Paint().apply {
                color = Color.parseColor("#7C6DAF")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val textPaint = TextPaint().apply {
                color = Color.parseColor("#2B2735")
                textSize = 15f
                isAntiAlias = true
            }

            // Memory Title
            canvas.drawText("ITEM / MEMORY TITLE", 45f, yOffset, labelPaint)
            yOffset += 24f
            val memoryTitlePaint = Paint().apply {
                color = Color.parseColor("#2B2735")
                textSize = 22f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(memory.title.ifBlank { "Untitled Memory" }, 45f, yOffset, memoryTitlePaint)
            yOffset += 32f

            // Metadata Row: Date & Category & Location
            val dividerPaint = Paint().apply {
                color = Color.parseColor("#EFEBF8")
                strokeWidth = 1f
            }
            canvas.drawLine(45f, yOffset, pageWidth - 45f, yOffset, dividerPaint)
            yOffset += 25f

            canvas.drawText("DATE OF MOMENT:", 45f, yOffset, labelPaint)
            canvas.drawText(memory.formattedDate(), 180f, yOffset, textPaint)
            yOffset += 24f

            canvas.drawText("CATEGORY:", 45f, yOffset, labelPaint)
            canvas.drawText(memory.category, 180f, yOffset, textPaint)
            yOffset += 24f

            val yearsSince = memory.yearsSince(LocalDate.now())
            canvas.drawText("TIME ELAPSED:", 45f, yOffset, labelPaint)
            canvas.drawText("$yearsSince years ago", 180f, yOffset, textPaint)
            yOffset += 24f

            if (!memory.location.isNullOrBlank()) {
                canvas.drawText("LOCATION:", 45f, yOffset, labelPaint)
                canvas.drawText(memory.location, 180f, yOffset, textPaint)
                yOffset += 24f
            }

            if (!memory.people.isNullOrBlank()) {
                canvas.drawText("COMPANIONS:", 45f, yOffset, labelPaint)
                canvas.drawText(memory.people, 180f, yOffset, textPaint)
                yOffset += 24f
            }

            canvas.drawLine(45f, yOffset, pageWidth - 45f, yOffset, dividerPaint)
            yOffset += 25f

            // Story / Description
            canvas.drawText("JOURNAL RECORD & DESCRIPTION", 45f, yOffset, labelPaint)
            yOffset += 15f

            val storyText = memory.description.ifBlank { "A treasured personal moment recorded in the OCT15 timeline." }
            val storyLayout = StaticLayout.Builder.obtain(
                storyText, 0, storyText.length, textPaint, pageWidth - 90
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(4f, 1f)
                .build()

            canvas.save()
            canvas.translate(45f, yOffset)
            storyLayout.draw(canvas)
            canvas.restore()

            yOffset += storyLayout.height + 40f

            // Tags
            if (memory.tags.isNotEmpty()) {
                canvas.drawText("TAGS:", 45f, yOffset, labelPaint)
                canvas.drawText(memory.tags.joinToString("  •  ") { "#$it" }, 100f, yOffset, textPaint)
                yOffset += 30f
            }

            // Bottom Seal / Certificate Footer
            val footerY = pageHeight - 70f
            canvas.drawLine(45f, footerY - 20f, pageWidth - 45f, footerY - 20f, dividerPaint)

            val footerPaint = Paint().apply {
                color = Color.parseColor("#988FA5")
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("Keepsake Reference ID: ${memory.id.ifBlank { "OCT15-${System.currentTimeMillis()}" }}", 45f, footerY, footerPaint)
            canvas.drawText("Generated on ${LocalDate.now()} • OCT15 Android App", 45f, footerY + 16f, footerPaint)
            canvas.drawText("VERIFIED GENUINE MEMORY ARCHIVE", pageWidth - 260f, footerY + 8f, Paint().apply {
                color = Color.parseColor("#8B72D6")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            })

            pdfDoc.finishPage(page)

            val cacheFile = File(context.cacheDir, "OCT15_Invoice_${System.currentTimeMillis()}.pdf")
            FileOutputStream(cacheFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            val shareUri = getShareUri(context, cacheFile)
            launchShareIntent(context, shareUri, "application/pdf", "Share Memory Invoice PDF")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Exports memory list or single memory to CSV format.
     */
    fun exportToCsv(context: Context, memories: List<Memory>) {
        try {
            val sb = java.lang.StringBuilder()
            sb.append("ID,Title,Date,Category,Location,People,Tags,YearsSince,Description\n")
            memories.forEach { m ->
                val escapedTitle = escapeCsv(m.title)
                val escapedCat = escapeCsv(m.category)
                val escapedLoc = escapeCsv(m.location ?: "")
                val escapedPeople = escapeCsv(m.people ?: "")
                val escapedTags = escapeCsv(m.tags.joinToString(";"))
                val escapedDesc = escapeCsv(m.description)
                val years = m.yearsSince(LocalDate.now())
                sb.append("${m.id},$escapedTitle,${m.date},$escapedCat,$escapedLoc,$escapedPeople,$escapedTags,$years,$escapedDesc\n")
            }

            val cacheFile = File(context.cacheDir, "OCT15_Memories_Export.csv")
            cacheFile.writeText(sb.toString(), Charsets.UTF_8)

            val shareUri = getShareUri(context, cacheFile)
            launchShareIntent(context, shareUri, "text/csv", "Share Memories CSV")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Exports memories to formatted Markdown (.md).
     */
    fun exportToMarkdown(context: Context, memories: List<Memory>, title: String = "OCT15 Memory Journal") {
        try {
            val sb = java.lang.StringBuilder()
            sb.append("# $title\n\n")
            sb.append("> *\"Your life, remembered. Preserved with OCT15.\"*\n\n")
            sb.append("**Export Date:** ${LocalDate.now()}  \n")
            sb.append("**Total Memories:** ${memories.size}  \n\n")
            sb.append("---\n\n")

            memories.forEachIndexed { index, m ->
                sb.append("## ${index + 1}. ${m.title.ifBlank { "Untitled Moment" }}\n\n")
                sb.append("- **Date:** ${m.formattedDate()} (${m.yearsSince(LocalDate.now())} years ago)\n")
                sb.append("- **Category:** `${m.category}`\n")
                if (!m.location.isNullOrBlank()) sb.append("- **Location:** 📍 ${m.location}\n")
                if (!m.people.isNullOrBlank()) sb.append("- **People:** 👥 ${m.people}\n")
                if (m.tags.isNotEmpty()) sb.append("- **Tags:** ${m.tags.joinToString(" ") { "`#$it`" }}\n")
                sb.append("\n")
                sb.append("### Story\n\n")
                sb.append("> ${m.description.ifBlank { "No story description recorded." }}\n\n")
                sb.append("---\n\n")
            }

            val cacheFile = File(context.cacheDir, "OCT15_Journal.md")
            cacheFile.writeText(sb.toString(), Charsets.UTF_8)

            val shareUri = getShareUri(context, cacheFile)
            launchShareIntent(context, shareUri, "text/markdown", "Share Memories Markdown")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Exports memories to clean plain TXT format.
     */
    fun exportToTxt(context: Context, memories: List<Memory>) {
        try {
            val sb = java.lang.StringBuilder()
            sb.append("========================================\n")
            sb.append("      OCT15 — YOUR LIFE, REMEMBERED     \n")
            sb.append("========================================\n\n")
            sb.append("Exported: ${LocalDate.now()}\n")
            sb.append("Total entries: ${memories.size}\n\n")

            memories.forEachIndexed { i, m ->
                sb.append("----------------------------------------\n")
                sb.append("#${i + 1}: ${m.title}\n")
                sb.append("Date: ${m.formattedDate()} (${m.yearsSince(LocalDate.now())} years ago)\n")
                sb.append("Category: ${m.category}\n")
                if (!m.location.isNullOrBlank()) sb.append("Location: ${m.location}\n")
                if (!m.people.isNullOrBlank()) sb.append("People: ${m.people}\n")
                if (m.tags.isNotEmpty()) sb.append("Tags: ${m.tags.joinToString(", ")}\n")
                sb.append("\nStory:\n${m.description}\n\n")
            }

            val cacheFile = File(context.cacheDir, "OCT15_Memories.txt")
            cacheFile.writeText(sb.toString(), Charsets.UTF_8)

            val shareUri = getShareUri(context, cacheFile)
            launchShareIntent(context, shareUri, "text/plain", "Share Memories Text")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Exports a Memory as an Ultra HD image up to 4K resolution (3840x2160 or custom resolution),
     * lossless JPEG (quality 100) or PNG.
     */
    fun exportToUltraHdImage(
        context: Context,
        memory: Memory,
        width: Int = 3840,
        height: Int = 2160,
        isLosslessPng: Boolean = false,
        themePrimaryHex: String = "#8B72D6",
        themeBgHex: String = "#FFF9F1"
    ) {
        try {
            val w = width.coerceIn(720, 4096)
            val h = height.coerceIn(720, 4096)

            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background pastel gradient
            val primaryColor = Color.parseColor(themePrimaryHex)
            val bgColor = Color.parseColor(themeBgHex)
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, w.toFloat(), h.toFloat(),
                    bgColor,
                    Color.parseColor("#F4EFFF"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

            // Decorative border
            val borderPaint = Paint().apply {
                color = primaryColor
                style = Paint.Style.STROKE
                strokeWidth = (w * 0.008f)
                isAntiAlias = true
            }
            val margin = w * 0.035f
            canvas.drawRoundRect(RectF(margin, margin, w - margin, h - margin), 48f, 48f, borderPaint)

            // Top Header: OCT15 branding
            val headerPaint = Paint().apply {
                color = primaryColor
                textSize = h * 0.035f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("OCT15  ★  YOUR LIFE, REMEMBERED", margin + 50f, margin + 90f, headerPaint)

            val dateBadgePaint = Paint().apply {
                color = Color.parseColor("#777181")
                textSize = h * 0.025f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }
            val dateText = "Recorded: ${memory.formattedDate()} • ${memory.yearsSince(LocalDate.now())} Years Ago"
            canvas.drawText(dateText, margin + 50f, margin + 160f, dateBadgePaint)

            // Draw Polaroid Frame on Left side (or center depending on aspect ratio)
            val isLandscape = w > h
            val polaroidLeft = margin + 50f
            val polaroidTop = margin + 220f
            val polaroidWidth = if (isLandscape) w * 0.38f else w * 0.85f
            val polaroidHeight = if (isLandscape) h * 0.65f else h * 0.45f

            // White Polaroid Card
            val polaroidPaint = Paint().apply {
                color = Color.WHITE
                setShadowLayer(30f, 0f, 15f, Color.argb(60, 0, 0, 0))
                isAntiAlias = true
            }
            canvas.drawRoundRect(
                RectF(polaroidLeft, polaroidTop, polaroidLeft + polaroidWidth, polaroidTop + polaroidHeight),
                32f, 32f, polaroidPaint
            )

            // Photo inside Polaroid
            val photoMargin = polaroidWidth * 0.06f
            val photoBottomMargin = polaroidHeight * 0.18f
            val photoRect = RectF(
                polaroidLeft + photoMargin,
                polaroidTop + photoMargin,
                polaroidLeft + polaroidWidth - photoMargin,
                polaroidTop + polaroidHeight - photoBottomMargin
            )

            val photoUriStr = memory.cutoutImageUri ?: memory.imageUri
            var photoDrawn = false
            if (!photoUriStr.isNullOrBlank()) {
                try {
                    val uri = Uri.parse(photoUriStr)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeStream(stream, null, options)
                    }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val sampleOptions = BitmapFactory.Options().apply {
                            inSampleSize = 1
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        val decodedPhoto = BitmapFactory.decodeStream(stream, null, sampleOptions)
                        if (decodedPhoto != null) {
                            val srcRect = Rect(0, 0, decodedPhoto.width, decodedPhoto.height)
                            canvas.drawBitmap(decodedPhoto, srcRect, photoRect, Paint(Paint.FILTER_BITMAP_FLAG))
                            photoDrawn = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!photoDrawn) {
                val placeholderPaint = Paint().apply {
                    color = Color.parseColor("#EAE4F5")
                }
                canvas.drawRoundRect(photoRect, 16f, 16f, placeholderPaint)
                val iconTextPaint = Paint().apply {
                    color = primaryColor
                    textSize = photoRect.height() * 0.15f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText(
                    "♥ ${memory.category} ♥",
                    photoRect.centerX(),
                    photoRect.centerY() + 20f,
                    iconTextPaint
                )
            }

            // Polaroid caption
            val captionPaint = Paint().apply {
                color = Color.parseColor("#292532")
                textSize = polaroidHeight * 0.05f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(
                memory.title.take(30),
                polaroidLeft + (polaroidWidth / 2f),
                polaroidTop + polaroidHeight - (photoBottomMargin * 0.4f),
                captionPaint
            )

            // Right side / bottom content: Title, Category Badge, Story description
            val contentLeft = if (isLandscape) polaroidLeft + polaroidWidth + 80f else polaroidLeft
            val contentTop = if (isLandscape) polaroidTop + 20f else polaroidTop + polaroidHeight + 60f
            val contentWidth = if (isLandscape) (w - contentLeft - margin - 50f) else polaroidWidth

            // Category Pill badge
            val catBadgePaint = Paint().apply {
                color = primaryColor
                isAntiAlias = true
            }
            val catText = "  ★ CATEGORY: ${memory.category.uppercase()}  "
            val catTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = h * 0.024f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val badgeWidth = catTextPaint.measureText(catText) + 40f
            val badgeHeight = h * 0.045f
            canvas.drawRoundRect(
                RectF(contentLeft, contentTop, contentLeft + badgeWidth, contentTop + badgeHeight),
                badgeHeight / 2f, badgeHeight / 2f, catBadgePaint
            )
            canvas.drawText(catText, contentLeft + 20f, contentTop + (badgeHeight * 0.68f), catTextPaint)

            // Memory Title
            var curY = contentTop + badgeHeight + 60f
            val titleTextPaint = Paint().apply {
                color = Color.parseColor("#1F1C28")
                textSize = h * 0.055f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(memory.title.ifBlank { "A Treasured Memory" }, contentLeft, curY, titleTextPaint)
            curY += 50f

            // Story Text
            val storyPaint = TextPaint().apply {
                color = Color.parseColor("#383344")
                textSize = h * 0.032f
                isAntiAlias = true
            }
            val story = memory.description.ifBlank { "Every moment deserves a place. Preserved with love." }
            val storyLayout = StaticLayout.Builder.obtain(
                story, 0, story.length, storyPaint, contentWidth.toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(10f, 1.1f)
                .build()

            canvas.save()
            canvas.translate(contentLeft, curY)
            storyLayout.draw(canvas)
            canvas.restore()

            // Footer branding
            val footerPaint = Paint().apply {
                color = Color.parseColor("#7A7387")
                textSize = h * 0.022f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(
                "Exported at ${w}x${h} Ultra HD  •  Made with love by Rahul Shah  •  OCT15",
                margin + 50f,
                h - margin - 30f,
                footerPaint
            )

            // Write to file
            val extension = if (isLosslessPng) "png" else "jpg"
            val mimeType = if (isLosslessPng) "image/png" else "image/jpeg"
            val format = if (isLosslessPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

            val cacheFile = File(context.cacheDir, "OCT15_${w}x${h}_${System.currentTimeMillis()}.$extension")
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(format, 100, out)
            }

            val shareUri = getShareUri(context, cacheFile)
            launchShareIntent(context, shareUri, mimeType, "Share ${w}x${h} Ultra HD Memory")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun escapeCsv(str: String): String {
        val replaced = str.replace("\"", "\"\"")
        return "\"$replaced\""
    }
}
