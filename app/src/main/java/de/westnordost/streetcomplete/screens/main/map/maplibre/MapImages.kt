package de.westnordost.streetcomplete.screens.main.map.maplibre

import android.content.res.Resources
import android.graphics.Bitmap
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.Style
import java.lang.System.currentTimeMillis

class MapImages(private val resources: Resources, private val style: Style) {
    private val images = HashSet<Int>()
    private val mutex = Mutex()

    suspend fun addOnce(id: Int, createBitmap: (Int) -> Pair<Bitmap, Boolean>) = mutex.withLock {
        if (id !in images) {
            val name = resources.getResourceEntryName(id)
            val (bitmap, sdf) = createBitmap(id)
            withContext(Dispatchers.Main) { style.addImage(name, bitmap, sdf) }
            images.add(id)
            Log.v("MapImages", "Loaded 1 image")
        }
    }

    suspend fun addOnce(
        ids: Collection<Int>,
        createBitmap: (id: Int) -> Pair<Bitmap, Boolean>
    ) = mutex.withLock {
        val loadIds = ids.filter { it !in images }.toSet()
        if (loadIds.isEmpty()) return@withLock

        val data = loadIds.map {
            val (bitmap, sdf) = createBitmap(it)
            ImageWithMetadata(resources.getResourceEntryName(it), bitmap, sdf)
        }

        val sdfImages = data.filter { it.sdf }.associateTo(HashMap()) { it.name to it.bitmap }
        val nonSdfImages = data.filterNot { it.sdf }.associateTo(HashMap()) { it.name to it.bitmap }

        withContext(Dispatchers.Main) {
            if (nonSdfImages.isNotEmpty()) style.addImages(nonSdfImages, false)
            if (sdfImages.isNotEmpty()) style.addImages(sdfImages, true)
        }

        images.addAll(loadIds)
        Log.v("MapImages", "Loaded ${loadIds.size} images")
    }

    private data class ImageWithMetadata(val name: String, val bitmap: Bitmap, val sdf: Boolean)
}
