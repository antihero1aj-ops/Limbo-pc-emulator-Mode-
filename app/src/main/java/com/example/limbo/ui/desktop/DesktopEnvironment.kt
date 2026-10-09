package com.example.limbo.ui.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.limbo.model.MachineConfig
import com.example.limbo.simulator.VmTelemetry
import com.example.limbo.ui.components.Virgl3dCubeCanvas
import com.example.ui.theme.AcceleratorPurple
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.GpuCyan
import com.example.ui.theme.HardwareGreen
import com.example.ui.theme.LimboSurfaceDark
import com.example.ui.theme.LimboSurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class WindowType {
    GPU_3D_BENCHMARK,
    FILE_EXPLORER,
    TASK_MANAGER,
    TERMINAL,
    NOTEPAD,
    GPU_CONTROL_PANEL
}

data class OpenWindow(
    val type: WindowType,
    val title: String,
    val icon: ImageVector,
    var isMinimized: Boolean = false,
    var isMaximized: Boolean = false,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
)

/**
 * Rich, authentic and interactive PC Desktop Environment.
 * Displays OS-specific wallpapers, desktop icons, Start Menu, Taskbar,
 * and openable/movable application windows.
 */
@Composable
fun FullPcDesktopEnvironment(
    config: MachineConfig?,
    cubeAngle: Float,
    isVirglActive: Boolean,
    telemetry: VmTelemetry,
    onCubeTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWindowsXp = config?.name?.contains("XP", ignoreCase = true) == true
    val isUbuntu = config?.name?.contains("Ubuntu", ignoreCase = true) == true || config?.name?.contains("Linux", ignoreCase = true) == true
    val isRetro98 = config?.name?.contains("98", ignoreCase = true) == true

    // Open windows list
    val openWindows = remember {
        mutableStateListOf(
            OpenWindow(
                type = WindowType.GPU_3D_BENCHMARK,
                title = "VirGL 3D GPU Benchmark",
                icon = Icons.Default.VideogameAsset,
                isMinimized = false,
                offsetX = 16f,
                offsetY = 16f
            )
        )
    }

    var activeWindowType by remember { mutableStateOf<WindowType?>(WindowType.GPU_3D_BENCHMARK) }
    var isStartMenuOpen by remember { mutableStateOf(false) }
    var currentTimeString by remember { mutableStateOf("") }
    var notepadText by remember { mutableStateOf("Limbo PC 6.1.0\nHardware Acceleration: ACTIVE\nVirGL 3D Pipeline: OK\nHost OpenGL ES 3.2: 60 FPS") }

    // Live clock update
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            currentTimeString = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Helper to open or bring window to front
    fun openOrFocusWindow(type: WindowType, title: String, icon: ImageVector) {
        val existing = openWindows.find { it.type == type }
        if (existing != null) {
            existing.isMinimized = false
            activeWindowType = type
        } else {
            val newWin = OpenWindow(
                type = type,
                title = title,
                icon = icon,
                offsetX = (openWindows.size * 20f) % 60f,
                offsetY = (openWindows.size * 20f) % 60f
            )
            openWindows.add(newWin)
            activeWindowType = type
        }
        isStartMenuOpen = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Clicking desktop closes start menu
                        isStartMenuOpen = false
                    }
                )
            }
    ) {
        // 1. Desktop Wallpaper
        DesktopWallpaper(isWindowsXp = isWindowsXp, isUbuntu = isUbuntu, isRetro98 = isRetro98)

        // 2. Desktop Icons Grid (Left column)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp)
                .width(80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopIconItem(
                title = if (isWindowsXp) "My Computer" else if (isUbuntu) "Files" else "This PC",
                icon = Icons.Default.Computer,
                iconColor = GpuCyan,
                onOpen = {
                    openOrFocusWindow(WindowType.FILE_EXPLORER, "File Explorer", Icons.Default.Folder)
                }
            )

            DesktopIconItem(
                title = "VirGL 3D Demo",
                icon = Icons.Default.VideogameAsset,
                iconColor = HardwareGreen,
                badge = "3D GPU",
                onOpen = {
                    openOrFocusWindow(WindowType.GPU_3D_BENCHMARK, "VirGL 3D GPU Benchmark", Icons.Default.VideogameAsset)
                }
            )

            DesktopIconItem(
                title = "Task Manager",
                icon = Icons.Default.Speed,
                iconColor = WarningAmber,
                badge = "GPU+CPU",
                onOpen = {
                    openOrFocusWindow(WindowType.TASK_MANAGER, "Task Manager - Performance", Icons.Default.Speed)
                }
            )

            DesktopIconItem(
                title = if (isUbuntu) "Terminal" else "Command Prompt",
                icon = Icons.Default.Terminal,
                iconColor = TextPrimaryDark,
                onOpen = {
                    openOrFocusWindow(WindowType.TERMINAL, "Command Prompt (CMD)", Icons.Default.Terminal)
                }
            )

            DesktopIconItem(
                title = "GPU Settings",
                icon = Icons.Default.Settings,
                iconColor = AcceleratorPurple,
                onOpen = {
                    openOrFocusWindow(WindowType.GPU_CONTROL_PANEL, "VirGL GPU Control Center", Icons.Default.Settings)
                }
            )

            DesktopIconItem(
                title = "Notepad",
                icon = Icons.Default.Description,
                iconColor = Color.White,
                onOpen = {
                    openOrFocusWindow(WindowType.NOTEPAD, "Notepad", Icons.Default.Description)
                }
            )

            DesktopIconItem(
                title = "Recycle Bin",
                icon = Icons.Default.Delete,
                iconColor = Color(0xFFB0BEC5),
                onOpen = {
                    openOrFocusWindow(WindowType.FILE_EXPLORER, "Recycle Bin", Icons.Default.Delete)
                }
            )
        }

        // 3. Open Windows Layer
        openWindows.forEach { win ->
            if (!win.isMinimized) {
                DraggableDesktopWindow(
                    window = win,
                    isActive = activeWindowType == win.type,
                    onFocus = { activeWindowType = win.type },
                    onClose = {
                        openWindows.remove(win)
                        if (activeWindowType == win.type) {
                            activeWindowType = openWindows.lastOrNull()?.type
                        }
                    },
                    onMinimize = {
                        win.isMinimized = true
                        if (activeWindowType == win.type) {
                            activeWindowType = openWindows.firstOrNull { !it.isMinimized }?.type
                        }
                    },
                    onToggleMaximize = {
                        win.isMaximized = !win.isMaximized
                    }
                ) {
                    when (win.type) {
                        WindowType.GPU_3D_BENCHMARK -> {
                            GpuBenchmarkWindowContent(
                                cubeAngle = cubeAngle,
                                isVirglActive = isVirglActive,
                                telemetry = telemetry,
                                onCubeTap = onCubeTap
                            )
                        }
                        WindowType.FILE_EXPLORER -> {
                            FileExplorerWindowContent(config = config)
                        }
                        WindowType.TASK_MANAGER -> {
                            TaskManagerWindowContent(telemetry = telemetry, config = config, isVirglActive = isVirglActive)
                        }
                        WindowType.TERMINAL -> {
                            TerminalWindowContent(isVirglActive = isVirglActive)
                        }
                        WindowType.NOTEPAD -> {
                            NotepadWindowContent(
                                text = notepadText,
                                onTextChange = { notepadText = it }
                            )
                        }
                        WindowType.GPU_CONTROL_PANEL -> {
                            GpuControlPanelContent(config = config, isVirglActive = isVirglActive)
                        }
                    }
                }
            }
        }

        // 4. Start Menu Pop-up
        AnimatedVisibility(
            visible = isStartMenuOpen,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(if (isWindowsXp) Alignment.BottomStart else Alignment.BottomCenter)
                .padding(bottom = 44.dp, start = if (isWindowsXp) 8.dp else 0.dp)
        ) {
            StartMenuPopup(
                isWindowsXp = isWindowsXp,
                isUbuntu = isUbuntu,
                onOpenApp = { type, title, icon ->
                    openOrFocusWindow(type, title, icon)
                },
                onShutdown = { isStartMenuOpen = false }
            )
        }

        // 5. Desktop Taskbar (Bottom)
        DesktopTaskbar(
            isWindowsXp = isWindowsXp,
            isUbuntu = isUbuntu,
            isStartMenuOpen = isStartMenuOpen,
            openWindows = openWindows,
            activeWindow = activeWindowType,
            currentTime = currentTimeString,
            isVirglActive = isVirglActive,
            onToggleStartMenu = { isStartMenuOpen = !isStartMenuOpen },
            onSelectWindow = { win ->
                if (win.isMinimized) {
                    win.isMinimized = false
                    activeWindowType = win.type
                } else if (activeWindowType == win.type) {
                    win.isMinimized = true
                    activeWindowType = openWindows.firstOrNull { !it.isMinimized }?.type
                } else {
                    activeWindowType = win.type
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==================== DESKTOP ICONS ====================

@Composable
private fun DesktopIconItem(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    badge: String? = null,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onOpen() }
            .padding(4.dp)
            .testTag("desktop_icon_$title"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(GpuCyan)
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.Black,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 11.sp
        )
    }
}

// ==================== DRAGGABLE DESKTOP WINDOW ====================

@Composable
private fun DraggableDesktopWindow(
    window: OpenWindow,
    isActive: Boolean,
    onFocus: () -> Unit,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(window.offsetX) }
    var offsetY by remember { mutableFloatStateOf(window.offsetY) }

    val windowModifier = if (window.isMaximized) {
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .padding(bottom = 36.dp)
    } else {
        Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .fillMaxWidth(0.85f)
            .height(290.dp)
    }

    Card(
        modifier = windowModifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onFocus() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF101726).copy(alpha = 0.96f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) GpuCyan.copy(alpha = 0.8f) else Color(0xFF28364F)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 12.dp else 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Window Title Bar (Draggable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isActive) Color(0xFF16233B) else Color(0xFF0D1524))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (!window.isMaximized) {
                                offsetX = (offsetX + dragAmount.x).coerceIn(-100f, 300f)
                                offsetY = (offsetY + dragAmount.y).coerceIn(-50f, 200f)
                                window.offsetX = offsetX
                                window.offsetY = offsetY
                            }
                            onFocus()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = window.icon,
                        contentDescription = null,
                        tint = if (isActive) GpuCyan else TextSecondaryDark,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = window.title,
                        color = if (isActive) TextPrimaryDark else TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                // Window Control Buttons (Minimize, Maximize, Close)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Minimize
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF37474F))
                            .clickable { onMinimize() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Minimize, contentDescription = "Minimize", tint = Color.LightGray, modifier = Modifier.size(11.dp))
                    }
                    // Maximize
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF37474F))
                            .clickable { onToggleMaximize() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (window.isMaximized) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Maximize",
                            tint = Color.LightGray,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    // Close
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(DangerCrimson.copy(alpha = 0.8f))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(11.dp))
                    }
                }
            }

            // Window Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF090E17))
            ) {
                content()
            }
        }
    }
}

