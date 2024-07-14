package com.zs.gallery

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.primex.core.getText2
import com.primex.preferences.Key
import com.primex.preferences.Preferences
import com.primex.preferences.observeAsState
import com.zs.compose_ktx.LocalWindowSize
import com.zs.compose_ktx.calculateWindowSizeClass
import com.zs.compose_ktx.toast.ToastHostState
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.SystemFacade
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Manages SplashScreen
 */
context(ComponentActivity)
private fun configureSplashScreen(isColdStart: Boolean) {
    // Installs the Splash Screen provided by the SplashScreen compat library
    installSplashScreen().let { screen ->
        // Only animate the exit if this is a cold start of the app
        if (!isColdStart) return@let
        // Configure the exit animation to play when the splash screen is ready to be removed
        screen.setOnExitAnimationListener { provider ->
            val splashScreenView = provider.view
            // Create a fade-out animation for the splash screen
            val alpha = ObjectAnimator.ofFloat(
                splashScreenView, View.ALPHA, 1f, 0f
            )
            alpha.interpolator = AnticipateInterpolator()
            alpha.duration = 700L
            // Remove the splash screen after the animation completes
            alpha.doOnEnd { provider.remove() }
            // Start the fade-out animation
            alpha.start()
        }
    }
}

class MainActivity : ComponentActivity(), SystemFacade {

    private val toastHostState: ToastHostState by inject()
    private val preferences: Preferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app has started from scratch if savedInstanceState is null.
        val isColdStart = savedInstanceState == null //why?
        // Manage SplashScreen
        configureSplashScreen(isColdStart)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(activity = this)
            CompositionLocalProvider(
                LocalWindowSize provides windowSizeClass,
                // Disable absolute elevation.
                LocalAbsoluteElevation provides 0.dp,
                LocalSystemFacade provides this,
                content = { Home(toastHostState) }
            )
        }
    }

    override fun showToast(
        message: CharSequence,
        icon: ImageVector?,
        action: CharSequence?,
        duration: Int
    ) {
        lifecycleScope.launch {
            toastHostState.showToast(message, action, icon, duration = duration)
        }
    }

    override fun showToast(message: Int, icon: ImageVector?, action: Int, duration: Int) {
        lifecycleScope.launch {
            toastHostState.showToast(
                resources.getText2(id = message),
                if (action == ResourcesCompat.ID_NULL) null else resources.getText2(action),
                icon,
                duration = duration
            )
        }
    }

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key.Key1<S, O>): State<O?> =
        preferences.observeAsState(key = key)

    @Composable
    @NonRestartableComposable
    override fun <S, O> observeAsState(key: Key.Key2<S, O>): State<O> =
        preferences.observeAsState(key = key)
}

