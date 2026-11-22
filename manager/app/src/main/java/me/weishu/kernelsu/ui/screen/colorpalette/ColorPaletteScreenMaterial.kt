package me.weishu.kernelsu.ui.screen.colorpalette

import me.weishu.kernelsu.ui.util.customText
import me.weishu.kernelsu.ui.util.CustomTextKey
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Opacity
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import me.weishu.kernelsu.R
import me.weishu.kernelsu.ui.component.HomeCardStickerStage
import me.weishu.kernelsu.ui.component.HomeCardStickerThumbnail
import me.weishu.kernelsu.ui.component.bottombar.useNavigationRail
import me.weishu.kernelsu.ui.component.material.ExpressiveScaffold
import me.weishu.kernelsu.ui.component.material.ExpressiveToggleButton
import me.weishu.kernelsu.ui.component.material.SegmentedColumn
import me.weishu.kernelsu.ui.component.material.SegmentedDropdownItem
import me.weishu.kernelsu.ui.component.material.SegmentedListItem
import me.weishu.kernelsu.ui.component.material.SegmentedSwitchItem
import me.weishu.kernelsu.ui.component.material.TonalCard
import me.weishu.kernelsu.ui.component.material.TopBarBackButton
import me.weishu.kernelsu.ui.component.material.expressiveTopAppBarColors
import me.weishu.kernelsu.ui.theme.ColorMode
import me.weishu.kernelsu.ui.theme.keyColorOptions
import me.weishu.kernelsu.ui.theme.rememberKernelSUColorScheme
import me.weishu.kernelsu.ui.util.ImageStorage
import me.weishu.kernelsu.ui.util.HomeCardSticker
import me.weishu.kernelsu.ui.util.HomeCardStickerTransform
import kotlin.math.roundToInt

