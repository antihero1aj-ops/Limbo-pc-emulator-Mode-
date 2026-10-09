package com.example.limbo.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.limbo.model.AudioBackend
import com.example.limbo.model.AudioDevice
import com.example.limbo.model.BootDevice
import com.example.limbo.model.GlRendererMode
import com.example.limbo.model.GpuDevice
import com.example.limbo.model.HardwareAccelConfig
import com.example.limbo.model.MachineConfig
import com.example.limbo.model.NetworkDevice
import com.example.limbo.model.ScalingFilter
import com.example.limbo.model.StorageInterface
import com.example.limbo.model.VirtualDisk
import com.example.limbo.model.VmArchitecture
import com.example.limbo.simulator.QemuCommandLineBuilder
import com.example.limbo.ui.components.AccelerationBadge
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
import com.example.ui.theme.TextTertiaryDark
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VmConfigScreen(
    initialConfig: MachineConfig,
    onSaveConfig: (MachineConfig) -> Unit,
    onStartVm: (MachineConfig) -> Unit,
    onBack: () -> Unit
) {
    var config by remember { mutableStateOf(initialConfig) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val tabs = listOf("GPU & 3D", "CPU & VIRT", "STORAGE", "SOUND & NET", "QEMU CLI")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = config.name,
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Text(
                            text = "HARDWARE CONFIGURATION",
                            color = GpuCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onSaveConfig(config)
                            Toast.makeText(context, "Hardware configuration saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("btn_save_config")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = HardwareGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LimboSurfaceDark)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = LimboSurfaceDark,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveConfig(config)
                            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimboSurfaceVariantDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = TextPrimaryDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onSaveConfig(config)
                            onStartVm(config)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_run_vm_from_config"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GpuCyan,
                            contentColor = Color(0xFF00363D)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LAUNCH VM", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        },
        containerColor = LimboBgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs Bar
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = LimboSurfaceDark,
                contentColor = GpuCyan
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    )
                }
            }

            // Tab Content Body
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: GPU & 3D Acceleration
                        item {
                            GpuConfigSection(
                                config = config,
                                onUpdate = { config = it }
                            )
                        }
                    }
                    1 -> {
                        // TAB 1: CPU & Virtualization
                        item {
                            CpuConfigSection(
                                config = config,
                                onUpdate = { config = it }
                            )
                        }
                    }
                    2 -> {
                        // TAB 2: Storage Drives
                        item {
                            StorageConfigSection(
                                config = config,
                                onUpdate = { config = it }
                            )
                        }
                    }
                    3 -> {
                        // TAB 3: Sound & Network
                        item {
                            SoundAndNetConfigSection(
                                config = config,
                                onUpdate = { config = it }
                            )
                        }
                    }
                    4 -> {
                        // TAB 4: QEMU CLI Inspector
                        item {
                            QemuCliSection(
                                config = config,
                                onCopyCli = {
                                    val cmd = QemuCommandLineBuilder.buildCommandLine(config)
                                    clipboard.setText(AnnotatedString(cmd))
                                    Toast.makeText(context, "Copied QEMU command line!", Toast.LENGTH_SHORT).show()
                                },
                                onUpdateExtra = { extra ->
                                    config = config.copy(extraQemuParams = extra)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 0: GPU & 3D ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GpuConfigSection(
    config: MachineConfig,
    onUpdate: (MachineConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // VirGL 3D Master Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (config.hardwareAccel.enableVirgl3d) GpuCyan else BorderSubtleDark
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = GpuCyan)
                            Text(
                                text = "VirGL 3D Hardware Acceleration",
                                color = TextPrimaryDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Bridges guest OpenGL / Direct3D calls directly to host Android OpenGL ES 3.2 GPU pipeline.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = config.hardwareAccel.enableVirgl3d,
                        onCheckedChange = { checked ->
                            onUpdate(
                                config.copy(
                                    hardwareAccel = config.hardwareAccel.copy(enableVirgl3d = checked)
                                )
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GpuCyan,
                            checkedTrackColor = GpuCyan.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("switch_virgl_3d")
                    )
                }

                if (config.hardwareAccel.enableVirgl3d) {
                    AccelerationBadge(
                        text = "HOST GLES 3.2 3D PIPELINE: ACTIVE",
                        isActive = true,
                        activeColor = GpuCyan
                    )
                }
            }
        }

        // GPU Model Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "GRAPHICS ADAPTER (VGA/GPU)",
                    color = GpuCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                var gpuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = gpuExpanded,
                    onExpandedChange = { gpuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = config.gpuDevice.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gpuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GpuCyan,
                            unfocusedBorderColor = BorderSubtleDark
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = gpuExpanded,
                        onDismissRequest = { gpuExpanded = false },
                        modifier = Modifier.background(LimboSurfaceDark)
                    ) {
                        GpuDevice.entries.forEach { device ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(device.displayName, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                                        Text(device.description, color = TextSecondaryDark, fontSize = 10.sp)
                                    }
                                },
                                onClick = {
                                    onUpdate(config.copy(gpuDevice = device))
                                    gpuExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = config.gpuDevice.description,
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        // VRAM Video Memory Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("GPU VIDEO MEMORY (VRAM)", color = GpuCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("${config.hardwareAccel.vramMb} MB", color = TextPrimaryDark, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                }

                val vramOptions = listOf(32, 64, 128, 256, 512, 1024)
                Slider(
                    value = vramOptions.indexOf(config.hardwareAccel.vramMb).coerceAtLeast(0).toFloat(),
                    onValueChange = { index ->
                        val selected = vramOptions[index.toInt().coerceIn(0, vramOptions.size - 1)]
                        onUpdate(config.copy(hardwareAccel = config.hardwareAccel.copy(vramMb = selected)))
                    },
                    valueRange = 0f..(vramOptions.size - 1).toFloat(),
                    steps = vramOptions.size - 2,
                    colors = SliderDefaults.colors(thumbColor = GpuCyan, activeTrackColor = GpuCyan)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    vramOptions.forEach { v ->
                        Text("${v}M", fontSize = 9.sp, color = TextTertiaryDark, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Host Surface Renderer & Scaling Filter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("HOST GL SURFACE & FILTERING", color = GpuCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                // Scaling Filter options
                Text("Texture Scaler Filter:", color = TextSecondaryDark, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScalingFilter.entries.forEach { filter ->
                        val isSelected = config.hardwareAccel.scalingFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GpuCyan.copy(alpha = 0.2f) else LimboSurfaceVariantDark)
                                .border(1.dp, if (isSelected) GpuCyan else BorderSubtleDark, RoundedCornerShape(6.dp))
                                .clickable {
                                    onUpdate(config.copy(hardwareAccel = config.hardwareAccel.copy(scalingFilter = filter)))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.name.substringBefore("_"),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GpuCyan else TextSecondaryDark,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Target FPS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target Display Refresh Rate:", color = TextSecondaryDark, fontSize = 11.sp)
                    Text("${config.hardwareAccel.targetFps} FPS", color = HardwareGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                val fpsOptions = listOf(30, 60, 90, 120)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    fpsOptions.forEach { fps ->
                        val isSelected = config.hardwareAccel.targetFps == fps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) HardwareGreen.copy(alpha = 0.2f) else LimboSurfaceVariantDark)
                                .border(1.dp, if (isSelected) HardwareGreen else BorderSubtleDark, RoundedCornerShape(6.dp))
                                .clickable {
                                    onUpdate(config.copy(hardwareAccel = config.hardwareAccel.copy(targetFps = fps)))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$fps FPS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) HardwareGreen else TextSecondaryDark,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Host Shader Cache Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Precompiled Shader Disk Cache", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Saves compiled OpenGL shaders to eliminate 3D game stutter.", color = TextSecondaryDark, fontSize = 10.sp)
                    }
                    Switch(
                        checked = config.hardwareAccel.enableShaderCache,
                        onCheckedChange = {
                            onUpdate(config.copy(hardwareAccel = config.hardwareAccel.copy(enableShaderCache = it)))
                        }
                    )
                }
            }
        }
    }
}

// ==================== TAB 1: CPU & VIRTUALIZATION ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CpuConfigSection(
    config: MachineConfig,
    onUpdate: (MachineConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Architecture & Machine
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PROCESSOR ARCHITECTURE", color = AcceleratorPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                var archExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = archExpanded,
                    onExpandedChange = { archExpanded = it }
                ) {
                    OutlinedTextField(
                        value = config.arch.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = archExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AcceleratorPurple,
                            unfocusedBorderColor = BorderSubtleDark
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = archExpanded,
                        onDismissRequest = { archExpanded = false },
                        modifier = Modifier.background(LimboSurfaceDark)
                    ) {
                        VmArchitecture.entries.forEach { a ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(a.displayName, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                                        Text(a.description, color = TextSecondaryDark, fontSize = 10.sp)
                                    }
                                },
                                onClick = {
                                    onUpdate(
                                        config.copy(
                                            arch = a,
                                            machineType = a.defaultMachine,
                                            cpuModel = a.defaultCpu
                                        )
                                    )
                                    archExpanded = false
                                }
                            )
                        }
                    }
                }

                // Machine Type & CPU Model
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = config.machineType,
                        onValueChange = { onUpdate(config.copy(machineType = it)) },
                        label = { Text("Machine Model") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = config.cpuModel,
                        onValueChange = { onUpdate(config.copy(cpuModel = it)) },
                        label = { Text("CPU Type") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // SMP Multi-Core Cores Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CPU CORES (SMP)", color = AcceleratorPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("${config.cpuCores} Core(s)", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Slider(
                    value = config.cpuCores.toFloat(),
                    onValueChange = { onUpdate(config.copy(cpuCores = it.toInt())) },
                    valueRange = 1f..16f,
                    steps = 14,
                    colors = SliderDefaults.colors(thumbColor = AcceleratorPurple, activeTrackColor = AcceleratorPurple)
                )

                Text(
                    text = "Multi-threaded TCG distributes emulation across all ${config.cpuCores} virtual cores.",
                    color = TextSecondaryDark,
                    fontSize = 10.sp
                )
            }
        }

        // RAM Memory Slider
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SYSTEM MEMORY (RAM)", color = AcceleratorPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("${config.ramMb} MB (${String.format("%.1f", config.ramMb / 1024f)} GB)", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                val ramSteps = listOf(128, 256, 512, 1024, 2048, 3072, 4096, 6144, 8192, 12288, 16384)
                Slider(
                    value = ramSteps.indexOf(config.ramMb).coerceAtLeast(0).toFloat(),
                    onValueChange = { idx ->
                        val ramVal = ramSteps[idx.toInt().coerceIn(0, ramSteps.size - 1)]
                        onUpdate(config.copy(ramMb = ramVal))
                    },
                    valueRange = 0f..(ramSteps.size - 1).toFloat(),
                    steps = ramSteps.size - 2,
                    colors = SliderDefaults.colors(thumbColor = AcceleratorPurple, activeTrackColor = AcceleratorPurple)
                )
            }
        }

        // Hardware Acceleration Switches: KVM & MTTCG & JIT Cache
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("HARDWARE VIRTUALIZATION ENGINE", color = HardwareGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                // KVM switch
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kernel KVM Virtualization", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Enables native CPU hardware execution if /dev/kvm is accessible.", color = TextSecondaryDark, fontSize = 10.sp)
                    }
                    Switch(
                        checked = config.hardwareAccel.enableKvm,
                        onCheckedChange = { onUpdate(config.copy(hardwareAccel = config.hardwareAccel.copy(enableKvm = it))) }
                    )
                }

                // MTTCG switch
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Multi-Threaded TCG (MTTCG)", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Spawns a native host thread for each guest CPU core.", color = TextSecondaryDark, fontSize = 10.sp)
                    }
                    Switch(
                        checked = config.hardwareAccel.enableMttcg,
                        onCheckedChange = { onUpdate(config.copy(hardwareAccel = config.hardwareAccel.copy(enableMttcg = it))) }
                    )
                }

                // JIT Cache size
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("JIT Translation Cache (-tb-size):", color = TextSecondaryDark, fontSize = 11.sp)
                    Text("${config.hardwareAccel.jitCacheMb} MB", color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                val jitOptions = listOf(64, 128, 256, 512)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    jitOptions.forEach { mb ->
                        val isSelected = config.hardwareAccel.jitCacheMb == mb
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) HardwareGreen.copy(alpha = 0.2f) else LimboSurfaceVariantDark)
                                .border(1.dp, if (isSelected) HardwareGreen else BorderSubtleDark, RoundedCornerShape(6.dp))
                                .clickable {
                                    onUpdate(config.copy(hardwareAccel = config.hardwareAccel.copy(jitCacheMb = mb)))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${mb}M", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) HardwareGreen else TextSecondaryDark, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 2: STORAGE ====================

@Composable
private fun StorageConfigSection(
    config: MachineConfig,
    onUpdate: (MachineConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Hard Disks Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VIRTUAL HARD DRIVES", color = GpuCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    IconButton(
                        onClick = {
                            val newDisk = VirtualDisk(
                                id = UUID.randomUUID().toString(),
                                name = "Drive_${config.hardDisks.size + 1}.qcow2",
                                sizeGb = 20.0,
                                format = "qcow2",
                                bus = StorageInterface.VIRTIO_BLK,
                                filePath = "/sdcard/Limbo/drive_${config.hardDisks.size + 1}.qcow2",
                                isBootable = config.hardDisks.isEmpty()
                            )
                            onUpdate(config.copy(hardDisks = config.hardDisks + newDisk))
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Disk", tint = GpuCyan)
                    }
                }

                if (config.hardDisks.isEmpty()) {
                    Text("No hard disks attached. Click '+' to add a virtual drive.", color = TextSecondaryDark, fontSize = 11.sp)
                } else {
                    config.hardDisks.forEachIndexed { idx, disk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LimboSurfaceVariantDark)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = GpuCyan)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(disk.name, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text("${disk.sizeGb} GB • ${disk.format.uppercase()} • ${disk.bus.displayName}", color = TextSecondaryDark, fontSize = 10.sp)
                            }
                            IconButton(
                                onClick = {
                                    onUpdate(config.copy(hardDisks = config.hardDisks.filterIndexed { i, _ -> i != idx }))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = DangerCrimson, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Removable Media (CD-ROM ISO & Floppy)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("REMOVABLE MEDIA & BOOT", color = GpuCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                OutlinedTextField(
                    value = config.cdromIso ?: "",
                    onValueChange = { onUpdate(config.copy(cdromIso = it.ifBlank { null })) },
                    label = { Text("CD-ROM / ISO Image Path") },
                    placeholder = { Text("/sdcard/Download/installer.iso") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = config.floppyImg ?: "",
                    onValueChange = { onUpdate(config.copy(floppyImg = it.ifBlank { null })) },
                    label = { Text("Floppy Disk Image Path (.img / .ima)") },
                    placeholder = { Text("/sdcard/Download/boot.img") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Boot Priority
                Text("Primary Boot Device:", color = TextSecondaryDark, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BootDevice.entries.forEach { dev ->
                        val isSelected = config.bootDevice == dev
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) GpuCyan.copy(alpha = 0.2f) else LimboSurfaceVariantDark)
                                .border(1.dp, if (isSelected) GpuCyan else BorderSubtleDark, RoundedCornerShape(6.dp))
                                .clickable { onUpdate(config.copy(bootDevice = dev)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                dev.displayName.substringBefore(" "),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) GpuCyan else TextSecondaryDark,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== TAB 3: SOUND & NET ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundAndNetConfigSection(
    config: MachineConfig,
    onUpdate: (MachineConfig) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Audio Emulation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("AUDIO EMULATION (SOUND CARD)", color = AcceleratorPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                var audioExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = audioExpanded,
                    onExpandedChange = { audioExpanded = it }
                ) {
                    OutlinedTextField(
                        value = config.audioDevice.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = audioExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = audioExpanded,
                        onDismissRequest = { audioExpanded = false },
                        modifier = Modifier.background(LimboSurfaceDark)
                    ) {
                        AudioDevice.entries.forEach { ad ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(ad.displayName, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                                        Text(ad.description, color = TextSecondaryDark, fontSize = 10.sp)
                                    }
                                },
                                onClick = {
                                    onUpdate(config.copy(audioDevice = ad))
                                    audioExpanded = false
                                }
                            )
                        }
                    }
                }

                // Audio Backend
                Text("Host Audio Backend:", color = TextSecondaryDark, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AudioBackend.entries.forEach { b ->
                        val isSelected = config.audioBackend == b
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AcceleratorPurple.copy(alpha = 0.2f) else LimboSurfaceVariantDark)
                                .border(1.dp, if (isSelected) AcceleratorPurple else BorderSubtleDark, RoundedCornerShape(6.dp))
                                .clickable { onUpdate(config.copy(audioBackend = b)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(b.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) AcceleratorPurple else TextSecondaryDark, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Network Emulation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("NETWORK CONTROLLER (NIC)", color = GpuCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                var netExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = netExpanded,
                    onExpandedChange = { netExpanded = it }
                ) {
                    OutlinedTextField(
                        value = config.networkDevice.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = netExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = netExpanded,
                        onDismissRequest = { netExpanded = false },
                        modifier = Modifier.background(LimboSurfaceDark)
                    ) {
                        NetworkDevice.entries.forEach { nd ->
                            DropdownMenuItem(
                                text = { Text(nd.displayName, color = TextPrimaryDark) },
                                onClick = {
                                    onUpdate(config.copy(networkDevice = nd))
                                    netExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = config.portForwarding,
                    onValueChange = { onUpdate(config.copy(portForwarding = it)) },
                    label = { Text("Port Forwarding (SLIRP hostfwd)") },
                    placeholder = { Text("tcp:2222-22,tcp:8080-80") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ==================== TAB 4: QEMU CLI ====================

@Composable
private fun QemuCliSection(
    config: MachineConfig,
    onCopyCli: () -> Unit,
    onUpdateExtra: (String) -> Unit
) {
    val fullCli = remember(config) {
        QemuCommandLineBuilder.buildCommandLine(config)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GENERATED QEMU INVOCATION", color = GpuCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    IconButton(onClick = onCopyCli, modifier = Modifier.testTag("btn_copy_qemu_cli")) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GpuCyan)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF04060A))
                        .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = fullCli,
                        color = Color(0xFF81D4FA),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("EXTRA USER QEMU ARGUMENTS", color = AcceleratorPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                OutlinedTextField(
                    value = config.extraQemuParams,
                    onValueChange = onUpdateExtra,
                    placeholder = { Text("-vnc :1 -usb -device usb-mouse") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AcceleratorPurple)
                )
            }
        }
    }
}
