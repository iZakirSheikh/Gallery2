/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 11-07-2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zs.foundation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.ScaleToBounds
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.core.graphics.ColorUtils
import com.primex.core.MetroGreen2
import com.primex.core.OrientRed
import com.primex.core.Rose
import com.primex.core.SignalWhite
import com.primex.core.SkyBlue
import com.primex.core.TrafficYellow
import com.primex.core.UmbraGrey
import com.primex.core.hsl
import com.zs.foundation.AppTheme.colors
import com.zs.foundation.AppTheme.elevation
import com.zs.foundation.AppTheme.padding
import com.zs.foundation.AppTheme.shapes
import com.zs.foundation.AppTheme.typography
import com.zs.foundation.Colors.accent
import com.zs.foundation.Colors.background
import com.zs.foundation.Colors.error
import com.zs.foundation.Colors.onAccent
import com.zs.foundation.Colors.onBackground
import com.zs.foundation.ContentElevation.high
import com.zs.foundation.ContentElevation.low
import com.zs.foundation.ContentElevation.medium
import com.zs.foundation.ContentElevation.none
import com.zs.foundation.ContentElevation.xHigh
import com.zs.foundation.ContentPadding.large
import com.zs.foundation.ContentPadding.medium
import com.zs.foundation.ContentPadding.normal
import com.zs.foundation.ContentPadding.small
import com.zs.foundation.ContentPadding.xLarge
import com.zs.foundation.Typography.bodyLarge
import com.zs.foundation.Typography.bodyMedium
import com.zs.foundation.Typography.button
import com.zs.foundation.Typography.caption
import com.zs.foundation.Typography.displayLarge
import com.zs.foundation.Typography.displayMedium
import com.zs.foundation.Typography.displaySmall
import com.zs.foundation.Typography.headlineLarge
import com.zs.foundation.Typography.headlineMedium
import com.zs.foundation.Typography.headlineSmall
import com.zs.foundation.Typography.overline
import com.zs.foundation.Typography.titleLarge
import com.zs.foundation.Typography.titleMedium
import com.zs.foundation.Typography.titleSmall
import kotlin.math.ln

private const val TAG = "AppTheme"

/**
 * Provides a set of colors that represent the application's color palette,
 * building upon the colors provided by [MaterialTheme].*
 * @property accent The accent color of the application.
 * @property background The background color of the application.
 * @property onAccent The color used for text and icons displayed on top of the [accent] color.
 * @property onBackground The color used for text and icons displayedon top of the [background] color.
 * @property error The color used to indicate errors in the application.
 */
object Colors {

    val accent: Color @ReadOnlyComposable @Composable inline get() = MaterialTheme.colors.primary
    val background: Color @ReadOnlyComposable @Composable inline get() = MaterialTheme.colors.background

    /**
     * Calculates a background color with an overlay based on the provided elevation.
     *
     * @param elevation The elevation value to calculate the overlay alpha for.
     * @return A [Color] representing the background with an overlay.*/
    @Composable
    @ReadOnlyComposable
    fun background(elevation: Dp) =
        applyTonalElevation(accent, background, if (isLight) elevation else 0.5f * elevation)

    val onAccent: Color @ReadOnlyComposable @Composable inline get() = MaterialTheme.colors.onPrimary
    val onBackground @ReadOnlyComposable @Composable inline get() = MaterialTheme.colors.onBackground
    val error @ReadOnlyComposable @Composable inline get() = MaterialTheme.colors.error
    val onError @ReadOnlyComposable @Composable inline get() = MaterialTheme.colors.onError
    val isLight @ReadOnlyComposable @Composable inline get() = MaterialTheme.colors.isLight
    val lightShadowColor
        @Composable @ReadOnlyComposable inline get() = if (isLight) Color.White else Color.White.copy(
            0.025f
        )
    val darkShadowColor
        @Composable @ReadOnlyComposable inline get() = if (isLight) Color(0xFFAEAEC0).copy(0.7f) else Color.Black.copy(
            0.6f
        )
}

private fun applyTonalElevation(accent: Color, background: Color, elevation: Dp) =
    accent.copy(alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f)
        .compositeOver(background)

/**
 * A variant of [MaterialTheme.shapes.small] with a corner radius of 8dp.
 */
private val small2 = RoundedCornerShape(8.dp)

object Shapes {
    /**
     * A variant of [androidx.compose.material.Shapes.small] with a corner radius of 8dp.
     */
    val compact get() = small2

    /**
     * @see androidx.compose.material.Shapes.medium
     */
    val small @Composable @ReadOnlyComposable inline get() = MaterialTheme.shapes.small

    /**
     * @see androidx.compose.material.Shapes.medium
     */
    val medium @Composable @ReadOnlyComposable inline get() = MaterialTheme.shapes.medium

