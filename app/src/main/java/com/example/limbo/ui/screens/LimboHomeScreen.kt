package com.example.limbo.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.limbo.model.MachineConfig
import com.example.limbo.simulator.HostHardwareProfile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimboHomeScreen(
    vms: List<MachineConfig>,
    hostProfile: HostHardwareProfile,
    onStartVm: (MachineConfig) -> Unit,
    onEditVm: (MachineConfig) -> Unit,
    onCloneVm: (String) -> Unit,
    onDeleteVm: (String) -> Unit,
    onOpenTemplates: () -> Unit,
    onCreateCustomVm: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(GpuCyan.copy(alpha = 0.2f))
                                .border(1.dp, GpuCyan, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideogameAsset,
                                contentDescription = null,
                                tint = GpuCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "LIMBO PC SIMULATOR",
                                color = TextPrimaryDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "GPU 3D ACCELERATED EDITION",
                                color = GpuCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDiagnostics,
                        modifier = Modifier.testTag("btn_host_diagnostics")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Host Diagnostics",
                            tint = GpuCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LimboSurfaceDark
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FloatingActionButton(
                    onClick = onOpenTemplates,
                    containerColor = AcceleratorPurple,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_templates")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("OS Templates", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                FloatingActionButton(
                    onClick = onCreateCustomVm,
                    containerColor = GpuCyan,
                    contentColor = Color(0xFF00363D),
                    modifier = Modifier.testTag("fab_create_vm")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("New Machine", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
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
            // Item 1: Host Acceleration & Hardware Overview Card
            item {
                HostAccelerationCard(
                    profile = hostProfile,
                    onOpenDiagnostics = onOpenDiagnostics
                )
            }

            // Item 2: Virtual Machines Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIRTUAL MACHINES (${vms.size})",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "QEMU 8.2 / VIRGL 3D",
                        color = GpuCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Items: VM Cards
            if (vms.isEmpty()) {
                item {
                    EmptyVmStateCard(onOpenTemplates = onOpenTemplates)
                }
            } else {
                items(vms, key = { it.id }) { vm ->
                    VmCard(
                        vm = vm,
                        onStart = { onStartVm(vm) },
                        onEdit = { onEditVm(vm) },
                        onClone = { onCloneVm(vm.id) },
                        onDelete = { onDeleteVm(vm.id) }
                    )
                }
            }

            // Extra space for FAB
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun HostAccelerationCard(
    profile: HostHardwareProfile,
    onOpenDiagnostics: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenDiagnostics() },
        colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = null,
                        tint = HardwareGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "HOST HARDWARE ENGINE",
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "DIAGNOSTICS >",
                    color = GpuCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Hardware Badges Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AccelerationBadge(
                    text = "GLES ${profile.glEsVersionString}",
                    isActive = profile.hasGlEs32Support,
                    modifier = Modifier.weight(1f),
                    activeColor = GpuCyan
                )
                AccelerationBadge(
                    text = if (profile.supportsVirgl3D) "VIRGL 3D OK" else "VIRGL N/A",
                    isActive = profile.supportsVirgl3D,
                    modifier = Modifier.weight(1f),
                    activeColor = GpuCyan
                )
                AccelerationBadge(
                    text = "SMP ${profile.hostCpuCores}x CPU",
                    isActive = profile.hostCpuCores >= 4,
                    modifier = Modifier.weight(1f),
                    activeColor = HardwareGreen
                )
                AccelerationBadge(
                    text = if (profile.hasKvmSupport) "KVM ACCEL" else "MTTCG",
                    isActive = true,
                    modifier = Modifier.weight(1f),
                    activeColor = if (profile.hasKvmSupport) HardwareGreen else AcceleratorPurple
                )
            }

            Text(
                text = profile.accelerationVerdict,
                color = TextSecondaryDark,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun VmCard(
    vm: MachineConfig,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vm_card_${vm.id}"),
        colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: VM Name & Arch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = vm.name,
                        color = TextPrimaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${vm.arch.displayName} • Machine: ${vm.machineType}",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                // Quick Clone / Delete icon buttons
                Row {
                    IconButton(onClick = onClone, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Clone VM",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete VM",
                            tint = DangerCrimson.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Specs Row (CPU, RAM, GPU, VRAM)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LimboSurfaceVariantDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecPill(label = "CPU", value = "${vm.cpuCores} Cores")
                SpecPill(label = "RAM", value = "${vm.ramMb} MB")
                SpecPill(label = "VRAM", value = "${vm.hardwareAccel.vramMb} MB")
                SpecPill(label = "DISKS", value = "${vm.hardDisks.size} Drive(s)")
            }

            // Acceleration & Features Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (vm.gpuDevice.supportsVirgl && vm.hardwareAccel.enableVirgl3d) {
                    AccelerationBadge(
                        text = "3D VIRGL GLES",
                        isActive = true,
                        activeColor = GpuCyan
                    )
                } else if (vm.gpuDevice.is3DAccelerated) {
                    AccelerationBadge(
                        text = "VMWARE 3D",
                        isActive = true,
                        activeColor = GpuCyan
                    )
                } else {
                    AccelerationBadge(
                        text = vm.gpuDevice.name,
                        isActive = false
                    )
                }

                if (vm.hardwareAccel.enableMttcg) {
                    AccelerationBadge(
                        text = "MTTCG ${vm.cpuCores}x",
                        isActive = true,
                        activeColor = AcceleratorPurple
                    )
                }

                if (vm.hardwareAccel.enableKvm) {
                    AccelerationBadge(
                        text = "KVM",
                        isActive = true,
                        activeColor = HardwareGreen
                    )
                }
            }

            // Action Buttons Row: Start & Configure
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_config_${vm.id}"),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CONFIG",
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("btn_start_${vm.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GpuCyan,
                        contentColor = Color(0xFF00363D)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "START VM",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecPill(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = TextTertiaryDark,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = TextPrimaryDark,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun EmptyVmStateCard(onOpenTemplates: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LimboSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = GpuCyan,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No Virtual Machines Configured",
                color = TextPrimaryDark,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "Get started in 1-click by picking an optimized Windows, Linux, or FreeDOS template.",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onOpenTemplates,
                colors = ButtonDefaults.buttonColors(containerColor = AcceleratorPurple)
            ) {
                Text("Browse OS Templates")
            }
        }
    }
}
