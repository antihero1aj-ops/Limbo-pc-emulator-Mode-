package com.example.limbo.simulator

import com.example.limbo.model.AudioDevice
import com.example.limbo.model.GlRendererMode
import com.example.limbo.model.MachineConfig
import com.example.limbo.model.NetworkDevice
import com.example.limbo.model.StorageInterface

object QemuCommandLineBuilder {

    /**
     * Builds the complete QEMU executable invocation command string
     * with high-performance 3D GPU and hardware virtualization flags.
     */
    fun buildCommandLine(config: MachineConfig): String {
        val args = buildArgsList(config)
        return "${config.arch.binaryName} \\\n  " + args.joinToString(" \\\n  ")
    }

    /**
     * Builds the ordered list of arguments passed to QEMU.
     */
    fun buildArgsList(config: MachineConfig): List<String> {
        val args = mutableListOf<String>()

        // 1. Machine Model & Hardware Accelerator selection
        val accelArg = buildString {
            if (config.hardwareAccel.enableKvm) {
                append("accel=kvm:tcg")
            } else {
                append("accel=tcg")
            }
            if (config.hardwareAccel.enableMttcg) {
                append(",thread=multi")
            }
        }
        args.add("-M ${config.machineType},$accelArg")

        // 2. CPU Architecture, Microarchitecture & Virtualization Flags
        val cpuFlags = buildString {
            append(config.cpuModel)
            if (config.hardwareAccel.enableKvm) {
                append(",kvm=on")
            }
            // Add vector SIMD extensions for high performance
            if (config.arch.name.contains("X86")) {
                append(",+ssse3,+sse4.1,+sse4.2")
            }
        }
        args.add("-cpu $cpuFlags")

        // 3. Multi-Core SMP (Symmetric Multiprocessing)
        args.add("-smp cores=${config.cpuCores},threads=1,sockets=1")

        // 4. Guest RAM allocation
        args.add("-m ${config.ramMb}M")

        // 5. Translation Block JIT Cache (huge speed boost on TCG)
        args.add("-tb-size ${config.hardwareAccel.jitCacheMb}")

        // 6. GPU Graphics Adapter & 3D Hardware Acceleration
        val gpu = config.gpuDevice
        val accel = config.hardwareAccel

        if (gpu.supportsVirgl && accel.enableVirgl3d) {
            // VIRGL 3D HARDWARE ACCELERATION
            val vramParam = "vram64_size_mb=${accel.vramMb}"
            args.add("-device ${gpu.qemuDevice},virgl=on,$vramParam,max_outputs=1,xres=1920,yres=1080")

            when (accel.rendererMode) {
                GlRendererMode.OPENGL_ES_3 -> {
                    args.add("-display sdl,gl=on")
                }
                GlRendererMode.VULKAN_VENUS -> {
                    args.add("-display sdl,gl=on,vulkan=on")
                }
                GlRendererMode.EGL_HEADLESS -> {
                    args.add("-display egl-headless,rendernode=/dev/dri/renderD128")
                }
            }
        } else if (gpu.is3DAccelerated) {
            // VMware SVGA II 3D
            args.add("-device ${gpu.qemuDevice},vgamem_mb=${accel.vramMb}")
            args.add("-display sdl")
        } else {
            // Standard 2D adapter
            args.add("-device ${gpu.qemuDevice}")
            args.add("-display sdl")
        }

        // 7. Input Devices (VirtIO Tablet gives fluid absolute touch tracking)
        args.add("-device virtio-tablet-pci")
        args.add("-device virtio-keyboard-pci")

        // 8. Storage Drives & Hard Disks
        config.hardDisks.forEachIndexed { index, disk ->
            val driveId = "drive$index"
            val cacheMode = "writeback"
            val driveArg = "-drive file=${disk.filePath},if=none,id=$driveId,format=${disk.format},cache=$cacheMode"
            args.add(driveArg)

            val deviceArg = when (disk.bus) {
                StorageInterface.VIRTIO_BLK -> "-device virtio-blk-pci,drive=$driveId,bootindex=${if (disk.isBootable) index else index + 1}"
                StorageInterface.AHCI_SATA -> "-device ide-hd,drive=$driveId,bus=ide.$index"
                StorageInterface.IDE -> "-device ide-hd,drive=$driveId"
                StorageInterface.SCSI -> "-device scsi-hd,drive=$driveId"
            }
            args.add(deviceArg)
        }

        // 9. CD-ROM / ISO image
        if (!config.cdromIso.isNullOrBlank()) {
            args.add("-cdrom ${config.cdromIso}")
        }

        // 10. Floppy disk
        if (!config.floppyImg.isNullOrBlank()) {
            args.add("-fda ${config.floppyImg}")
        }

        // 11. Audio Emulation & AAudio Hardware Backend
        if (config.audioDevice != AudioDevice.NONE) {
            args.add("-audiodev ${config.audioBackend.qemuBackend},id=snd0")
            args.add("-device ${config.audioDevice.qemuName},audiodev=snd0")
        }

        // 12. Network Device & Port Forwarding
        if (config.networkDevice != NetworkDevice.NONE) {
            val netBuilder = StringBuilder("user,model=${config.networkDevice.qemuModel}")
            if (config.portForwarding.isNotBlank()) {
                val rules = config.portForwarding.split(",")
                for (rule in rules) {
                    val trimmed = rule.trim()
                    if (trimmed.isNotBlank()) {
                        netBuilder.append(",hostfwd=$trimmed")
                    }
                }
            }
            args.add("-nic $netBuilder")
        } else {
            args.add("-nic none")
        }

        // 13. High Precision Event Timer & RTC
        if (accel.enableHpet) {
            args.add("-no-hpet")
            args.add("-rtc base=localtime,driftfix=slew")
        }

        // 14. Boot Device Priority
        args.add("-boot ${config.bootDevice.qemuArg}")

        // 15. Shared Folder (9pfs virtio)
        if (!config.sharedFolder.isNullOrBlank()) {
            args.add("-fsdev local,id=shared0,path=${config.sharedFolder},security_model=none")
            args.add("-device virtio-9p-pci,fsdev=shared0,mount_tag=limbo_share")
        }

        // 16. Custom extra user parameters
        if (config.extraQemuParams.isNotBlank()) {
            args.add(config.extraQemuParams)
        }

        return args
    }
}
