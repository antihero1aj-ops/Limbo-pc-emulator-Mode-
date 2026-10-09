package com.example.limbo.model

/**
 * CPU Architectures supported by the Limbo PC Emulator
 */
enum class VmArchitecture(
    val displayName: String,
    val binaryName: String,
    val description: String,
    val defaultMachine: String,
    val defaultCpu: String
) {
    X86_64(
        displayName = "x86_64 (64-Bit PC)",
        binaryName = "qemu-system-x86_64",
        description = "Modern Intel/AMD 64-Bit PC with PCI Express & VirGL 3D",
        defaultMachine = "q35",
        defaultCpu = "qemu64"
    ),
    X86(
        displayName = "x86 (32-Bit i386)",
        binaryName = "qemu-system-i386",
        description = "Classic 32-Bit PC (Windows 95/98/XP, MS-DOS, Legacy OS)",
        defaultMachine = "pc",
        defaultCpu = "qemu32"
    ),
    ARM64(
        displayName = "ARM64 (AArch64)",
        binaryName = "qemu-system-aarch64",
        description = "Modern 64-Bit ARM platform with UEFI firmware & VirtIO 3D",
        defaultMachine = "virt",
        defaultCpu = "cortex-a72"
    ),
    ARM(
        displayName = "ARM 32-Bit",
        binaryName = "qemu-system-arm",
        description = "ARMv7 embedded systems and Raspberry Pi emulation",
        defaultMachine = "versatilepb",
        defaultCpu = "cortex-a9"
    ),
    PPC(
        displayName = "PowerPC (PPC)",
        binaryName = "qemu-system-ppc",
        description = "Power Mac G4 & PowerPC Linux emulation",
        defaultMachine = "mac99",
        defaultCpu = "G4"
    ),
    SPARC(
        displayName = "SPARC",
        binaryName = "qemu-system-sparc",
        description = "Sun Microsystems SPARCstation & Solaris emulation",
        defaultMachine = "SS-5",
        defaultCpu = "TI-MicroSPARC-II"
    )
}

/**
 * GPU & Graphics Display Devices with 3D & 2D Hardware Acceleration
 */
enum class GpuDevice(
    val displayName: String,
    val qemuDevice: String,
    val description: String,
    val is3DAccelerated: Boolean,
    val defaultVramMb: Int,
    val supportsVirgl: Boolean
) {
    VIRTIO_VGA_GL(
        displayName = "VirtIO-VGA 3D (VirGL + BIOS VGA)",
        qemuDevice = "virtio-vga-gl",
        description = "Full 3D GPU hardware acceleration via host OpenGL ES 3.2 with legacy BIOS VGA compatibility.",
        is3DAccelerated = true,
        defaultVramMb = 512,
        supportsVirgl = true
    ),
    VIRTIO_GPU_GL(
        displayName = "VirtIO-GPU 3D (VirGL Native GLES)",
        qemuDevice = "virtio-gpu-gl-pci",
        description = "High-speed paravirtualized 3D pipeline for modern Linux, Android-x86, and Windows with VirtIO drivers.",
        is3DAccelerated = true,
        defaultVramMb = 512,
        supportsVirgl = true
    ),
    VMWARE_SVGA(
        displayName = "VMware SVGA II (Direct3D 3D)",
        qemuDevice = "vmware-svga",
        description = "Direct3D 9 / OpenGL acceleration with built-in Windows XP / 7 / 10 VMware Tools driver support.",
        is3DAccelerated = true,
        defaultVramMb = 256,
        supportsVirgl = false
    ),
    QXL_VGA(
        displayName = "QXL GPU (SPICE Acceleration)",
        qemuDevice = "qxl-vga",
        description = "Fast 2D hardware acceleration, automatic resolution resizing, and SPICE protocol integration.",
        is3DAccelerated = false,
        defaultVramMb = 128,
        supportsVirgl = false
    ),
    VIRTIO_GPU_2D(
        displayName = "VirtIO-GPU 2D",
        qemuDevice = "virtio-gpu-pci",
        description = "Standard VirtIO paravirtualized display adapter without host 3D pipeline.",
        is3DAccelerated = false,
        defaultVramMb = 128,
        supportsVirgl = false
    ),
    CIRRUS(
        displayName = "Cirrus Logic CL-GD5446",
        qemuDevice = "cirrus-vga",
        description = "Maximum compatibility for retro operating systems: Windows 95, 98, ME, and early Linux.",
        is3DAccelerated = false,
        defaultVramMb = 32,
        supportsVirgl = false
    ),
    STD_VGA(
        displayName = "Standard VGA (VESA VBE 2.0)",
        qemuDevice = "VGA",
        description = "Universal VESA 2.0 BIOS display adapter with wide operating system compatibility.",
        is3DAccelerated = false,
        defaultVramMb = 64,
        supportsVirgl = false
    ),
    ATI_VGA(
        displayName = "ATI Rage 128 Pro",
        qemuDevice = "ati-vga",
        description = "Hardware 2D/3D accelerator card for classic Windows 98/2000 DirectX 6/7 gaming.",
        is3DAccelerated = false,
        defaultVramMb = 64,
        supportsVirgl = false
    )
}

