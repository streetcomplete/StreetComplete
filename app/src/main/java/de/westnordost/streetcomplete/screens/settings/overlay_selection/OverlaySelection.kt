package de.westnordost.streetcomplete.screens.settings.overlay_selection

import androidx.compose.runtime.Immutable
import de.westnordost.streetcomplete.overlays.Overlay

@Immutable
data class OverlaySelection(val overlay: Overlay, val selected: Boolean)
