package me.weishu.kernelsu.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import me.weishu.kernelsu.ui.component.liquid.rememberCombinedBackdrop
import me.weishu.kernelsu.ui.theme.LocalAppBackgroundBackdrop
import me.weishu.kernelsu.ui.theme.LocalBackgroundImagePath
import me.weishu.kernelsu.ui.theme.LocalCardBackgroundAlpha
import me.weishu.kernelsu.ui.theme.LocalEnableProgressiveBlur
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Blur radius shared by the standard bar blur and its progressive variant. */
const val BarBlurRadius: Float = 25f

/**
 * Solid-enough tint layered over the bar blur. Over a custom background the cards already use
 * [LocalCardBackgroundAlpha], so the bar follows them; without one the tint has to be strong
 * enough to keep the bar chrome readable while content scrolls underneath.
 */
const val OpaqueBarTintAlpha = 0.87f

/**
 * Fade stops of the top-bar veil used when there is no backdrop to blur. Kept low on purpose:
 * the veil only has to separate the title from the wallpaper, not repaint the bar.
 */
private const val TopBarVeilTopAlpha = 0.28f
private const val TopBarVeilBottomAlpha = 0.12f

/**
 * Upper bound for the bar tint over a custom background.
 *
 * [LocalCardBackgroundAlpha] is tuned for small cards and is often pushed to 80-90 % so text stays
 * readable on a busy wallpaper. Reusing that value for the full-width bar turns the whole top of
 * the screen into a white slab, and it is applied twice (the capture layer pre-paints the same
 * tint, then the blur blends tint on top again). The bar only needs enough tint to separate the
 * title from the wallpaper, so clamp it.
 */
const val MaxBarTintOverBackground = 0.38f

@Composable
fun rememberBlurBackdrop(
    enableBlur: Boolean,
    allowProgressive: Boolean = true,
): LayerBackdrop? {
    // The progressive band draws a blur even when the plain bar blur is off, so it needs the same
    // page capture.
    val progressiveBlur = allowProgressive && LocalEnableProgressiveBlur.current &&
        isRuntimeShaderSupported()
    if (!enableBlur && !progressiveBlur) return null
    if (!isRenderEffectSupported()) return null
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val cardAlpha = LocalCardBackgroundAlpha.current
    return rememberLayerBackdrop {
        // Without a custom background the layer has to be opaque on its own. With one the page
        // stays transparent so the wallpaper shows through, but the layer keeps the same
        // translucent tint the cards use: sampling fully transparent pixels bleeds black into
        // whatever blurs this layer.
        if (!hasCustomBackground) {
            drawRect(surfaceColor)
        } else {
            // Only enough tint to keep the blurred sample from bleeding black; the full card
            // opacity here would make every bar that samples this layer look almost white.
            drawRect(surfaceColor.copy(alpha = cardAlpha.coerceAtMost(MaxBarTintOverBackground)))
        }
        drawContent()
    }
}

/**
 * Stacks the app background under the page layer captured by [backdrop].
 *
 * Over a custom background the page layer is transparent wherever the page paints nothing, and
 * blurring those pixels bleeds black into the bar. The wallpaper fills exactly those pixels, so
 * drawing both layers reproduces what is really on screen behind the bar.
 *
 * Returns null when only the page layer is available; [BlurredBar] then falls back to a plain
 * translucent surface instead of blurring an incomplete backdrop.
 */
@Composable
fun rememberBackdropWithAppBackground(backdrop: Backdrop?): Backdrop? {
    if (backdrop == null) return null
    val appBackground = LocalAppBackgroundBackdrop.current
    if (appBackground != null) return rememberCombinedBackdrop(appBackground, backdrop)
    // Over a custom background the page layer alone is transparent wherever nothing is drawn,
    // so leaving it out is only safe when the layer was filled with the opaque surface colour.
    return backdrop.takeIf { LocalBackgroundImagePath.current.isEmpty() }
}

