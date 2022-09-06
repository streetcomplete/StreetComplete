package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.religion.Religion.ANIMIST
import de.westnordost.streetcomplete.quests.religion.Religion.BAHAI
import de.westnordost.streetcomplete.quests.religion.Religion.BUDDHIST
import de.westnordost.streetcomplete.quests.religion.Religion.CAODAISM
import de.westnordost.streetcomplete.quests.religion.Religion.CHINESE_FOLK
import de.westnordost.streetcomplete.quests.religion.Religion.CHRISTIAN
import de.westnordost.streetcomplete.quests.religion.Religion.HINDU
import de.westnordost.streetcomplete.quests.religion.Religion.JAIN
import de.westnordost.streetcomplete.quests.religion.Religion.JEWISH
import de.westnordost.streetcomplete.quests.religion.Religion.MULTIFAITH
import de.westnordost.streetcomplete.quests.religion.Religion.MUSLIM
import de.westnordost.streetcomplete.quests.religion.Religion.SHINTO
import de.westnordost.streetcomplete.quests.religion.Religion.SIKH
import de.westnordost.streetcomplete.quests.religion.Religion.TAOIST
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun Religion.asItem(): DisplayItem<Religion>? {
    val iconResId = iconResId ?: return null
    val titleResId = titleResId  ?: return null
    return Item(this, iconResId, titleResId)
}

private val Religion.titleResId: Int? get() = when (this) {
    CHRISTIAN ->    R.string.quest_religion_christian
    MUSLIM ->       R.string.quest_religion_muslim
    BUDDHIST ->     R.string.quest_religion_buddhist
    HINDU ->        R.string.quest_religion_hindu
    JEWISH ->       R.string.quest_religion_jewish
    CHINESE_FOLK -> R.string.quest_religion_chinese_folk
    ANIMIST ->      R.string.quest_religion_animist
    BAHAI ->        R.string.quest_religion_bahai
    SIKH ->         R.string.quest_religion_sikh
    TAOIST ->       R.string.quest_religion_taoist
    JAIN ->         R.string.quest_religion_jain
    SHINTO ->       R.string.quest_religion_shinto
    CAODAISM ->     R.string.quest_religion_caodaist
    MULTIFAITH ->   null
}

private val Religion.iconResId: Int? get() = when (this) {
    CHRISTIAN ->    R.drawable.ic_religion_christian
    MUSLIM ->       R.drawable.ic_religion_muslim
    BUDDHIST ->     R.drawable.ic_religion_buddhist
    HINDU ->        R.drawable.ic_religion_hindu
    JEWISH ->       R.drawable.ic_religion_jewish
    CHINESE_FOLK -> R.drawable.ic_religion_chinese_folk
    ANIMIST ->      R.drawable.ic_religion_animist
    BAHAI ->        R.drawable.ic_religion_bahai
    SIKH ->         R.drawable.ic_religion_sikh
    TAOIST ->       R.drawable.ic_religion_taoist
    JAIN ->         R.drawable.ic_religion_jain
    SHINTO ->       R.drawable.ic_religion_shinto
    CAODAISM ->     R.drawable.ic_religion_caodaist
    MULTIFAITH ->   null
}
