package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import de.westnordost.streetcomplete.screens.main.map.isStyleHandleRace
import de.westnordost.streetcomplete.screens.main.map.toImageBitmap
import de.westnordost.streetcomplete.screens.main.map.toSdf
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.map.MapEvent
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
 * Rasterizes and eagerly installs the dynamic image families registered by the current map content.
 * The bitmap cache survives base-style reloads, while installed IDs belong to one loaded style
 * generation.
 */
@Stable
internal class DynamicStyleImageRegistry {
    private val imagesByOwner = MutableStateFlow<Map<String, List<DynamicStyleImage>>>(emptyMap())
    internal val images = MutableStateFlow<Map<String, DynamicStyleImage>>(emptyMap())
    private val bitmapCache = mutableMapOf<Any, RasterizedDynamicStyleImage>()
    private val bitmapCacheMutex = Mutex()
    internal val styleGeneration = MutableStateFlow(0L)
    private val installedImageIds = MutableStateFlow<Set<String>>(emptySet())
    val installedImages: StateFlow<Set<String>> = installedImageIds

    fun replace(owner: String, ownerImages: List<DynamicStyleImage>) {
        if (
            imagesByOwner.value[owner]?.map(DynamicStyleImage::cacheKey) ==
                ownerImages.map(DynamicStyleImage::cacheKey)
        )
            return
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
            bitmapCache[image.cacheKey]
                ?: withContext(Dispatchers.Default) {
                        val bitmap =
                            image.painter
                                .toImageBitmap(
                                    density = image.density,
                                    layoutDirection = image.layoutDirection,
                                    size = image.size,
                                    colorFilter = image.colorFilter,
                                )
                                .let { if (image.sdf) it.toSdf() else it }
                        RasterizedDynamicStyleImage(
                            bitmap = bitmap,
                            sdf = image.sdf,
                        )
                    }
                    .also { bitmapCache[image.cacheKey] = it }
        }
    }

    fun onStyleLoaded() {
        installedImageIds.value = emptySet()
        styleGeneration.update { it + 1 }
    }

    fun pendingImageIds(registeredIds: Set<String>): Set<String> =
        registeredIds - installedImageIds.value

    fun recordInstalled(id: String, generation: Long) {
        if (styleGeneration.value == generation) installedImageIds.update { it + id }
    }
}

internal data class RasterizedDynamicStyleImage(
    val bitmap: androidx.compose.ui.graphics.ImageBitmap,
    val sdf: Boolean,
)

/**
 * One style-wide namespace for plain overlay and geometry-marker icons, matching master's cache.
 */
internal fun plainStyleImageId(resource: DrawableResource): String =
    "map-icon-${resource.id ?: error("Map icon is not a Compose resource")}"

@Composable
internal fun rememberPlainStyleImages(resources: List<DrawableResource>): List<DynamicStyleImage> {
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
                cacheKey =
                    listOf(
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
        coroutineScope {
            launch(start = CoroutineStart.UNDISPATCHED) {
                mapState.events.filterIsInstance<MapEvent.StyleLoaded>().collect {
                    registry.onStyleLoaded()
                }
            }

            launch {
                combine(
                        snapshotFlow { mapState.style.loadState },
                        registry.images,
                        registry.styleGeneration,
                    ) { loadState, images, generation ->
                        Triple(loadState, images, generation)
                    }
                    .collectLatest { (loadState, images, generation) ->
                        if (loadState != StyleLoadState.Ready) return@collectLatest
                        for (id in registry.pendingImageIds(images.keys)) {
                            val image = registry.resolve(id) ?: continue
                            try {
                                // Adding and recording are one non-cancellable operation. Otherwise
                                // a newer registry snapshot could cancel between them and retry an
                                // image that the style already owns.
                                withContext(NonCancellable) {
                                    withContext(Dispatchers.Default) {
                                        mapState.style.images.add(
                                            id,
                                            image.bitmap,
                                            sdf = image.sdf,
                                        )
                                    }
                                    registry.recordInstalled(id, generation)
                                }
                            } catch (error: IllegalStateException) {
                                // The new style generation will replay the current registry.
                                if (!error.isStyleHandleRace()) throw error
                                return@collectLatest
                            }
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
