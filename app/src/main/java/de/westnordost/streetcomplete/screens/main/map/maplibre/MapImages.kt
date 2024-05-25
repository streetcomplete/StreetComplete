package de.westnordost.streetcomplete.screens.main.map.maplibre

import android.graphics.Bitmap
import org.maplibre.android.maps.Style

class MapImages(private val style: Style) {
    private val images = HashSet<String>()

    fun add(name: String, sdf: Boolean = false, createBitmap: (name: String) -> Bitmap) {
        if (name !in images) {
            style.addImage(name, createBitmap(name), sdf)
            images.add(name)
        }
    }
}
