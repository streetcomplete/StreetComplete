package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
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
import de.westnordost.streetcomplete.view.image_select.Item

class AddReligionForm : AImageListQuestForm<Religion, Religion>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_religion_for_place_of_worship_answer_multi) { applyAnswer(MULTIFAITH) }
    )

    override val items get() = listOf(
        // sorted by worldwide usages, *minus* country specific ones
        Item(CHRISTIAN,    R.drawable.ic_religion_christian, R.string.quest_religion_christian),
        Item(MUSLIM,       R.drawable.ic_religion_muslim,    R.string.quest_religion_muslim),
        Item(BUDDHIST,     R.drawable.ic_religion_buddhist,  R.string.quest_religion_buddhist),
        Item(HINDU,        R.drawable.ic_religion_hindu,     R.string.quest_religion_hindu),

        Item(JEWISH,       R.drawable.ic_religion_jewish,    R.string.quest_religion_jewish),
        // difficult to get the numbers on this, as they are counted alternating as buddhists,
        // taoists, confucianists, not religious or "folk religion" in statistics. See
        // https://en.wikipedia.org/wiki/Chinese_folk_religion
        // sorting relatively far up because there are many Chinese expats around the world
        Item(CHINESE_FOLK, R.drawable.ic_religion_chinese_folk, R.string.quest_religion_chinese_folk),
        Item(ANIMIST,      R.drawable.ic_religion_animist,   R.string.quest_religion_animist),
        Item(BAHAI,        R.drawable.ic_religion_bahai,     R.string.quest_religion_bahai),
        Item(SIKH,         R.drawable.ic_religion_sikh,      R.string.quest_religion_sikh),

        Item(TAOIST,       R.drawable.ic_religion_taoist,    R.string.quest_religion_taoist),
        Item(JAIN,         R.drawable.ic_religion_jain,      R.string.quest_religion_jain), // India
        Item(SHINTO,       R.drawable.ic_religion_shinto,    R.string.quest_religion_shinto), // Japan
        Item(CAODAISM,     R.drawable.ic_religion_caodaist,  R.string.quest_religion_caodaist) // Vietnam
    ).sortedBy { religionPosition(it.value!!.osmValue) }

    fun religionPosition(osmValue: String): Int {
        val position = countryInfo.popularReligions.indexOf(osmValue)
        if (position < 0) {
            // not present at all in config, so should be put at the end
            return Integer.MAX_VALUE
        }
        return position
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<Religion>) {
        applyAnswer(selectedItems.single())
    }
}
