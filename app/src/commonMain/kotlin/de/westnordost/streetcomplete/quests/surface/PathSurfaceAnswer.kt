package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.Surface

sealed interface PathSurfaceAnswer {
    data object IsSteps : PathSurfaceAnswer
    data object IsIndoors : PathSurfaceAnswer
}

data class SurfaceAnswer(val value: Surface) : PathSurfaceAnswer
