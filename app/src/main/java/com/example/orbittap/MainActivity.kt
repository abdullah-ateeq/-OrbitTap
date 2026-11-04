package com.example.orbittap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.orbittap.game.GameViewModel
import com.example.orbittap.ui.navigation.OrbitTapNavGraph
import com.example.orbittap.ui.theme.OrbitTapTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

            OrbitTapTheme(themeMode = preferences.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    OrbitTapNavGraph(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseGame()
    }
}
