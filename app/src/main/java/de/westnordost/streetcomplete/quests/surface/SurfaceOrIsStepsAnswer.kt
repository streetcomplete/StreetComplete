package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

sealed interface SurfaceOrIsStepsAnswer
data object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer
data object IsIndoorsAnswer : SurfaceOrIsStepsAnswer
data class SurfaceAnswer(val value: SurfaceAndNote) : SurfaceOrIsStepsAnswer
