package me.weishu.kernelsu.ui.screen.settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.saveable.listSaver
import me.weishu.kernelsu.ui.util.CustomTextGroup

data class ThemePackageOptions(
    val colors: Boolean = true,
    val backgroundImage: Boolean = true,
    val backgroundAppearance: Boolean = true,
    val homeCardImage: Boolean = true,
    val stickers: Boolean = true,
    val homeCardDisplay: Boolean = true,
    val appIcon: Boolean = true,
    val customTexts: Boolean = true,
    val customTextGroups: Set<CustomTextGroup> = CustomTextGroup.entries.toSet(),
    val interfaceMode: Boolean = true,
    val interfaceSwitches: Boolean = true,
    val interactionBehavior: Boolean = true,
    val moduleDisplay: Boolean = true,
    val customColors: Boolean = true,
    val imageEffects: Boolean = true,
    val bottomBarIcons: Boolean = true,
)

val ThemePackageOptionsSaver = listSaver(
    save = {
        listOf(
            it.colors,
            it.backgroundImage,
            it.backgroundAppearance,
            it.homeCardImage,
            it.stickers,
            it.homeCardDisplay,
            it.appIcon,
            it.customTexts,
            it.customTextGroups.contains(CustomTextGroup.HOME),
            it.customTextGroups.contains(CustomTextGroup.PAGE_MAIN),
            it.customTextGroups.contains(CustomTextGroup.PAGE_FEATURE),
            it.customTextGroups.contains(CustomTextGroup.SETTINGS),
            it.interfaceMode,
            it.interfaceSwitches,
            it.interactionBehavior,
            it.moduleDisplay,
            it.customColors,
            it.imageEffects,
            it.bottomBarIcons,
        )
    },
    restore = {
        ThemePackageOptions(
            colors = it[0],
            backgroundImage = it[1],
            backgroundAppearance = it[2],
            homeCardImage = it[3],
            stickers = it[4],
            homeCardDisplay = it[5],
            appIcon = it[6],
            customTexts = it[7],
            customTextGroups = buildSet {
                if (it.getOrElse(8) { true }) add(CustomTextGroup.HOME)
                if (it.getOrElse(9) { true }) add(CustomTextGroup.PAGE_MAIN)
                if (it.getOrElse(10) { true }) add(CustomTextGroup.PAGE_FEATURE)
                if (it.getOrElse(11) { true }) add(CustomTextGroup.SETTINGS)
            },
            interfaceMode = it.getOrElse(12) { true },
            interfaceSwitches = it.getOrElse(13) { true },
            interactionBehavior = it.getOrElse(14) { true },
            moduleDisplay = it.getOrElse(15) { true },
            customColors = it.getOrElse(16) { true },
            imageEffects = it.getOrElse(17) { true },
            bottomBarIcons = it.getOrElse(18) { true },
        )
    },
)
