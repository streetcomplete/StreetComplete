package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.ui.graphics.GraphicsLayerScope

var GraphicsLayerScope.scale: Float
    get() = scaleX
    set(value) {
        scaleX = value
        scaleY = value
    }

var GraphicsLayerScope.translation: Float
    get() = translationX
    set(value) {
        translationX = value
        translationY = value
    }
