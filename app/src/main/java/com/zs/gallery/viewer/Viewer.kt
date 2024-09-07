@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.zs.gallery.viewer

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.FabPosition
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.PlayCircleFilled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.primex.core.SignalWhite
import com.primex.core.findActivity
import com.primex.core.plus
import com.primex.core.rememberVectorPainter
import com.primex.material2.IconButton
import com.zs.domain.store.isImage
import com.zs.domain.store.mediaUri
import com.zs.foundation.AppTheme
import com.zs.foundation.ContentPadding
import com.zs.foundation.LocalWindowSize
import com.zs.foundation.Range
import com.zs.foundation.WindowSize
import com.zs.foundation.adaptive.HorizontalTwoPaneStrategy
import com.zs.foundation.adaptive.StackedTwoPaneStrategy
import com.zs.foundation.adaptive.TwoPane
import com.zs.foundation.adaptive.TwoPaneStrategy
import com.zs.foundation.adaptive.VerticalTwoPaneStrategy
import com.zs.foundation.adaptive.enterAnimation
import com.zs.foundation.adaptive.exitAnimation
import com.zs.foundation.adaptive.margin
import com.zs.foundation.adaptive.padding
import com.zs.foundation.adaptive.shape
import com.zs.foundation.menu.Menu
import com.zs.foundation.menu.MenuItem
import com.zs.foundation.renderAsNavDestBackground
import com.zs.foundation.renderInSharedTransitionScopeOverlay
import com.zs.foundation.sharedElement
import com.zs.foundation.thenIf
import com.zs.gallery.R
import com.zs.gallery.common.LocalNavController
import com.zs.gallery.common.LocalSystemFacade
import com.zs.gallery.common.preferCachedThumbnail
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.saket.telephoto.zoomable.DoubleClickToZoomListener
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

/**
 * The zIndex for sharedBounds of this screen within the SharedTransitionLayout.
 */
private const val SCR_Z_INDEX = 0.3f

private const val EVENT_BACK_PRESS = 0
private const val EVENT_SHOW_INFO = 1
private const val EVENT_IMMERSIVE_VIEW = 2

private val ViewerViewState.index
    get() = if (data.isEmpty()) 0 else data.indexOfFirst { focused == it.id }
private val DEFAULT_ZOOM_SPECS = ZoomSpec(5f)

/**
 * The background color of the app-bar
 */
private val AppBarOverlay =
    Brush.verticalGradient(listOf(Color.Black, Color.Transparent))

/**
 * Scales and centers content based on Painter's size.
 * @param painter Provides intrinsic size for scaling.
 */
private suspend fun ZoomableState.scaledInsideAndCenterAlignedFrom(painter: Painter) {
    // Do nothing if intrinsic size is unknown
    if (painter.intrinsicSize.isUnspecified) return

    // Scale and center content based on intrinsic size
    // TODO - Make this suspend fun instead of runBlocking
    setContentLocation(
        ZoomableContentLocation.scaledInsideAndCenterAligned(
            painter.intrinsicSize
        )
    )
}

/**
 * Indicates whether the content is currently at its default zoom level (not zoomed in).
 */
private val ZoomableState.isZoomedOut get() = zoomFraction == null || zoomFraction == 0f

/**
 * TODO - Instead of opening video in 3rd party; Add inBuilt Impl in future versions.
 */
private fun VideoIntent(uri: Uri) =
    Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "video/*") // Set data and MIME type
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission
    }

@Composable
private fun FloatingActionMenu(
    actions: List<MenuItem>,
    modifier: Modifier = Modifier,
    onAction: (action: MenuItem) -> Unit
) {
    Surface(
        modifier = modifier.scale(0.85f),
        color = AppTheme.colors.background(elevation = 2.dp),
        contentColor = AppTheme.colors.onBackground,
        shape = CircleShape,
        border = BorderStroke(1.dp, AppTheme.colors.background(elevation = 4.dp)),
        elevation = 12.dp,
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.animateContentSize()
            ) {
                Menu(actions, onItemClicked = onAction)
            }
        },
    )
}

