package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.ui.graphics.ImageBitmap

/** Creates an image from unpremultiplied ARGB pixels. */
internal expect fun IntArray.toPlatformImageBitmap(width: Int, height: Int): ImageBitmap
