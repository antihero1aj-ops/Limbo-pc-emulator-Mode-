package com.example.limbo.data

import android.content.Context
import android.content.SharedPreferences
import com.example.limbo.model.AudioDevice
import com.example.limbo.model.GpuDevice
import com.example.limbo.model.HardwareAccelConfig
import com.example.limbo.model.MachineConfig
import com.example.limbo.model.NetworkDevice
import com.example.limbo.model.StorageInterface
import com.example.limbo.model.VirtualDisk
import com.example.limbo.model.VmArchitecture
import com.example.limbo.model.VmPresetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class VmRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("limbo_vm_configs", Context.MODE_PRIVATE)

    private val _vms = MutableStateFlow<List<MachineConfig>>(emptyList())
    val vms: StateFlow<List<MachineConfig>> = _vms.asStateFlow()

    init {
        loadVms()
    }

    private fun loadVms() {
        val jsonString = prefs.getString("vms_list", null)
        if (jsonString != null) {
            try {
                val loaded = parseVmsJson(jsonString)
                if (loaded.isNotEmpty()) {
                    _vms.value = loaded
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Initialize default machines if first launch
        val defaultList = createDefaultMachines()
        saveVmsList(defaultList)
    }

    fun saveVm(config: MachineConfig) {
        val current = _vms.value.toMutableList()
        val index = current.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            current[index] = config
        } else {
            current.add(0, config)
        }
        saveVmsList(current)
    }

    fun deleteVm(id: String) {
        val updated = _vms.value.filter { it.id != id }
        saveVmsList(updated)
    }

    fun cloneVm(id: String): MachineConfig? {
        val original = _vms.value.find { it.id == id } ?: return null
        val cloned = original.copy(
            id = UUID.randomUUID().toString(),
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis()
        )
        saveVm(cloned)
        return cloned
    }

    fun createFromPreset(preset: VmPresetType): MachineConfig {
        val diskId = UUID.randomUUID().toString()
        val diskPath = File(context.filesDir, "${preset.name.lowercase()}_disk.qcow2").absolutePath
        val primaryDisk = VirtualDisk(
            id = diskId,
            name = "${preset.osName} Hard Disk",
            sizeGb = preset.recommendedDiskGb,
            format = preset.recommendedDiskFormat,
            bus = StorageInterface.VIRTIO_BLK,
            filePath = diskPath,
            isBootable = true
        )

        val vm = MachineConfig(
            id = UUID.randomUUID().toString(),
            name = preset.title,
            arch = preset.arch,
            machineType = preset.machineType,
            cpuModel = preset.cpuModel,
            cpuCores = preset.cpuCores,
            ramMb = preset.ramMb,
            gpuDevice = preset.gpuDevice,
            hardwareAccel = HardwareAccelConfig(
                enableKvm = true,
                enableMttcg = true,
                enableVirgl3d = preset.gpuDevice.supportsVirgl,
                vramMb = preset.vramMb
            ),
            audioDevice = preset.audioDevice,
            networkDevice = preset.networkDevice,
            hardDisks = listOf(primaryDisk),
            presetType = preset
        )
        saveVm(vm)
        return vm
    }

    private fun saveVmsList(list: List<MachineConfig>) {
        _vms.value = list
        try {
            val jsonArray = JSONArray()
            for (vm in list) {
                jsonArray.put(vmToJson(vm))
            }
            prefs.edit().putString("vms_list", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vmToJson(vm: MachineConfig): JSONObject {
        val obj = JSONObject()
        obj.put("id", vm.id)
        obj.put("name", vm.name)
        obj.put("arch", vm.arch.name)
        obj.put("machineType", vm.machineType)
        obj.put("cpuModel", vm.cpuModel)
        obj.put("cpuCores", vm.cpuCores)
        obj.put("ramMb", vm.ramMb)
        obj.put("gpuDevice", vm.gpuDevice.name)

        val accelObj = JSONObject()
        accelObj.put("enableKvm", vm.hardwareAccel.enableKvm)
        accelObj.put("enableMttcg", vm.hardwareAccel.enableMttcg)
        accelObj.put("enableVirgl3d", vm.hardwareAccel.enableVirgl3d)
        accelObj.put("enableVenusVulkan", vm.hardwareAccel.enableVenusVulkan)
        accelObj.put("enableShaderCache", vm.hardwareAccel.enableShaderCache)
        accelObj.put("jitCacheMb", vm.hardwareAccel.jitCacheMb)
        accelObj.put("vramMb", vm.hardwareAccel.vramMb)
        accelObj.put("rendererMode", vm.hardwareAccel.rendererMode.name)
        accelObj.put("scalingFilter", vm.hardwareAccel.scalingFilter.name)
        accelObj.put("targetFps", vm.hardwareAccel.targetFps)
        accelObj.put("vsyncEnabled", vm.hardwareAccel.vsyncEnabled)
        obj.put("hardwareAccel", accelObj)

        obj.put("audioDevice", vm.audioDevice.name)
        obj.put("audioBackend", vm.audioBackend.name)
        obj.put("networkDevice", vm.networkDevice.name)
        obj.put("portForwarding", vm.portForwarding)
        obj.put("bootDevice", vm.bootDevice.name)
        obj.put("cdromIso", vm.cdromIso ?: "")
        obj.put("floppyImg", vm.floppyImg ?: "")
        obj.put("sharedFolder", vm.sharedFolder ?: "")
        obj.put("extraQemuParams", vm.extraQemuParams)
        obj.put("presetType", vm.presetType?.name ?: "")
        obj.put("createdAt", vm.createdAt)

        val disksArray = JSONArray()
        for (disk in vm.hardDisks) {
            val dObj = JSONObject()
            dObj.put("id", disk.id)
            dObj.put("name", disk.name)
            dObj.put("sizeGb", disk.sizeGb)
            dObj.put("format", disk.format)
            dObj.put("bus", disk.bus.name)
            dObj.put("filePath", disk.filePath)
            dObj.put("isBootable", disk.isBootable)
            disksArray.put(dObj)
        }
        obj.put("hardDisks", disksArray)

        return obj
    }

    private fun parseVmsJson(jsonString: String): List<MachineConfig> {
        val list = mutableListOf<MachineConfig>()
        val jsonArray = JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.getString("id")
            val name = obj.getString("name")
            val arch = try { VmArchitecture.valueOf(obj.getString("arch")) } catch (_: Exception) { VmArchitecture.X86_64 }
            val machineType = obj.optString("machineType", "q35")
            val cpuModel = obj.optString("cpuModel", "qemu64")
            val cpuCores = obj.optInt("cpuCores", 4)
            val ramMb = obj.optInt("ramMb", 2048)
            val gpuDevice = try { GpuDevice.valueOf(obj.getString("gpuDevice")) } catch (_: Exception) { GpuDevice.VIRTIO_VGA_GL }

            val accelObj = obj.optJSONObject("hardwareAccel")
            val hardwareAccel = if (accelObj != null) {
                HardwareAccelConfig(
                    enableKvm = accelObj.optBoolean("enableKvm", true),
                    enableMttcg = accelObj.optBoolean("enableMttcg", true),
                    enableVirgl3d = accelObj.optBoolean("enableVirgl3d", true),
                    enableVenusVulkan = accelObj.optBoolean("enableVenusVulkan", false),
                    enableShaderCache = accelObj.optBoolean("enableShaderCache", true),
                    jitCacheMb = accelObj.optInt("jitCacheMb", 256),
                    vramMb = accelObj.optInt("vramMb", 512),
                    targetFps = accelObj.optInt("targetFps", 60),
                    vsyncEnabled = accelObj.optBoolean("vsyncEnabled", true)
                )
            } else {
                HardwareAccelConfig()
            }

            val audioDevice = try { AudioDevice.valueOf(obj.optString("audioDevice", "INTEL_HDA")) } catch (_: Exception) { AudioDevice.INTEL_HDA }
            val networkDevice = try { NetworkDevice.valueOf(obj.optString("networkDevice", "VIRTIO_NET")) } catch (_: Exception) { NetworkDevice.VIRTIO_NET }

            val disksList = mutableListOf<VirtualDisk>()
            val disksArray = obj.optJSONArray("hardDisks")
            if (disksArray != null) {
                for (j in 0 until disksArray.length()) {
                    val d = disksArray.getJSONObject(j)
                    disksList.add(
                        VirtualDisk(
                            id = d.optString("id", UUID.randomUUID().toString()),
                            name = d.optString("name", "Virtual Disk"),
                            sizeGb = d.optDouble("sizeGb", 20.0),
                            format = d.optString("format", "qcow2"),
                            bus = try { StorageInterface.valueOf(d.optString("bus", "VIRTIO_BLK")) } catch (_: Exception) { StorageInterface.VIRTIO_BLK },
                            filePath = d.optString("filePath", ""),
                            isBootable = d.optBoolean("isBootable", true)
                        )
                    )
                }
            }

            val presetType = try {
                val pStr = obj.optString("presetType", "")
                if (pStr.isNotBlank()) VmPresetType.valueOf(pStr) else null
            } catch (_: Exception) {
                null
            }

            list.add(
                MachineConfig(
                    id = id,
                    name = name,
                    arch = arch,
                    machineType = machineType,
                    cpuModel = cpuModel,
                    cpuCores = cpuCores,
                    ramMb = ramMb,
                    gpuDevice = gpuDevice,
                    hardwareAccel = hardwareAccel,
                    audioDevice = audioDevice,
                    networkDevice = networkDevice,
                    portForwarding = obj.optString("portForwarding", "tcp:2222-22"),
                    hardDisks = disksList,
                    cdromIso = obj.optString("cdromIso").ifBlank { null },
                    floppyImg = obj.optString("floppyImg").ifBlank { null },
                    sharedFolder = obj.optString("sharedFolder").ifBlank { null },
                    extraQemuParams = obj.optString("extraQemuParams", ""),
                    presetType = presetType,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun createDefaultMachines(): List<MachineConfig> {
        val list = mutableListOf<MachineConfig>()

        // 1. Windows 11 Pro 64-Bit (VirGL 3D GPU)
        list.add(
            MachineConfig(
                id = "vm_win11_gpu",
                name = "Windows 11 Pro (3D GPU)",
                arch = VmArchitecture.X86_64,
                machineType = "q35",
                cpuModel = "Skylake-Client",
                cpuCores = 4,
                ramMb = 4096,
                gpuDevice = GpuDevice.VIRTIO_VGA_GL,
                hardwareAccel = HardwareAccelConfig(
                    enableKvm = true,
                    enableMttcg = true,
                    enableVirgl3d = true,
                    vramMb = 512,
                    jitCacheMb = 256,
                    targetFps = 60
                ),
                audioDevice = AudioDevice.INTEL_HDA,
                networkDevice = NetworkDevice.VIRTIO_NET,
                hardDisks = listOf(
                    VirtualDisk(
                        id = "d_win11",
                        name = "Windows11_System.qcow2",
                        sizeGb = 64.0,
                        format = "qcow2",
                        bus = StorageInterface.VIRTIO_BLK,
                        filePath = File(context.filesDir, "win11.qcow2").absolutePath,
                        isBootable = true
                    )
                ),
                presetType = VmPresetType.WIN11_PRO
            )
        )

        // 2. Ubuntu Linux 22.04 LTS (Native VirGL 3D)
        list.add(
            MachineConfig(
                id = "vm_ubuntu_virgl",
                name = "Ubuntu Linux 22.04 (VirGL 3D)",
                arch = VmArchitecture.X86_64,
                machineType = "q35",
                cpuModel = "host",
                cpuCores = 4,
                ramMb = 4096,
                gpuDevice = GpuDevice.VIRTIO_GPU_GL,
                hardwareAccel = HardwareAccelConfig(
                    enableKvm = true,
                    enableMttcg = true,
                    enableVirgl3d = true,
                    vramMb = 512,
                    jitCacheMb = 256,
                    targetFps = 60
                ),
                audioDevice = AudioDevice.INTEL_HDA,
                networkDevice = NetworkDevice.VIRTIO_NET,
                hardDisks = listOf(
                    VirtualDisk(
                        id = "d_ubuntu",
                        name = "Ubuntu2204_Root.qcow2",
                        sizeGb = 32.0,
                        format = "qcow2",
                        bus = StorageInterface.VIRTIO_BLK,
                        filePath = File(context.filesDir, "ubuntu.qcow2").absolutePath,
                        isBootable = true
                    )
                ),
                presetType = VmPresetType.UBUNTU_3D
            )
        )

        // 3. Windows XP Professional (VMware SVGA 3D)
        list.add(
            MachineConfig(
                id = "vm_winxp_3d",
                name = "Windows XP SP3 (Direct3D)",
                arch = VmArchitecture.X86,
                machineType = "pc",
                cpuModel = "pentium3",
                cpuCores = 2,
                ramMb = 1024,
                gpuDevice = GpuDevice.VMWARE_SVGA,
                hardwareAccel = HardwareAccelConfig(
                    enableKvm = false,
                    enableMttcg = true,
                    enableVirgl3d = false,
                    vramMb = 128,
                    jitCacheMb = 128,
                    targetFps = 60
                ),
                audioDevice = AudioDevice.AC97,
                networkDevice = NetworkDevice.RTL8139,
                hardDisks = listOf(
                    VirtualDisk(
                        id = "d_winxp",
                        name = "WinXP_System.qcow2",
                        sizeGb = 10.0,
                        format = "qcow2",
                        bus = StorageInterface.IDE,
                        filePath = File(context.filesDir, "winxp.qcow2").absolutePath,
                        isBootable = true
                    )
                ),
                presetType = VmPresetType.WIN_XP
            )
        )

        // 4. FreeDOS 1.3 (Classic DOS Gaming)
        list.add(
            MachineConfig(
                id = "vm_freedos",
                name = "FreeDOS 1.3 (Retro PC)",
                arch = VmArchitecture.X86,
                machineType = "pc",
                cpuModel = "486",
                cpuCores = 1,
                ramMb = 64,
                gpuDevice = GpuDevice.STD_VGA,
                hardwareAccel = HardwareAccelConfig(
                    enableKvm = false,
                    enableMttcg = false,
                    enableVirgl3d = false,
                    vramMb = 32,
                    jitCacheMb = 64,
                    targetFps = 60
                ),
                audioDevice = AudioDevice.SB16,
                networkDevice = NetworkDevice.NONE,
                hardDisks = listOf(
                    VirtualDisk(
                        id = "d_freedos",
                        name = "FreeDOS_C_Drive.raw",
                        sizeGb = 1.0,
                        format = "raw",
                        bus = StorageInterface.IDE,
                        filePath = File(context.filesDir, "freedos.raw").absolutePath,
                        isBootable = true
                    )
                ),
                presetType = VmPresetType.FREEDOS_13
            )
        )

        return list
    }
}
