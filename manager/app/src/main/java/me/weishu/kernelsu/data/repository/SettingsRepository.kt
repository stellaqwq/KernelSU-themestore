package me.weishu.kernelsu.data.repository

import me.weishu.kernelsu.data.model.ThemeStoreSourceConfig
import me.weishu.kernelsu.ui.util.HomeCardSticker

interface SettingsRepository {
    var uiMode: String
    var checkUpdate: Boolean
    var checkModuleUpdate: Boolean
    var themeMode: Int
    var miuixMonet: Boolean
    var keyColor: Int
    var colorStyle: String
    var colorSpec: String
    var enablePredictiveBack: Boolean
    var enableSwipeDismiss: Boolean
    var pagerInterceptionMode: Int
    var enableBlur: Boolean
    var enableFloatingBottomBar: Boolean
    var enableFloatingBottomBarBlur: Boolean
    var enableNavigationBadge: Boolean
    var navigationRailExpanded: Boolean
    var pageScale: Float
    var moduleDescriptionMaxLines: Int
    var enableWebDebugging: Boolean
    var moduleSortEnabledFirst: Boolean
    var moduleSortActionFirst: Boolean
    /** Name of the [me.weishu.kernelsu.ui.screen.modulerepo.RepoSort] entry. */
    var moduleRepoSortOrder: String
    var superuserShowSystemApps: Boolean
    var superuserShowOnlyPrimaryUserApps: Boolean
    var superuserSortOption: Int
    var suLogFilters: Set<String>?
    var autoJailbreak: Boolean
    var useSoftReboot: Boolean
    val intentToken: String

    suspend fun getSuCompatStatus(): String
    suspend fun getSuCompatPersistValue(): Long?
    fun isSuEnabled(): Boolean
    fun setSuEnabled(enabled: Boolean): Boolean
    fun setSuCompatModePref(mode: Int)
    fun getSuCompatModePref(): Int

    suspend fun getKernelUmountStatus(): String
    fun isKernelUmountEnabled(): Boolean
    fun setKernelUmountEnabled(enabled: Boolean): Boolean

    suspend fun getSelinuxHideStatus(): String
    fun isSelinuxHideEnabled(): Boolean
    fun setSelinuxHideEnabled(enabled: Boolean): Int

    suspend fun getSulogStatus(): String
    suspend fun getSulogPersistValue(): Long?
    suspend fun getSulogValue(): Long?
    fun setSulogEnabled(enabled: Boolean): Boolean

    suspend fun getAdbRootStatus(): String
    suspend fun getAdbRootPersistValue(): Long?
    suspend fun getAdbRootValue(): Long?
    fun setAdbRootEnabled(enabled: Boolean): Boolean
    suspend fun getShellSuAdbOnlyStatus(): String
    suspend fun getShellSuAdbOnlyPersistValue(): Long?
    suspend fun getShellSuAdbOnlyValue(): Long?
    fun setShellSuAdbOnlyEnabled(enabled: Boolean): Boolean

    fun isDefaultUmountModules(): Boolean
    fun setDefaultUmountModules(enabled: Boolean): Boolean

    fun isLkmMode(): Boolean

    fun execKsudFeatureSave()

    // Appearance / theme package settings (ported from SukiSU Nova).
    var customBackgroundUri: String
    var customBackgroundUpdatedAt: Long
    var useSystemWallpaper: Boolean
    var extractColorFromBackground: Boolean
    var cardBackgroundAlphaPercent: Int
    var customBackgroundBlurPercent: Int
    var customBackgroundScrimPercent: Int

    var customHomeTitle: String
    var customTexts: Map<String, String>

    var themeStoreBaseUrl: String
    var themeStoreToken: String
    var themeStoreGridLayout: Boolean
    var themeStoreAuthorId: String
    var themeStoreAuthorName: String
    var themeStoreAdminUrl: String
    var themeStoreExtraSources: List<ThemeStoreSourceConfig>
    var themeStoreUploadSource: String

    var homeCardImagePath: String
    var hideHomeCardStatusText: Boolean
    var hideHomeCardDecoration: Boolean
    var homeCardStickers: List<HomeCardSticker>
    var bottomBarIcons: Map<String, String>
    var homeLayout: String
    var homeStatusAlignment: String
    var homeStatusScale: Float
    var homePillScale: Float
    var homeStatusWidthFraction: Float
    var homeCardImageStyle: String
    var showHomeCardImageWithoutBackground: Boolean

    var adaptiveThemeJson: String
    var adaptiveAppliedMode: Int
    var adaptiveDraftJson: String
    var customAppearanceJson: String

    var appIconId: String
    var appIconDynamicColor: Boolean
    var enableProgressiveBlur: Boolean

    var moduleSortWebUiFirst: Boolean
    var moduleSortZygiskFirst: Boolean
    var moduleSortMetamoduleFirst: Boolean
    var moduleSortNameOrder: String
    var moduleSortManual: Boolean
    var moduleManualOrder: String

    var showFullStatus: Boolean
    var hideHomeWarnings: Boolean
}
