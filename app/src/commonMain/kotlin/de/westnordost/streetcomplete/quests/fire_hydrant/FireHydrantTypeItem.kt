package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.quests.fire_hydrant.FireHydrantType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.fire_hydrant_pillar
import de.westnordost.streetcomplete.resources.fire_hydrant_pipe
import de.westnordost.streetcomplete.resources.fire_hydrant_underground
import de.westnordost.streetcomplete.resources.fire_hydrant_wall
import de.westnordost.streetcomplete.resources.quest_fireHydrant_type_pillar
import de.westnordost.streetcomplete.resources.quest_fireHydrant_type_pipe
import de.westnordost.streetcomplete.resources.quest_fireHydrant_type_underground
import de.westnordost.streetcomplete.resources.quest_fireHydrant_type_wall
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val FireHydrantType.title: StringResource get() = when (this) {
    PILLAR ->      Res.string.quest_fireHydrant_type_pillar
    UNDERGROUND -> Res.string.quest_fireHydrant_type_underground
    WALL ->        Res.string.quest_fireHydrant_type_wall
    PIPE ->        Res.string.quest_fireHydrant_type_pipe
}

val FireHydrantType.icon: DrawableResource get() = when (this) {
    PILLAR ->      Res.drawable.fire_hydrant_pillar
    UNDERGROUND -> Res.drawable.fire_hydrant_underground
    WALL ->        Res.drawable.fire_hydrant_wall
    PIPE ->        Res.drawable.fire_hydrant_pipe
}
