package me.weishu.kernelsu.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.system.Os
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.BuildConfig
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.data.repository.SettingsRepository
import me.weishu.kernelsu.data.repository.SettingsRepositoryImpl
import me.weishu.kernelsu.getKernelVersion
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.screen.home.HomeUiState
import me.weishu.kernelsu.ui.screen.home.SystemInfo
import me.weishu.kernelsu.ui.screen.home.getManagerVersion
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.HomeStatusStyle
import me.weishu.kernelsu.ui.util.checkNewVersion
import me.weishu.kernelsu.ui.util.getModuleCount
import me.weishu.kernelsu.ui.util.getSuperuserCount
import me.weishu.kernelsu.ui.util.getSELinuxStatusRaw
import me.weishu.kernelsu.ui.util.module.LatestVersionInfo
import me.weishu.kernelsu.ui.util.resolveDeviceName
import me.weishu.kernelsu.ui.util.rootAvailable
import java.io.File

class HomeViewModel(
    private val settingsRepo: SettingsRepository = SettingsRepositoryImpl()
) : ViewModel() {

    private val settingsPrefs: SharedPreferences =
        ksuApp.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Stickers are edited from the appearance page, which lives in a different ViewModel. The
    // home page has to pick the change up without a full refresh, otherwise a freshly added
    // sticker only shows up the next time the home page is re-entered.
    private val settingsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key == KEY_HOME_CARD_STICKERS) {
                _uiState.update { it.copy(homeCardStickers = existingHomeCardStickers()) }
            }
        }

    init {
        settingsPrefs.registerOnSharedPreferenceChangeListener(settingsListener)
    }

    override fun onCleared() {
        settingsPrefs.unregisterOnSharedPreferenceChangeListener(settingsListener)
        super.onCleared()
    }

    fun refresh() {
        viewModelScope.launch {
            val baseState = withContext(Dispatchers.IO) { buildState() }
            _uiState.update { baseState }
            val counts = withContext(Dispatchers.IO) {
                runCatching { getSuperuserCount() }.getOrNull() to
                    runCatching { getModuleCount() }.getOrNull()
            }
            _uiState.update { it.copy(superuserCount = counts.first, moduleCount = counts.second) }
            if (baseState.checkUpdateEnabled) {
                val latestVersionInfo = withContext(Dispatchers.IO) { checkNewVersion() }
                _uiState.update { it.copy(latestVersionInfo = latestVersionInfo) }
            }
        }
    }

    private fun existingHomeCardStickers(): List<HomeCardSticker> =
        settingsRepo.homeCardStickers.filter { File(it.path).isFile }

    private fun buildState(): HomeUiState {
        val kernelVersion = getKernelVersion()
        val isManager = Natives.isManager
        // Demo builds (KSU_MOCK_ACTIVE=true) render the "working" card on a device that has no
        // driver at all, only to review how a theme styles it. Everything below stays real for
        // every other build because BuildConfig.MOCK_ACTIVE_STATE is false by default.
        val mockActive = BuildConfig.MOCK_ACTIVE_STATE && !isManager
        val ksuVersion = if (isManager) Natives.version else if (mockActive) MOCK_KSU_VERSION else null
        val kernelUAPIVersion = if (isManager) {
            Natives.kernelUAPIVersion
        } else if (mockActive) {
            MOCK_KERNEL_UAPI
        } else {
            null
        }
        val managerUAPIVersion = Natives.managerUAPIVersion
        val lkmMode = if (mockActive) {
            true
        } else {
            ksuVersion?.let { if (kernelVersion.isGKI()) Natives.isLkmMode else null }
        }
        val isRootAvailable = if (mockActive) true else rootAvailable()
        val managerVersion = getManagerVersion(ksuApp)

        return HomeUiState(
            kernelVersion = kernelVersion,
            ksuVersion = ksuVersion,
            lkmMode = lkmMode,
            isLkmBundled = lkmMode == true && Natives.isLkmBundled,
            isManager = isManager,
            isManagerPrBuild = BuildConfig.IS_PR_BUILD,
            isKernelPrBuild = Natives.isPrBuild,
            requiresNewKernel = isManager && Natives.managerUAPIVersion > Natives.kernelUAPIVersion,
            requiresNewManager = isManager && Natives.managerUAPIVersion < Natives.kernelUAPIVersion,
            kernelUAPIVersion = kernelUAPIVersion,
            managerUAPIVersion = managerUAPIVersion,
            isRootAvailable = isRootAvailable,
            isSafeMode = if (mockActive) false else Natives.isSafeMode,
            isLateLoadMode = if (mockActive) false else Natives.isLateLoadMode,
            checkUpdateEnabled = settingsRepo.checkUpdate,
            latestVersionInfo = LatestVersionInfo(),
            currentManagerVersionCode = managerVersion.versionCode,
            systemInfo = SystemInfo(
                kernelVersion = Os.uname().release,
                managerVersion = "${managerVersion.versionName} (${managerVersion.versionCode}-${managerUAPIVersion})",
                deviceModel = resolveDeviceName(),
                fingerprint = Build.FINGERPRINT,
                selinuxStatus = getSELinuxStatusRaw(),
                seccompStatus = runCatching {
                    Os.prctl(21 /* PR_GET_SECCOMP */, 0, 0, 0, 0)
                }.getOrDefault(-1),
            ),
            homeCardStickers = existingHomeCardStickers(),
            homeCardImagePath = settingsRepo.homeCardImagePath,
            hideHomeCardStatusText = settingsRepo.hideHomeCardStatusText,
            hideHomeCardDecoration = settingsRepo.hideHomeCardDecoration,
            homeLayout = HomeLayout.fromValue(settingsRepo.homeLayout),
            homeStatusAlignment = HomeStatusAlignment.fromValue(settingsRepo.homeStatusAlignment),
            homeStatusScale = HomeStatusStyle.coerceScale(settingsRepo.homeStatusScale),
            homePillScale = HomeStatusStyle.coercePillScale(settingsRepo.homePillScale),
            homeStatusWidthFraction = HomeStatusStyle.coerceWidth(settingsRepo.homeStatusWidthFraction),
            homeCardImageStyle = HomeCardImageStyle.fromValue(settingsRepo.homeCardImageStyle),
            showHomeCardImageWithoutBackground = settingsRepo.showHomeCardImageWithoutBackground,
            workingModeLabel = when (lkmMode) {
                true -> "LKM"
                false -> "GKI"
                null -> ""
            },
        )
    }

    companion object {
        /** Shown by demo builds; the values only have to look like a real KernelSU boot. */
        private const val MOCK_KSU_VERSION = 32680
        private const val MOCK_KERNEL_UAPI = 5

        /** Same key as [SettingsRepositoryImpl.homeCardStickers]. */
        private const val KEY_HOME_CARD_STICKERS = "home_card_stickers"
    }
}
