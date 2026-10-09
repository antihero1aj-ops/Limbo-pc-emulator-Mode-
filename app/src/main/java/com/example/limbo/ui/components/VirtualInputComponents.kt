package com.example.limbo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AcceleratorPurple
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.GpuCyan
import com.example.ui.theme.HardwareGreen
import com.example.ui.theme.LimboSurfaceDark
import com.example.ui.theme.LimboSurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlin.math.cos
import kotlin.math.sin

/**
 * On-screen PC Keyboard drawer with essential PC keys
 */
@Composable
fun VirtualPcKeyboard(
    onKeyPress: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(1.dp, BorderSubtleDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = LimboSurfaceDark.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Keyboard",
                        tint = GpuCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "VirtIO PC Keyboard",
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Keyboard",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Function Keys Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("ESC", "F1", "F2", "F3", "F4", "F5", "F8", "F10", "F12", "DEL").forEach { key ->
                    KeyButton(
                        label = key,
                        modifier = Modifier.weight(1f),
                        isSpecial = key in listOf("ESC", "DEL"),
                        onClick = { onKeyPress(key) }
                    )
                }
            }

            // Number Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "BS").forEach { key ->
                    KeyButton(
                        label = key,
                        modifier = Modifier.weight(if (key == "BS") 1.4f else 1f),
                        isSpecial = key == "BS",
                        onClick = { onKeyPress(key) }
                    )
                }
            }

            // QWERTY Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("TAB", "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "\\").forEach { key ->
                    KeyButton(
                        label = key,
                        modifier = Modifier.weight(if (key == "TAB") 1.3f else 1f),
                        onClick = { onKeyPress(key) }
                    )
                }
            }

            // QWERTY Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("CAPS", "A", "S", "D", "F", "G", "H", "J", "K", "L", "ENTER").forEach { key ->
                    KeyButton(
                        label = key,
                        modifier = Modifier.weight(if (key in listOf("CAPS", "ENTER")) 1.4f else 1f),
                        isSpecial = key == "ENTER",
                        onClick = { onKeyPress(key) }
                    )
                }
            }

            // QWERTY Row 3 + Modifiers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("SHIFT", "Z", "X", "C", "V", "B", "N", "M", ",", ".", "UP", "CTRL+ALT+DEL").forEach { key ->
                    KeyButton(
                        label = key,
                        modifier = Modifier.weight(if (key == "CTRL+ALT+DEL") 1.8f else if (key == "SHIFT") 1.2f else 1f),
                        isSpecial = key in listOf("SHIFT", "CTRL+ALT+DEL"),
                        onClick = { onKeyPress(key) }
                    )
                }
            }

            // Space & Arrows Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("CTRL", "WIN", "ALT", "SPACE", "LEFT", "DOWN", "RIGHT").forEach { key ->
                    KeyButton(
                        label = key,
                        modifier = Modifier.weight(if (key == "SPACE") 3.5f else 1.1f),
                        isSpecial = key in listOf("CTRL", "WIN", "ALT"),
                        onClick = { onKeyPress(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    onClick: () -> Unit
) {
    val bg = if (isSpecial) AcceleratorPurple.copy(alpha = 0.25f) else LimboSurfaceVariantDark
    val borderCol = if (isSpecial) AcceleratorPurple.copy(alpha = 0.6f) else BorderSubtleDark

    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .testTag("key_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSpecial) GpuCyan else TextPrimaryDark,
            fontSize = if (label.length > 4) 8.sp else 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

/**
 * Virtual Mouse & Trackpad bar with buttons
 */
@Composable
fun VirtualMouseTrackpad(
    onCursorMoveDelta: (Float, Float) -> Unit,
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtleDark, RoundedCornerShape(12.dp)),
        color = LimboSurfaceDark.copy(alpha = 0.90f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Trackpad sensitive area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LimboSurfaceVariantDark)
                    .border(1.dp, GpuCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Sensitivity scale
                            val scale = 0.0018f
                            onCursorMoveDelta(dragAmount.x * scale, dragAmount.y * scale)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TOUCH TRACKPAD (SWIPE TO MOVE)",
                    color = TextSecondaryDark.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Left Click Button
            Button(
                onClick = { onButtonClick("Left Click") },
                modifier = Modifier
                    .height(54.dp)
                    .width(72.dp)
                    .testTag("mouse_left_click"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GpuCyan.copy(alpha = 0.25f),
                    contentColor = GpuCyan
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "L-CLICK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Right Click Button
            Button(
                onClick = { onButtonClick("Right Click") },
                modifier = Modifier
                    .height(54.dp)
                    .width(72.dp)
                    .testTag("mouse_right_click"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcceleratorPurple.copy(alpha = 0.25f),
                    contentColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "R-CLICK",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * On-screen Gamepad overlay for PC Games & Emulation
 */
@Composable
fun VirtualGamepadOverlay(
    onButtonPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // D-Pad on Left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(130.dp)
                .clip(CircleShape)
                .background(LimboSurfaceDark.copy(alpha = 0.75f))
                .border(1.dp, BorderSubtleDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Up
            IconButton(
                onClick = { onButtonPress("DPAD_UP") },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(42.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = GpuCyan)
            }
            // Down
            IconButton(
                onClick = { onButtonPress("DPAD_DOWN") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(42.dp)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = GpuCyan)
            }
            // Left
            IconButton(
                onClick = { onButtonPress("DPAD_LEFT") },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(42.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = GpuCyan)
            }
            // Right
            IconButton(
                onClick = { onButtonPress("DPAD_RIGHT") },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(42.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = GpuCyan)
            }
        }

        // Action Buttons on Right (A, B, X, Y)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(130.dp)
                .clip(CircleShape)
                .background(LimboSurfaceDark.copy(alpha = 0.75f))
                .border(1.dp, BorderSubtleDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Y
            GamepadActionButton("Y", Modifier.align(Alignment.TopCenter), Color(0xFFFFD54F)) {
                onButtonPress("BUTTON_Y")
            }
            // A
            GamepadActionButton("A", Modifier.align(Alignment.BottomCenter), HardwareGreen) {
                onButtonPress("BUTTON_A")
            }
            // X
            GamepadActionButton("X", Modifier.align(Alignment.CenterStart), GpuCyan) {
                onButtonPress("BUTTON_X")
            }
            // B
            GamepadActionButton("B", Modifier.align(Alignment.CenterEnd), Color(0xFFFF5252)) {
                onButtonPress("BUTTON_B")
            }
        }
    }
}

@Composable
private fun GamepadActionButton(
    label: String,
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.25f))
            .border(1.5.dp, color, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Real-time 3D Wireframe & Solid Cube Canvas for VirGL 3D GPU Demo
 */
@Composable
fun Virgl3dCubeCanvas(
    angleDegrees: Float,
    isHardwareAccelerated: Boolean,
    modifier: Modifier = Modifier
) {
    val angleRad = (angleDegrees * Math.PI / 180.0).toFloat()
    val cosA = cos(angleRad)
    val sinA = sin(angleRad)

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val cubeSize = minOf(size.width, size.height) * 0.28f

        // 8 vertices of a 3D cube in model coordinates (-1..1)
        val rawVertices = listOf(
            floatArrayOf(-1f, -1f, -1f),
            floatArrayOf( 1f, -1f, -1f),
            floatArrayOf( 1f,  1f, -1f),
            floatArrayOf(-1f,  1f, -1f),
            floatArrayOf(-1f, -1f,  1f),
            floatArrayOf( 1f, -1f,  1f),
            floatArrayOf( 1f,  1f,  1f),
            floatArrayOf(-1f,  1f,  1f)
        )

        // Rotate along Y and X axes
        val projected = rawVertices.map { v ->
            // Rotate Y
            val x1 = v[0] * cosA - v[2] * sinA
            val z1 = v[0] * sinA + v[2] * cosA
            val y1 = v[1]

            // Rotate X slightly
            val x2 = x1
            val y2 = y1 * cos(0.4f) - z1 * sin(0.4f)
            val z2 = y1 * sin(0.4f) + z1 * cos(0.4f)

            // Perspective projection
            val distance = 3.2f
            val fov = 1f / (distance + z2)
            val projX = centerX + x2 * cubeSize * fov * 3.2f
            val projY = centerY + y2 * cubeSize * fov * 3.2f
            Offset(projX, projY)
        }

        // 12 edges of cube
        val edges = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0, // front
            4 to 5, 5 to 6, 6 to 7, 7 to 4, // back
            0 to 4, 1 to 5, 2 to 6, 3 to 7  // connectors
        )

        // Draw glowing wireframe
        val edgeColor = if (isHardwareAccelerated) GpuCyan else Color(0xFF81D4FA)
        edges.forEach { (i, j) ->
            drawLine(
                color = edgeColor,
                start = projected[i],
                end = projected[j],
                strokeWidth = if (isHardwareAccelerated) 3.5f else 2f
            )
        }

        // Draw vertex nodes
        projected.forEach { p ->
            drawCircle(
                color = if (isHardwareAccelerated) HardwareGreen else Color.White,
                radius = 4.5f,
                center = p
            )
        }
    }
}
