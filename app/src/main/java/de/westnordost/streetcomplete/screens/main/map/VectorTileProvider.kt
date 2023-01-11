package de.westnordost.streetcomplete.screens.main.map

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
    abstract fun makeStyleUrl(): String
}
