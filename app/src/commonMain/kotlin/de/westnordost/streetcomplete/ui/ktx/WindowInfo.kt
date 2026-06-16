package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.ui.platform.WindowInfo

val WindowInfo.isLandscape: Boolean get() =
    containerSize.width > containerSize.height
