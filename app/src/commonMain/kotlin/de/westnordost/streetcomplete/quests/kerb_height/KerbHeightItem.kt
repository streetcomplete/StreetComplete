package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.kerb_height_flush
import de.westnordost.streetcomplete.resources.kerb_height_lowered
import de.westnordost.streetcomplete.resources.kerb_height_lowered_ramp
import de.westnordost.streetcomplete.resources.kerb_height_no
import de.westnordost.streetcomplete.resources.kerb_height_raised
import de.westnordost.streetcomplete.resources.quest_kerb_height_flush
import de.westnordost.streetcomplete.resources.quest_kerb_height_lowered
import de.westnordost.streetcomplete.resources.quest_kerb_height_lowered_ramp
import de.westnordost.streetcomplete.resources.quest_kerb_height_no
import de.westnordost.streetcomplete.resources.quest_kerb_height_raised
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val KerbHeight.title: StringResource get() = when (this) {
    RAISED ->    Res.string.quest_kerb_height_raised
    LOWERED ->   Res.string.quest_kerb_height_lowered
    FLUSH ->     Res.string.quest_kerb_height_flush
    KERB_RAMP -> Res.string.quest_kerb_height_lowered_ramp
    NO_KERB ->   Res.string.quest_kerb_height_no
}

val KerbHeight.icon: DrawableResource get() = when (this) {
    RAISED ->    Res.drawable.kerb_height_raised
    LOWERED ->   Res.drawable.kerb_height_lowered
    FLUSH ->     Res.drawable.kerb_height_flush
    KERB_RAMP -> Res.drawable.kerb_height_lowered_ramp
    NO_KERB ->   Res.drawable.kerb_height_no
}
