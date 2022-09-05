package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.BUST
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.OBELISK
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.PLAQUE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.SCULPTURE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.STATUE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.STONE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.STONE_STELE
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.WAR_MEMORIAL
import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.WOODEN_STELE
import de.westnordost.streetcomplete.view.image_select.Item

fun MemorialType.asItem() = Item(this, iconResId, titleResId)

private val MemorialType.titleResId: Int get() = when (this) {
    STATUE ->       R.string.quest_memorialType_statue
    BUST ->         R.string.quest_memorialType_bust
    PLAQUE ->       R.string.quest_memorialType_plaque
    WAR_MEMORIAL -> R.string.quest_memorialType_war_memorial
    STONE ->        R.string.quest_memorialType_stone
    OBELISK ->      R.string.quest_memorialType_obelisk
    WOODEN_STELE -> R.string.quest_memorialType_stele_wooden
    STONE_STELE ->  R.string.quest_memorialType_stele_stone
    SCULPTURE ->    R.string.quest_memorialType_sculpture
}

private val MemorialType.iconResId: Int get() = when (this) {
    STATUE ->       R.drawable.memorial_type_statue
    BUST ->         R.drawable.memorial_type_bust
    PLAQUE ->       R.drawable.memorial_type_plaque
    WAR_MEMORIAL -> R.drawable.memorial_type_war_memorial
    STONE ->        R.drawable.memorial_type_stone
    OBELISK ->      R.drawable.memorial_type_obelisk
    WOODEN_STELE -> R.drawable.memorial_type_stele_wooden
    STONE_STELE ->  R.drawable.memorial_type_stele_stone
    SCULPTURE ->    R.drawable.memorial_type_sculpture
}
