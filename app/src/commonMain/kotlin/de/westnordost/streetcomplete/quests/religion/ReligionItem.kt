package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.quests.religion.Religion.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Religion.title: StringResource get() = when (this) {
    CHRISTIAN ->    Res.string.quest_religion_christian
    MUSLIM ->       Res.string.quest_religion_muslim
    BUDDHIST ->     Res.string.quest_religion_buddhist
    HINDU ->        Res.string.quest_religion_hindu
    JEWISH ->       Res.string.quest_religion_jewish
    CHINESE_FOLK -> Res.string.quest_religion_chinese_folk
    ANIMIST ->      Res.string.quest_religion_animist
    BAHAI ->        Res.string.quest_religion_bahai
    SIKH ->         Res.string.quest_religion_sikh
    TAOIST ->       Res.string.quest_religion_taoist
    JAIN ->         Res.string.quest_religion_jain
    SHINTO ->       Res.string.quest_religion_shinto
    CAODAISM ->     Res.string.quest_religion_caodaist
    MULTIFAITH ->   Res.string.quest_religion_for_place_of_worship_answer_multi
}

val Religion.icon: DrawableResource get() = when (this) {
    CHRISTIAN ->    Res.drawable.religion_christian
    MUSLIM ->       Res.drawable.religion_muslim
    BUDDHIST ->     Res.drawable.religion_buddhist
    HINDU ->        Res.drawable.religion_hindu
    JEWISH ->       Res.drawable.religion_jewish
    CHINESE_FOLK -> Res.drawable.religion_chinese_folk
    ANIMIST ->      Res.drawable.religion_animist
    BAHAI ->        Res.drawable.religion_bahai
    SIKH ->         Res.drawable.religion_sikh
    TAOIST ->       Res.drawable.religion_taoist
    JAIN ->         Res.drawable.religion_jain
    SHINTO ->       Res.drawable.religion_shinto
    CAODAISM ->     Res.drawable.religion_caodaist
    MULTIFAITH ->   Res.drawable.empty_96
}
