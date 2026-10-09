package com.example.limbo.simulator

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/**
 * Detailed profile of the Host Android device hardware capabilities,
 * GPU support, and acceleration capabilities.
 */
data class HostHardwareProfile(
    val cpuArch: String,
    val hostCpuCores: Int,
    val totalRamMb: Int,
    val availableRamMb: Int,
    val hasKvmSupport: Boolean,
    val kvmPath: String,
    val glEsVersionString: String,
    val hasGlEs32Support: Boolean,
    val hasVulkanSupport: Boolean,
    val supportsVirgl3D: Boolean,
    val supportsMttcg: Boolean,
    val socModel: String,
    val osBuild: String,
    val accelerationVerdict: String
)

object HostHardwareDetector {

    fun detect(context: Context): HostHardwareProfile {
        val cpuArch = Build.SUPPORTED_ABIS.firstOrNull() ?: System.getProperty("os.arch") ?: "unknown"
        val hostCpuCores = Runtime.getRuntime().availableProcessors()

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).toInt()
        val availableRamMb = (memInfo.availMem / (1024 * 1024)).toInt()

        // Check for Linux Kernel KVM device node (/dev/kvm)
        val kvmFile = File("/dev/kvm")
        val hasKvm = kvmFile.exists() && kvmFile.canRead()

        // Check OpenGL ES version
        val configInfo = actManager?.deviceConfigurationInfo
        val glEsVersionInt = configInfo?.reqGlEsVersion ?: 0
        val majorGl = (glEsVersionInt shr 16) and 0xFFFF
        val minorGl = glEsVersionInt and 0xFFFF
        val glEsVersionString = "$majorGl.$minorGl"
        val hasGlEs32 = (majorGl > 3) || (majorGl == 3 && minorGl >= 2)

        // Check Vulkan Hardware level
        val pm = context.packageManager
        val hasVulkan = pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION) ||
                pm.hasSystemFeature("android.hardware.vulkan.level")

        // VirGL 3D requires at least OpenGL ES 3.0+
        val supportsVirgl = majorGl >= 3

        val socModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        val osBuild = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

        val verdict = buildString {
            if (hasGlEs32 && hostCpuCores >= 4) {
                append("Excellent: OpenGL ES 3.2 3D GPU Passthrough & VirGL fully supported with $hostCpuCores CPU cores.")
            } else if (supportsVirgl) {
                append("Good: 3D Hardware Acceleration supported via OpenGL ES $glEsVersionString.")
            } else {
                append("Basic: 2D paravirtualized graphics active; 3D acceleration will use software rasterizer.")
            }
            if (hasKvm) {
                append(" KVM Hardware Virtualization node is available.")
            } else {
                append(" MTTCG Multi-Threaded TCG will accelerate CPU execution across all $hostCpuCores cores.")
            }
        }

        return HostHardwareProfile(
            cpuArch = cpuArch,
            hostCpuCores = hostCpuCores,
            totalRamMb = totalRamMb,
            availableRamMb = availableRamMb,
            hasKvmSupport = hasKvm,
            kvmPath = if (hasKvm) "/dev/kvm (Active)" else "/dev/kvm (Not present/No root)",
            glEsVersionString = glEsVersionString,
            hasGlEs32Support = hasGlEs32,
            hasVulkanSupport = hasVulkan,
            supportsVirgl3D = supportsVirgl,
            supportsMttcg = hostCpuCores > 1,
            socModel = socModel,
            osBuild = osBuild,
            accelerationVerdict = verdict
        )
    }
}
