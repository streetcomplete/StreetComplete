package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.screens.main.map.toImageBitmap
import de.westnordost.streetcomplete.screens.main.map.toSdf
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState

/** An app-owned image which may be installed in the current MapLibre style. */
internal data class DynamicStyleImage(
    val id: String,
    val painter: Painter,
    val density: Density,
    val layoutDirection: LayoutDirection,
    val size: DpSize? = null,
    val colorFilter: ColorFilter? = null,
    val sdf: Boolean = false,
    val cacheKey: Any = id,
)

/**
 * Rasterizes and installs the image families registered by the current map content.
 *
 * The bitmap cache survives base-style reloads. Loaded-style tracking stays here only until the
 * snapshot exposes MapState's missing-image resolver, which can own that lifecycle itself.
 */
@Stable
internal class DynamicStyleImageRegistry {
    private val imagesByOwner = MutableStateFlow<Map<String, List<DynamicStyleImage>>>(emptyMap())
    val images = MutableStateFlow<Map<String, DynamicStyleImage>>(emptyMap())
    private val bitmapCache = mutableMapOf<Any, RasterizedDynamicStyleImage>()
    private val bitmapCacheMutex = Mutex()
    private val installations = DynamicStyleImageInstallations()
    private val installedImageIds = MutableStateFlow<Set<String>>(emptySet())

    fun replace(owner: String, ownerImages: List<DynamicStyleImage>) {
        if (imagesByOwner.value[owner]?.map(DynamicStyleImage::cacheKey) ==
            ownerImages.map(DynamicStyleImage::cacheKey)
        ) return
        imagesByOwner.update { current ->
            current + (owner to ownerImages)
        }
        images.value = imagesByOwner.value.values.flatten().associateBy(DynamicStyleImage::id)
    }

    fun remove(owner: String) {
        imagesByOwner.update { it - owner }
        images.value = imagesByOwner.value.values.flatten().associateBy(DynamicStyleImage::id)
    }

    suspend fun resolve(id: String): RasterizedDynamicStyleImage? {
        val image = images.value[id] ?: return null
        return bitmapCacheMutex.withLock {
            bitmapCache[image.cacheKey] ?: withContext(Dispatchers.Default) {
                val bitmap = image.painter.toImageBitmap(
                    density = image.density,
                    layoutDirection = image.layoutDirection,
                    size = image.size,
                    colorFilter = image.colorFilter,
                ).let { if (image.sdf) it.toSdf() else it }
                RasterizedDynamicStyleImage(
                    bitmap = bitmap,
                    sdf = image.sdf,
                )
            }.also { bitmapCache[image.cacheKey] = it }
        }
    }

    fun pendingImageIds(
        baseStyle: Any?,
        loadState: StyleLoadState,
        registeredIds: Set<String>,
    ): Set<String> {
        val pending = installations.pendingImageIds(baseStyle, loadState, registeredIds)
        installedImageIds.value = if (loadState == StyleLoadState.Ready) {
            registeredIds - pending
        } else {
            emptySet()
        }
        return pending
    }

    fun recordInstalled(id: String) {
        installations.recordInstalled(id)
        installedImageIds.update { it + id }
    }

    suspend fun awaitInstalled(ids: Set<String>) {
        if (ids.isEmpty()) return
        installedImageIds.first { installed -> installed.containsAll(ids) }
    }
}

/** Installation state survives temporary removal of the map host from composition. */
internal class DynamicStyleImageInstallations {
    private var baseStyleInitialized = false
    private var baseStyle: Any? = null
    private var hasReachedReady = false
    private var lostLoadedStyle = false
    private val installedIds = mutableSetOf<String>()

    fun pendingImageIds(
        currentBaseStyle: Any?,
        loadState: StyleLoadState,
        registeredIds: Set<String>,
    ): Set<String> {
        if (!baseStyleInitialized || baseStyle != currentBaseStyle) {
            baseStyleInitialized = true
            baseStyle = currentBaseStyle
            installedIds.clear()
        }
        if (loadState != StyleLoadState.Ready && hasReachedReady) {
            lostLoadedStyle = true
        }
        if (loadState == StyleLoadState.Ready && lostLoadedStyle) {
            lostLoadedStyle = false
            installedIds.clear()
        }
        if (loadState == StyleLoadState.Ready) hasReachedReady = true
        return if (loadState == StyleLoadState.Ready) {
            registeredIds - installedIds
        } else {
            emptySet()
        }
    }

    fun recordInstalled(id: String) {
        installedIds += id
    }
}

internal data class RasterizedDynamicStyleImage(
    val bitmap: androidx.compose.ui.graphics.ImageBitmap,
    val sdf: Boolean,
)

/** One style-wide namespace for plain overlay and geometry-marker icons, matching master's cache. */
internal fun plainStyleImageId(resource: DrawableResource): String =
    "map-icon-${resource.id ?: error("Map icon is not a Compose resource")}"

@Composable
internal fun rememberPlainStyleImages(
    resources: List<DrawableResource>,
): List<DynamicStyleImage> {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    return resources.mapNotNull { resource ->
        val resourceId = resource.id ?: return@mapNotNull null
        val id = plainStyleImageId(resource)
        key(id) {
            DynamicStyleImage(
                id = id,
                painter = painterResource(resource),
                density = density,
                layoutDirection = layoutDirection,
                size = null,
                sdf = resourceId.startsWith("preset_"),
                cacheKey = listOf(
                    "plain-map-icon",
                    resourceId,
                    density.density,
                    density.fontScale,
                    layoutDirection,
                ),
            )
        }
    }
}

@Composable
internal fun BindDynamicStyleImages(
    mapState: MapState,
    registry: DynamicStyleImageRegistry,
) {
    LaunchedEffect(mapState, registry) {
        combine(
            snapshotFlow { mapState.style.baseStyle to mapState.style.loadState },
            registry.images,
        ) { style, images -> Triple(style.first, style.second, images) }
            .collectLatest { (currentBaseStyle, loadState, images) ->
                val ids = registry.pendingImageIds(
                    currentBaseStyle,
                    loadState,
                    images.keys,
                )
                if (ids.isEmpty()) return@collectLatest
                ids.forEach { id ->
                    val image = registry.resolve(id) ?: return@forEach
                    withFrameNanos {}
                    while (true) {
                        snapshotFlow { mapState.style.loadState }
                            .first { it == StyleLoadState.Ready }
                        try {
                            withContext(NonCancellable) {
                                withContext(Dispatchers.Default) {
                                    mapState.style.images.add(id, image.bitmap, sdf = image.sdf)
                                }
                                registry.recordInstalled(id)
                            }
                            break
                        } catch (error: IllegalStateException) {
                            if (!error.isStyleHandleRace()) throw error
                            withFrameNanos {}
                        }
                    }
                }
            }
    }
}

@Composable
internal fun RegisterDynamicStyleImages(
    registry: DynamicStyleImageRegistry,
    owner: String,
    images: List<DynamicStyleImage>,
) {
    SideEffect { registry.replace(owner, images) }
    DisposableEffect(registry, owner) {
        onDispose { registry.remove(owner) }
    }
}