// ==================== WINDOW CONTENTS ====================

@Composable
private fun GpuBenchmarkWindowContent(
    cubeAngle: Float,
    isVirglActive: Boolean,
    telemetry: VmTelemetry,
    onCubeTap: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { onCubeTap() },
            contentAlignment = Alignment.Center
        ) {
            Virgl3dCubeCanvas(
                angleDegrees = cubeAngle,
                isHardwareAccelerated = isVirglActive,
                modifier = Modifier.fillMaxSize()
            )

            // Real-time overlay stats
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "VirGL 3D Hardware Engine: ${if (isVirglActive) "Host OpenGL ES 3.2" else "Software"}",
                    color = GpuCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "FPS: ${String.format("%.1f", telemetry.fps)} | Draws: ${telemetry.virglDrawCalls}/s",
                    color = HardwareGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "VRAM: ${telemetry.vramUsedMb} MB | Load: ${telemetry.gpuLoadPercent}%",
                    color = TextSecondaryDark,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Bottom stats banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131D2E))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Renderer: VirGL 3D (Native GLES 3.2)", color = GpuCyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Tap to spin 3D mesh", color = TextSecondaryDark, fontSize = 9.sp)
        }
    }
}

@Composable
private fun FileExplorerWindowContent(config: MachineConfig?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Path bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF141F33))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = GpuCyan, modifier = Modifier.size(14.dp))
            Text("This PC > Devices and Drives", color = TextPrimaryDark, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }

        // Drives list
        Text("Drives & Volumes (3)", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Local Disk C:
            DriveCardItem(
                label = "Local Disk (C:)",
                spec = "VirtIO-Blk • 38.2 GB free of 64.0 GB",
                progress = 0.40f,
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )

            // CD-ROM D:
            DriveCardItem(
                label = "CD Drive (D:)",
                spec = if (config?.cdromIso != null) "ISO Image Loaded" else "Empty Disc Tray",
                progress = if (config?.cdromIso != null) 0.85f else 0.0f,
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )
        }

        // System folders
        Text("Folders", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FolderItem("Desktop", Modifier.weight(1f))
            FolderItem("Downloads", Modifier.weight(1f))
            FolderItem("Documents", Modifier.weight(1f))
        }
    }
}

