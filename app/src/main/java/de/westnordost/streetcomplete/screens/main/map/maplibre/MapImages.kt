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

    suspend fun addOnce(id: Int, createBitmap: () -> Pair<Bitmap, Boolean>) = mutex.withLock {
        if (id !in images) {
            val name = resources.getResourceEntryName(id)
            val (bitmap, sdf) = createBitmap()
            withContext(Dispatchers.Main) { style.addImage(name, bitmap, sdf) }
            images.add(id)
            Log.d("MapImages", name)
        }
    }
}
