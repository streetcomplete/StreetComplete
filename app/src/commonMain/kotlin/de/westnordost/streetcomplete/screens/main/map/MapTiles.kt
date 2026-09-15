package de.westnordost.streetcomplete.screens.main.map

/** The vector tile source shared by the displayed map and offline tile downloads. The bundled
 *  files/map-download-style.json must declare the same URL and maximum zoom. */
object MapTiles {
    private const val ACCESS_TOKEN = "mL9X4SwxfsAGfojvGiion9hPKuGLKxPbogLyMbtakA2gJ3X88gcVlTSQ7OD6OfbZ"

    const val MAX_ZOOM = 16
    const val URL_TEMPLATE =
        "https://tile.jawg.io/streets-v2+hillshade-v1/{z}/{x}/{y}.pbf?access-token=$ACCESS_TOKEN"
}
