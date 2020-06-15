package de.westnordost.streetcomplete.map

data class VectorTileProvider(
    val title: String,
    val copyrightText: String,
    val copyrightLink: String,
    val privacyStatementLink: String,
    val sceneFilePath: String,
    val apiKey: String
)