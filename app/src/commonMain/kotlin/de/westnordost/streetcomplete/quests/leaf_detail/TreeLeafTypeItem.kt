package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.quests.leaf_detail.TreeLeafType.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val TreeLeafType.title: StringResource get() = when (this) {
    NEEDLELEAVED -> Res.string.quest_leaf_type_needles
    BROADLEAVED ->  Res.string.quest_leaf_type_broadleaved
    LEAFLESS ->     Res.string.quest_leaf_type_leafless
}

val TreeLeafType.icon: DrawableResource get() = when (this) {
    NEEDLELEAVED -> Res.drawable.leaf_type_needleleaved
    BROADLEAVED ->  Res.drawable.leaf_type_broadleaved
    LEAFLESS ->     Res.drawable.leaf_type_leafless
}
