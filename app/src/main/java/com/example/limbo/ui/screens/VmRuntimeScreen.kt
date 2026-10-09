package com.example.limbo.ui.screens

import com.example.limbo.ui.desktop.FullPcDesktopEnvironment
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.limbo.model.MachineConfig
import com.example.limbo.simulator.VmExecutionStatus
import com.example.limbo.simulator.VmRuntimeManager
import com.example.limbo.ui.components.AccelerationBadge
import com.example.limbo.ui.components.TelemetryHudBar
import com.example.limbo.ui.components.Virgl3dCubeCanvas
import com.example.limbo.ui.components.VirtualGamepadOverlay
import com.example.limbo.ui.components.VirtualMouseTrackpad
import com.example.limbo.ui.components.VirtualPcKeyboard
import com.example.ui.theme.AcceleratorPurple
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.GpuCyan
import com.example.ui.theme.HardwareGreen
import com.example.ui.theme.LimboBgDark
import com.example.ui.theme.LimboSurfaceDark
import com.example.ui.theme.LimboSurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmRuntimeScreen(
    runtimeManager: VmRuntimeManager,
    onExitToDashboard: () -> Unit
) {
    val status by runtimeManager.status.collectAsState()
    val config by runtimeManager.activeConfig.collectAsState()
    val telemetry by runtimeManager.telemetry.collectAsState()
    val cubeAngle by runtimeManager.cubeRotationAngle.collectAsState()
    val cursorX by runtimeManager.cursorX.collectAsState()
    val cursorY by runtimeManager.cursorY.collectAsState()
    val dosHistory by runtimeManager.dosHistory.collectAsState()
    val logs by runtimeManager.consoleLogs.collectAsState()

    var showKeyboard by remember { mutableStateOf(false) }
    var showTrackpad by remember { mutableStateOf(true) }
    var showGamepad by remember { mutableStateOf(false) }
    var showQmpSheet by remember { mutableStateOf(false) }
    var qmpInput by remember { mutableStateOf("") }
    var qmpResponse by remember { mutableStateOf("") }

    val isVirglActive = config?.gpuDevice?.supportsVirgl == true && config?.hardwareAccel?.enableVirgl3d == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main PC Screen Viewport
        Column(modifier = Modifier.fillMaxSize()) {
            // Top HUD Bar with Telemetry & Mini Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LimboSurfaceDark.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onExitToDashboard,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit to Dashboard",
                            tint = TextPrimaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = config?.name ?: "VM Runtime",
                        color = TextPrimaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AccelerationBadge(
                        text = if (isVirglActive) "VIRGL 3D GLES" else "2D GPU",
                        isActive = isVirglActive,
                        activeColor = GpuCyan
                    )
                    AccelerationBadge(
                        text = if (status == VmExecutionStatus.RUNNING) "RUNNING" else status.name,
                        isActive = status == VmExecutionStatus.RUNNING,
                        activeColor = HardwareGreen
                    )
                }
            }

            // Real-Time Telemetry Performance HUD
            TelemetryHudBar(
                telemetry = telemetry,
                isVirglActive = isVirglActive,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )

            // The Virtual Display Monitor (Aspect Ratio 16:9 or 4:3)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF030508))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val normX = offset.x / size.width
                                val normY = offset.y / size.height
                                runtimeManager.setCursorPos(normX, normY)
                                runtimeManager.sendMouseClick("Left Click")
                            }
                        )
                    }
            ) {
                when (status) {
                    VmExecutionStatus.STARTING_BIOS -> {
                        BiosPostScreen(config = config)
                    }
                    VmExecutionStatus.INITIALIZING_GPU -> {
                        GpuInitScreen(config = config)
                    }
                    VmExecutionStatus.RUNNING, VmExecutionStatus.PAUSED, VmExecutionStatus.SAVING_SNAPSHOT -> {
                        if (config?.arch?.name?.contains("X86") == true && config?.name?.contains("FreeDOS") == true) {
                            FreeDosInteractiveScreen(
                                history = dosHistory,
                                onSendCommand = { cmd -> runtimeManager.executeDosCommand(cmd) }
                            )
                        } else {
                            FullPcDesktopEnvironment(
                                config = config,
                                cubeAngle = cubeAngle,
                                isVirglActive = isVirglActive,
                                telemetry = telemetry,
                                onCubeTap = { runtimeManager.sendKeyPress("3D_INTERACT") },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Render Virtual Mouse Cursor
                        VirtualMouseCursor(x = cursorX, y = cursorY)
                    }
                    VmExecutionStatus.STOPPED -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "PC POWERED OFF\nTap Play to Boot Machine",
                                color = TextSecondaryDark,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                    VmExecutionStatus.ERROR -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "KERNEL PANIC / QEMU FAULT\nCheck QEMU CLI arguments",
                                color = DangerCrimson,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Bottom Virtual Trackpad / Mouse Drawer if enabled
            if (showTrackpad && status == VmExecutionStatus.RUNNING) {
                VirtualMouseTrackpad(
                    onCursorMoveDelta = { dx, dy -> runtimeManager.moveCursorDelta(dx, dy) },
                    onButtonClick = { btn -> runtimeManager.sendMouseClick(btn) }
                )
            }

            // Bottom On-Screen Keyboard Drawer if enabled
            if (showKeyboard) {
                VirtualPcKeyboard(
                    onKeyPress = { key ->
                        runtimeManager.sendKeyPress(key)
                        if (config?.name?.contains("FreeDOS") == true && key != "DEL") {
                            runtimeManager.executeDosCommand(key)
                        }
                    },
                    onClose = { showKeyboard = false }
                )
            }

            // Floating Bottom Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LimboSurfaceDark)
                    .border(1.dp, BorderSubtleDark)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause
                IconButton(
                    onClick = {
                        if (status == VmExecutionStatus.RUNNING) runtimeManager.pauseVm()
                        else if (status == VmExecutionStatus.PAUSED) runtimeManager.resumeVm()
                        else config?.let { runtimeManager.startVm(it) }
                    },
                    modifier = Modifier.testTag("btn_runtime_play_pause")
                ) {
                    Icon(
                        imageVector = if (status == VmExecutionStatus.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (status == VmExecutionStatus.RUNNING) WarningAmber else HardwareGreen
                    )
                }

                // Reset
                IconButton(
                    onClick = { runtimeManager.resetVm() },
                    modifier = Modifier.testTag("btn_runtime_reset")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = TextPrimaryDark)
                }

                // Power Off
                IconButton(
                    onClick = { runtimeManager.stopVm() },
                    modifier = Modifier.testTag("btn_runtime_power")
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power Off", tint = DangerCrimson)
                }

                // Snapshot
                IconButton(
                    onClick = { runtimeManager.saveSnapshot("Checkpoint") },
                    modifier = Modifier.testTag("btn_runtime_snapshot")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Save Snapshot", tint = AcceleratorPurple)
                }

                // Toggle Keyboard
                IconButton(
                    onClick = {
                        showKeyboard = !showKeyboard
                        if (showKeyboard) showTrackpad = false
                    },
                    modifier = Modifier.testTag("btn_runtime_keyboard")
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Toggle Keyboard",
                        tint = if (showKeyboard) GpuCyan else TextSecondaryDark
                    )
                }

                // Toggle Trackpad
                IconButton(
                    onClick = {
                        showTrackpad = !showTrackpad
                        if (showTrackpad) showKeyboard = false
                    },
                    modifier = Modifier.testTag("btn_runtime_trackpad")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mouse,
                        contentDescription = "Toggle Trackpad",
                        tint = if (showTrackpad) GpuCyan else TextSecondaryDark
                    )
                }

                // Toggle Gamepad
                IconButton(
                    onClick = { showGamepad = !showGamepad },
                    modifier = Modifier.testTag("btn_runtime_gamepad")
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Toggle Gamepad",
                        tint = if (showGamepad) GpuCyan else TextSecondaryDark
                    )
                }

                // QEMU Console
                IconButton(
                    onClick = { showQmpSheet = true },
                    modifier = Modifier.testTag("btn_runtime_qmp")
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = "QEMU Console", tint = GpuCyan)
                }
            }
        }

        // Virtual Gamepad Overlay
        if (showGamepad && status == VmExecutionStatus.RUNNING) {
            VirtualGamepadOverlay(
                onButtonPress = { btn -> runtimeManager.sendKeyPress(btn) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // QEMU Monitor (QMP) Bottom Sheet
        if (showQmpSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQmpSheet = false },
                containerColor = LimboSurfaceDark,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = GpuCyan)
                            Text(
                                text = "QEMU MONITOR (QMP)",
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }
                        IconButton(onClick = { showQmpSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                        }
                    }

                    // Console Log Output Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF04070D))
                            .border(1.dp, BorderSubtleDark, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(reverseLayout = true) {
                            items(logs.reversed()) { logLine ->
                                Text(
                                    text = logLine,
                                    color = if (logLine.contains("VIRGL") || logLine.contains("GPU")) GpuCyan else Color(0xFF90CAF9),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    // Command Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = qmpInput,
                            onValueChange = { qmpInput = it },
                            placeholder = { Text("e.g. virgl stats, info pci, info status") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qmp_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GpuCyan)
                        )
                        Button(
                            onClick = {
                                if (qmpInput.isNotBlank()) {
                                    qmpResponse = runtimeManager.executeQmpCommand(qmpInput)
                                    qmpInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GpuCyan, contentColor = Color(0xFF00363D)),
                            modifier = Modifier.testTag("qmp_send_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send")
                        }
                    }

                    // Quick Command Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("virgl stats", "info pci", "info status", "info block").forEach { chip ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LimboSurfaceVariantDark)
                                    .border(1.dp, BorderSubtleDark, RoundedCornerShape(4.dp))
                                    .clickable {
                                        qmpResponse = runtimeManager.executeQmpCommand(chip)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(chip, color = GpuCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== SCREEN MODES ====================

@Composable
private fun BiosPostScreen(config: MachineConfig?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("LIMBO PC BIOS v6.1.0-GPU (C) 2024-2026 Limbo Emulation Group", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        Text("Host Architecture: ${config?.arch?.displayName}", color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text("Emulated CPU: ${config?.cpuModel} (${config?.cpuCores} Cores SMP @ 3.40 GHz)", color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text("Memory Test: ${config?.ramMb} MB OK", color = HardwareGreen, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text("PCI Bus 00:02.0: VirtIO-GPU 3D [VirGL Native GLES 3.2]", color = GpuCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text("PCI Bus 00:03.0: VirtIO-Net Gigabit Adapter", color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text("Primary Storage: VirtIO-Blk Bus (C:) - Near-Native I/O", color = Color.LightGray, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Text("Boot Sequence: ${config?.bootDevice?.displayName} ...", color = WarningAmber, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
    }
}

@Composable
private fun GpuInitScreen(config: MachineConfig?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GpuCyan.copy(alpha = 0.2f))
                    .border(2.dp, GpuCyan, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = GpuCyan,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "INITIALIZING 3D HARDWARE PIPELINE",
                color = GpuCyan,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
            Text(
                text = "VirGL 3D Driver • Host OpenGL ES 3.2 • VRAM ${config?.hardwareAccel?.vramMb ?: 512} MB",
                color = TextSecondaryDark,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun FreeDosInteractiveScreen(
    history: List<String>,
    onSendCommand: (String) -> Unit
) {
    var commandInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001100))
            .padding(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(history) { line ->
                Text(
                    text = line,
                    color = Color(0xFF00FF66),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("C:\\> ", color = Color(0xFF00FF66), fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("dos_command_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF00FF66),
                    unfocusedTextColor = Color(0xFF00FF66),
                    focusedBorderColor = Color(0xFF00FF66),
                    unfocusedBorderColor = Color(0xFF005500)
                ),
                placeholder = { Text("DIR, 3DDEMO, BENCH3D", color = Color(0xFF008833), fontSize = 11.sp) }
            )
            IconButton(
                onClick = {
                    if (commandInput.isNotBlank()) {
                        onSendCommand(commandInput)
                        commandInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Run", tint = Color(0xFF00FF66))
            }
        }
    }
}

@Composable
private fun VirtualMouseCursor(x: Float, y: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val posX = x * size.width
        val posY = y * size.height

        // Draw arrow cursor path
        val path = Path().apply {
            moveTo(posX, posY)
            lineTo(posX, posY + 16.dp.toPx())
            lineTo(posX + 4.dp.toPx(), posY + 12.dp.toPx())
            lineTo(posX + 9.dp.toPx(), posY + 18.dp.toPx())
            lineTo(posX + 11.dp.toPx(), posY + 16.dp.toPx())
            lineTo(posX + 6.dp.toPx(), posY + 11.dp.toPx())
            lineTo(posX + 12.dp.toPx(), posY + 11.dp.toPx())
            close()
        }

        drawPath(path, color = GpuCyan)
        drawPath(path, color = Color.Black, style = Stroke(width = 1.5.dp.toPx()))
    }
}
