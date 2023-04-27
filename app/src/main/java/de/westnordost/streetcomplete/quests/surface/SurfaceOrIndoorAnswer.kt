package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

sealed interface SurfaceOrIndoorAnswer
object IsIndoorsAnswer : SurfaceOrIndoorAnswer
data class SurfaceAnswer(val value: SurfaceAndNote) : SurfaceOrIndoorAnswer