@Composable
private fun TopAppBar(
    onRequest: (request: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material.TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                onClick = { onRequest(EVENT_BACK_PRESS) }
            )
        },
        actions = {
            IconButton(
                imageVector = Icons.Default.Info,
                onClick = { onRequest(EVENT_SHOW_INFO) }
            )
        },
        contentColor = Color.White,
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        modifier = modifier.background(AppBarOverlay),
        windowInsets = WindowInsets.statusBars,
    )
}

/**
 * Returns the strategy to use for displaying two panes based on the window size.
 */
private val WindowSize.strategy: TwoPaneStrategy
    get() {
        val (wClazz, hClazz) = this
        return when {
            // If the window is compact (e.g., 360 x 360 || 400 x 400),
            // use a stacked strategy with the dialog at the center.
            wClazz == Range.Compact && hClazz == Range.Compact -> StackedTwoPaneStrategy(0.5f)

            // If the width is greater than the height, use a horizontal strategy
            // that splits the window at 50% of the width.
            wClazz > hClazz -> HorizontalTwoPaneStrategy(0.6f)

            // If the height is greater than the width, use a vertical strategy
            // that splits the window at 50% of the height.
            else -> VerticalTwoPaneStrategy(0.3f)
        }
    }

@Composable
private fun MainContent(
    viewState: ViewerViewState,
    onRequest: (request: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // TODO - Properly handle case, when data is empty
    val values = viewState.data
    if (values.isEmpty()) return

    // Construct the state variables for pager.
    val pager = rememberPagerState(initialPage = viewState.index, pageCount = { values.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val zoomable = rememberZoomableState(DEFAULT_ZOOM_SPECS).apply {
        contentScale = ContentScale.None
    }

    // Modifier for zoomable images,
    // triggering immersive mode on click and handling double-tap zoom
    val zoomableModifier = Modifier.zoomable(
        zoomable,
        onClick = { onRequest(EVENT_IMMERSIVE_VIEW) },
        onDoubleClick = DoubleClickToZoomListener.cycle(2f)
    )

    // TODO - Remove this once inBuilt Video Player is available.
    val vector = rememberVectorPainter(Icons.Outlined.PlayCircleFilled)
    val playIconModifier = Modifier.drawWithCache {
        onDrawWithContent {
            drawContent()
            // overlay icon on it.
            val iconSize = Size(74.dp.toPx(), 74.dp.toPx())
            // draw at the centre of the screen.
            translate(size.width / 2 - iconSize.width / 2, size.height / 2 - iconSize.height / 2) {
                with(vector) {
                    draw(iconSize, alpha = 1f, colorFilter = ColorFilter.tint(Color.White))
                }
            }
        }
    }

    // Handle BackPress
    BackHandler {
        when {
            !zoomable.isZoomedOut -> scope.launch { zoomable.resetZoom() }
            else -> onRequest(EVENT_BACK_PRESS)
        }
    }

    // Horizontal pager to display the images/videos
    // Disable swipe when zoomed in
    // Preload adjacent pages for smoother transitions
    HorizontalPager(
        state = pager,
        key = { values[it].id },
        pageSpacing = 16.dp,
        modifier = modifier,
        userScrollEnabled = zoomable.isZoomedOut,
        beyondViewportPageCount = 1,
    ) { index ->
        val item = values[index]

        // isFocused indicates whether this item is currently the focused item in the viewpager.
        // It's used to selectively apply properties (like shared element modifiers) only to the
        // focused item,
        // ensuring smooth animations and optimized performance by avoiding unnecessary modifications
        // to other items.
        val isFocused = pager.currentPage == index
        // Constructs the painter for this item, handling both images and videos.
        // TODO: Allow user to choose image quality/filter for optimized loading.
        // In success state, update intrinsic size only if specified AND the item is focused.
        // This prevents glitches when transitioning zoom from a non-focused item.
        // For videos, load the preview image; otherwise, load the original image.
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .apply {
                    data(item.mediaUri)
                    if (item.isImage) {
                        // Make sure that image is not loaded from Thumbnail repo.
                        preferCachedThumbnail(false)
                        diskCachePolicy(CachePolicy.DISABLED)
                    }
                    // Placeholder for errors
                    error(R.drawable.ic_error_image_placeholder)
                }.build(),
            onState = {
                if (it is AsyncImagePainter.State.Success && isFocused)
                    scope.launch { zoomable.scaledInsideAndCenterAlignedFrom(it.painter) }
            },
        )

        // if the user navigated to this item
        if (isFocused) {
            viewState.focused = item.id
            runBlocking { zoomable.scaledInsideAndCenterAlignedFrom(painter) }
        }

        // Display the image/video
        // Shared element transition for focused item
        // Apply zoom behavior only to focused, non-error images
        val sharedFrameKey = RouteViewer.buildSharedFrameKey(item.id)
        Image(
            painter = painter,
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .thenIf(isFocused) {
                    sharedElement(sharedFrameKey, zIndexInOverlay = SCR_Z_INDEX + 0.01f)
                }
                .thenIf(isFocused && !item.isImage) {
                    playIconModifier.clickable(null, null) {
                        context.startActivity(VideoIntent(item.mediaUri))
                    }
                }
                .thenIf(item.isImage && isFocused && painter.state !is AsyncImagePainter.State.Error) {
                    zoomableModifier
                }
                .fillMaxSize()
        )
    }
}

@Composable
fun Viewer(
    viewState: ViewerViewState
) {
    // obtain required dependencies
    val navController = LocalNavController.current
    val facade = LocalSystemFacade.current
    val context = LocalContext.current

    // Define required state variables
    var immersive by remember { mutableStateOf(false) }
    val onRequest: (Int) -> Unit = { request: Int ->
        when (request) {
            // Toggle the visibility of detailed information.
            EVENT_SHOW_INFO -> {
                viewState.showDetails = !viewState.showDetails;
                immersive = viewState.showDetails
            }
            // Toggle immersive mode and update system UI accordingly.
            EVENT_IMMERSIVE_VIEW -> {
                immersive = !immersive
                facade.enableEdgeToEdge(immersive, false, false)
            }
            // Handle back press events, prioritizing focused states (immersive, details)
            // before navigating up.
            EVENT_BACK_PRESS -> {
                when {
                    // consume in making not immersive
                    immersive -> {
                        immersive = false
                        facade.enableEdgeToEdge(false, false, false)
                    }
                    // consume in hiding the details this action
                    viewState.showDetails -> viewState.showDetails = false
                    // Navigate up if no focused states
                    else -> navController.navigateUp()
                }
            }
            // Handle unexpected events
            else -> error("Unknown event: $request")
        }
    }

    // Layout
    val strategy = LocalWindowSize.current.strategy
    TwoPane(
        fabPosition = FabPosition.Center,
        strategy = strategy,
        background = Color.Black,
        onColor = Color.SignalWhite,
        content = {
            MainContent(
                viewState = viewState,
                onRequest = onRequest,
                modifier = Modifier.fillMaxSize()
            )
        },
        topBar = {
            AnimatedVisibility(
                visible = !immersive,
                enter = fadeIn(),
                exit = fadeOut(),
                content = {
                    TopAppBar(
                        onRequest,
                        modifier = Modifier.renderInSharedTransitionScopeOverlay(SCR_Z_INDEX + 0.02f)
                    )
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !immersive,
                enter = fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                content = {
                    FloatingActionMenu(
                        actions = viewState.actions,
                        onAction = {
                            viewState.onAction(it, context.findActivity())
                        },
                        modifier = Modifier
                            .renderInSharedTransitionScopeOverlay(SCR_Z_INDEX + 0.02f)
                            .padding(bottom = AppTheme.padding.normal),
                    )
                }
            )
        },
        onDismissRequest = { viewState.showDetails = false },
        modifier = Modifier.renderAsNavDestBackground(SCR_Z_INDEX),
        details = {
            val details = viewState.details
            AnimatedVisibility(
                details != null,
                enter = strategy.enterAnimation,
                exit = strategy.exitAnimation
            ) {
                Details(
                    details ?: return@AnimatedVisibility,
                    viewState.actions,
                    shape = strategy.shape,
                    contentPadding = strategy.padding + PaddingValues(horizontal = ContentPadding.medium),
                    onAction = {
                        viewState.onAction(it, context.findActivity())
                    },
                    modifier = Modifier.padding(strategy.margin)
                )
            }
        }
    )

    // set/reset
    DisposableEffect(key1 = Unit) {
        facade.enableEdgeToEdge(dark = false, translucent = false)
        onDispose {
            // Reset to default on disposal
            facade.enableEdgeToEdge()
        }
    }
}