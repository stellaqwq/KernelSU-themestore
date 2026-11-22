package me.weishu.kernelsu.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.edit
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.topjohnwu.superuser.ShellUtils
import me.weishu.kernelsu.Natives
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.magica.BootCompletedReceiver
import me.weishu.kernelsu.ui.UiMode
import me.weishu.kernelsu.ui.screen.modulerepo.RepoSort
import me.weishu.kernelsu.ui.util.execKsud
import me.weishu.kernelsu.ui.util.getFeaturePersistValue
import me.weishu.kernelsu.ui.util.getFeatureValue
import me.weishu.kernelsu.ui.util.getFeatureStatus
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeStatusStyle
import me.weishu.kernelsu.ui.util.ImageStorage
import kotlinx.serialization.json.Json
import java.security.SecureRandom

private const val SETTINGS_PREFS = "settings"
private const val KEY_USE_SOFT_REBOOT = "soft_reboot"

/** Prefer soft reboot: always in jailbreak mode, or when the setting is enabled. */
fun isSoftRebootPreferred(): Boolean =
    Natives.isLateLoadMode || ksuApp.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_USE_SOFT_REBOOT, false)

class SettingsRepositoryImpl : SettingsRepository {

    private companion object {
        private const val INTENT_TOKEN_KEY = "intent_token"
        private val secureRandom = SecureRandom()
    }

    private val prefs by lazy {
        ksuApp.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
    }

    override var uiMode: String
        get() = prefs.getString("ui_mode", UiMode.DEFAULT_VALUE) ?: UiMode.DEFAULT_VALUE
        set(value) = prefs.edit { putString("ui_mode", value) }

    override var checkUpdate: Boolean
        get() = prefs.getBoolean("check_update", true)
        set(value) = prefs.edit { putBoolean("check_update", value) }

    override var checkModuleUpdate: Boolean
        get() = prefs.getBoolean("module_check_update", true)
        set(value) = prefs.edit { putBoolean("module_check_update", value) }

    override var themeMode: Int
        get() = prefs.getInt("color_mode", 0)
        set(value) = prefs.edit { putInt("color_mode", value) }

    override var miuixMonet: Boolean
        get() = prefs.getBoolean("miuix_monet", false)
        set(value) = prefs.edit { putBoolean("miuix_monet", value) }

    override var keyColor: Int
        get() = prefs.getInt("key_color", 0)
        set(value) = prefs.edit { putInt("key_color", value) }

    override var colorStyle: String
        get() = prefs.getString("color_style", PaletteStyle.TonalSpot.name) ?: PaletteStyle.TonalSpot.name
        set(value) = prefs.edit { putString("color_style", value) }

    override var colorSpec: String
        get() = prefs.getString("color_spec", ColorSpec.SpecVersion.SPEC_2025.name) ?: ColorSpec.SpecVersion.SPEC_2025.name
        set(value) = prefs.edit { putString("color_spec", value) }

    override var enablePredictiveBack: Boolean
        get() = prefs.getBoolean("enable_predictive_back", false)
        set(value) = prefs.edit { putBoolean("enable_predictive_back", value) }

    override var enableSwipeDismiss: Boolean
        get() = prefs.getBoolean("enable_swipe_dismiss", true)
        set(value) = prefs.edit { putBoolean("enable_swipe_dismiss", value) }

    override var pagerInterceptionMode: Int
        get() = prefs.getInt("pager_interception_mode", 1)
        set(value) = prefs.edit { putInt("pager_interception_mode", value.coerceIn(0, 2)) }

    override var enableBlur: Boolean
        get() = prefs.getBoolean("enable_blur", false)
        set(value) = prefs.edit { putBoolean("enable_blur", value) }

    override var enableFloatingBottomBar: Boolean
        get() = prefs.getBoolean("enable_floating_bottom_bar", false)
        set(value) = prefs.edit { putBoolean("enable_floating_bottom_bar", value) }

    override var enableFloatingBottomBarBlur: Boolean
        get() = prefs.getBoolean("enable_floating_bottom_bar_blur", false)
        set(value) = prefs.edit { putBoolean("enable_floating_bottom_bar_blur", value) }

