package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.quests.leaf_detail.TreeLeafType.BROADLEAVED
import de.westnordost.streetcomplete.quests.leaf_detail.TreeLeafType.NEEDLELEAVED
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val TreeLeafType.title: StringResource get() = when (this) {
    NEEDLELEAVED -> Res.string.quest_leaf_type_needles
    BROADLEAVED ->  Res.string.quest_leaf_type_broadleaved
}

val TreeLeafType.icon: DrawableResource get() = when (this) {
    NEEDLELEAVED -> Res.drawable.leaf_type_needleleaved
    BROADLEAVED ->  Res.drawable.leaf_type_broadleaved
}
