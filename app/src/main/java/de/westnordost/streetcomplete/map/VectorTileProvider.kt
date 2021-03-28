package de.westnordost.streetcomplete.map

abstract class VectorTileProvider(
    val title: String,
    val maxZoom: Int,
    val copyrightText: String,
    val copyrightLink: String,
    val privacyStatementLink: String,
    val sceneFilePath: String,
    val apiKey: String
) {
    abstract fun getTileUrl(zoom: Int, x: Int, y: Int): String
}