    override var enableNavigationBadge: Boolean
        get() = prefs.getBoolean("enable_navigation_badge", true)
        set(value) = prefs.edit { putBoolean("enable_navigation_badge", value) }

    override var navigationRailExpanded: Boolean
        get() = prefs.getBoolean("nav_rail_expanded", false)
        set(value) = prefs.edit { putBoolean("nav_rail_expanded", value) }

    override var pageScale: Float
        get() = prefs.getFloat("page_scale", 1.0f)
        set(value) = prefs.edit { putFloat("page_scale", value) }

    override var moduleDescriptionMaxLines: Int
        get() = prefs.getInt("module_description_max_lines", 4)
        set(value) = prefs.edit { putInt("module_description_max_lines", value) }

    override var enableWebDebugging: Boolean
        get() = prefs.getBoolean("enable_web_debugging", false)
        set(value) = prefs.edit { putBoolean("enable_web_debugging", value) }

    override var moduleSortEnabledFirst: Boolean
        get() = prefs.getBoolean("module_sort_enabled_first", false)
        set(value) = prefs.edit { putBoolean("module_sort_enabled_first", value) }

    override var moduleSortActionFirst: Boolean
        get() = prefs.getBoolean("module_sort_action_first", false)
        set(value) = prefs.edit { putBoolean("module_sort_action_first", value) }

    override var moduleRepoSortOrder: String
        get() = RepoSort.fromStored(
            name = prefs.getString("module_repo_sort", null),
            legacyOrdinal = if (prefs.contains("module_repo_sort_order")) prefs.getInt("module_repo_sort_order", 0) else null,
        ).name
        set(value) = prefs.edit {
            putString("module_repo_sort", value)
            remove("module_repo_sort_order")
        }

    override var superuserShowSystemApps: Boolean
        get() = prefs.getBoolean("show_system_apps", false)
        set(value) = prefs.edit { putBoolean("show_system_apps", value) }

    override var superuserShowOnlyPrimaryUserApps: Boolean
        get() = prefs.getBoolean("show_only_primary_user_apps", false)
        set(value) = prefs.edit { putBoolean("show_only_primary_user_apps", value) }

    override var superuserSortOption: Int
        get() = prefs.getInt("superuser_sort_option", 0)
        set(value) = prefs.edit { putInt("superuser_sort_option", value) }

    override var suLogFilters: Set<String>?
        get() = prefs.getStringSet("sulog_filters", null)?.toSet()
        set(filters) = prefs.edit { putStringSet("sulog_filters", filters) }