/**
 * Host OpenGL ES / Vulkan display surface pipeline
 */
enum class GlRendererMode(val displayName: String, val description: String) {
    OPENGL_ES_3("OpenGL ES 3.2 (Host Native)", "Direct Android HW Surface with GL ES 3.2 VirGL backend"),
    VULKAN_VENUS("Vulkan Venus 3D Passthrough", "Next-gen Vulkan-based 3D virtualization backend"),
    EGL_HEADLESS("EGL Headless HW Surface", "Direct EGL hardware rendering buffer")
}

/**
 * Display scaling and texture filtering
 */
enum class ScalingFilter(val displayName: String) {
    BILINEAR_HW("Bilinear Hardware Filtering (Smooth)"),
    NEAREST("Nearest-Neighbor (Crisp Retro Pixels)"),
    FXAA_SHARP("FXAA Anti-Aliased Sharpening")
}

/**
 * Hardware Acceleration & GPU Optimization settings
 */
data class HardwareAccelConfig(
    val enableKvm: Boolean = true,          // /dev/kvm Hardware Virtualization
    val enableMttcg: Boolean = true,        // Multi-Threaded TCG for multi-core host CPU
    val enableVirgl3d: Boolean = true,      // VirGL 3D GPU Hardware Acceleration
    val enableVenusVulkan: Boolean = false, // Vulkan Venus 3D pipeline
    val enableShaderCache: Boolean = true,  // Host GPU shader binary disk cache
    val jitCacheMb: Int = 256,              // Translation block cache (-tb-size)
    val vramMb: Int = 512,                  // GPU Video RAM (VRAM)
    val rendererMode: GlRendererMode = GlRendererMode.OPENGL_ES_3,
    val scalingFilter: ScalingFilter = ScalingFilter.BILINEAR_HW,
    val targetFps: Int = 60,                // 30, 60, 90, 120 FPS
    val vsyncEnabled: Boolean = true,
    val enableHugePages: Boolean = true,
    val enableHpet: Boolean = true          // High Precision Event Timer
)

/**
 * Audio Emulation Cards
 */
enum class AudioDevice(val displayName: String, val qemuName: String, val description: String) {
    INTEL_HDA("Intel High Definition Audio", "hda-duplex", "High Definition audio for Win7/10/11 & Linux"),
    AC97("AC97 Audio Codec", "AC97", "Optimal driver support for Windows XP & 2000"),
    SB16("Sound Blaster 16", "sb16", "Iconic sound card for MS-DOS & Windows 95/98 gaming"),
    VIRTIO_SND("VirtIO Sound", "virtio-snd-pci", "Low latency paravirtualized sound for Linux"),
    NONE("Disabled", "none", "Audio emulation disabled")
}

/**
 * Audio Host Backend
 */
enum class AudioBackend(val displayName: String, val qemuBackend: String) {
    AAUDIO("Android AAudio (Low Latency HW)", "aaudio"),
    OPENSL("OpenSL ES", "opensles"),
    SDL("SDL2 Audio Output", "sdl")
}

/**
 * Network Controller
 */
enum class NetworkDevice(val displayName: String, val qemuModel: String) {
    VIRTIO_NET("VirtIO-Net (Hardware Accelerated)", "virtio-net-pci"),
    E1000("Intel Gigabit 82540EM", "e1000"),
    RTL8139("Realtek RTL8139 Fast Ethernet", "rtl8139"),
    NONE("Disabled", "none")
}

/**
 * Storage Bus Interface
 */
enum class StorageInterface(val displayName: String, val qemuBus: String) {
    VIRTIO_BLK("VirtIO Block (Near-Native HW I/O)", "virtio"),
    AHCI_SATA("AHCI SATA Controller", "sata"),
    IDE("IDE / PATA (Legacy PC)", "ide"),
    SCSI("SCSI Controller", "scsi")
}

