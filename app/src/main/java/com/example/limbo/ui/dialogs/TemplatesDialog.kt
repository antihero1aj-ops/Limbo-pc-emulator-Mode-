package com.example.limbo.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.limbo.model.GpuDevice
import com.example.limbo.model.HardwareAccelConfig
import com.example.limbo.model.MachineConfig
import com.example.limbo.model.StorageInterface
import com.example.limbo.model.VirtualDisk
import com.example.limbo.model.VmArchitecture
import com.example.limbo.model.VmPresetType
import com.example.limbo.ui.components.AccelerationBadge
import com.example.ui.theme.AcceleratorPurple
import com.example.ui.theme.BorderSubtleDark
import com.example.ui.theme.GpuCyan
import com.example.ui.theme.HardwareGreen
import com.example.ui.theme.LimboSurfaceDark
import com.example.ui.theme.LimboSurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesBottomSheet(
    onSelectPreset: (VmPresetType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LimboSurfaceDark,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = GpuCyan)
                    Column {
                        Text(
                            text = "PRE-CONFIGURED OS TEMPLATES",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Optimized with 3D GPU acceleration & MTTCG presets",
                            color = GpuCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(VmPresetType.entries) { preset ->
                    PresetCard(
                        preset = preset,
                        onClick = {
                            onSelectPreset(preset)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    preset: VmPresetType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("preset_${preset.name}"),
        colors = CardDefaults.cardColors(containerColor = LimboSurfaceVariantDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.title,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (preset.gpuDevice.supportsVirgl) {
                    AccelerationBadge(text = "VIRGL 3D", isActive = true, activeColor = GpuCyan)
                } else if (preset.gpuDevice.is3DAccelerated) {
                    AccelerationBadge(text = "3D ACCEL", isActive = true, activeColor = GpuCyan)
                } else {
                    AccelerationBadge(text = "2D VGA", isActive = false)
                }
            }

            Text(
                text = preset.description,
                color = TextSecondaryDark,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${preset.cpuCores} Cores • ${preset.ramMb} MB RAM • ${preset.vramMb} MB VRAM • ${preset.gpuDevice.displayName}",
                    color = GpuCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GpuCyan, contentColor = Color(0xFF00363D)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("USE TEMPLATE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomVmBottomSheet(
    onCreateVm: (MachineConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("New PC Machine") }
    var arch by remember { mutableStateOf(VmArchitecture.X86_64) }
    var gpu by remember { mutableStateOf(GpuDevice.VIRTIO_VGA_GL) }
    var cores by remember { mutableStateOf("4") }
    var ram by remember { mutableStateOf("2048") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = LimboSurfaceDark,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CREATE CUSTOM VIRTUAL MACHINE",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Machine Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_new_vm_name")
            )

            // Architecture choice
            Text("Architecture:", color = TextSecondaryDark, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(VmArchitecture.X86_64, VmArchitecture.X86, VmArchitecture.ARM64).forEach { a ->
                    val isSel = arch == a
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) GpuCyan.copy(alpha = 0.2f) else LimboSurfaceVariantDark)
                            .border(1.dp, if (isSel) GpuCyan else BorderSubtleDark, RoundedCornerShape(6.dp))
                            .clickable { arch = a }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(a.name, color = if (isSel) GpuCyan else TextSecondaryDark, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // GPU Choice
            Text("Graphics Adapter:", color = TextSecondaryDark, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(GpuDevice.VIRTIO_VGA_GL, GpuDevice.VMWARE_SVGA, GpuDevice.CIRRUS).forEach { g ->
                    val isSel = gpu == g
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) GpuCyan.copy(alpha = 0.2f) else LimboSurfaceVariantDark)
                            .border(1.dp, if (isSel) GpuCyan else BorderSubtleDark, RoundedCornerShape(6.dp))
                            .clickable { gpu = g }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(g.name.substringBefore("_"), color = if (isSel) GpuCyan else TextSecondaryDark, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Button(
                onClick = {
                    val newConfig = MachineConfig(
                        id = UUID.randomUUID().toString(),
                        name = name.ifBlank { "Virtual PC" },
                        arch = arch,
                        machineType = arch.defaultMachine,
                        cpuModel = arch.defaultCpu,
                        cpuCores = cores.toIntOrNull() ?: 4,
                        ramMb = ram.toIntOrNull() ?: 2048,
                        gpuDevice = gpu,
                        hardwareAccel = HardwareAccelConfig(
                            enableKvm = true,
                            enableMttcg = true,
                            enableVirgl3d = gpu.supportsVirgl,
                            vramMb = gpu.defaultVramMb
                        )
                    )
                    onCreateVm(newConfig)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_submit_create_vm"),
                colors = ButtonDefaults.buttonColors(containerColor = GpuCyan, contentColor = Color(0xFF00363D)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("CREATE & CONFIGURE", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
