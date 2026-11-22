package me.weishu.kernelsu.ui.screen.colorpalette

import androidx.compose.runtime.Immutable
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import me.weishu.kernelsu.ui.screen.settings.SettingsUiState
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.util.HomeCardStickerTransform

@Immutable
data class ColorPaletteUiState(
    val uiState: SettingsUiState,
    val currentColorMode: ColorMode,
    val currentPaletteStyle: PaletteStyle,
    val currentColorSpec: ColorSpec.SpecVersion,
)

@Immutable
data class ColorPaletteScreenActions(
    val onBack: () -> Unit,
    val onSetThemeMode: (Int) -> Unit,
    val onSetMiuixMonet: (Boolean) -> Unit,
    val onSetKeyColor: (Int) -> Unit,
    val onSetColorMode: (ColorMode) -> Unit,
    val onSetColorStyle: (String) -> Unit,
    val onSetColorSpec: (String) -> Unit,
    val onSetEnableBlur: (Boolean) -> Unit,
    val onSetEnableFloatingBottomBar: (Boolean) -> Unit,
    val onSetEnableFloatingBottomBarBlur: (Boolean) -> Unit,
    val onSetEnableNavigationBadge: (Boolean) -> Unit,
    val onSetEnablePredictiveBack: (Boolean) -> Unit,
    val onSetEnableSwipeDismiss: (Boolean) -> Unit,
    val onSetPagerInterceptionMode: (Int) -> Unit,
    val onSetPageScale: (Float) -> Unit,
    val onSetModuleDescriptionMaxLines: (Int) -> Unit,
    /** Opens the system photo picker; the picker itself lives in the screen. */
    val onPickBackground: () -> Unit = {},
    val onClearBackground: () -> Unit = {},
    val onSetBackgroundBlurPercent: (Int) -> Unit = {},
    val onSetBackgroundScrimPercent: (Int) -> Unit = {},
    val onSetCardBackgroundAlphaPercent: (Int) -> Unit = {},
    val onOpenCustomTexts: () -> Unit = {},
    val onOpenAppIcon: () -> Unit = {},
    /** Opens the system photo picker; the picked uri is added to the status card by the screen. */
    val onAddHomeCardSticker: () -> Unit = {},
    val onRemoveHomeCardSticker: (Int) -> Unit = {},
    val onUpdateHomeCardStickerTransform: (Int, HomeCardStickerTransform) -> Unit = { _, _ -> },
)
