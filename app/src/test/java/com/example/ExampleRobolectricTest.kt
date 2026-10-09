package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.limbo.data.VmRepository
import com.example.limbo.model.GpuDevice
import com.example.limbo.model.VmPresetType
import com.example.limbo.simulator.QemuCommandLineBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Limbo PC", appName)
  }

  @Test
  fun `vm repository initializes default machines with GPU acceleration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = VmRepository(context)
    val vms = repo.vms.value
    assertTrue("Default VMs should not be empty", vms.isNotEmpty())

    val win11 = vms.firstOrNull { it.id == "vm_win11_gpu" }
    assertTrue("Windows 11 GPU machine should exist", win11 != null)
    assertTrue("Windows 11 should have VirGL 3D enabled", win11!!.hardwareAccel.enableVirgl3d)
    assertEquals(GpuDevice.VIRTIO_VGA_GL, win11.gpuDevice)

    // Verify QEMU CLI generator produces VirGL 3D flags
    val cli = QemuCommandLineBuilder.buildCommandLine(win11)
    assertTrue("CLI must contain virgl=on", cli.contains("virgl=on"))
    assertTrue("CLI must contain gl=on", cli.contains("gl=on"))
    assertTrue("CLI must contain thread=multi (MTTCG)", cli.contains("thread=multi"))
  }
}
