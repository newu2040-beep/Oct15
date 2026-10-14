package com.example.ui.screens

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.PreferencesDataStore
import com.example.data.receiver.ReminderNotificationReceiver
import com.example.ui.components.ScrapbookBackground
import com.example.ui.theme.LocalOctDimensions
import com.example.ui.theme.LocalOctThemePalette
import com.example.ui.theme.OctThemes
import kotlinx.coroutines.launch

@Composable
fun CustomizationScreen(
    preferencesDataStore: PreferencesDataStore,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalOctThemePalette.current
    val dimensions = LocalOctDimensions.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentThemeName by preferencesDataStore.themeFlow.collectAsState(initial = "Lavender Dream")
    val currentBackground by preferencesDataStore.backgroundFlow.collectAsState(initial = "Pastel")
    val currentCustomBgUri by preferencesDataStore.customBackgroundUriFlow.collectAsState(initial = null)
    val currentFont by preferencesDataStore.fontFlow.collectAsState(initial = "Default")
    val currentCompactMode by preferencesDataStore.compactModeFlow.collectAsState(initial = "Auto")
    val notificationsEnabled by preferencesDataStore.notificationsFlow.collectAsState(initial = true)
    val callReminderEnabled by preferencesDataStore.callReminderFlow.collectAsState(initial = true)

    val backgroundStyles = listOf("Pastel", "Scrapbook", "Grid", "Stars", "Dots", "Clouds", "Custom")
    val fontStyles = listOf("Default", "Serif", "Casual", "Modern", "Monospace", "Cute Sans")
    val compactModes = listOf("Auto", "Compact", "Comfortable")

    val customWallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                preferencesDataStore.setCustomBackground(uri.toString())
                Toast.makeText(context, "Custom gallery wallpaper applied! 🖼️✨", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted! 🔔", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications disabled. Enable in Settings.", Toast.LENGTH_LONG).show()
        }
    }

    ScrapbookBackground(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = if (dimensions.isCompact) 8.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Customization & Aesthetics",
                        fontSize = if (dimensions.isCompact) 18.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text("Themes, wallpapers, fonts & display mode", fontSize = 11.sp, color = palette.textSecondary)
                }
            }

            // Scrollable Options
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = dimensions.paddingScreen)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensions.spacerMedium)
            ) {
                // Preview Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensions.bannerHeight)
                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = palette.primary.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.surface)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.theme_banner_1791972361447),
                            contentDescription = "Theme banner preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.40f))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OCT15 ♥ $currentThemeName • $currentFont • $currentBackground ♥",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Display Mode / Compact Mode for Small Phones Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = palette.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Display Mode & Small Screen Scaling", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                                Text("Adaptive layout sizing in real-time", fontSize = 11.sp, color = palette.textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            compactModes.forEach { mode ->
                                val isSelected = mode.equals(currentCompactMode, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        scope.launch {
                                            preferencesDataStore.setCompactMode(mode)
                                            Toast.makeText(context, "$mode Display Mode active ✨", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (mode == "Auto") "Auto (Adaptive)" else if (mode == "Compact") "Compact (Small Phone)" else "Comfortable",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = palette.surface,
                                        labelColor = palette.textPrimary
                                    )
                                )
                            }
                        }

                        if (dimensions.isCompact) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚡ Compact Mode is currently scaling margins and cards for maximum readability on your screen.",
                                fontSize = 11.sp,
                                color = palette.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Theme Color Swatches Section
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Palette, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pastel Theme Palettes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OctThemes.forEach { th ->
                            val isSelected = th.name.equals(currentThemeName, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch {
                                            preferencesDataStore.setTheme(th.name)
                                            Toast.makeText(context, "${th.name} applied ✨", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(4.dp)
                                    .testTag("theme_swatch_${th.name}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(th.primary)
                                        .border(
                                            if (isSelected) 3.dp else 1.dp,
                                            if (isSelected) palette.textPrimary else Color.White,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = th.name.split(" ").first(),
                                    fontSize = 11.sp,
                                    color = if (isSelected) palette.primary else palette.textSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Background Wallpaper Swatches
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Wallpaper, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Background Wallpaper Style", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        backgroundStyles.forEach { bg ->
                            val isSelected = bg.equals(currentBackground, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .width(88.dp)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) palette.primary.copy(alpha = 0.15f)
                                        else palette.cardBackground
                                    )
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) palette.primary else palette.surface,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        scope.launch {
                                            preferencesDataStore.setBackground(bg)
                                            Toast.makeText(context, "$bg background applied ✨", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bg,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) palette.primary else palette.textPrimary
                                )
                            }
                        }
                    }

                    // Custom Gallery Photo Wallpaper Picker
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.surface.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!currentCustomBgUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = currentCustomBgUri,
                                        contentDescription = "Current custom wallpaper",
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(palette.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = palette.primary)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Custom Photo Wallpaper", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                                    Text(
                                        if (!currentCustomBgUri.isNullOrBlank()) "Gallery photo active" else "Pick image from your gallery",
                                        fontSize = 11.sp,
                                        color = palette.textSecondary
                                    )
                                }
                            }

                            Row {
                                OutlinedButton(
                                    onClick = {
                                        customWallpaperPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Select Photo", fontSize = 11.sp)
                                }

                                if (!currentCustomBgUri.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                preferencesDataStore.setBackground("Pastel")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Reset", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Font Style Section
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatSize, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Font & Typography", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        fontStyles.forEach { font ->
                            val isSelected = font.equals(currentFont, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .width(84.dp)
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) palette.surface
                                        else palette.cardBackground
                                    )
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) palette.primary else palette.surface,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        scope.launch {
                                            preferencesDataStore.setFont(font)
                                            Toast.makeText(context, "$font font applied ✨", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Aa",
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) palette.primary else palette.textPrimary
                                    )
                                    Text(
                                        text = font,
                                        fontSize = 10.sp,
                                        color = if (isSelected) palette.primary else palette.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Notification Toggles & Real-time Call Reminder Testing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Notifications toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = palette.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Notifications", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                                    Text("Get reminded on your special days", fontSize = 11.sp, color = palette.textSecondary)
                                }
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { isChecked ->
                                    scope.launch { preferencesDataStore.setNotifications(isChecked) }
                                    if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                            )
                        }

                        // Call Reminder / Alarm toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Call, contentDescription = null, tint = palette.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Call Reminder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
                                    Text("Audible ringtone alarm on the date", fontSize = 11.sp, color = palette.textSecondary)
                                }
                            }
                            Switch(
                                checked = callReminderEnabled,
                                onCheckedChange = { isChecked ->
                                    scope.launch { preferencesDataStore.setCallReminder(isChecked) }
                                    if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = palette.primary)
                            )
                        }

                        // Real-Time Testing Buttons
                        Text(
                            text = "Test Real-Time Reminders:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    ReminderNotificationReceiver.sendImmediateTestNotification(context, isCallAlert = false)
                                    Toast.makeText(context, "Notification reminder sent! 🔔", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Bell", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    ReminderNotificationReceiver.sendImmediateTestNotification(context, isCallAlert = true)
                                    Toast.makeText(context, "📞 Ringtone call alert triggered!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Test Call", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}
