package com.example.limbo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.limbo.simulator.VmTelemetry
import com.example.ui.theme.GpuCyan
import com.example.ui.theme.HardwareGreen
import com.example.ui.theme.LimboSurfaceDark
import com.example.ui.theme.LimboSurfaceVariantDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber

/**
 * High-tech glowing badge indicating GPU 3D acceleration or Virtualization mode
 */
@Composable
fun AccelerationBadge(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = GpuCyan
) {
    val bg = if (isActive) activeColor.copy(alpha = 0.15f) else Color(0xFF263238).copy(alpha = 0.4f)
    val border = if (isActive) activeColor.copy(alpha = 0.6f) else Color(0xFF455A64).copy(alpha = 0.3f)
    val textCol = if (isActive) activeColor else TextSecondaryDark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) activeColor else Color.Gray)
            )
            Text(
                text = text,
                color = textCol,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Real-time Virtual Machine Telemetry HUD overlay
 */
@Composable
fun TelemetryHudBar(
    telemetry: VmTelemetry,
    isVirglActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        LimboSurfaceDark.copy(alpha = 0.92f),
                        LimboSurfaceVariantDark.copy(alpha = 0.85f)
                    )
                )
            )
            .border(1.dp, Color(0xFF2A3B5C), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FPS
            HudStatItem(
                icon = Icons.Default.Speed,
                label = "FPS",
                value = String.format("%.1f", telemetry.fps),
                valueColor = if (telemetry.fps >= 55f) HardwareGreen else WarningAmber
            )

            // GPU & VirGL Load
            HudStatItem(
                icon = Icons.Default.VideogameAsset,
                label = if (isVirglActive) "VirGL 3D" else "GPU",
                value = "${telemetry.gpuLoadPercent}%",
                valueColor = GpuCyan
            )

            // Draw calls / sec
            if (isVirglActive) {
                HudStatItem(
                    icon = Icons.Default.ElectricBolt,
                    label = "Draw Calls",
                    value = "${telemetry.virglDrawCalls}/s",
                    valueColor = GpuCyan
                )
            }

            // VRAM
            HudStatItem(
                icon = Icons.Default.Memory,
                label = "VRAM",
                value = "${telemetry.vramUsedMb}M",
                valueColor = TextPrimaryDark
            )

            // Guest RAM
            HudStatItem(
                icon = Icons.Default.Memory,
                label = "RAM",
                value = "${telemetry.ramUsedMb}M",
                valueColor = TextPrimaryDark
            )
        }
    }
}

@Composable
private fun HudStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = valueColor.copy(alpha = 0.8f),
            modifier = Modifier.size(13.dp)
        )
        Column {
            Text(
                text = label,
                color = TextSecondaryDark,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 10.sp
            )
            Text(
                text = value,
                color = valueColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                lineHeight = 13.sp
            )
        }
    }
}
