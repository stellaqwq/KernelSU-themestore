package me.weishu.kernelsu.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.MainActivity

@Immutable
data class BuiltinAppIcon(
    val id: String,
    @param:StringRes val nameRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val foregroundRes: Int,
    val aliasClassName: String?,
)

/**
 * Launcher icons backed by `activity-alias` entries in the manifest.
 *
 * Exactly one alias may carry the LAUNCHER category at a time. The `*Static` aliases have no
 * monochrome layer and are used when dynamic colouring is off; the others support themed icons
 * on launchers that implement Android 13's monochrome icon API.
 */
object BuiltinAppIcons {
    const val DEFAULT_ID = "default"
    const val KERNELSU_ID = "kernelsu"
    const val LOGO_ID = "sukisu_logo"
    const val ZAKO_ID = "zako"

    private const val TAG = "BuiltinAppIcons"
    private val mainActivityName = MainActivity::class.java.name

    val all: List<BuiltinAppIcon> = listOf(
        BuiltinAppIcon(
            id = DEFAULT_ID,
            nameRes = R.string.app_icon_builtin_default,
            iconRes = R.mipmap.ic_launcher,
            foregroundRes = R.drawable.ic_launcher_foreground,
            aliasClassName = mainActivityName + "AliasDefault",
        ),
        BuiltinAppIcon(
            id = KERNELSU_ID,
            nameRes = R.string.app_icon_builtin_kernelsu,
            iconRes = R.mipmap.ic_launcher_alt,
            foregroundRes = R.drawable.ic_launcher_foreground_alt,
            aliasClassName = mainActivityName + "Alias",
        ),
        BuiltinAppIcon(
            id = LOGO_ID,
            nameRes = R.string.app_icon_builtin_logo,
            iconRes = R.mipmap.ic_launcher_logo,
            foregroundRes = R.drawable.ic_launcher_foreground_logo,
            aliasClassName = mainActivityName + "AliasLogo",
        ),
        BuiltinAppIcon(
            id = ZAKO_ID,
            nameRes = R.string.app_icon_builtin_zako,
            iconRes = R.mipmap.ic_launcher_zako,
            foregroundRes = R.drawable.ic_launcher_foreground_zako,
            aliasClassName = mainActivityName + "AliasZako",
        ),
    )

    fun find(id: String?): BuiltinAppIcon = all.firstOrNull { it.id == id } ?: all.first()

    /** Enables only the alias for [icon]; `DONT_KILL_APP` keeps the current session alive. */
    fun apply(context: Context, icon: BuiltinAppIcon, dynamicColor: Boolean = false) {
        val pm = context.packageManager
        val selectedClass = componentClassName(icon, dynamicColor)
        setEnabled(pm, ComponentName(context, mainActivityName), true)
        setEnabled(pm, ComponentName(context, selectedClass), true)
        allAliasClassNames()
            .filter { it != selectedClass }
            .forEach { setEnabled(pm, ComponentName(context, it), false) }
    }

    /**
     * Applies [icon] only when the component states do not already match it: toggling an
     * already-selected alias can restart the process on some ROMs.
     */
    fun applyIfNeeded(context: Context, icon: BuiltinAppIcon, dynamicColor: Boolean = false) {
        if (!isApplied(context, icon, dynamicColor)) apply(context, icon, dynamicColor)
    }

    fun isApplied(context: Context, icon: BuiltinAppIcon, dynamicColor: Boolean = false): Boolean {
        val pm = context.packageManager
        if (!isComponentEnabled(pm, ComponentName(context, mainActivityName))) return false
        val selectedClass = componentClassName(icon, dynamicColor)
        return isComponentEnabled(pm, ComponentName(context, selectedClass)) &&
            allAliasClassNames()
                .filter { it != selectedClass }
                .none { isComponentEnabled(pm, ComponentName(context, it)) }
    }

    private fun isComponentEnabled(pm: PackageManager, component: ComponentName): Boolean {
        val state = try {
            pm.getComponentEnabledSetting(component)
        } catch (t: Throwable) {
            Log.w(TAG, "Cannot read the enabled state of $component", t)
            return false
        }
        return when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
            -> false

            else -> try {
                pm.getActivityInfo(component, 0).enabled
            } catch (_: Throwable) {
                false
            }
        }
    }

    private fun componentClassName(icon: BuiltinAppIcon, dynamicColor: Boolean): String {
        val base = icon.aliasClassName ?: (mainActivityName + "AliasDefault")
        return if (dynamicColor) base else base + "Static"
    }

    private fun allAliasClassNames(): List<String> = buildList {
        all.forEach { icon ->
            val base = icon.aliasClassName ?: (mainActivityName + "AliasDefault")
            add(base)
            add(base + "Static")
        }
    }

    private fun setEnabled(pm: PackageManager, component: ComponentName, enabled: Boolean) {
        try {
            pm.setComponentEnabledSetting(
                component,
                if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                },
                PackageManager.DONT_KILL_APP,
            )
        } catch (t: Throwable) {
            Log.w(TAG, "Cannot change the enabled state of $component", t)
        }
    }
}
