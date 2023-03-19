package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

sealed interface SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer
object IsIndoorsAnswer : SurfaceOrIsStepsAnswer
data class SurfaceAnswer(val value: SurfaceAndNote) : SurfaceOrIsStepsAnswer