    /**
     * @see androidx.compose.material.Shapes.large
     */
    val large @Composable @ReadOnlyComposable inline get() = MaterialTheme.shapes.large
}

/**
 * Provides a set of text styles that represent the application's typography,
 * building upon the typography provided by [MaterialTheme].This object bridges
 * Material 2 and Material 3 typography styles for a consistent experience.
 *
 * @property displayLarge The largest display style. Use sparingly for short, prominent text.
 *   Corresponds to [androidx.compose.material3.Typography.displayLarge] in Material 3.
 * @property displayMedium A large display style for short, important text.
 *   Corresponds to [androidx.compose.material3.Typography.displayMedium] in Material 3.
 * @property displaySmall A smaller display style for short text elements.
 *   Corresponds to [androidx.compose.material3.Typography.displaySmall] in Material 3.
 * @property headlineLarge A large headline style for prominent headings.
 *   Corresponds to [androidx.compose.material3.Typography.headlineLarge] inMaterial 3.
 * @property headlineMedium A medium headline style for section headings.
 *   Corresponds to [androidx.compose.material3.Typography.headlineMedium] in Material 3.
 * @property headlineSmall A smaller headline style for sub-section headings.
 *   Corresponds to [androidx.compose.material3.Typography.headlineSmall] in Material 3.
 * @property titleLarge A large title style for important titles.
 *   Corresponds to [androidx.compose.material3.Typography.titleLarge] in Material 3.
 * @property titleMedium A medium title style for less prominent titles.
 *   Corresponds to [androidx.compose.material3.Typography.titleMedium] in Material 3.
 * @property titleSmall A small title style for short titles.
 *   Corresponds to [androidx.compose.material3.Typography.titleSmall] in Material 3.
 * @property bodyLarge The default body style for longer text blocks.
 *   Corresponds to [androidx.compose.material3.Typography.bodyLarge] in Material 3.
 * @property bodyMedium A smaller body style for less importanttext.
 *   Corresponds to [androidx.compose.material3.Typography.bodyMedium] in Material 3.
 * @property caption A small text style for captions and labels.
 *   Corresponds to [androidx.compose.material3.Typography.bodySmall] in Material 3.
 * @property overline A very small text style for short, subtle text.
 *   Corresponds to [androidx.compose.material3.Typography.labelSmall] in Material 3.
 * @property button The text style to use for buttons.
 *   Corresponds to [androidx.compose.material3.Typography.labelMedium] in Material 3.
 */
object Typography {

    val displayLarge @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.h1
    val displayMedium @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.h2
    val displaySmall @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.h3

    val headlineLarge @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.h4

    // TODO - find alternative of this.
    val headlineMedium @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.h4
    val headlineSmall @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.h5
    val titleLarge @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.h6
    val titleMedium @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.subtitle1
    val titleSmall @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.subtitle2
    val bodyLarge @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.body1
    val bodyMedium @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.body2
    val caption @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.caption
    val overline @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.overline
    val button @Composable @ReadOnlyComposable inline get() = MaterialTheme.typography.button
}

/**
 * Provides a setof standard content padding values to ensure consistency across the application.
 *
 * @property small A small 4 [Dp] padding.
 * @property medium A medium 8 [Dp] padding.
 * @property normal Normal 16 [Dp] padding.
 * @property large Large22 [Dp] padding.
 * @property xLarge Extra large 32 [Dp] padding.
 */
object ContentPadding {
    val small: Dp = 4.dp
    val medium: Dp = 8.dp
    val normal: Dp = 16.dp
    val large: Dp = 22.dp
    val xLarge: Dp = 32.dp
}

/**
 * Provides a set of standard elevation values to maintain visual consistency across the application.
 *
 * @property none Zero elevation.
 * @property low Low elevationof 6 [Dp].
 * @property medium Medium elevation of 12 [Dp].
 * @property high High elevation of 20 [Dp].
 * @property xHigh Extra high elevation of 30 [Dp].
 */
object ContentElevation {
    val none = 0.dp
    val low = 6.dp
    val medium = 12.dp
    val high = 20.dp
    val xHigh = 30.dp
}

/**
 * The recommended divider Alpha
 */
val ContentAlpha.Divider
    get() = com.zs.foundation.Divider
private const val Divider = 0.12f

/**
 * The recommended LocalIndication Alpha
 */
val ContentAlpha.Indication
    get() = com.zs.foundation.Indication
private const val Indication = 0.1f


