package de.westnordost.streetcomplete.quests.bollard_type

import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FIXED
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FLEXIBLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FOLDABLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.REMOVABLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.RISING
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.bollard_fixed
import de.westnordost.streetcomplete.resources.bollard_flexible
import de.westnordost.streetcomplete.resources.bollard_foldable
import de.westnordost.streetcomplete.resources.bollard_removable
import de.westnordost.streetcomplete.resources.bollard_rising
import de.westnordost.streetcomplete.resources.quest_bollard_type_fixed
import de.westnordost.streetcomplete.resources.quest_bollard_type_flexible
import de.westnordost.streetcomplete.resources.quest_bollard_type_foldable2
import de.westnordost.streetcomplete.resources.quest_bollard_type_removable
import de.westnordost.streetcomplete.resources.quest_bollard_type_rising
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BollardType.title: StringResource get() = when (this) {
    RISING ->    Res.string.quest_bollard_type_rising
    REMOVABLE -> Res.string.quest_bollard_type_removable
    FOLDABLE ->  Res.string.quest_bollard_type_foldable2
    FLEXIBLE ->  Res.string.quest_bollard_type_flexible
    FIXED ->     Res.string.quest_bollard_type_fixed
}

val BollardType.icon: DrawableResource get() = when (this) {
    RISING ->    Res.drawable.bollard_rising
    REMOVABLE -> Res.drawable.bollard_removable
    FOLDABLE ->  Res.drawable.bollard_foldable
    FLEXIBLE ->  Res.drawable.bollard_flexible
    FIXED ->     Res.drawable.bollard_fixed
}