@Composable
private fun DriveCardItem(
    label: String,
    spec: String,
    progress: Float,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141E30)),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, contentDescription = null, tint = GpuCyan, modifier = Modifier.size(16.dp))
                Text(label, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = GpuCyan,
                trackColor = Color(0xFF263238)
            )
            Text(spec, color = TextSecondaryDark, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun FolderItem(name: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF141E30))
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
        Text(name, color = TextPrimaryDark, fontSize = 10.sp)
    }
}

@Composable
private fun TaskManagerWindowContent(
    telemetry: VmTelemetry,
    config: MachineConfig?,
    isVirglActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("PERFORMANCE MONITOR", color = GpuCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // CPU Metric
            MetricBox(
                title = "CPU (SMP ${config?.cpuCores ?: 4} Cores)",
                value = "${telemetry.cpuCoreLoads.firstOrNull() ?: 35}%",
                sub = "${telemetry.cpuCoreLoads.size} Cores Active",
                color = AcceleratorPurple,
                modifier = Modifier.weight(1f)
            )

            // GPU Metric
            MetricBox(
                title = if (isVirglActive) "GPU (VirGL 3D GLES)" else "GPU (Emulated)",
                value = "${telemetry.gpuLoadPercent}%",
                sub = "${telemetry.virglDrawCalls} Draw Calls/s",
                color = GpuCyan,
                modifier = Modifier.weight(1f)
            )

            // Memory Metric
            MetricBox(
                title = "Memory (RAM)",
                value = "${telemetry.ramUsedMb} MB",
                sub = "of ${config?.ramMb ?: 2048} MB",
                color = HardwareGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Live CPU Core Bars
        Text("Per-Core MTTCG Load Distribution:", color = TextSecondaryDark, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            telemetry.cpuCoreLoads.forEachIndexed { idx, load ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { load / 100f },
                        modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(3.dp)),
                        color = AcceleratorPurple,
                        trackColor = Color(0xFF1E2638)
                    )
                    Text("C$idx: $load%", color = TextSecondaryDark, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121B2B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text(title, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text(value, color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(sub, color = TextSecondaryDark, fontSize = 8.sp, maxLines = 1)
        }
    }
}

@Composable
private fun TerminalWindowContent(isVirglActive: Boolean) {
    var commandInput by remember { mutableStateOf("") }
    val history = remember {
        mutableStateListOf(
            "Microsoft Windows [Version 10.0.19045.3803]",
            "(c) Microsoft Corporation. All rights reserved.",
            "",
            "C:\\Users\\User> glxinfo | grep -i opengl",
            "OpenGL vendor string: Red Hat, Inc.",
            "OpenGL renderer string: virgl (Host OpenGL ES 3.2 Direct Pipe)",
            "OpenGL version string: 4.3 (Core Profile) Mesa 23.2.1",
            "",
            "C:\\Users\\User> type 'help' for guest diagnostics"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060A))
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(history) { line ->
                Text(line, color = Color(0xFF81D4FA), fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 13.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("C:\\> ", color = GpuCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier.weight(1f).height(42.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GpuCyan,
                    unfocusedBorderColor = Color(0xFF21324E)
                )
            )
            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        history.add("C:\\> $commandInput")
                        val cmd = commandInput.trim().lowercase()
                        when {
                            cmd == "help" -> history.add("Commands: dxdiag, glxinfo, virgl, dir, cls, ver")
                            cmd == "dxdiag" -> history.add("DirectX 12.0: VirtIO 3D Hardware Accelerated (512 MB VRAM)")
                            cmd == "virgl" -> history.add("VirGL 3D Pipeline: OK (Host GLES 3.2 @ 60 FPS)")
                            cmd == "cls" -> history.clear()
                            else -> history.add("Command executed successfully.")
                        }
                        commandInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = GpuCyan, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun NotepadWindowContent(
    text: String,
    onTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier.fillMaxSize().padding(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimaryDark,
            unfocusedTextColor = TextPrimaryDark,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    )
}

@Composable
private fun GpuControlPanelContent(config: MachineConfig?, isVirglActive: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("VIRGL 3D GRAPHICS CONTROL CENTER", color = GpuCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Host GPU API: OpenGL ES 3.2 (Direct HW Acceleration)", color = TextPrimaryDark, fontSize = 10.sp)
                Text("VirGL Driver Version: v24.1.0-limbo", color = TextSecondaryDark, fontSize = 10.sp)
                Text("Assigned Video Memory (VRAM): ${config?.hardwareAccel?.vramMb ?: 512} MB", color = GpuCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Texture Filtering: Bilinear Hardware Scaler", color = TextSecondaryDark, fontSize = 10.sp)
                Text("Shader Compilation Pipeline: Direct Host Shader Cache", color = HardwareGreen, fontSize = 10.sp)
            }
        }
    }
}

// ==================== START MENU & TASKBAR ====================

@Composable
private fun StartMenuPopup(
    isWindowsXp: Boolean,
    isUbuntu: Boolean,
    onOpenApp: (WindowType, String, ImageVector) -> Unit,
    onShutdown: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(310.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWindowsXp) Color(0xFF0C3B78) else Color(0xFF111928)
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GpuCyan.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Header User Profile
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GpuCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PC", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Column {
                        Text("Administrator", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Limbo Workstation", color = TextSecondaryDark, fontSize = 9.sp)
                    }
                }

                Text("Pinned Applications", color = GpuCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                StartMenuItem("File Explorer", Icons.Default.Folder) {
                    onOpenApp(WindowType.FILE_EXPLORER, "File Explorer", Icons.Default.Folder)
                }
                StartMenuItem("VirGL 3D Benchmark", Icons.Default.VideogameAsset) {
                    onOpenApp(WindowType.GPU_3D_BENCHMARK, "VirGL 3D GPU Benchmark", Icons.Default.VideogameAsset)
                }
                StartMenuItem("Task Manager", Icons.Default.Speed) {
                    onOpenApp(WindowType.TASK_MANAGER, "Task Manager", Icons.Default.Speed)
                }
                StartMenuItem("Command Prompt", Icons.Default.Terminal) {
                    onOpenApp(WindowType.TERMINAL, "Command Prompt (CMD)", Icons.Default.Terminal)
                }
                StartMenuItem("GPU Settings", Icons.Default.Settings) {
                    onOpenApp(WindowType.GPU_CONTROL_PANEL, "VirGL GPU Control Center", Icons.Default.Settings)
                }
                StartMenuItem("Notepad", Icons.Default.Description) {
                    onOpenApp(WindowType.NOTEPAD, "Notepad", Icons.Default.Description)
                }
            }

            // Power button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E2838))
                    .clickable { onShutdown() }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = DangerCrimson, modifier = Modifier.size(16.dp))
                Text("Shutdown System", color = TextPrimaryDark, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun StartMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = GpuCyan, modifier = Modifier.size(16.dp))
        Text(title, color = TextPrimaryDark, fontSize = 11.sp)
    }
}

@Composable
private fun DesktopTaskbar(
    isWindowsXp: Boolean,
    isUbuntu: Boolean,
    isStartMenuOpen: Boolean,
    openWindows: List<OpenWindow>,
    activeWindow: WindowType?,
    currentTime: String,
    isVirglActive: Boolean,
    onToggleStartMenu: () -> Unit,
    onSelectWindow: (OpenWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    val taskbarBg = if (isWindowsXp) {
        Color(0xFF1E52A4)
    } else if (isUbuntu) {
        Color(0xFF111111)
    } else {
        Color(0xFF0B111D).copy(alpha = 0.96f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(taskbarBg)
            .border(1.dp, BorderSubtleDark)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Start Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isStartMenuOpen) GpuCyan else if (isWindowsXp) Color(0xFF388E3C) else Color(0xFF19263D))
                    .clickable { onToggleStartMenu() }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Start",
                        tint = if (isStartMenuOpen) Color.Black else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isWindowsXp) "Start" else "Start",
                        color = if (isStartMenuOpen) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Running Window Tabs in Taskbar
            openWindows.forEach { win ->
                val isFocused = activeWindow == win.type && !win.isMinimized
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isFocused) Color(0xFF223554) else Color(0xFF121B2B))
                        .border(1.dp, if (isFocused) GpuCyan else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { onSelectWindow(win) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(win.icon, contentDescription = null, tint = GpuCyan, modifier = Modifier.size(12.dp))
                        Text(
                            text = win.title.take(14),
                            color = TextPrimaryDark,
                            fontSize = 10.sp,
                            maxLines = 1,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // System Tray & Clock (Right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isVirglActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(GpuCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("3D GPU", color = GpuCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(13.dp))
            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(13.dp))
            Text(
                text = currentTime.ifBlank { "12:00" },
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== WALLPAPERS ====================

@Composable
private fun DesktopWallpaper(
    isWindowsXp: Boolean,
    isUbuntu: Boolean,
    isRetro98: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (isWindowsXp) {
            // Windows XP "Bliss" Rolling Green Hills + Blue Sky
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E88E5), // Sky Blue
                        Color(0xFF64B5F6),
                        Color(0xFFBBDEFB)
                    ),
                    startY = 0f,
                    endY = size.height * 0.55f
                )
            )
            // Green Rolling Hills
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF43A047),
                        Color(0xFF2E7D32),
                        Color(0xFF1B5E20)
                    ),
                    startY = size.height * 0.50f,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height * 0.50f)
            )
        } else if (isUbuntu) {
            // Ubuntu Aubergine & Orange Gradient
            drawRect(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF2C001E), // Ubuntu Aubergine
                        Color(0xFF77216F),
                        Color(0xFF5E2750),
                        Color(0xFFE95420)  // Ubuntu Orange
                    )
                )
            )
        } else if (isRetro98) {
            // Classic Teal
            drawRect(color = Color(0xFF008080))
        } else {
            // Windows 11 Deep Bloom Blue
            drawRect(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0xFF1E3A8A), // Deep Royal Bloom
                        Color(0xFF0F172A),
                        Color(0xFF060911)
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                    radius = size.width * 0.8f
                )
            )
        }
    }
}
