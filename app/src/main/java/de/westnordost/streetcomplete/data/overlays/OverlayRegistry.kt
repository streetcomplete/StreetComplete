package de.westnordost.streetcomplete.data.overlays

import de.westnordost.streetcomplete.overlays.Overlay

/** Every overlay must be registered here
 *
 * Could theoretically be done with Reflection, but that doesn't really work on Android
 */
class OverlayRegistry(private val overlays: List<Overlay>) : List<Overlay> by overlays {

    private val typeMap: Map<String, Overlay>

    init {
        val map = mutableMapOf<String, Overlay>()
        for (overlay in this) {
            val overlayName = overlay.name
            require(!map.containsKey(overlayName)) {
                "An overlay name must be unique! \"$overlayName\" is defined twice!"
            }
            map[overlayName] = overlay
        }
        typeMap = map
    }

    fun getByName(typeName: String): Overlay? = typeMap[typeName]
}