/**
 * Provides a standard interface to interact with the underlying theme, offering a unified and
 * consistent visual experience across the application. Insteadof using [MaterialTheme] directly,
 * utilize this object to set up the theme and access its core elements.
 *
 * Built on top of Material Design, it incorporates best practices from both Material 2 and Material 3,
 * providing a seamless transition between the two. It also includes default standard padding,
 * elevation, and alpha values for convenience and consistency.
 *
 * @property typography A collection of text styles that define the application's typography hierarchy.
 * @property shapes A set of shape definitions that determine the outlines and contours of UI elements.
 * @property colors The color palette used throughout the application, ensuring a harmonious visual language.
 * @property padding Standard content padding values to ensure consistency across the application.
 * @property elevation Standard elevation values to maintain visual consistency across the application.
 * @property alpha Standard alpha values for different UI elements and states.
 *
 *Internally, it leverages the underlying Material theme, ensuring a streamlined and efficient theming experience.
 */
object AppTheme {
    val typography: Typography get() = com.zs.foundation.Typography
    val shapes: Shapes get() = com.zs.foundation.Shapes
    val colors: Colors get() = com.zs.foundation.Colors
    val padding get() = ContentPadding
    val elevation get() = ContentElevation

    @OptIn(ExperimentalSharedTransitionApi::class)
    val sharedTransitionScope
        @Composable
        @ReadOnlyComposable
        get() = LocalSharedTransitionScope.current
}


/**
 * Creates a [Colors] instance with the provided color values, configuring a Material color palette.
 *
 * @param accent The primary accent color of the application. Defaults to [Color.MetroGreen2].
 * @param background The background color of the application. Defaults to [Color.White].
 * @param onBackground The color used for text and icons displayed on top of the [background] color. Defaults to [Color.UmbraGrey].
 * @param onAccent The color used for text and icons displayed on top of the [accent] color. Defaults to [Color.SignalWhite].
 * @param error The color used to indicate errors in the application. Defaults to [Color.Rose].
 * @param onError The color used for text and icons displayed on top of the [error] color. Defaults to [Color.SignalWhite].
 * @param isLight Whether the theme is considered light or dark. Defaults to `true`.
 *
 * @return A [Colors] instance representing the configured Material color palette.
 */
private fun Colors(
    accent: Color = Color.MetroGreen2,
    background: Color = Color.White,
    onBackground: Color = Color.UmbraGrey,
    onAccent: Color = Color.SignalWhite,
    error: Color = Color.Rose,
    onError: Color = Color.SignalWhite,
    isLight: Boolean = true,
) = androidx.compose.material.Colors(
    primary = accent,
// darken it a bit
    primaryVariant = accent.hsl(lightness = accent.lightness * 0.9f),
    secondary = accent,
    secondaryVariant = accent.hsl(lightness = accent.lightness * 0.9f),
    background = background,
    surface = applyTonalElevation(accent, background, 1.dp),
    error = error,
    onPrimary = onAccent,
    onSecondary = onAccent,
    onBackground = onBackground,
    onSurface = onBackground,
    onError = onError,
    isLight = isLight,
)

/**
 * Calculates and returns the lightness component of this color in the HSL color space.
 * The lightness value is represented as afloat in the range [0.0, 1.0], where 0.0 is the darkest
 * and 1.0 is the lightest.
 */
private val Color.lightness: Float
    get() {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(toArgb(), hsl)
        return hsl[2]
    }

private val DefaultColorSpec = tween<Color>(AnimationConstants.DefaultDurationMillis)

/**
 * Provides a composable function to set up the application's theme using the provided
 * colors, typography, and shapes.
 *
 * @param isLight  if true, applies the light theme.
 * @param fontFamily  the font family to be used in the theme.
 * @param content  the composable content to be displayed within the theme.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppTheme(
    isLight: Boolean,
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (!isLight) Color(0xFF0E0E0F) else Color(0xFFF5F5FA),
        animationSpec = DefaultColorSpec
    )
    val accent = if (!isLight) Color.TrafficYellow else Color.SkyBlue
    val colors = Colors(
        accent = accent,
        background = background,
        onBackground = if (isLight) Color.UmbraGrey else Color.SignalWhite,
        onAccent = Color.SignalWhite,
        error = Color.OrientRed,
        onError = Color.SignalWhite,
        isLight = isLight,
    )

    // Actual theme compose; in future handle fonts etc.
    SharedTransitionLayout {
        MaterialTheme(
            colors = colors,
            content = {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this,
                    content = content
                )
            },
            typography = androidx.compose.material.Typography(defaultFontFamily = fontFamily)
        )
    }
}

/**
 * Provides a [CompositionLocal] to access the current [SharedTransitionScope].
 *
 * This CompositionLocal should be provided bya parent composable that manages shared transitions.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope> {
        error("CompositionLocal LocalSharedTransition not present")
    }

/**
 * Provides a[CompositionLocal] to access the current [AnimatedVisibilityScope].
 *
 * This CompositionLocal should be provided by a parent composable that manages animated visibility.
 */
