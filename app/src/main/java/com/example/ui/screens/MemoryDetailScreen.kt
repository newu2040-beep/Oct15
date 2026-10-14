package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Memory
import com.example.data.repository.MemoryRepository
import com.example.data.util.ExportHelper
import com.example.ui.components.ScrapbookBackground
import com.example.ui.theme.LocalOctDimensions
import com.example.ui.theme.LocalOctThemePalette
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MemoryDetailScreen(
    memoryId: String,
    memoryRepository: MemoryRepository,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalOctThemePalette.current
    val dimensions = LocalOctDimensions.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var memory by remember { mutableStateOf<Memory?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showUltraHdDialog by remember { mutableStateOf(false) }

    LaunchedEffect(memoryId) {
        memory = memoryRepository.getMemoryById(memoryId)
    }

    if (memory == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(palette.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading memory...", color = palette.textSecondary)
        }
        return
    }

    val item = memory!!
    val years = item.yearsSince(LocalDate.now())
    val nextAnniversaryDate = item.nextAnniversary(LocalDate.now())

    ScrapbookBackground(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (dimensions.isCompact) 8.dp else 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Export / Share Studio
                    IconButton(
                        onClick = { showExportSheet = true },
                        modifier = Modifier.testTag("memory_share_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = palette.primary)
                    }

                    // Edit
                    IconButton(onClick = { onEdit(item.id) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = palette.primary)
                    }

                    // Delete
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }

            // Memory Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensions.paddingScreen)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacerMedium)
            ) {
                // Polaroid Presentation Box
                val photo = item.cutoutImageUri ?: item.imageUri
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(if (dimensions.isCompact) 10.dp else 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (!photo.isNullOrBlank()) {
                            AsyncImage(
                                model = photo,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(dimensions.polaroidHeight)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = if (item.cutoutImageUri != null) ContentScale.Fit else ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (dimensions.isCompact) 130.dp else 180.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(palette.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✨ ${item.category} ✨",
                                    fontSize = if (dimensions.isCompact) 18.sp else 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Date & Category badge in polaroid caption area
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.formattedDate(),
                                fontSize = if (dimensions.isCompact) 12.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.surface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.primary
                                )
                            }
                        }
                    }
                }

                // Title and Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = if (dimensions.isCompact) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val updated = item.copy(isFavorite = !item.isFavorite)
                            scope.launch {
                                memoryRepository.saveMemory(updated)
                                memory = updated
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFEF4444) else palette.textSecondary
                        )
                    }
                }

                // Anniversary Pill / Years Counter
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(palette.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("★", color = Color.White, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$years Years Ago",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = palette.textPrimary
                            )
                            if (nextAnniversaryDate != null) {
                                Text(
                                    text = "Next anniversary on ${nextAnniversaryDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${nextAnniversaryDate.dayOfMonth}",
                                    fontSize = 12.sp,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }
                }

                // Description Story Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Story & Thoughts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.description.ifBlank { "No detailed notes written for this moment." },
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = palette.textPrimary
                        )
                    }
                }

                // Metadata Details: Location & Companions
                if (!item.location.isNullOrBlank() || !item.people.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!item.location.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.location, fontSize = 13.sp, color = palette.textPrimary)
                                }
                            }
                            if (!item.people.isNullOrBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.People, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.people, fontSize = 13.sp, color = palette.textPrimary)
                                }
                            }
                        }
                    }
                }

                // Reminder Status
                if (item.reminderEnabled) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Anniversary reminder set at ${item.reminderTime}",
                                fontSize = 12.sp,
                                color = palette.textSecondary
                            )
                        }
                    }
                }

                // Tags
                if (item.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(palette.cardBackground)
                                    .border(1.dp, palette.surface, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("#$tag", fontSize = 12.sp, color = palette.primary)
                            }
                        }
                    }
                }

                // Quick Export & Share Studio Button
                OutlinedButton(
                    onClick = { showExportSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export & Share in Multi-Formats", color = palette.primary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Export Options Modal Dialog
    if (showExportSheet) {
        Dialog(onDismissRequest = { showExportSheet = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = palette.cardBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Share & Export Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Choose your desired export format for \"${item.title}\":",
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // PDF Invoice / Keepsake
                    ExportFormatRow(
                        icon = Icons.Default.PictureAsPdf,
                        title = "PDF Memory Invoice & Certificate",
                        subtitle = "Official certificate layout for printing & archive",
                        palette = palette,
                        onClick = {
                            showExportSheet = false
                            ExportHelper.exportToPdfInvoice(context, item)
                            Toast.makeText(context, "Generating PDF invoice...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 4K Ultra HD Poster / JPEG / Lossless PNG
                    ExportFormatRow(
                        icon = Icons.Default.Image,
                        title = "Ultra HD Image Poster (Lossless / up to 4K)",
                        subtitle = "Export at 4K, 2K, 1080p, or custom resolution",
                        palette = palette,
                        onClick = {
                            showExportSheet = false
                            showUltraHdDialog = true
                        }
                    )

                    // CSV
                    ExportFormatRow(
                        icon = Icons.Default.TableChart,
                        title = "CSV Spreadsheet (.csv)",
                        subtitle = "Comma-separated structured data for Excel/Sheets",
                        palette = palette,
                        onClick = {
                            showExportSheet = false
                            ExportHelper.exportToCsv(context, listOf(item))
                            Toast.makeText(context, "Exporting CSV...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Markdown
                    ExportFormatRow(
                        icon = Icons.Default.Description,
                        title = "Markdown Journal Entry (.md)",
                        subtitle = "Rich Markdown for Obsidian, Notion & GitHub",
                        palette = palette,
                        onClick = {
                            showExportSheet = false
                            ExportHelper.exportToMarkdown(context, listOf(item), title = item.title)
                            Toast.makeText(context, "Exporting Markdown...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // TXT
                    ExportFormatRow(
                        icon = Icons.Default.TextSnippet,
                        title = "Plain Text Record (.txt)",
                        subtitle = "Clean text archive of your memory",
                        palette = palette,
                        onClick = {
                            showExportSheet = false
                            ExportHelper.exportToTxt(context, listOf(item))
                            Toast.makeText(context, "Exporting TXT...", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Quick Plain Text Share
                    ExportFormatRow(
                        icon = Icons.Default.Share,
                        title = "Quick Text Share",
                        subtitle = "Share memory snippet to WhatsApp, Messages, etc.",
                        palette = palette,
                        onClick = {
                            showExportSheet = false
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, item.title)
                                putExtra(Intent.EXTRA_TEXT, "OCT15 Memory: \"${item.title}\" (${item.formattedDate()})\n${item.description}\n\nPreserved with OCT15 — Your life, remembered.")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Memory"))
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { showExportSheet = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close", color = palette.primary)
                    }
                }
            }
        }
    }

    // 4K Ultra HD & Custom Resolution Dialog
    if (showUltraHdDialog) {
        var selectedRes by remember { mutableStateOf("4K Ultra HD (3840x2160)") }
        var isLosslessPng by remember { mutableStateOf(false) }
        var customWidth by remember { mutableStateOf("1920") }
        var customHeight by remember { mutableStateOf("1080") }

        Dialog(onDismissRequest = { showUltraHdDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = palette.cardBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Export Ultra HD Image Poster",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = "Select preset or enter custom resolution up to 4K Ultra HD:",
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )

                    val resOptions = listOf(
                        "4K Ultra HD (3840x2160)",
                        "2K QHD (2560x1440)",
                        "Full HD (1920x1080)",
                        "Square HD (2048x2048)",
                        "Custom Resolution"
                    )

                    resOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedRes = opt }
                                .background(if (selectedRes == opt) palette.surface else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = opt,
                                fontSize = 13.sp,
                                fontWeight = if (selectedRes == opt) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedRes == opt) palette.primary else palette.textPrimary
                            )
                        }
                    }

                    if (selectedRes == "Custom Resolution") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customWidth,
                                onValueChange = { customWidth = it.filter { c -> c.isDigit() } },
                                label = { Text("Width (px)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = customHeight,
                                onValueChange = { customHeight = it.filter { c -> c.isDigit() } },
                                label = { Text("Height (px)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Format Toggle: Lossless PNG vs 100% Quality JPEG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isLosslessPng,
                            onClick = { isLosslessPng = false },
                            label = { Text("JPEG (100% Quality)") },
                            shape = RoundedCornerShape(12.dp)
                        )
                        FilterChip(
                            selected = isLosslessPng,
                            onClick = { isLosslessPng = true },
                            label = { Text("Lossless PNG") },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showUltraHdDialog = false }) {
                            Text("Cancel", color = palette.textSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val (w, h) = when (selectedRes) {
                                    "4K Ultra HD (3840x2160)" -> 3840 to 2160
                                    "2K QHD (2560x1440)" -> 2560 to 1440
                                    "Full HD (1920x1080)" -> 1920 to 1080
                                    "Square HD (2048x2048)" -> 2048 to 2048
                                    else -> {
                                        val cw = customWidth.toIntOrNull() ?: 1920
                                        val ch = customHeight.toIntOrNull() ?: 1080
                                        cw to ch
                                    }
                                }

                                showUltraHdDialog = false
                                Toast.makeText(context, "Rendering ${w}x${h} Ultra HD image...", Toast.LENGTH_SHORT).show()
                                ExportHelper.exportToUltraHdImage(
                                    context = context,
                                    memory = item,
                                    width = w,
                                    height = h,
                                    isLosslessPng = isLosslessPng
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                        ) {
                            Text("Export Poster")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Memory?") },
            text = { Text("Are you sure you want to delete \"${item.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            memoryRepository.deleteMemory(item.id)
                            showDeleteDialog = false
                            Toast.makeText(context, "Memory deleted", Toast.LENGTH_SHORT).show()
                            onDeleted()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExportFormatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    palette: com.example.ui.theme.OctThemePalette,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(palette.surface.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(palette.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = palette.textSecondary)
        }
    }
}
