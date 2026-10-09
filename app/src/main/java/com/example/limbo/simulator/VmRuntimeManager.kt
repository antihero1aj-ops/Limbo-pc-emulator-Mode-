package com.example.limbo.simulator

import android.os.SystemClock
import com.example.limbo.model.GpuDevice
import com.example.limbo.model.MachineConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class VmExecutionStatus {
    STOPPED,
    STARTING_BIOS,
    INITIALIZING_GPU,
    RUNNING,
    PAUSED,
    SAVING_SNAPSHOT,
    ERROR
}

data class VmTelemetry(
    val fps: Float = 0f,
    val gpuLoadPercent: Int = 0,
    val virglDrawCalls: Int = 0,
    val vramUsedMb: Int = 0,
    val cpuCoreLoads: List<Int> = emptyList(),
    val ramUsedMb: Int = 0,
    val diskReadKbps: Int = 0,
    val diskWriteKbps: Int = 0,
    val uptimeSeconds: Long = 0,
    val activeResolution: String = "1920x1080 @ 60Hz"
)

data class VmRuntimeSnapshot(
    val id: String,
    val name: String,
    val timestamp: Long,
    val memoryUsageMb: Int,
    val previewDescription: String
)

class VmRuntimeManager(
    private val scope: CoroutineScope
) {
    private val _status = MutableStateFlow(VmExecutionStatus.STOPPED)
    val status: StateFlow<VmExecutionStatus> = _status.asStateFlow()

    private val _activeConfig = MutableStateFlow<MachineConfig?>(null)
    val activeConfig: StateFlow<MachineConfig?> = _activeConfig.asStateFlow()

    private val _telemetry = MutableStateFlow(VmTelemetry())
    val telemetry: StateFlow<VmTelemetry> = _telemetry.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<String>>(emptyList())
    val consoleLogs: StateFlow<List<String>> = _consoleLogs.asStateFlow()

    private val _snapshots = MutableStateFlow<List<VmRuntimeSnapshot>>(emptyList())
    val snapshots: StateFlow<List<VmRuntimeSnapshot>> = _snapshots.asStateFlow()

    // 3D Engine state for guest simulation
    private val _cubeRotationAngle = MutableStateFlow(0f)
    val cubeRotationAngle: StateFlow<Float> = _cubeRotationAngle.asStateFlow()

    private val _gearRotationAngle = MutableStateFlow(0f)
    val gearRotationAngle: StateFlow<Float> = _gearRotationAngle.asStateFlow()

    // Virtual Mouse Pointer position (normalized 0.0 .. 1.0)
    private val _cursorX = MutableStateFlow(0.5f)
    val cursorX: StateFlow<Float> = _cursorX.asStateFlow()

    private val _cursorY = MutableStateFlow(0.5f)
    val cursorY: StateFlow<Float> = _cursorY.asStateFlow()

    // Active Window in Simulation
    private val _activeSimWindow = MutableStateFlow("3D_BENCHMARK") // 3D_BENCHMARK, TERMINAL, EXPLORER
    val activeSimWindow: StateFlow<String> = _activeSimWindow.asStateFlow()

    // FreeDOS command history
    private val _dosHistory = MutableStateFlow(
        listOf(
            "LIMBO PC MS-DOS 7.10 / FreeDOS Kernel 1.3",
            "Hardware: 3D VirtIO-VGA Adapter / Sound Blaster 16",
            "Drive C: mounted as FAT32 virtual volume (1024 MB)",
            "Type 'HELP' or '3DDEMO' to launch GPU test."
        )
    )
    val dosHistory: StateFlow<List<String>> = _dosHistory.asStateFlow()

    private var simulationJob: Job? = null
    private var telemetryJob: Job? = null
    private var startTimestamp = 0L

    fun startVm(config: MachineConfig) {
        if (_status.value != VmExecutionStatus.STOPPED) return

        _activeConfig.value = config
        _consoleLogs.value = emptyList()
        addLog("[LIMBO] Initializing QEMU Engine for VM '${config.name}' (${config.arch.displayName})")
        addLog("[QEMU] Command line: ${QemuCommandLineBuilder.buildCommandLine(config)}")

        simulationJob?.cancel()
        telemetryJob?.cancel()

        simulationJob = scope.launch(Dispatchers.Default) {
            // Stage 1: BIOS POST
            _status.value = VmExecutionStatus.STARTING_BIOS
            addLog("[BIOS] POST: Limbo Modern PC BIOS v6.1.0 initialized")
            addLog("[BIOS] CPU: ${config.cpuModel} x ${config.cpuCores} Cores detected")
            addLog("[BIOS] RAM Check: ${config.ramMb} MB High-Speed Guest Memory OK")
            delay(900)

            // Stage 2: 3D GPU & Hardware Acceleration Initialization
            _status.value = VmExecutionStatus.INITIALIZING_GPU
            if (config.gpuDevice.supportsVirgl && config.hardwareAccel.enableVirgl3d) {
                addLog("[GPU] Host OpenGL ES 3.2 Pipeline: INITIALIZED")
                addLog("[GPU] VirGL 3D Driver: ACTIVE (${config.hardwareAccel.vramMb} MB VRAM)")
                addLog("[GPU] Hardware Texture Filter: ${config.hardwareAccel.scalingFilter.displayName}")
                addLog("[HW-ACCEL] Multi-Threaded TCG (MTTCG) bound to ${config.cpuCores} host cores")
            } else {
                addLog("[GPU] Emulated GPU: ${config.gpuDevice.displayName} (${config.hardwareAccel.vramMb} MB VRAM)")
            }
            delay(1100)

            // Stage 3: Running OS
            _status.value = VmExecutionStatus.RUNNING
            startTimestamp = SystemClock.elapsedRealtime()
            addLog("[KERNEL] Bootloader handing over to guest OS...")
            addLog("[QMP] QEMU Monitor Protocol listening on socket.")

            startTelemetryLoop(config)
        }
    }

    private fun startTelemetryLoop(config: MachineConfig) {
        telemetryJob = scope.launch(Dispatchers.Default) {
            val numCores = config.cpuCores
            val targetFps = config.hardwareAccel.targetFps.toFloat()

            while (isActive && _status.value == VmExecutionStatus.RUNNING) {
                // Update 3D Rotation Animation
                _cubeRotationAngle.value = (_cubeRotationAngle.value + 2.5f) % 360f
                _gearRotationAngle.value = (_gearRotationAngle.value + 4.0f) % 360f

                val uptime = (SystemClock.elapsedRealtime() - startTimestamp) / 1000
                val jitter = (Random.nextFloat() - 0.5f) * 1.8f
                val currentFps = (targetFps + jitter).coerceIn(24f, 120f)

                val coreLoads = List(numCores) {
                    Random.nextInt(15, 65)
                }

                val is3dActive = config.gpuDevice.supportsVirgl && config.hardwareAccel.enableVirgl3d
                val gpuLoad = if (is3dActive) Random.nextInt(35, 78) else Random.nextInt(8, 25)
                val drawCalls = if (is3dActive) Random.nextInt(1200, 3400) else Random.nextInt(80, 240)
                val vramUsage = if (is3dActive) {
                    (config.hardwareAccel.vramMb * 0.42f + Random.nextInt(5, 25)).toInt()
                } else {
                    (config.hardwareAccel.vramMb * 0.20f).toInt()
                }

                val ramUsed = (config.ramMb * 0.55f + Random.nextInt(10, 40)).toInt()

                _telemetry.value = VmTelemetry(
                    fps = currentFps,
                    gpuLoadPercent = gpuLoad,
                    virglDrawCalls = drawCalls,
                    vramUsedMb = vramUsage,
                    cpuCoreLoads = coreLoads,
                    ramUsedMb = ramUsed,
                    diskReadKbps = Random.nextInt(120, 2400),
                    diskWriteKbps = Random.nextInt(40, 850),
                    uptimeSeconds = uptime,
                    activeResolution = "1920x1080 @ ${config.hardwareAccel.targetFps}Hz"
                )

                delay(300)
            }
        }
    }

    fun pauseVm() {
        if (_status.value == VmExecutionStatus.RUNNING) {
            _status.value = VmExecutionStatus.PAUSED
            addLog("[QMP] VM paused by user.")
        }
    }

    fun resumeVm() {
        if (_status.value == VmExecutionStatus.PAUSED) {
            _status.value = VmExecutionStatus.RUNNING
            addLog("[QMP] VM resumed.")
            _activeConfig.value?.let { startTelemetryLoop(it) }
        }
    }

    fun resetVm() {
        _activeConfig.value?.let { cfg ->
            addLog("[QMP] Hard reset triggered (Ctrl+Alt+Del).")
            simulationJob?.cancel()
            telemetryJob?.cancel()
            _status.value = VmExecutionStatus.STOPPED
            startVm(cfg)
        }
    }

    fun stopVm() {
        simulationJob?.cancel()
        telemetryJob?.cancel()
        _status.value = VmExecutionStatus.STOPPED
        addLog("[QEMU] Guest ACPI Power Off. Process terminated cleanly.")
    }

    fun saveSnapshot(name: String) {
        scope.launch {
            val prevStatus = _status.value
            _status.value = VmExecutionStatus.SAVING_SNAPSHOT
            addLog("[SNAPSHOT] Capturing RAM state and VirGL 3D texture cache...")
            delay(700)
            val snapshot = VmRuntimeSnapshot(
                id = "snap_${System.currentTimeMillis()}",
                name = name.ifBlank { "Checkpoint #${_snapshots.value.size + 1}" },
                timestamp = System.currentTimeMillis(),
                memoryUsageMb = _telemetry.value.ramUsedMb,
                previewDescription = "${_activeConfig.value?.name ?: "VM"} state with 3D VirGL GPU context"
            )
            _snapshots.value = _snapshots.value + snapshot
            addLog("[SNAPSHOT] Checkpoint '${snapshot.name}' saved successfully.")
            _status.value = prevStatus
        }
    }

    fun restoreSnapshot(snapshot: VmRuntimeSnapshot) {
        scope.launch {
            addLog("[SNAPSHOT] Restoring state '${snapshot.name}'...")
            delay(600)
            addLog("[SNAPSHOT] State restored successfully.")
        }
    }

    fun setCursorPos(normalizedX: Float, normalizedY: Float) {
        _cursorX.value = normalizedX.coerceIn(0.02f, 0.98f)
        _cursorY.value = normalizedY.coerceIn(0.02f, 0.98f)
    }

    fun moveCursorDelta(dx: Float, dy: Float) {
        _cursorX.value = (_cursorX.value + dx).coerceIn(0.02f, 0.98f)
        _cursorY.value = (_cursorY.value + dy).coerceIn(0.02f, 0.98f)
    }

    fun sendMouseClick(button: String) {
        addLog("[INPUT] Mouse $button at (${(_cursorX.value * 1920).toInt()}, ${(_cursorY.value * 1080).toInt()})")
    }

    fun sendKeyPress(keyLabel: String) {
        addLog("[INPUT] VirtIO-Keyboard Key: $keyLabel")
        if (keyLabel == "Ctrl+Alt+Del") {
            addLog("[INPUT] Sent ACPI Reboot signal.")
        }
    }

    fun executeDosCommand(cmd: String) {
        val trimmed = cmd.trim().uppercase()
        val current = _dosHistory.value.toMutableList()
        current.add("C:\\> $cmd")

        when {
            trimmed == "DIR" -> {
                current.add(" Volume in drive C has no label.")
                current.add(" Volume Serial Number is 1F2D-8B4A")
                current.add(" Directory of C:\\")
                current.add(" COMMAND  COM        93,040  06-11-23  11:00a")
                current.add(" AUTOEXEC BAT           182  06-11-23  11:00a")
                current.add(" CONFIG   SYS           240  06-11-23  11:00a")
                current.add(" VIRGL3D  EXE       245,760  06-11-23  02:15p")
                current.add(" BENCH3D  EXE       184,320  06-11-23  03:40p")
                current.add(" GLDEMO   EXE       312,500  06-11-23  04:20p")
                current.add("        6 file(s)        836,042 bytes")
                current.add("        0 dir(s)     984,210,432 bytes free")
            }
            trimmed == "VER" -> {
                current.add("Limbo PC Simulator [Version 6.1.0-GPU Accelerated]")
            }
            trimmed.startsWith("3DDEMO") || trimmed.startsWith("GLDEMO") -> {
                current.add("Initializing VirtIO-VGA 3D (VirGL OpenGL ES)...")
                current.add("Hardware acceleration: 3D Wireframe Pipeline ACTIVE.")
                _activeSimWindow.value = "3D_BENCHMARK"
            }
            trimmed == "BENCH3D" -> {
                current.add("Running Limbo GPU 3D Benchmark:")
                current.add("Host GLES 3.2: 60.0 FPS | Draw calls: 2450/sec | VRAM: OK")
            }
            trimmed == "CLS" -> {
                current.clear()
                current.add("C:\\>")
            }
            trimmed == "HELP" -> {
                current.add("Available commands: DIR, VER, 3DDEMO, GLDEMO, BENCH3D, CLS, HELP")
            }
            else -> {
                current.add("Bad command or file name: $cmd")
            }
        }
        _dosHistory.value = current
    }

    fun executeQmpCommand(command: String): String {
        val cmd = command.trim()
        addLog("[QMP Console] > $cmd")
        val response = when (cmd.lowercase()) {
            "info status" -> "VM status: ${_status.value} (running=${_status.value == VmExecutionStatus.RUNNING})"
            "info pci" -> """
                Bus  0, device   1, function 0:
                  VGA controller: PCI device 1af4:1050 (VirtIO-GPU 3D VirGL)
                    IRQ 11, pin A
                    BAR0: 64-bit prefetchable memory at 0x80000000 [0x8fffffff]
                Bus  0, device   2, function 0:
                  Ethernet controller: PCI device 1af4:1000 (VirtIO-Net)
                Bus  0, device   3, function 0:
                  SCSI storage controller: PCI device 1af4:1001 (VirtIO-Blk)
            """.trimIndent()
            "info block" -> """
                drive0: file=c_drive.qcow2 (qcow2, 40 GiB)
                  ro=0, drv='qcow2', encrypted=0
                cdrom0: file=win10.iso (raw, read-only)
            """.trimIndent()
            "info registers" -> """
                EAX=00000001 EBX=00000000 ECX=00000000 EDX=00000670
                ESI=00000000 EDI=00000000 EBP=00007c00 ESP=00007c00
                EIP=00000020 EFL=00000002 [-------]
            """.trimIndent()
            "virgl stats" -> """
                VirGL 3D Acceleration Statistics:
                  Renderer: Host OpenGL ES 3.2 (Direct HW Pipe)
                  Allocated Textures: 142
                  Draw calls / sec: ${_telemetry.value.virglDrawCalls}
                  VRAM: ${_telemetry.value.vramUsedMb} MB / ${_activeConfig.value?.hardwareAccel?.vramMb ?: 512} MB
                  GPU Load: ${_telemetry.value.gpuLoadPercent}%
                  Hardware Shader Cache: HIT (98.4%)
            """.trimIndent()
            "stop" -> {
                pauseVm()
                "VM execution suspended."
            }
            "cont" -> {
                resumeVm()
                "VM execution resumed."
            }
            "system_reset" -> {
                resetVm()
                "System rebooted."
            }
            "help" -> "Commands: info status, info pci, info block, info registers, virgl stats, stop, cont, system_reset"
            else -> "Unknown command: '$cmd'. Type 'help' for command list."
        }
        addLog("[QMP Console] $response")
        return response
    }

    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        _consoleLogs.value = (_consoleLogs.value + "[$timestamp] $message").takeLast(100)
    }
}
