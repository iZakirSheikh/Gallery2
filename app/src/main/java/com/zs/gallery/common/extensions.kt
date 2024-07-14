/*
 * Copyright 2024 Zakir Sheikh
 *
 * Created by Zakir Sheikh on 12-07-2024.
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

package com.zs.gallery.common

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.primex.core.composableOrNull
import com.primex.material2.Label
import com.primex.material2.Text
import com.zs.compose_ktx.AppTheme
import com.zs.compose_ktx.lottieAnimationPainter

/**
 * Used to provide access to the [NavHostController] through composition without needing to pass it down the tree.
 *
 * To use this composition local, you can call [LocalNavController.current] to get the [NavHostController].
 * If no [NavHostController] has been set, an error will be thrown.
 *
 * Example usage:
 *
 * ```
 * val navController = LocalNavController.current
 * navController.navigate("destination")
 * ```
 */
val LocalNavController =
    staticCompositionLocalOf<NavHostController> {
    error("no local nav host controller found")
}

/**
 * Returns the current route of the [NavHostController]
 */
val NavHostController.current
    @Composable inline get() = currentBackStackEntryAsState().value?.destination

/**
 * Extracts the domain portion from a [NavDestination]'s route.
 *
 * The domain is considered to be the part ofthe route before the first '/'.
 * For example, for the route "settings/profile", the domain would be "settings".
 *
 * @return The domain portion of the route, or null if the route is null or does not contain a '/'.
 */
val NavDestination.domain:String? get() {
    // Get the route, or return null if it's not available.
    val route = route ?: return null

    // Find the index of the first '/' character.
    val index = route.indexOf('/')

    // Return the substring before the '/' if it exists, otherwise return the entire route.
    return if (index == -1) route else route.substring(0, index)
}

//This file holds the simple extension, utility methods of compose.
/**
 * Composes placeholder with lottie icon.
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun Placeholder(
    title: String,
    modifier: Modifier = Modifier,
    vertical: Boolean = true,
    @RawRes iconResId: Int,
    message: CharSequence? = null,
    noinline action: @Composable (() -> Unit)? = null
) {
    com.primex.material2.Placeholder(
        modifier = modifier, vertical = vertical,
        message = composableOrNull(message != null) {
            Text(
                text = message!!,
                color = AppTheme.colors.onBackground
            )
        },
        title = {
            Label(
                text = title.ifEmpty { " " },
                maxLines = 2,
                color = AppTheme.colors.onBackground
            )
        },
        icon = {
            Image(
                painter = lottieAnimationPainter(id = iconResId),
                contentDescription = null
            )
        },
        action = action,
    )
}

