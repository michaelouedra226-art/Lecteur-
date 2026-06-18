package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.AudioPlayerScreen
import com.example.ui.screens.MediaListScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MediaPlayerViewModel

enum class ScreenState {
    DASHBOARD, VIDEO_PLAYER, AUDIO_PLAYER
}

class MainActivity : ComponentActivity() {

    private val viewModel: MediaPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Runtime permissions handler
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    viewModel.scanMediaFiles()
                }

                LaunchedEffect(Unit) {
                    val reqPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_AUDIO,
                            android.Manifest.permission.READ_MEDIA_VIDEO,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                    } else {
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    launcher.launch(reqPermissions)
                }

                var currentScreen by remember { mutableStateOf(ScreenState.DASHBOARD) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(350))
                        },
                        label = "screen_trans"
                    ) { screen ->
                        when (screen) {
                            ScreenState.DASHBOARD -> {
                                MediaListScreen(
                                    viewModel = viewModel,
                                    onOpenVideoPlayer = { item ->
                                        viewModel.playMedia(item)
                                        currentScreen = ScreenState.VIDEO_PLAYER
                                    },
                                    onOpenAudioPlayer = {
                                        currentScreen = ScreenState.AUDIO_PLAYER
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                )
                            }
                            ScreenState.VIDEO_PLAYER -> {
                                VideoPlayerScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        viewModel.pause()
                                        currentScreen = ScreenState.DASHBOARD
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            ScreenState.AUDIO_PLAYER -> {
                                AudioPlayerScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        currentScreen = ScreenState.DASHBOARD
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