@Composable
fun ColorPaletteScreenMaterial(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val uiState = state.uiState
    val currentColorMode = state.currentColorMode
    val currentKeyColor = uiState.keyColor
    val colorStyle = state.currentPaletteStyle
    val colorSpec = state.currentColorSpec
    val haptic = LocalHapticFeedback.current

    ExpressiveScaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                navigationIcon = {
                    TopBarBackButton(onClick = actions.onBack)
                },
                title = { Text(customText(CustomTextKey.PAGE_APPEARANCE)) },
                colors = expressiveTopAppBarColors(),
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        val navBars = WindowInsets.navigationBars.asPaddingValues()
        val captionBar = WindowInsets.captionBar.asPaddingValues()
        val isDark = currentColorMode.isDark || currentColorMode.isSystem && isSystemInDarkTheme()
        val isAmoled = currentColorMode.isAmoled

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            item {
                ThemePreviewCard(
                    keyColor = currentKeyColor,
                    isDark = isDark,
                    isAmoled = isAmoled,
                    paletteStyle = colorStyle,
                    colorSpec = colorSpec,
                )
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        ColorButtonMaterial(
                            color = Color.Unspecified,
                            isSelected = currentKeyColor == 0,
                            isDark = isDark,
                            isAmoled = isAmoled,
                            paletteStyle = colorStyle,
                            colorSpec = colorSpec,
                            onClick = {
                                actions.onSetKeyColor(0)
                            }
                        )
                    }

                    items(keyColorOptions) { color ->
                        ColorButtonMaterial(
                            color = Color(color),
                            isSelected = currentKeyColor == color,
                            isDark = isDark,
                            isAmoled = isAmoled,
                            paletteStyle = colorStyle,
                            colorSpec = colorSpec,
                            onClick = {
                                actions.onSetKeyColor(color)
                            }
                        )
                    }
                }
            }

            item {
                val options = listOf(
                    listOf(ColorMode.SYSTEM) to stringResource(R.string.settings_theme_mode_system),
                    listOf(ColorMode.LIGHT) to stringResource(R.string.settings_theme_mode_light),
                    listOf(ColorMode.DARK) to stringResource(R.string.settings_theme_mode_dark),
                    listOf(ColorMode.DARK_AMOLED) to stringResource(R.string.settings_theme_mode_dark)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.chunked(4).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                        ) {
                            rowOptions.forEachIndexed { index, (modes, label) ->
                                ExpressiveToggleButton(
                                    checked = currentColorMode in modes,
                                    onCheckedChange = {
                                        if (it) {
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            actions.onSetColorMode(modes.first())
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { role = Role.RadioButton },
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        rowOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                ) {
                                    Icon(
                                        imageVector = when (modes.first()) {
                                            ColorMode.SYSTEM -> Icons.Filled.Brightness4
                                            ColorMode.LIGHT -> Icons.Filled.Brightness7
                                            ColorMode.DARK -> Icons.Filled.Brightness3
                                            ColorMode.DARK_AMOLED -> Icons.Filled.Brightness1
                                            else -> Icons.Filled.Brightness4
                                        },
                                        contentDescription = label
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            val styles = PaletteStyle.entries
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.Style,
                                title = stringResource(R.string.settings_color_style),
                                items = styles.map { it.name },
                                selectedIndex = styles.indexOf(colorStyle),
                                onItemSelected = { index ->
                                    actions.onSetColorStyle(styles[index].name)
                                }
                            )
                        },
                        {
                            val specs = ColorSpec.SpecVersion.entries
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.DesignServices,
                                title = stringResource(R.string.settings_color_spec),
                                items = specs.map { it.name },
                                selectedIndex = specs.indexOf(colorSpec).coerceAtLeast(0),
                                onItemSelected = { index ->
                                    actions.onSetColorSpec(specs[index].name)
                                }
                            )
                        }
                    )
                )
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.Pin,
                                title = stringResource(id = R.string.settings_navigation_badge),
                                summary = stringResource(id = R.string.settings_navigation_badge_summary),
                                checked = uiState.enableNavigationBadge,
                                onCheckedChange = actions.onSetEnableNavigationBadge
                            )
                        }
                    )
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                item {
                    SegmentedColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        content = listOf(
                            {
                                SegmentedSwitchItem(
                                    icon = Icons.AutoMirrored.Rounded.MenuOpen,
                                    title = stringResource(id = R.string.settings_enable_predictive_back),
                                    summary = stringResource(id = R.string.settings_enable_predictive_back_summary),
                                    checked = uiState.enablePredictiveBack,
                                    onCheckedChange = actions.onSetEnablePredictiveBack
                                )
                            }
                        )
                    )
                }
            }

            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = listOf(
                        {
                            SegmentedSwitchItem(
                                icon = Icons.Rounded.Swipe,
                                title = stringResource(id = R.string.settings_enable_swipe_dismiss),
                                summary = stringResource(id = R.string.settings_enable_swipe_dismiss_summary),
                                checked = uiState.enableSwipeDismiss,
                                onCheckedChange = actions.onSetEnableSwipeDismiss,
                            )
                        },
                        {
                            SegmentedDropdownItem(
                                icon = Icons.Rounded.ViewCarousel,
                                title = stringResource(id = R.string.settings_pager_gesture_mode),
                                items = listOf(
                                    stringResource(id = R.string.settings_pager_gesture_native),
                                    stringResource(id = R.string.settings_pager_gesture_cross_axis),
                                    stringResource(id = R.string.settings_pager_gesture_ios_like),
                                ),
                                selectedIndex = uiState.pagerInterceptionMode.coerceIn(0, 2),
                                onItemSelected = actions.onSetPagerInterceptionMode,
                            )
                        },
                    ),
                )
            }

            item {
                val hasBackground = uiState.customBackgroundUri.isNotEmpty()
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    content = buildList {
                        add {
                            SegmentedListItem(
                                onClick = actions.onPickBackground,
                                headlineContent = {
                                    Text(stringResource(R.string.appearance_background_image))
                                },
                                supportingContent = {
                                    Text(
                                        if (hasBackground) {
                                            stringResource(R.string.appearance_background_set)
                                        } else {
                                            stringResource(R.string.appearance_background_none)
                                        }
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                trailingContent = {
                                    Text(
                                        text = stringResource(R.string.appearance_background_pick),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            )
                        }
                        if (hasBackground) {
                            add {
                                SegmentedListItem(
                                    onClick = actions.onClearBackground,
                                    headlineContent = {
                                        Text(stringResource(R.string.appearance_background_clear))
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                )
                            }
                        }
                        add {
                            SegmentedListItem(
                                onClick = actions.onOpenCustomTexts,
                                headlineContent = { Text(stringResource(R.string.settings_custom_texts)) },
                                supportingContent = {
                                    Text(stringResource(R.string.theme_opt_custom_texts))
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.TextFields,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                    )
                                }
                            )
                        }
                        add {
                            SegmentedListItem(
                                onClick = actions.onOpenAppIcon,
                                headlineContent = { Text(stringResource(R.string.app_icon_title)) },
                                supportingContent = {
                                    Text(stringResource(R.string.app_icon_summary))
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.Style,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                    )
                                }
                            )
                        }
                    }
                )
            }

            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        AppearanceSliderRow(
                            icon = Icons.Rounded.BlurOn,
                            title = stringResource(R.string.appearance_background_blur),
                            summary = stringResource(R.string.appearance_background_blur_summary),
                            value = uiState.customBackgroundBlurPercent.toFloat(),
                            valueText = "${uiState.customBackgroundBlurPercent}%",
                            range = ImageStorage.BACKGROUND_BLUR_PERCENT_MIN.toFloat()..
                                ImageStorage.BACKGROUND_BLUR_PERCENT_MAX.toFloat(),
                            onValueChangeFinished = {
                                actions.onSetBackgroundBlurPercent(it.roundToInt())
                            },
                        )
                        AppearanceSliderRow(
                            icon = Icons.Rounded.Contrast,
                            title = stringResource(R.string.appearance_background_scrim),
                            summary = stringResource(R.string.appearance_background_scrim_summary),
                            value = uiState.customBackgroundScrimPercent.toFloat(),
                            valueText = "${uiState.customBackgroundScrimPercent}%",
                            range = ImageStorage.BACKGROUND_SCRIM_MIN.toFloat()..
                                ImageStorage.BACKGROUND_SCRIM_MAX.toFloat(),
                            onValueChangeFinished = {
                                actions.onSetBackgroundScrimPercent(it.roundToInt())
                            },
                        )
                        AppearanceSliderRow(
                            icon = Icons.Rounded.Opacity,
                            title = stringResource(R.string.appearance_card_alpha),
                            summary = stringResource(R.string.appearance_card_alpha_summary),
                            value = uiState.cardBackgroundAlphaPercent.toFloat(),
                            valueText = "${uiState.cardBackgroundAlphaPercent}%",
                            range = ImageStorage.CARD_ALPHA_MIN.toFloat()..
                                ImageStorage.CARD_ALPHA_MAX.toFloat(),
                            onValueChangeFinished = {
                                actions.onSetCardBackgroundAlphaPercent(it.roundToInt())
                            },
                        )
                    }
                }
            }

            item {
                HomeCardStickerSection(state = state, actions = actions)
            }

            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val sliderState = rememberSliderState(
                        value = uiState.pageScale,
                        trackRange = 0.8f..1.1f
                    )

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.AspectRatio,
                                contentDescription = stringResource(id = R.string.settings_page_scale),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_page_scale),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = R.string.settings_page_scale_summary),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(sliderState.value * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Slider(
                            state = sliderState,
                            onValueChangeFinished = { actions.onSetPageScale(sliderState.value) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val sliderState = rememberSliderState(
                        value = uiState.moduleDescriptionMaxLines.toFloat(),
                        steps = 3,
                        trackRange = 1f..5f
                    )

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Description,
                                contentDescription = stringResource(id = R.string.settings_module_description_max_lines),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = stringResource(R.string.settings_module_description_max_lines),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(id = R.string.settings_module_description_max_lines_summary),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${sliderState.value.roundToInt()} " + stringResource(R.string.unit_lines),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Slider(
                            state = sliderState,
                            onValueChangeFinished = {
                                actions.onSetModuleDescriptionMaxLines(sliderState.value.roundToInt())
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp + navBars.calculateBottomPadding() + captionBar.calculateBottomPadding()))
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ThemePreviewCard(
    keyColor: Int,
    isDark: Boolean,
    isAmoled: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2025,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val screenRatio = screenWidth / screenHeight
    val useRail = useNavigationRail(enableFloatingBottomBar = false)

    val colorScheme = rememberKernelSUColorScheme(
        seedColor = if (keyColor == 0) Color.Unspecified else Color(keyColor),
        isDark = isDark,
        isAmoled = isAmoled,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .aspectRatio(screenRatio),
            color = colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, color = colorScheme.outlineVariant)
        ) {
            val content: @Composable ColumnScope.() -> Unit = {
                // top bar
                Box(
                    modifier = Modifier
                        .height(if (useRail) 36.dp else 48.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 12.dp, top = if (useRail) 8.dp else 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurface
                        )
                    }
                }

                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    val showInfoCard = maxHeight >= 72.dp
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TonalCard(
                            containerColor = colorScheme.secondaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            content = { }
                        )
                        if (showInfoCard) {
                            TonalCard(
                                containerColor = colorScheme.surfaceBright,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                content = { }
                            )
                        }
                    }
                }
            }

            if (useRail) {
                Row {
                    Surface(
                        color = colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Home, null, tint = colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) { content() }
                }
            } else {
                Column {
                    content()

                    // bottom bar
                    Surface(
                        color = colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Home, null, tint = colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorButtonMaterial(
    color: Color,
    isSelected: Boolean,
    isDark: Boolean,
    isAmoled: Boolean = false,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    colorSpec: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2025,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val colorScheme = rememberKernelSUColorScheme(
        seedColor = color,
        isDark = isDark,
        isAmoled = isAmoled,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surfaceContainer,
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(48.dp)) {
                drawArc(
                    color = colorScheme.primaryContainer,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true
                )
                drawArc(
                    color = colorScheme.tertiaryContainer,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true
                )
            }

            val scale by animateFloatAsState(targetValue = if (isSelected) 1.1f else 1.0f)
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(2.dp, colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = colorScheme.onPrimary,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(16.dp)
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = !isSelected,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(colorScheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * One appearance slider row: icon, title/summary, current value and the track. The value is
 * committed on release so dragging never writes preferences dozens of times per second;
 * [onValueChange] is only used where the caller needs a live preview (sticker rotation/size).
 */
@Composable
private fun AppearanceSliderRow(
    icon: ImageVector,
    title: String,
    summary: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Float) -> Unit,
    onValueChange: ((Float) -> Unit)? = null,
) {
    val sliderState = rememberSliderState(value = value, trackRange = range)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Slider(
            state = sliderState,
            onValueChange = { if (onValueChange != null) onValueChange(it) },
            onValueChangeFinished = { onValueChangeFinished(sliderState.value) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Sticker editor for the home status card. The stage mirrors the card proportions, so dragging
 * a sticker there is enough to place it; rotation and size are the only numeric controls.
 */
@Composable
private fun HomeCardStickerSection(
    state: ColorPaletteUiState,
    actions: ColorPaletteScreenActions,
) {
    val saved = state.uiState.homeCardStickers
    // Local copy: dragging must repaint at touch speed, while the stored transform is only
    // written when the gesture or the slider finishes.
    var draft by remember { mutableStateOf(saved) }
    var selected by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(saved) { draft = saved }
    LaunchedEffect(draft.size) {
        if (selected > draft.lastIndex) selected = draft.lastIndex.coerceAtLeast(0)
    }

    TonalCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_home_card_sticker),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.sticker_section_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HomeCardStickerStage(
                backgroundImagePath = state.uiState.homeCardImagePath,
                stickers = draft,
                selectedIndex = selected,
                onSelect = { selected = it },
                onTransformChange = { index, transform ->
                    draft = draft.mapIndexed { i, sticker ->
                        if (i == index) sticker.copy(transform = transform) else sticker
                    }
                },
                onTransformCommit = { index, transform ->
                    actions.onUpdateHomeCardStickerTransform(index, transform)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                accentColor = MaterialTheme.colorScheme.onPrimary,
                accentContentColor = MaterialTheme.colorScheme.primary,
            )

            if (draft.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_home_card_sticker_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = stringResource(R.string.settings_home_card_sticker_drag_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                draft.forEachIndexed { index, sticker ->
                    HomeCardStickerThumbnail(
                        path = sticker.path,
                        selected = index == selected,
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = { selected = index },
                    )
                }
                if (draft.size >= HomeCardSticker.MAX_COUNT) {
                    Text(
                        text = stringResource(R.string.settings_home_card_sticker_limit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (draft.size < HomeCardSticker.MAX_COUNT) {
                    TextButton(
                        onClick = actions.onAddHomeCardSticker,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.settings_home_card_sticker_add))
                    }
                }
                if (draft.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            actions.onRemoveHomeCardSticker(selected.coerceIn(0, draft.lastIndex))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.settings_home_card_sticker_remove))
                    }
                }
            }

            if (draft.isNotEmpty()) {
                val editIndex = selected.coerceIn(0, draft.lastIndex)
                val editTransform = draft[editIndex].transform
                // Recreated per selection so the sliders start from the newly selected sticker.
                key(editIndex) {
                    AppearanceSliderRow(
                        icon = Icons.Rounded.Refresh,
                        title = stringResource(R.string.settings_home_card_sticker_rotation),
                        summary = stringResource(R.string.settings_home_card_sticker_drag_hint),
                        value = editTransform.rotation,
                        valueText = "${editTransform.rotation.roundToInt()}°",
                        range = HomeCardStickerTransform.ROTATION_MIN..HomeCardStickerTransform.ROTATION_MAX,
                        onValueChange = { rotation ->
                            draft = draft.mapIndexed { i, sticker ->
                                if (i == editIndex) {
                                    sticker.copy(transform = sticker.transform.copy(rotation = rotation))
                                } else {
                                    sticker
                                }
                            }
                        },
                        onValueChangeFinished = { rotation ->
                            actions.onUpdateHomeCardStickerTransform(
                                editIndex,
                                editTransform.copy(rotation = rotation.roundToInt().toFloat()),
                            )
                        },
                    )
                    AppearanceSliderRow(
                        icon = Icons.Rounded.AspectRatio,
                        title = stringResource(R.string.settings_home_card_sticker_size),
                        summary = stringResource(R.string.sticker_section_summary),
                        value = editTransform.size,
                        valueText = "${(editTransform.size * 100).roundToInt()}%",
                        range = HomeCardStickerTransform.SIZE_MIN..HomeCardStickerTransform.SIZE_MAX,
                        onValueChange = { size ->
                            draft = draft.mapIndexed { i, sticker ->
                                if (i == editIndex) {
                                    sticker.copy(transform = sticker.transform.copy(size = size))
                                } else {
                                    sticker
                                }
                            }
                        },
                        onValueChangeFinished = { size ->
                            actions.onUpdateHomeCardStickerTransform(
                                editIndex,
                                editTransform.copy(size = size),
                            )
                        },
                    )
                }
            }
        }
    }
}
