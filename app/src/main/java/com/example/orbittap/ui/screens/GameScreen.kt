package com.example.orbittap.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.orbittap.game.GameMath
import com.example.orbittap.game.GameStatus
import com.example.orbittap.game.GameViewModel
import com.example.orbittap.ui.components.OrbitPlayfieldCanvas
import com.example.orbittap.ui.theme.LocalOrbitVisualColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val orbitColors = LocalOrbitVisualColors.current

    // Navigate to Result screen on game over after shake / flash duration
    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.GAME_OVER) {
            delay(550)
            onGameOver()
        }
    }

    // Screen shake calculation
    val shakeX = remember(gameState.shakeOffset) {
        if (gameState.shakeOffset > 0f) {
            (Random.nextFloat() - 0.5f) * gameState.shakeOffset
        } else 0f
    }
    val shakeY = remember(gameState.shakeOffset) {
        if (gameState.shakeOffset > 0f) {
            (Random.nextFloat() - 0.5f) * gameState.shakeOffset
        } else 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (gameState.status == GameStatus.RUNNING) {
                    viewModel.onTap()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Multiplier
                Column {
                    Text(
                        text = "SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${gameState.score}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Streak Badge
                val multiplier = GameMath.calculateStreakMultiplier(gameState.streak)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (gameState.streak >= 3) {
                            orbitColors.targetArc.copy(alpha = 0.2f)
                        } else {
                            orbitColors.elevatedCard
                        }
                    ),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = if (gameState.streak >= 3) orbitColors.targetArc else orbitColors.subtleBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STREAK ${gameState.streak}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (gameState.streak >= 3) orbitColors.targetArc else MaterialTheme.colorScheme.onSurface
                        )
                        if (multiplier > 1.0f) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${multiplier}x",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = orbitColors.perfectFlash
                            )
                        }
                    }
                }

                // Pause Button
                IconButton(
                    onClick = { viewModel.pauseGame() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(orbitColors.elevatedCard)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Central Orbit Playfield & Popups
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                OrbitPlayfieldCanvas(
                    gameState = gameState,
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Score Popups
                for (popup in gameState.scorePopups) {
                    Text(
                        text = popup.text,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = if (popup.isPerfect) orbitColors.perfectFlash else orbitColors.targetArc,
                        modifier = Modifier
                            .offset(y = popup.offsetY.dp)
                            .alpha(popup.alpha)
                    )
                }
            }

            // Bottom Prompt
            Text(
                text = if (gameState.status == GameStatus.GAME_OVER) "MISSED" else "TAP ANYWHERE TO HIT TARGET",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (gameState.status == GameStatus.GAME_OVER) {
                    orbitColors.missFlash
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Pause Dialog
        if (gameState.status == GameStatus.PAUSED) {
            Dialog(onDismissRequest = { viewModel.resumeGame() }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(1.dp, orbitColors.subtleBorder, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = orbitColors.elevatedCard)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Resume Button
                        Button(
                            onClick = { viewModel.resumeGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = orbitColors.targetArc,
                                contentColor = Color(0xFF00363D)
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESUME", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Restart Button
                        OutlinedButton(
                            onClick = { viewModel.startNewGame() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("RESTART", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Home Button
                        OutlinedButton(
                            onClick = {
                                viewModel.startNewGame()
                                onNavigateHome()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QUIT TO MENU", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