/**
 * Progressive variant of the bar blur: the blur fades out towards the bottom edge instead of
 * stopping at a hard line. Without this the bar's tint ends abruptly and reads as a bright band
 * across the top of the wallpaper.
 */
@Suppress("ModifierFactoryExtensionFunction")
@Composable
private fun rememberProgressiveBarBlurModifier(
    backdrop: Backdrop?,
    gradient: ProgressiveBlur,
    tint: Color,
    tintAlpha: Float,
    blurRadius: Float = BarBlurRadius,
    shape: androidx.compose.ui.graphics.Shape = RectangleShape,
): Modifier {
    if (!isRuntimeShaderSupported()) return Modifier
    val effectiveBackdrop = rememberBackdropWithAppBackground(backdrop) ?: return Modifier
    return androidx.compose.runtime.remember(effectiveBackdrop, gradient, tint, tintAlpha, blurRadius, shape) {
        Modifier.progressiveTextureBlur(
            backdrop = effectiveBackdrop,
            shape = shape,
            blurRadius = blurRadius,
            gradient = gradient,
            colors = BlurColors(
                blendColors = listOf(BlendColorEntry(color = tint.copy(alpha = tintAlpha))),
            ),
        )
    }
}

@Composable
fun BlurredBar(
    backdrop: LayerBackdrop?,
    modifier: Modifier = Modifier,
    scrollBehavior: ScrollBehavior? = null,
    blurActive: Boolean? = null,
    progressiveGradient: ProgressiveBlur = ProgressiveBlur.Top,
    content: @Composable () -> Unit,
) {
    // The bar only paints once the page is scrolled: at rest the wallpaper should run straight to
    // the status bar, which is what the reference build does. Without it the top of every page
    // starts with a tinted band that reads as a white block.
    val isScrolled by remember(scrollBehavior) {
        derivedStateOf {
            scrollBehavior?.state?.collapsedFraction?.let { it > 0.01f } ?: true
        }
    }
    val effectiveBlurActive = blurActive ?: (scrollBehavior == null || isScrolled)
    val progressiveBlur = LocalEnableProgressiveBlur.current && isRuntimeShaderSupported()
    val hasCustomBackground = LocalBackgroundImagePath.current.isNotEmpty()
    val cardAlpha = LocalCardBackgroundAlpha.current
    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurBackdrop = rememberBackdropWithAppBackground(backdrop)
    val tintAlpha = if (hasCustomBackground) {
        cardAlpha.coerceAtMost(MaxBarTintOverBackground)
    } else {
        OpaqueBarTintAlpha
    }

    val blurModifier = if (progressiveBlur && effectiveBlurActive) {
        // Progressive blur fades out towards the content, so the bar has no hard edge. It still
        // waits for the page to scroll: painting it at rest leaves a frosted band over the
        // wallpaper that reads as a white block at the top of the screen.
        // A plain rectangular blur ends with a hard edge and the tint reads as a white slab.
        rememberProgressiveBarBlurModifier(
            backdrop = backdrop,
            gradient = progressiveGradient,
            tint = surfaceColor,
            tintAlpha = tintAlpha,
        )
    } else if (effectiveBlurActive && blurBackdrop != null) {
        Modifier.textureBlur(
            backdrop = blurBackdrop,
            shape = RectangleShape,
            blurRadius = BarBlurRadius,
            colors = BlurColors(
                blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(tintAlpha))),
            ),
        )
    } else if (effectiveBlurActive && hasCustomBackground) {
        // No sampled backdrop to blur. A flat tint here ends abruptly at the bar's bottom edge and
        // reads as a bright band across the top of the wallpaper, so fade it out instead: solid
        // enough at the screen edge to keep the title readable, gone before the content starts.
        Modifier.background(
            Brush.verticalGradient(
                0f to surfaceColor.copy(alpha = TopBarVeilTopAlpha),
                0.65f to surfaceColor.copy(alpha = TopBarVeilBottomAlpha),
                1f to Color.Transparent,
            )
        )
    } else {
        Modifier
    }

    Box(modifier = modifier.then(blurModifier)) {
        content()
    }
}
