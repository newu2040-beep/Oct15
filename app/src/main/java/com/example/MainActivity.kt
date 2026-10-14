package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.Oct15BottomBar
import com.example.ui.components.OctTab
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CreateEditMemoryScreen
import com.example.ui.screens.CustomizationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoriesScreen
import com.example.ui.screens.MemoryDetailScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileAboutScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.Oct15Theme
import java.time.LocalDate

sealed class Screen {
    object Welcome : Screen()
    object Onboarding : Screen()
    object Main : Screen()
    data class CreateMemory(val initialDate: LocalDate? = null) : Screen()
    data class EditMemory(val memoryId: String) : Screen()
    data class MemoryDetail(val memoryId: String) : Screen()
    object Customization : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as Oct15Application
        val memoryRepo = app.memoryRepository
        val authRepo = app.authRepository
        val prefs = app.preferencesDataStore

        // Check if opened from reminder notification deep link
        val initialMemoryId = intent?.getStringExtra("navigate_to_memory_id")

        setContent {
            val currentThemeName by prefs.themeFlow.collectAsState(initial = "Lavender Dream")
            val currentBackground by prefs.backgroundFlow.collectAsState(initial = "Pastel")
            val currentCustomBgUri by prefs.customBackgroundUriFlow.collectAsState(initial = null)
            val currentFont by prefs.fontFlow.collectAsState(initial = "Default")
            val currentCompactMode by prefs.compactModeFlow.collectAsState(initial = "Auto")
            val onboardingDone by prefs.onboardingDoneFlow.collectAsState(initial = false)

            // Auto-request notifications on Android 13+
            val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { /* granted or denied handled */ }

            LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            Oct15Theme(
                themeName = currentThemeName,
                backgroundStyle = currentBackground,
                customBackgroundUri = currentCustomBgUri,
                fontStyle = currentFont,
                compactModeSetting = currentCompactMode
            ) {
                var currentScreen by remember {
                    mutableStateOf<Screen>(
                        if (!initialMemoryId.isNullOrBlank()) {
                            Screen.MemoryDetail(initialMemoryId)
                        } else if (onboardingDone || authRepo.isUserSignedIn) {
                            Screen.Main
                        } else {
                            Screen.Welcome
                        }
                    )
                }

                var currentTab by remember { mutableStateOf(OctTab.HOME) }

                // Seed initial memories if none exist
                LaunchedEffect(Unit) {
                    memoryRepo.seedInitialShowcaseMemoriesIfEmpty()
                }

                // Handle system back navigation
                BackHandler(enabled = currentScreen != Screen.Main && currentScreen != Screen.Welcome) {
                    when (currentScreen) {
                        is Screen.MemoryDetail -> currentScreen = Screen.Main
                        is Screen.CreateMemory -> currentScreen = Screen.Main
                        is Screen.EditMemory -> {
                            val id = (currentScreen as Screen.EditMemory).memoryId
                            currentScreen = Screen.MemoryDetail(id)
                        }
                        is Screen.Customization -> currentScreen = Screen.Main
                        is Screen.Onboarding -> currentScreen = Screen.Welcome
                        else -> currentScreen = Screen.Main
                    }
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Welcome -> {
                            WelcomeScreen(
                                authRepository = authRepo,
                                onAuthSuccess = { currentScreen = Screen.Main },
                                onSkipToOnboarding = { currentScreen = Screen.Onboarding }
                            )
                        }

                        is Screen.Onboarding -> {
                            OnboardingScreen(
                                memoryRepository = memoryRepo,
                                preferencesDataStore = prefs,
                                onComplete = { currentScreen = Screen.Main },
                                onBack = { currentScreen = Screen.Welcome }
                            )
                        }

                        is Screen.Main -> {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                bottomBar = {
                                    Oct15BottomBar(
                                        currentTab = currentTab,
                                        onTabSelected = { currentTab = it },
                                        onAddClick = { currentScreen = Screen.CreateMemory() }
                                    )
                                }
                            ) { innerPadding ->
                                Box(modifier = Modifier.padding(innerPadding)) {
                                    when (currentTab) {
                                        OctTab.HOME -> {
                                            HomeScreen(
                                                memoryRepository = memoryRepo,
                                                onMemoryClick = { currentScreen = Screen.MemoryDetail(it) },
                                                onAddMemoryClick = { currentScreen = Screen.CreateMemory() },
                                                onSearchClick = { currentTab = OctTab.MEMORIES },
                                                onCalendarClick = { currentTab = OctTab.CALENDAR }
                                            )
                                        }

                                        OctTab.CALENDAR -> {
                                            CalendarScreen(
                                                memoryRepository = memoryRepo,
                                                onMemoryClick = { currentScreen = Screen.MemoryDetail(it) },
                                                onAddMemoryForDate = { date ->
                                                    currentScreen = Screen.CreateMemory(initialDate = date)
                                                }
                                            )
                                        }

                                        OctTab.MEMORIES -> {
                                            MemoriesScreen(
                                                memoryRepository = memoryRepo,
                                                onMemoryClick = { currentScreen = Screen.MemoryDetail(it) }
                                            )
                                        }

                                        OctTab.MORE -> {
                                            ProfileAboutScreen(
                                                authRepository = authRepo,
                                                memoryRepository = memoryRepo,
                                                preferencesDataStore = prefs,
                                                onNavigateToCustomization = { currentScreen = Screen.Customization },
                                                onSignedOut = { currentScreen = Screen.Welcome }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        is Screen.CreateMemory -> {
                            CreateEditMemoryScreen(
                                memoryRepository = memoryRepo,
                                initialDate = screen.initialDate,
                                onBack = { currentScreen = Screen.Main },
                                onSaved = { currentScreen = Screen.Main }
                            )
                        }

                        is Screen.EditMemory -> {
                            CreateEditMemoryScreen(
                                memoryRepository = memoryRepo,
                                memoryIdToEdit = screen.memoryId,
                                onBack = { currentScreen = Screen.MemoryDetail(screen.memoryId) },
                                onSaved = { currentScreen = Screen.MemoryDetail(screen.memoryId) }
                            )
                        }

                        is Screen.MemoryDetail -> {
                            MemoryDetailScreen(
                                memoryId = screen.memoryId,
                                memoryRepository = memoryRepo,
                                onBack = { currentScreen = Screen.Main },
                                onEdit = { currentScreen = Screen.EditMemory(it) },
                                onDeleted = { currentScreen = Screen.Main }
                            )
                        }

                        is Screen.Customization -> {
                            CustomizationScreen(
                                preferencesDataStore = prefs,
                                onBack = { currentScreen = Screen.Main }
                            )
                        }
                    }
                }
            }
        }
    }
}
