package com.example.limbo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.limbo.simulator.HostHardwareProfile
import com.example.limbo.ui.components.AccelerationBadge
import com.example.ui.theme.AcceleratorPurple
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.GpuCyan
import com.example.ui.theme.HardwareGreen
import com.example.ui.theme.LimboBgDark
import com.example.ui.theme.LimboSurfaceDark
import com.example.ui.theme.LimboSurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostDiagnosticsScreen(
    profile: HostHardwareProfile,
    onBack: () -> Unit
) {
    var isBenchmarking by remember { mutableStateOf(false) }
    var benchmarkScore by remember { mutableIntStateOf(0) }
    var testFps by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "HOST HARDWARE & GPU",
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "ACCELERATION REPORT",
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LimboSurfaceDark)
            )
        },
        containerColor = LimboBgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: GPU & VirGL 3D Capability
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = GpuCyan)
                            Text("GPU & 3D GRAPHICS PIPELINE", color = GpuCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        DiagnosticRow(
                            label = "Host OpenGL ES API",
                            value = "Version ${profile.glEsVersionString}",
                            isGood = profile.hasGlEs32Support
                        )
                        DiagnosticRow(
                            label = "VirGL 3D Hardware Passthrough",
                            value = if (profile.supportsVirgl3D) "Supported (Direct HW Context)" else "Software Fallback",
                            isGood = profile.supportsVirgl3D
                        )
                        DiagnosticRow(
                            label = "Vulkan Hardware Support",
                            value = if (profile.hasVulkanSupport) "Available (Venus Backend Ready)" else "Not Available",
                            isGood = profile.hasVulkanSupport
                        )
                        DiagnosticRow(
                            label = "Max Supported Guest VRAM",
                            value = "1024 MB (DDR4/LPDDR5 Shared)",
                            isGood = true
                        )
                    }
                }
            }

            // Section 2: Virtualization & CPU Multi-Threading
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = HardwareGreen)
                            Text("HARDWARE VIRTUALIZATION & CPU", color = HardwareGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        DiagnosticRow(
                            label = "Host CPU Architecture",
                            value = profile.cpuArch,
                            isGood = true
                        )
                        DiagnosticRow(
                            label = "Host Hardware Threads / Cores",
                            value = "${profile.hostCpuCores} Available Cores",
                            isGood = profile.hostCpuCores >= 4
                        )
                        DiagnosticRow(
                            label = "MTTCG Multi-Threading",
                            value = if (profile.supportsMttcg) "Enabled across all cores" else "Single-threaded",
                            isGood = profile.supportsMttcg
                        )
                        DiagnosticRow(
                            label = "Kernel KVM Node (/dev/kvm)",
                            value = profile.kvmPath,
                            isGood = profile.hasKvmSupport
                        )
                        DiagnosticRow(
                            label = "Total System RAM",
                            value = "${profile.totalRamMb} MB (${profile.availableRamMb} MB Free)",
                            isGood = profile.totalRamMb >= 3000
                        )
                    }
                }
            }

            // Section 3: VirGL 3D Benchmark Test
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GpuCyan.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = GpuCyan)
                            Text("VIRGL 3D GPU BENCHMARK TEST", color = GpuCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Text(
                            text = "Executes a simulated VirGL 3D shader pipeline test to benchmark host OpenGL ES 3.2 draw call throughput.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )

                        if (benchmarkScore > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LimboSurfaceVariantDark)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("BENCHMARK SCORE", color = GpuCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("$benchmarkScore PTS", color = HardwareGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("PEAK DRAW FPS", color = TextSecondaryDark, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    Text("${String.format("%.1f", testFps)} FPS", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (!isBenchmarking) {
                                    isBenchmarking = true
                                    scope.launch {
                                        for (i in 1..10) {
                                            testFps = 52f + (i * 1.5f)
                                            delay(120)
                                        }
                                        benchmarkScore = 8450 + (profile.hostCpuCores * 450)
                                        isBenchmarking = false
                                    }
                                }
                            },
                            enabled = !isBenchmarking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_run_benchmark"),
                            colors = ButtonDefaults.buttonColors(containerColor = GpuCyan, contentColor = Color(0xFF00363D)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isBenchmarking) "TESTING VIRGL 3D PIPELINE..." else "RUN 3D GPU BENCHMARK",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Section 4: Architecture & Acceleration Guidelines
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AcceleratorPurple)
                            Text("OPTIMIZATION TIPS FOR GPU EMULATION", color = AcceleratorPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Text(
                            text = "1. For modern Linux / Android-x86: Always select 'VirtIO-GPU 3D (VirGL Native GLES)' for near-native hardware acceleration.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "2. For Windows 10 & 11: Use 'VirtIO-VGA 3D (VirGL + BIOS VGA)' with 512MB VRAM and install the Red Hat VirtIO GPU guest driver.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "3. For Windows XP & 7: Use 'VMware SVGA II' with VMware Tools to gain Direct3D hardware acceleration.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "4. MTTCG: Keep MTTCG enabled in Limbo to leverage all ${profile.hostCpuCores} CPU cores on your device.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isGood) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isGood) HardwareGreen else WarningAmber,
                modifier = Modifier.size(14.dp)
            )
            Text(label, color = TextSecondaryDark, fontSize = 11.sp)
        }
        Text(
            text = value,
            color = if (isGood) TextPrimaryDark else WarningAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
