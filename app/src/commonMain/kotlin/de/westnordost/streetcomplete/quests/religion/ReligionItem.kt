package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.quests.religion.Religion.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.empty_96
import de.westnordost.streetcomplete.resources.quest_religion_animist
import de.westnordost.streetcomplete.resources.quest_religion_bahai
import de.westnordost.streetcomplete.resources.quest_religion_buddhist
import de.westnordost.streetcomplete.resources.quest_religion_caodaist
import de.westnordost.streetcomplete.resources.quest_religion_chinese_folk
import de.westnordost.streetcomplete.resources.quest_religion_christian
import de.westnordost.streetcomplete.resources.quest_religion_for_place_of_worship_answer_multi
import de.westnordost.streetcomplete.resources.quest_religion_hindu
import de.westnordost.streetcomplete.resources.quest_religion_jain
import de.westnordost.streetcomplete.resources.quest_religion_jewish
import de.westnordost.streetcomplete.resources.quest_religion_muslim
import de.westnordost.streetcomplete.resources.quest_religion_shinto
import de.westnordost.streetcomplete.resources.quest_religion_sikh
import de.westnordost.streetcomplete.resources.quest_religion_taoist
import de.westnordost.streetcomplete.resources.religion_animist
import de.westnordost.streetcomplete.resources.religion_bahai
import de.westnordost.streetcomplete.resources.religion_buddhist
import de.westnordost.streetcomplete.resources.religion_caodaist
import de.westnordost.streetcomplete.resources.religion_chinese_folk
import de.westnordost.streetcomplete.resources.religion_christian
import de.westnordost.streetcomplete.resources.religion_hindu
import de.westnordost.streetcomplete.resources.religion_jain
import de.westnordost.streetcomplete.resources.religion_jewish
import de.westnordost.streetcomplete.resources.religion_muslim
import de.westnordost.streetcomplete.resources.religion_shinto
import de.westnordost.streetcomplete.resources.religion_sikh
import de.westnordost.streetcomplete.resources.religion_taoist
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