/**
 * Virtual Hard Disk specification
 */
data class VirtualDisk(
    val id: String,
    val name: String,
    val sizeGb: Double,
    val format: String = "qcow2", // qcow2, raw, vmdk, vdi
    val bus: StorageInterface = StorageInterface.VIRTIO_BLK,
    val filePath: String,
    val isBootable: Boolean = true
)

/**
 * Boot device priority
 */
enum class BootDevice(val displayName: String, val qemuArg: String) {
    HARD_DISK("Hard Disk (c)", "c"),
    CDROM("CD-ROM / ISO (d)", "d"),
    FLOPPY("Floppy Disk (a)", "a"),
    NETWORK("PXE Network (n)", "n")
}

/**
 * Pre-configured OS Templates with optimized GPU & HW Acceleration profiles
 */
enum class VmPresetType(
    val title: String,
    val osName: String,
    val iconCategory: String,
    val arch: VmArchitecture,
    val machineType: String,
    val cpuModel: String,
    val cpuCores: Int,
    val ramMb: Int,
    val gpuDevice: GpuDevice,
    val vramMb: Int,
    val recommendedDiskGb: Double,
    val recommendedDiskFormat: String,
    val audioDevice: AudioDevice,
    val networkDevice: NetworkDevice,
    val description: String
) {
    WIN11_PRO(
        title = "Windows 11 Pro 64-Bit",
        osName = "Windows 11",
        iconCategory = "windows",
        arch = VmArchitecture.X86_64,
        machineType = "q35",
        cpuModel = "Skylake-Client",
        cpuCores = 4,
        ramMb = 4096,
        gpuDevice = GpuDevice.VIRTIO_VGA_GL,
        vramMb = 512,
        recommendedDiskGb = 64.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.INTEL_HDA,
        networkDevice = NetworkDevice.VIRTIO_NET,
        description = "Optimized for Windows 11 with VirtIO-VGA 3D, MTTCG 4 Cores, UEFI, and 512MB VRAM."
    ),
    WIN10_PRO(
        title = "Windows 10 Pro 64-Bit",
        osName = "Windows 10",
        iconCategory = "windows",
        arch = VmArchitecture.X86_64,
        machineType = "q35",
        cpuModel = "qemu64",
        cpuCores = 4,
        ramMb = 4096,
        gpuDevice = GpuDevice.VIRTIO_VGA_GL,
        vramMb = 512,
        recommendedDiskGb = 40.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.INTEL_HDA,
        networkDevice = NetworkDevice.VIRTIO_NET,
        description = "High-performance Windows 10 environment with 3D VirGL GPU and hardware VirtIO-Net."
    ),
    WIN7_ULTIMATE(
        title = "Windows 7 Ultimate 64-Bit",
        osName = "Windows 7",
        iconCategory = "windows",
        arch = VmArchitecture.X86_64,
        machineType = "q35",
        cpuModel = "core2duo",
        cpuCores = 2,
        ramMb = 2048,
        gpuDevice = GpuDevice.VMWARE_SVGA,
        vramMb = 256,
        recommendedDiskGb = 25.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.INTEL_HDA,
        networkDevice = NetworkDevice.E1000,
        description = "Windows 7 with VMware SVGA II 3D Direct3D driver and multi-threaded CPU."
    ),
    WIN_XP(
        title = "Windows XP Professional SP3",
        osName = "Windows XP",
        iconCategory = "windows",
        arch = VmArchitecture.X86,
        machineType = "pc",
        cpuModel = "pentium3",
        cpuCores = 2,
        ramMb = 512,
        gpuDevice = GpuDevice.VMWARE_SVGA,
        vramMb = 128,
        recommendedDiskGb = 10.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.AC97,
        networkDevice = NetworkDevice.RTL8139,
        description = "Lightning-fast Windows XP retro workstation with AC97 audio and 128MB VRAM."
    ),
    WIN98_SE(
        title = "Windows 98 Second Edition",
        osName = "Windows 98",
        iconCategory = "retro",
        arch = VmArchitecture.X86,
        machineType = "pc",
        cpuModel = "pentium2",
        cpuCores = 1,
        ramMb = 128,
        gpuDevice = GpuDevice.CIRRUS,
        vramMb = 32,
        recommendedDiskGb = 4.0,
        recommendedDiskFormat = "raw",
        audioDevice = AudioDevice.SB16,
        networkDevice = NetworkDevice.RTL8139,
        description = "Classic PC gaming setup with Cirrus GD5446 and authentic Sound Blaster 16."
    ),
    UBUNTU_3D(
        title = "Ubuntu Linux 22.04 LTS (3D)",
        osName = "Ubuntu Linux",
        iconCategory = "linux",
        arch = VmArchitecture.X86_64,
        machineType = "q35",
        cpuModel = "host",
        cpuCores = 4,
        ramMb = 4096,
        gpuDevice = GpuDevice.VIRTIO_GPU_GL,
        vramMb = 512,
        recommendedDiskGb = 25.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.INTEL_HDA,
        networkDevice = NetworkDevice.VIRTIO_NET,
        description = "Full Linux desktop with VirGL 3D hardware acceleration via host OpenGL ES 3.2."
    ),
    ALPINE_LINUX(
        title = "Alpine Linux (Ultra-Fast)",
        osName = "Alpine Linux",
        iconCategory = "linux",
        arch = VmArchitecture.X86_64,
        machineType = "q35",
        cpuModel = "qemu64",
        cpuCores = 2,
        ramMb = 512,
        gpuDevice = GpuDevice.VIRTIO_GPU_2D,
        vramMb = 64,
        recommendedDiskGb = 2.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.NONE,
        networkDevice = NetworkDevice.VIRTIO_NET,
        description = "Super lightweight 50MB Linux system boots in 2 seconds with VirtIO block and net."
    ),
    REACTOS_3D(
        title = "ReactOS 0.4.14 (Open-Source Win)",
        osName = "ReactOS",
        iconCategory = "windows",
        arch = VmArchitecture.X86,
        machineType = "pc",
        cpuModel = "pentium3",
        cpuCores = 2,
        ramMb = 1024,
        gpuDevice = GpuDevice.VMWARE_SVGA,
        vramMb = 128,
        recommendedDiskGb = 8.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.AC97,
        networkDevice = NetworkDevice.RTL8139,
        description = "Open-source Windows compatible operating system with 3D graphics and low memory footprint."
    ),
    FREEDOS_13(
        title = "FreeDOS 1.3 (Classic DOS Gaming)",
        osName = "FreeDOS",
        iconCategory = "dos",
        arch = VmArchitecture.X86,
        machineType = "pc",
        cpuModel = "486",
        cpuCores = 1,
        ramMb = 64,
        gpuDevice = GpuDevice.STD_VGA,
        vramMb = 32,
        recommendedDiskGb = 1.0,
        recommendedDiskFormat = "raw",
        audioDevice = AudioDevice.SB16,
        networkDevice = NetworkDevice.NONE,
        description = "Pure DOS gaming environment with Sound Blaster 16 and VESA VGA support."
    ),
    ANDROID_X86(
        title = "Android-x86 9.0 Pie (3D GPU)",
        osName = "Android-x86",
        iconCategory = "android",
        arch = VmArchitecture.X86_64,
        machineType = "q35",
        cpuModel = "host",
        cpuCores = 4,
        ramMb = 3072,
        gpuDevice = GpuDevice.VIRTIO_GPU_GL,
        vramMb = 512,
        recommendedDiskGb = 16.0,
        recommendedDiskFormat = "qcow2",
        audioDevice = AudioDevice.INTEL_HDA,
        networkDevice = NetworkDevice.VIRTIO_NET,
        description = "Android inside Android with native VirGL GLES 3.2 hardware graphics acceleration."
    )
}

/**
 * Complete Virtual Machine Configuration Model
 */
data class MachineConfig(
    val id: String,
    val name: String,
    val arch: VmArchitecture = VmArchitecture.X86_64,
    val machineType: String = "q35",
    val cpuModel: String = "qemu64",
    val cpuCores: Int = 4,
    val ramMb: Int = 2048,
    val gpuDevice: GpuDevice = GpuDevice.VIRTIO_VGA_GL,
    val hardwareAccel: HardwareAccelConfig = HardwareAccelConfig(),
    val audioDevice: AudioDevice = AudioDevice.INTEL_HDA,
    val audioBackend: AudioBackend = AudioBackend.AAUDIO,
    val networkDevice: NetworkDevice = NetworkDevice.VIRTIO_NET,
    val portForwarding: String = "tcp:2222-22,tcp:8080-80",
    val bootDevice: BootDevice = BootDevice.HARD_DISK,
    val hardDisks: List<VirtualDisk> = emptyList(),
    val cdromIso: String? = null,
    val floppyImg: String? = null,
    val sharedFolder: String? = null,
    val extraQemuParams: String = "",
    val presetType: VmPresetType? = null,
    val createdAt: Long = System.currentTimeMillis()
)
