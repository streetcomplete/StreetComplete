package de.westnordost.streetcomplete.quests.memorial_type

import de.westnordost.streetcomplete.quests.memorial_type.MemorialType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.memorial_type_bust
import de.westnordost.streetcomplete.resources.memorial_type_obelisk
import de.westnordost.streetcomplete.resources.memorial_type_plaque
import de.westnordost.streetcomplete.resources.memorial_type_sculpture
import de.westnordost.streetcomplete.resources.memorial_type_statue
import de.westnordost.streetcomplete.resources.memorial_type_stele_stone
import de.westnordost.streetcomplete.resources.memorial_type_stele_wooden
import de.westnordost.streetcomplete.resources.memorial_type_stone
import de.westnordost.streetcomplete.resources.memorial_type_war_memorial
import de.westnordost.streetcomplete.resources.quest_memorialType_bust
import de.westnordost.streetcomplete.resources.quest_memorialType_obelisk
import de.westnordost.streetcomplete.resources.quest_memorialType_plaque
import de.westnordost.streetcomplete.resources.quest_memorialType_sculpture
import de.westnordost.streetcomplete.resources.quest_memorialType_statue
import de.westnordost.streetcomplete.resources.quest_memorialType_stele_stone
import de.westnordost.streetcomplete.resources.quest_memorialType_stele_wooden
import de.westnordost.streetcomplete.resources.quest_memorialType_stone
import de.westnordost.streetcomplete.resources.quest_memorialType_war_memorial
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val MemorialType.title: StringResource get() = when (this) {
    STATUE ->       Res.string.quest_memorialType_statue
    BUST ->         Res.string.quest_memorialType_bust
    PLAQUE ->       Res.string.quest_memorialType_plaque
    WAR_MEMORIAL -> Res.string.quest_memorialType_war_memorial
    STONE ->        Res.string.quest_memorialType_stone
    OBELISK ->      Res.string.quest_memorialType_obelisk
    WOODEN_STELE -> Res.string.quest_memorialType_stele_wooden
    STONE_STELE ->  Res.string.quest_memorialType_stele_stone
    SCULPTURE ->    Res.string.quest_memorialType_sculpture
}

val MemorialType.icon: DrawableResource get() = when (this) {
    STATUE ->       Res.drawable.memorial_type_statue
    BUST ->         Res.drawable.memorial_type_bust
    PLAQUE ->       Res.drawable.memorial_type_plaque
    WAR_MEMORIAL -> Res.drawable.memorial_type_war_memorial
    STONE ->        Res.drawable.memorial_type_stone
    OBELISK ->      Res.drawable.memorial_type_obelisk
    WOODEN_STELE -> Res.drawable.memorial_type_stele_wooden
    STONE_STELE ->  Res.drawable.memorial_type_stele_stone
    SCULPTURE ->    Res.drawable.memorial_type_sculpture
}
