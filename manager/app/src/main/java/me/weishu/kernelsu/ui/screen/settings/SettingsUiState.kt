package me.weishu.kernelsu.ui.screen.settings

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import me.weishu.kernelsu.data.model.ThemeStoreSourceConfig
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.util.AppearanceOverlayData
import me.weishu.kernelsu.ui.util.BuiltinAppIcons
import me.weishu.kernelsu.ui.util.HomeCardImageStyle
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeLayout
import me.weishu.kernelsu.ui.util.HomeStatusAlignment
import me.weishu.kernelsu.ui.util.ImportedTheme
import me.weishu.kernelsu.ui.util.ResolvedAppearance

@Immutable
data class SettingsUiState(
    val uiMode: String = UiMode.DEFAULT_VALUE,
    val checkUpdate: Boolean = true,
    val checkModuleUpdate: Boolean = true,
    val themeMode: Int = 0,
    val miuixMonet: Boolean = false,
    val keyColor: Int = 0,
    val colorStyle: String = PaletteStyle.TonalSpot.name,
    val colorSpec: String = ColorSpec.SpecVersion.SPEC_2025.name,
    val enablePredictiveBack: Boolean = false,
    val enableSwipeDismiss: Boolean = true,
    val pagerInterceptionMode: Int = 1,
    val enableBlur: Boolean = true,
    val enableFloatingBottomBar: Boolean = false,
    val enableFloatingBottomBarBlur: Boolean = false,
    val enableNavigationBadge: Boolean = true,
    val pageScale: Float = 1.0f,
    val moduleDescriptionMaxLines: Int = 4,
    val enableWebDebugging: Boolean = false,
    val enableProgressiveBlur: Boolean = false,
    val appIconId: String = BuiltinAppIcons.DEFAULT_ID,
    val appIconDynamicColor: Boolean = false,

    // Su Compat
    val suCompatStatus: String = "",
    val suCompatMode: Int = 0, // 0: enable default, 1: disable until reboot, 2: disable always
    val isSuEnabled: Boolean = false,

    // Kernel Umount
    val kernelUmountStatus: String = "",
    val isKernelUmountEnabled: Boolean = false,

    // SELinux Hide
    val selinuxHideStatus: String = "",
    val isSelinuxHideEnabled: Boolean = false,

    // SU Log
    val sulogStatus: String = "",
    val isSulogEnabled: Boolean = false,

    // Umount Modules
    val isDefaultUmountModules: Boolean = false,

    // ADB Root
    val adbRootStatus: String = "",
    val isAdbRootEnabled: Boolean = false,

    // Shell SU ADB Only
    val shellSuAdbOnlyStatus: String = "",
    val isShellSuAdbOnlyEnabled: Boolean = false,

    val isLkmMode: Boolean = false,
    val isLateLoadMode: Boolean = false,

    // Auto Jailbreak
    val autoJailbreak: Boolean = false,

    // Soft Reboot
    val useSoftReboot: Boolean = false,

    // Appearance / theme package (ported from SukiSU Nova)
    val customBackgroundUri: String = "",
    val customBackgroundUpdatedAt: Long = 0L,
    val useSystemWallpaper: Boolean = false,
    val systemWallpaperPath: String = "",
    val isSystemWallpaperBusy: Boolean = false,
    val extractColorFromBackground: Boolean = false,
    val cardBackgroundAlphaPercent: Int = 60,
    val customBackgroundBlurPercent: Int = 50,
    val customBackgroundScrimPercent: Int = -35,
    val customHomeTitle: String = "",
    val customTexts: Map<String, String> = emptyMap(),
    val homeCardImagePath: String = "",
    val hideHomeCardStatusText: Boolean = false,
    val hideHomeCardDecoration: Boolean = false,
    val homeCardStickers: List<HomeCardSticker> = emptyList(),
    val bottomBarIcons: Map<String, String> = emptyMap(),
    val homeLayout: HomeLayout = HomeLayout.CLASSIC,
    val homeStatusAlignment: HomeStatusAlignment = HomeStatusAlignment.START,
    val homeStatusScale: Float = 1f,
    val homePillScale: Float = 1f,
    val homeStatusWidthFraction: Float = 1f,
    val homeCardImageStyle: HomeCardImageStyle = HomeCardImageStyle.BANNER,
    val showHomeCardImageWithoutBackground: Boolean = false,

    // Theme store sources
    val themeStoreBaseUrl: String = "",
    val themeStoreToken: String = "",
    val themeStoreAuthorId: String = "",
    val themeStoreAuthorName: String = "",
    val themeStoreAdminUrl: String = "",
    val themeStoreExtraSources: List<ThemeStoreSourceConfig> = emptyList(),

    // Theme package import / export
    val pendingImportTheme: ImportedTheme? = null,
    val showThemeImportDialog: Boolean = false,

    // Adaptive theme draft
    val adaptiveThemeActive: Boolean = false,
    val adaptiveDraftLightCaptured: Boolean = false,
    val adaptiveDraftDarkCaptured: Boolean = false,
    val adaptiveDraftComplete: Boolean = false,
    val adaptiveDraftRevision: Int = 0,

    /** Canonical JSON of the static colour / image-effect overlay, empty when disabled. */
    val customAppearanceJson: String = "",
    val customColorsPresent: Boolean = false,
    val imageEffectsPresent: Boolean = false,
    val appearance: ResolvedAppearance? = null,
    val appearanceData: AppearanceOverlayData? = null,
)

@Immutable
data class SettingsScreenActions(
    val onSetCheckUpdate: (Boolean) -> Unit,
    val onSetCheckModuleUpdate: (Boolean) -> Unit,
    val onOpenTheme: () -> Unit,
    val onOpenThemeStore: () -> Unit = {},
    val onClearCustomAppearance: () -> Unit = {},
    val onSetUiModeIndex: (Int) -> Unit,
    val onOpenProfileTemplate: () -> Unit,
    val onSetSuCompatMode: (Int) -> Unit,
    val onSetKernelUmountEnabled: (Boolean) -> Unit,
    val onSetSelinuxHideEnabled: (Boolean) -> Unit,
    val onSetSulogEnabled: (Boolean) -> Unit,
    val onSetAdbRootEnabled: (Boolean) -> Unit,
    val onSetShellSuAdbOnlyEnabled: (Boolean) -> Unit,
    val onSetDefaultUmountModules: (Boolean) -> Unit,
    val onSetEnableWebDebugging: (Boolean) -> Unit,
    val onSetAutoJailbreak: (Boolean) -> Unit,
    val onSetUseSoftReboot: (Boolean) -> Unit,
    val onOpenAbout: () -> Unit,
)
