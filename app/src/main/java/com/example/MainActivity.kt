package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.limbo.data.VmRepository
import com.example.limbo.model.MachineConfig
import com.example.limbo.model.VmPresetType
import com.example.limbo.simulator.HostHardwareDetector
import com.example.limbo.simulator.VmRuntimeManager
import com.example.limbo.ui.dialogs.CreateCustomVmBottomSheet
import com.example.limbo.ui.dialogs.TemplatesBottomSheet
import com.example.limbo.ui.screens.HostDiagnosticsScreen
import com.example.limbo.ui.screens.LimboHomeScreen
import com.example.limbo.ui.screens.VmConfigScreen
import com.example.limbo.ui.screens.VmRuntimeScreen
import com.example.ui.theme.LimboBgDark
import com.example.ui.theme.MyApplicationTheme

enum class AppNavScreen {
    HOME,
    CONFIG,
    RUNTIME,
    DIAGNOSTICS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = VmRepository(this)
        val hostProfile = HostHardwareDetector.detect(this)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LimboBgDark
                ) {
                    LimboApp(
                        repository = repository,
                        hostProfile = hostProfile
                    )
                }
            }
        }
    }
}

@Composable
fun LimboApp(
    repository: VmRepository,
    hostProfile: com.example.limbo.simulator.HostHardwareProfile
) {
    val coroutineScope = rememberCoroutineScope()
    val runtimeManager = remember { VmRuntimeManager(coroutineScope) }

    val vms by repository.vms.collectAsState()

    var currentScreen by remember { mutableStateOf(AppNavScreen.HOME) }
    var selectedVmForConfig by remember { mutableStateOf<MachineConfig?>(null) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var showCreateCustomDialog by remember { mutableStateOf(false) }

    // Handle Back Press navigation
    BackHandler(enabled = currentScreen != AppNavScreen.HOME) {
        when (currentScreen) {
            AppNavScreen.CONFIG, AppNavScreen.DIAGNOSTICS -> {
                currentScreen = AppNavScreen.HOME
            }
            AppNavScreen.RUNTIME -> {
                currentScreen = AppNavScreen.HOME
            }
            AppNavScreen.HOME -> {
                // Exit
            }
        }
    }

    when (currentScreen) {
        AppNavScreen.HOME -> {
            LimboHomeScreen(
                vms = vms,
                hostProfile = hostProfile,
                onStartVm = { vm ->
                    runtimeManager.startVm(vm)
                    currentScreen = AppNavScreen.RUNTIME
                },
                onEditVm = { vm ->
                    selectedVmForConfig = vm
                    currentScreen = AppNavScreen.CONFIG
                },
                onCloneVm = { id ->
                    repository.cloneVm(id)
                },
                onDeleteVm = { id ->
                    repository.deleteVm(id)
                },
                onOpenTemplates = {
                    showTemplatesDialog = true
                },
                onCreateCustomVm = {
                    showCreateCustomDialog = true
                },
                onOpenDiagnostics = {
                    currentScreen = AppNavScreen.DIAGNOSTICS
                }
            )
        }
        AppNavScreen.CONFIG -> {
            selectedVmForConfig?.let { cfg ->
                VmConfigScreen(
                    initialConfig = cfg,
                    onSaveConfig = { updated ->
                        repository.saveVm(updated)
                        selectedVmForConfig = updated
                    },
                    onStartVm = { updated ->
                        repository.saveVm(updated)
                        runtimeManager.startVm(updated)
                        currentScreen = AppNavScreen.RUNTIME
                    },
                    onBack = {
                        currentScreen = AppNavScreen.HOME
                    }
                )
            } ?: run {
                currentScreen = AppNavScreen.HOME
            }
        }
        AppNavScreen.RUNTIME -> {
            VmRuntimeScreen(
                runtimeManager = runtimeManager,
                onExitToDashboard = {
                    currentScreen = AppNavScreen.HOME
                }
            )
        }
        AppNavScreen.DIAGNOSTICS -> {
            HostDiagnosticsScreen(
                profile = hostProfile,
                onBack = {
                    currentScreen = AppNavScreen.HOME
                }
            )
        }
    }

    // Templates Bottom Sheet
    if (showTemplatesDialog) {
        TemplatesBottomSheet(
            onSelectPreset = { preset ->
                val newVm = repository.createFromPreset(preset)
                selectedVmForConfig = newVm
                currentScreen = AppNavScreen.CONFIG
            },
            onDismiss = { showTemplatesDialog = false }
        )
    }

    // Create Custom VM Bottom Sheet
    if (showCreateCustomDialog) {
        CreateCustomVmBottomSheet(
            onCreateVm = { newVm ->
                repository.saveVm(newVm)
                selectedVmForConfig = newVm
                currentScreen = AppNavScreen.CONFIG
            },
            onDismiss = { showCreateCustomDialog = false }
        )
    }
}
