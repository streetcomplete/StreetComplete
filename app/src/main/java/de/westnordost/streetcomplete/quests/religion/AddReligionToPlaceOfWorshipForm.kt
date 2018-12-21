package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.sortedBy
import de.westnordost.streetcomplete.view.Item

class AddReligionToPlaceOfWorshipForm : ImageListQuestAnswerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        imageSelector.setCellLayout(R.layout.cell_icon_select_with_label_below)

        addOtherAnswer(R.string.quest_religion_for_place_of_worship_answer_multi) { applyMultiAnswer() }

        return view
    }

    override fun getMaxSelectableItems() = 1
    override fun getMaxNumberOfInitiallyShownItems() = 4

    override fun getItems() =
        ALL_RELIGION_VALUES.sortedBy(countryInfo.popularReligions).toTypedArray()

    private fun applyMultiAnswer() {
        val answer = Bundle()
        val strings = ArrayList<String>(1)
        strings.add("multifaith")
        answer.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, strings)
        applyAnswer(answer)
    }

    companion object {
        private val ALL_RELIGION_VALUES = arrayOf(
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
        )
    }
}