val LocalNavAnimatedVisibilityScope =
    staticCompositionLocalOf<AnimatedVisibilityScope> { error("CompositionLocal LocalSharedTransition not present") }

private val DefaultSpring = spring(
    stiffness = StiffnessMediumLow,
    visibilityThreshold = Rect.VisibilityThreshold
)

@ExperimentalSharedTransitionApi
private val DefaultBoundsTransform = BoundsTransform { _, _ -> DefaultSpring }

@ExperimentalSharedTransitionApi
private val ParentClip: OverlayClip =
    object : OverlayClip {
        override fun getClipPath(
            state: SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density
        ): Path? {
            return state.parentSharedContentState?.clipPathInOverlay
        }
    }

/**
 * @see androidx.compose.animation.SharedTransitionScope.sharedElement
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedElement(
    state: SharedContentState,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeHolderSize: PlaceHolderSize = contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip
) = composed {
    val navAnimatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        Modifier.sharedElement(
            state = state,
            placeHolderSize = placeHolderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            animatedVisibilityScope = navAnimatedVisibilityScope,
            boundsTransform = boundsTransform,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition
        )
    }
}


/**
 * A shared bounds modifier that uses scope from [AppTheme]'s [AppTheme.sharedTransitionScope] and [AnimatedVisibilityScope] from [LocalNavAnimatedVisibilityScope]
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedBounds(
    sharedContentState: SharedContentState,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    resizeMode: ResizeMode = ScaleToBounds(ContentScale.FillWidth, Center),
    placeHolderSize: PlaceHolderSize = contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip
) = composed {
    val navAnimatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        Modifier.sharedBounds(
            sharedContentState = sharedContentState,
            animatedVisibilityScope = navAnimatedVisibilityScope,
            enter = enter,
            exit = exit,
            boundsTransform = boundsTransform,
            resizeMode = resizeMode,
            placeHolderSize = placeHolderSize,
            renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition
        )
    }
}

/**
 * @return the state of shared contnet corresponding to [key].
 * @see androidx.compose.animation.SharedTransitionScope.rememberSharedContentState
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
inline fun rememberSharedContentState(key: Any): SharedContentState =
    with(AppTheme.sharedTransitionScope) {
        rememberSharedContentState(key = key)
    }


/**
 * @see androidx.compose.animation.SharedTransitionScope.sharedElement
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedElement(
    key: Any,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeHolderSize: PlaceHolderSize = contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip
) = composed {
    sharedElement(
        state = rememberSharedContentState(key = key),
        boundsTransform,
        placeHolderSize,
        renderInOverlayDuringTransition,
        zIndexInOverlay,
        clipInOverlayDuringTransition
    )
}

/**
 * @see androidx.compose.animation.SharedTransitionScope.sharedBounds
 */
@OptIn(ExperimentalSharedTransitionApi::class)
fun Modifier.sharedBounds(
    key: Any,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    resizeMode: ResizeMode = ScaleToBounds(ContentScale.FillWidth, Center),
    placeHolderSize: PlaceHolderSize = contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip
) = composed {
    sharedBounds(
        sharedContentState = rememberSharedContentState(key = key),
        enter = enter,
        exit = exit,
        boundsTransform = boundsTransform,
        resizeMode = resizeMode,
        placeHolderSize = placeHolderSize,
        renderInOverlayDuringTransition = renderInOverlayDuringTransition,
        zIndexInOverlay = zIndexInOverlay,
        clipInOverlayDuringTransition = clipInOverlayDuringTransition
    )
}

private val DefaultClipInOverlayDuringTransition: (LayoutDirection, Density) -> Path? =
    { _, _ -> null }

/**
 * @param renderInOverlay pass null to make this fun handle with default strategy.
 * @see androidx.compose.animation.SharedTransitionScope.renderInSharedTransitionScopeOverlay
 */
fun Modifier.renderInSharedTransitionScopeOverlay(
    zIndexInOverlay: Float = 0f,
    renderInOverlay: (() -> Boolean)? = null,
    clipInOverlayDuringTransition: (LayoutDirection, Density) -> Path? =
        DefaultClipInOverlayDuringTransition
) = composed {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    with(sharedTransitionScope) {
        Modifier.renderInSharedTransitionScopeOverlay(
            renderInOverlay = renderInOverlay ?: { isTransitionActive },
            zIndexInOverlay = zIndexInOverlay,
            clipInOverlayDuringTransition = clipInOverlayDuringTransition
        )
    }
}


fun Modifier.renderAsNavDestBackground(zIndexInOverlay: Float) =
    this then Modifier.sharedBounds(
        key = "nav_dest_background",
        zIndexInOverlay = zIndexInOverlay,
    )