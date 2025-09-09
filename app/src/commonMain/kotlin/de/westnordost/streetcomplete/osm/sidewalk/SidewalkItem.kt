package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.floating_separate
import de.westnordost.streetcomplete.resources.quest_sidewalk_value_no
import de.westnordost.streetcomplete.resources.quest_sidewalk_value_separate
import de.westnordost.streetcomplete.resources.quest_sidewalk_value_yes
import de.westnordost.streetcomplete.resources.sidewalk_illustration_no
import de.westnordost.streetcomplete.resources.sidewalk_illustration_yes
import de.westnordost.streetcomplete.resources.sidewalk_no
import de.westnordost.streetcomplete.resources.sidewalk_separate
import de.westnordost.streetcomplete.resources.sidewalk_yes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

// TODO ==INVALID -> don't show at all

val Sidewalk.icon: DrawableResource? get() = when (this) {
    YES -> Res.drawable.sidewalk_yes
    NO -> Res.drawable.sidewalk_no
    SEPARATE -> Res.drawable.sidewalk_separate
    else -> null
}

val Sidewalk.image: DrawableResource? get() = when (this) {
    YES -> Res.drawable.sidewalk_illustration_yes
    NO -> Res.drawable.sidewalk_illustration_no
    SEPARATE -> Res.drawable.sidewalk_illustration_no
    else -> null
}

val Sidewalk.floatingIcon: DrawableResource? get() = when (this) {
    SEPARATE -> Res.drawable.floating_separate
    else -> null
}

val Sidewalk.title: StringResource? get() = when (this) {
    YES -> Res.string.quest_sidewalk_value_yes
    NO -> Res.string.quest_sidewalk_value_no
    SEPARATE -> Res.string.quest_sidewalk_value_separate
    else -> null
}
