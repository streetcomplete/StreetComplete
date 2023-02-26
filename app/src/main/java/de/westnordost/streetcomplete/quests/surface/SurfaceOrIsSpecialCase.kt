package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.SurfaceAnswer

sealed interface SurfaceOrIsSpecialCase
object IsActuallyStepsAnswer : SurfaceOrIsSpecialCase
object IsIndoorsAnswer : SurfaceOrIsSpecialCase
data class SurfaceAnswerContainer(val surfaceAnswer: SurfaceAnswer) : SurfaceOrIsSpecialCase
