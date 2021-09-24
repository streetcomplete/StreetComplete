package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.religion.Religion.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddReligionForm : AImageListQuestAnswerFragment<Religion, Religion>() {

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
    ).sortedBy { countryInfo.popularReligions.indexOf(it.value!!.osmValue) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<Religion>) {
        applyAnswer(selectedItems.single())
    }
}
