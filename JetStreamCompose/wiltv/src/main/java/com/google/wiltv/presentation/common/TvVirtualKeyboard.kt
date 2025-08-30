package com.google.wiltv.presentation.common

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace

enum class KeyboardMode {
    ALPHABET, NUMBERS
}

data class KeyboardLayout(
    val rows: List<List<String>>
) {
    companion object {
        val ALPHABET = KeyboardLayout(
            rows = listOf(
                listOf("A", "B", "C", "D", "E", "F", "G"),
                listOf("H", "I", "J", "K", "L", "M", "N"),
                listOf("O", "P", "Q", "R", "S", "T", "U"),
                listOf("V", "W", "X", "Y", "Z", "ENTER"),
                listOf("123", "SPACE", "BACKSPACE", "CLEAR")
            )
        )
        
        val NUMBERS = KeyboardLayout(
            rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("0", "ENTER"),
                listOf("ABC", "SPACE", "BACKSPACE", "CLEAR")
            )
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvVirtualKeyboard(
    onKeyPress: (String) -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
    initialFocus: Boolean = true
) {
    var keyboardMode by remember { mutableStateOf(KeyboardMode.ALPHABET) }
    val keyboard = remember(keyboardMode) { 
        if (keyboardMode == KeyboardMode.ALPHABET) KeyboardLayout.ALPHABET else KeyboardLayout.NUMBERS
    }
    var focusedRow by remember { mutableIntStateOf(0) }
    var focusedCol by remember { mutableIntStateOf(0) }
    val focusRequesters = remember(keyboardMode) {
        keyboard.rows.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, _ ->
                FocusRequester()
            }
        }
    }

    // Auto-focus the first key when keyboard loads
    LaunchedEffect(initialFocus, keyboardMode) {
        if (initialFocus && focusRequesters.isNotEmpty() && focusRequesters[0].isNotEmpty()) {
            // Add small delay to ensure UI is ready
            delay(100)
            // Update state variables to match focus
            focusedRow = 0
            focusedCol = 0
            // Request focus on first key (A)
            focusRequesters[0][0].requestFocus()
        }
    }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        keyboard.rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
            ) {
                row.forEachIndexed { colIndex, key ->
                    if (key.isNotEmpty()) {
                        KeyboardKey(
                            key = key,
                            isFocused = focusedRow == rowIndex && focusedCol == colIndex,
                            onClick = {
                                when (key) {
                                    "CLEAR" -> onClear()
                                    "BACKSPACE" -> onDelete()
                                    "SPACE" -> onSpace()
                                    "ENTER" -> onEnter()
                                    "123" -> keyboardMode = KeyboardMode.NUMBERS
                                    "ABC" -> keyboardMode = KeyboardMode.ALPHABET
                                    else -> onKeyPress(key)
                                }
                            },
                            modifier = Modifier
                                .focusRequester(focusRequesters[rowIndex][colIndex])
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_UP -> {
                                                if (rowIndex > 0) {
                                                    focusedRow = rowIndex - 1
                                                    focusedCol = colIndex
                                                    focusRequesters[focusedRow][focusedCol].requestFocus()
                                                }
                                                true
                                            }

                                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                if (rowIndex < keyboard.rows.size - 1) {
                                                    focusedRow = rowIndex + 1
                                                    focusedCol = colIndex
                                                    // Adjust column if target row is shorter
                                                    val targetRowSize =
                                                        keyboard.rows[focusedRow].count { it.isNotEmpty() }
                                                    if (focusedCol >= targetRowSize) {
                                                        focusedCol = maxOf(0, targetRowSize - 1)
                                                    }
                                                    focusRequesters[focusedRow][focusedCol].requestFocus()
                                                }
                                                true
                                            }

                                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                                if (colIndex > 0) {
                                                    focusedCol = colIndex - 1
                                                    focusRequesters[rowIndex][focusedCol].requestFocus()
                                                    true
                                                } else {
                                                    // At leftmost position, allow navigation to escape keyboard
                                                    false
                                                }
                                            }

                                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                                val currentRowSize =
                                                    keyboard.rows[rowIndex].count { it.isNotEmpty() }
                                                if (colIndex < currentRowSize - 1) {
                                                    focusedCol = colIndex + 1
                                                    focusRequesters[rowIndex][focusedCol].requestFocus()
                                                    true
                                                } else {
                                                    // Allow navigation to move out of keyboard (to search results)
                                                    false
                                                }
                                            }

                                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                                when (key) {
                                                    "CLEAR" -> onClear()
                                                    "SPACE" -> onSpace()
                                                    "ENTER" -> onEnter()
                                                    "123" -> keyboardMode = KeyboardMode.NUMBERS
                                                    "ABC" -> keyboardMode = KeyboardMode.ALPHABET
                                                    else -> onKeyPress(key)
                                                }
                                                true
                                            }

                                            else -> false
                                        }
                                    } else {
                                        false
                                    }
                                }
                        )
                    } else {
                        // Empty space to maintain grid alignment
                        Spacer(modifier = Modifier.size(45.dp))
                    }
                }
            }

            if (rowIndex < keyboard.rows.size - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun KeyboardKey(
    key: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusedPurple = Color(0xFFA855F7)
    val keyWidth = when (key) {
        "SPACE" -> 80.dp
        "CLEAR", "ENTER" -> 70.dp
        "BACKSPACE" -> 65.dp
        "123", "ABC" -> 60.dp
        else -> 45.dp
    }
    val keyHeight = 40.dp

    androidx.tv.material3.Surface(
        onClick = onClick,
        modifier = modifier.size(width = keyWidth, height = keyHeight),
        shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = focusedPurple,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = Color.White
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        glow = ClickableSurfaceDefaults.glow(
            focusedGlow = Glow(
                elevationColor = focusedPurple.copy(alpha = 0.6f),
                elevation = 8.dp
            )
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = BorderStroke(
                    width = 2.dp,
                    color = focusedPurple
                ),
                shape = MaterialTheme.shapes.small
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (key == "BACKSPACE") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = key,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SearchQueryDisplay(
    query: String,
    modifier: Modifier = Modifier
) {
    var cursorVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // Blink every 500ms
            cursorVisible = !cursorVisible
        }
    }

    androidx.tv.material3.Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.tv.material3.ClickableSurfaceDefaults.shape(MaterialTheme.shapes.medium),
        colors = androidx.tv.material3.ClickableSurfaceDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        onClick = { } // Empty click handler since this is just display
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (query.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Start typing to search...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    AnimatedVisibility(visible = cursorVisible) {
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFA855F7),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    AnimatedVisibility(visible = cursorVisible) {
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFA855F7),
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}