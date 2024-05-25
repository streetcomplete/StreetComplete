package de.westnordost.streetcomplete.screens.main.map.maplibre

import android.content.res.Resources
import android.graphics.Bitmap
import org.maplibre.android.maps.Style

class MapImages( private val resources: Resources, private val style: Style) {
    private val images = HashSet<Int>()

    fun add(id: Int, createBitmap: () -> Pair<Bitmap, Boolean>) {
        if (id !in images) {
            val name = resources.getResourceEntryName(id)
            val (bitmap, sdf) = createBitmap()
            style.addImageAsync(name, bitmap, sdf)
            images.add(id)
            Log.d("MapImages", name)
        }
    }
}
