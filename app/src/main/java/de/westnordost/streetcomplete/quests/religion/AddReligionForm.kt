package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.sortedBy
import de.westnordost.streetcomplete.view.Item

class AddReligionForm : AImageListQuestAnswerFragment<String,String>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_religion_for_place_of_worship_answer_multi) { applyAnswer("multifaith") }
    )

    override val items get() = listOf(
        // sorted by worldwide usages, *minus* country specific ones
        Item("christian", R.drawable.ic_religion_christian, R.string.quest_religion_christian),
        Item("muslim",    R.drawable.ic_religion_muslim,    R.string.quest_religion_muslim),
        Item("buddhist",  R.drawable.ic_religion_buddhist,  R.string.quest_religion_buddhist),
        Item("hindu",     R.drawable.ic_religion_hindu,     R.string.quest_religion_hindu),

        Item("jewish",    R.drawable.ic_religion_jewish,    R.string.quest_religion_jewish),
        // difficult to get the numbers on this, as they are counted alternating as buddhists,
        // taoists, confucianists, not religious or "folk religion" in statistics. See
        // https://en.wikipedia.org/wiki/Chinese_folk_religion
        // sorting relatively far up because there are many Chinese expats around the world
        Item("chinese_folk", R.drawable.ic_religion_chinese_folk, R.string.quest_religion_chinese_folk),
        Item("bahai",     R.drawable.ic_religion_bahai,     R.string.quest_religion_bahai),
        Item("sikh",      R.drawable.ic_religion_sikh,      R.string.quest_religion_sikh),

        Item("taoist",    R.drawable.ic_religion_taoist,    R.string.quest_religion_taoist),
        Item("jain",      R.drawable.ic_religion_jain,      R.string.quest_religion_jain), // India
        Item("shinto",    R.drawable.ic_religion_shinto,    R.string.quest_religion_shinto), // Japan
        Item("caodaism",  R.drawable.ic_religion_caodaist,  R.string.quest_religion_caodaist) // Vietnam
    ).sortedBy(countryInfo.popularReligions)

    override val maxNumberOfInitiallyShownItems = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<String>) {
        applyAnswer(selectedItems.single())
    }
}