    override var autoJailbreak: Boolean
        get() = prefs.getBoolean("auto_jailbreak", false)
        set(value) {
            runCatching {
                ksuApp.packageManager.setComponentEnabledSetting(
                    ComponentName(ksuApp, BootCompletedReceiver::class.java),
                    if (value) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }.onFailure {
                Log.e("Settings", "failed to change boot receiver state to $value", it)
            }
            prefs.edit {
                putBoolean("auto_jailbreak", value)
            }
        }

    override var useSoftReboot: Boolean
        get() = prefs.getBoolean(KEY_USE_SOFT_REBOOT, false)
        set(value) = prefs.edit { putBoolean(KEY_USE_SOFT_REBOOT, value) }

    override val intentToken: String
        get() {
        val existing = prefs.getString(INTENT_TOKEN_KEY, null)
        if (!existing.isNullOrBlank()) return existing
        val token = ByteArray(32).also(secureRandom::nextBytes)
            .joinToString(separator = "") { "%02x".format(it) }
        prefs.edit { putString(INTENT_TOKEN_KEY, token) }
        return token
    }

    override suspend fun getSuCompatStatus(): String = getFeatureStatus("su_compat")

    override suspend fun getSuCompatPersistValue(): Long? = getFeaturePersistValue("su_compat")

    override fun isSuEnabled(): Boolean = Natives.isSuEnabled()

    override fun setSuEnabled(enabled: Boolean): Boolean = Natives.setSuEnabled(enabled)

    override fun setSuCompatModePref(mode: Int) = prefs.edit { putInt("su_compat_mode", mode) }

    override fun getSuCompatModePref(): Int = prefs.getInt("su_compat_mode", 0)

    override suspend fun getKernelUmountStatus(): String = getFeatureStatus("kernel_umount")

    override fun isKernelUmountEnabled(): Boolean = Natives.isKernelUmountEnabled()

    override fun setKernelUmountEnabled(enabled: Boolean): Boolean = Natives.setKernelUmountEnabled(enabled)

    override suspend fun getSelinuxHideStatus(): String = getFeatureStatus("selinux_hide")

    override fun isSelinuxHideEnabled(): Boolean = Natives.isSelinuxHideEnabled()

    override fun setSelinuxHideEnabled(enabled: Boolean): Int = Natives.setSelinuxHideEnabled(enabled)

    override suspend fun getSulogStatus(): String = getFeatureStatus("sulog")

    override suspend fun getSulogPersistValue(): Long? = getFeaturePersistValue("sulog")

    override suspend fun getSulogValue(): Long? = getFeatureValue("sulog")

    override fun setSulogEnabled(enabled: Boolean): Boolean = execKsud("feature set sulog ${if (enabled) 1 else 0}", true)

    override suspend fun getAdbRootStatus(): String = getFeatureStatus("adb_root")

    override suspend fun getAdbRootPersistValue(): Long? = getFeaturePersistValue("adb_root")

    override suspend fun getAdbRootValue(): Long? = getFeatureValue("adb_root")

    override fun setAdbRootEnabled(enabled: Boolean): Boolean =
        if (execKsud("feature set adb_root ${if (enabled) 1 else 0}", true)) {
            ShellUtils.fastCmd("setprop ctl.restart adbd")
            true
        } else {
            false
        }

    override suspend fun getShellSuAdbOnlyStatus(): String = getFeatureStatus("shell_su_adb_only")

    override suspend fun getShellSuAdbOnlyPersistValue(): Long? = getFeaturePersistValue("shell_su_adb_only")

    override suspend fun getShellSuAdbOnlyValue(): Long? = getFeatureValue("shell_su_adb_only")

    override fun setShellSuAdbOnlyEnabled(enabled: Boolean): Boolean =
        execKsud("feature set shell_su_adb_only ${if (enabled) 1 else 0}", true)

    override fun isDefaultUmountModules(): Boolean = Natives.isDefaultUmountModules()

    override fun setDefaultUmountModules(enabled: Boolean): Boolean = Natives.setDefaultUmountModules(enabled)

    override fun isLkmMode(): Boolean = Natives.isLkmMode

    override fun execKsudFeatureSave() {
        execKsud("feature save", true)
    }

    private val json = Json { ignoreUnknownKeys = true }

    override var customBackgroundUri: String
        get() = prefs.getString("custom_bg_uri", "") ?: ""
        set(value) = prefs.edit { putString("custom_bg_uri", value) }

    override var customBackgroundUpdatedAt: Long
        get() = prefs.getLong("custom_bg_updated_at", 0L)
        set(value) = prefs.edit { putLong("custom_bg_updated_at", value) }

    override var useSystemWallpaper: Boolean
        get() = prefs.getBoolean("use_system_wallpaper", false)
        set(value) = prefs.edit { putBoolean("use_system_wallpaper", value) }

    override var extractColorFromBackground: Boolean
        get() = prefs.getBoolean("extract_color_from_background", false)
        set(value) = prefs.edit { putBoolean("extract_color_from_background", value) }

    override var cardBackgroundAlphaPercent: Int
        get() = prefs.getInt("card_bg_alpha_percent", 60)
        set(value) = prefs.edit { putInt("card_bg_alpha_percent", value.coerceIn(0, 100)) }

    override var customBackgroundBlurPercent: Int
        get() = prefs.getInt("custom_bg_blur_percent", ImageStorage.defaultBackgroundBlurPercent(ksuApp))
        set(value) = prefs.edit { putInt("custom_bg_blur_percent", value.coerceIn(0, 100)) }

    override var customBackgroundScrimPercent: Int
        get() = prefs.getInt("custom_bg_scrim_percent", ImageStorage.DEFAULT_BACKGROUND_SCRIM)
        set(value) = prefs.edit { putInt("custom_bg_scrim_percent", value.coerceIn(0, 100)) }

    override var customHomeTitle: String
        get() = prefs.getString("custom_home_title", "") ?: ""
        set(value) = prefs.edit { putString("custom_home_title", value.trim()) }

    override var customTexts: Map<String, String>
        get() {
            val raw = prefs.getString("custom_texts", null) ?: return emptyMap()
            return runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
        }
        set(value) = prefs.edit { putString("custom_texts", json.encodeToString(value)) }

    override var themeStoreBaseUrl: String
        get() = prefs.getString("theme_store_base_url", "") ?: ""
        set(value) = prefs.edit { putString("theme_store_base_url", value.trim()) }

    override var themeStoreToken: String
        get() = prefs.getString("theme_store_token", "") ?: ""
        set(value) = prefs.edit { putString("theme_store_token", value.trim()) }

    override var themeStoreGridLayout: Boolean
        get() = prefs.getBoolean("theme_store_grid_layout", true)
        set(value) = prefs.edit { putBoolean("theme_store_grid_layout", value) }

    override var themeStoreAuthorId: String
        get() = prefs.getString("theme_store_author_id", "") ?: ""
        set(value) = prefs.edit { putString("theme_store_author_id", value.trim()) }

    override var themeStoreAuthorName: String
        get() = prefs.getString("theme_store_author_name", "") ?: ""
        set(value) = prefs.edit { putString("theme_store_author_name", value.trim()) }

    override var themeStoreAdminUrl: String
        get() = prefs.getString("theme_store_admin_url", "") ?: ""
        set(value) = prefs.edit { putString("theme_store_admin_url", value.trim()) }

    override var themeStoreExtraSources: List<me.weishu.kernelsu.data.model.ThemeStoreSourceConfig>
        get() {
            val raw = prefs.getString("theme_store_extra_sources", null) ?: return emptyList()
            return runCatching {
                json.decodeFromString<List<me.weishu.kernelsu.data.model.ThemeStoreSourceConfig>>(raw)
            }.getOrDefault(emptyList())
        }
        set(value) = prefs.edit { putString("theme_store_extra_sources", json.encodeToString(value)) }

    override var themeStoreUploadSource: String
        get() = prefs.getString("theme_store_upload_source", "") ?: ""
        set(value) = prefs.edit { putString("theme_store_upload_source", value.trim()) }

    override var homeCardImagePath: String
        get() = prefs.getString("home_card_image_path", "") ?: ""
        set(value) = prefs.edit { putString("home_card_image_path", value) }

    override var hideHomeCardStatusText: Boolean
        get() = prefs.getBoolean("hide_home_card_status_text", false)
        set(value) = prefs.edit { putBoolean("hide_home_card_status_text", value) }

    override var hideHomeCardDecoration: Boolean
        get() = prefs.getBoolean("hide_home_card_decoration", false)
        set(value) = prefs.edit { putBoolean("hide_home_card_decoration", value) }

    override var homeCardStickers: List<HomeCardSticker>
        get() {
            val raw = prefs.getString("home_card_stickers", null) ?: return emptyList()
            return runCatching { json.decodeFromString<List<HomeCardSticker>>(raw) }.getOrDefault(emptyList())
        }
        set(value) = prefs.edit { putString("home_card_stickers", json.encodeToString(value)) }

    override var bottomBarIcons: Map<String, String>
        get() {
            val raw = prefs.getString("bottom_bar_icons", null) ?: return emptyMap()
            return runCatching { json.decodeFromString<Map<String, String>>(raw) }.getOrDefault(emptyMap())
        }
        set(value) = prefs.edit { putString("bottom_bar_icons", json.encodeToString(value)) }

    override var homeLayout: String
        get() = prefs.getString("home_layout", "classic") ?: "classic"
        set(value) = prefs.edit { putString("home_layout", value) }

    override var homeStatusAlignment: String
        get() = prefs.getString("home_status_alignment", "start") ?: "start"
        set(value) = prefs.edit { putString("home_status_alignment", value) }

    override var homeStatusScale: Float
        get() = HomeStatusStyle.coerceScale(prefs.getFloat("home_status_scale", HomeStatusStyle.SCALE_DEFAULT))
        set(value) = prefs.edit { putFloat("home_status_scale", HomeStatusStyle.coerceScale(value)) }

    override var homePillScale: Float
        get() = HomeStatusStyle.coercePillScale(prefs.getFloat("home_pill_scale", HomeStatusStyle.PILL_SCALE_DEFAULT))
        set(value) = prefs.edit { putFloat("home_pill_scale", HomeStatusStyle.coercePillScale(value)) }

    override var homeStatusWidthFraction: Float
        get() = HomeStatusStyle.coerceWidth(prefs.getFloat("home_status_width_fraction", HomeStatusStyle.WIDTH_DEFAULT))
        set(value) = prefs.edit { putFloat("home_status_width_fraction", HomeStatusStyle.coerceWidth(value)) }

    override var homeCardImageStyle: String
        get() = prefs.getString("home_card_image_style", "banner") ?: "banner"
        set(value) = prefs.edit { putString("home_card_image_style", value) }

    override var showHomeCardImageWithoutBackground: Boolean
        get() = prefs.getBoolean("show_home_card_image_without_background", false)
        set(value) = prefs.edit { putBoolean("show_home_card_image_without_background", value) }

    override var adaptiveThemeJson: String
        get() = prefs.getString("adaptive_theme", "") ?: ""
        set(value) = prefs.edit { putString("adaptive_theme", value) }

    override var adaptiveAppliedMode: Int
        get() = prefs.getInt("adaptive_applied_mode", -1)
        set(value) = prefs.edit { putInt("adaptive_applied_mode", value) }

    override var adaptiveDraftJson: String
        get() = prefs.getString("adaptive_draft", "") ?: ""
        set(value) = prefs.edit { putString("adaptive_draft", value) }

    override var customAppearanceJson: String
        get() = prefs.getString("custom_appearance", "") ?: ""
        set(value) = prefs.edit { putString("custom_appearance", value) }

    override var appIconId: String
        get() = prefs.getString("app_icon_id", "") ?: ""
        set(value) = prefs.edit(commit = true) { putString("app_icon_id", value) }

    override var appIconDynamicColor: Boolean
        get() = prefs.getBoolean("app_icon_dynamic_color", false)
        set(value) = prefs.edit(commit = true) { putBoolean("app_icon_dynamic_color", value) }

    override var enableProgressiveBlur: Boolean
        get() = prefs.getBoolean("enable_progressive_blur", false)
        set(value) = prefs.edit { putBoolean("enable_progressive_blur", value) }

    override var moduleSortWebUiFirst: Boolean
        get() = prefs.getBoolean("module_sort_webui_first", false)
        set(value) = prefs.edit { putBoolean("module_sort_webui_first", value) }

    override var moduleSortZygiskFirst: Boolean
        get() = prefs.getBoolean("module_sort_zygisk_first", false)
        set(value) = prefs.edit { putBoolean("module_sort_zygisk_first", value) }

    override var moduleSortMetamoduleFirst: Boolean
        get() = prefs.getBoolean("module_sort_metamodule_first", false)
        set(value) = prefs.edit { putBoolean("module_sort_metamodule_first", value) }

    override var moduleSortNameOrder: String
        get() = prefs.getString("module_sort_name_order", "NAME") ?: "NAME"
        set(value) = prefs.edit { putString("module_sort_name_order", value) }

    override var moduleSortManual: Boolean
        get() = prefs.getBoolean("module_sort_manual", false)
        set(value) = prefs.edit { putBoolean("module_sort_manual", value) }

    override var moduleManualOrder: String
        get() = prefs.getString("module_manual_order", "") ?: ""
        set(value) = prefs.edit { putString("module_manual_order", value) }

    override var showFullStatus: Boolean
        get() = prefs.getBoolean("show_fingerprint", true)
        set(value) = prefs.edit { putBoolean("show_fingerprint", value) }

    override var hideHomeWarnings: Boolean
        get() = prefs.getBoolean("hide_home_warnings", false)
        set(value) = prefs.edit { putBoolean("hide_home_warnings", value) }
}
