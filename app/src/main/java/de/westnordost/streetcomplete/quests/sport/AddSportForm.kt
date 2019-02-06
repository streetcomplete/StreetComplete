package de.westnordost.streetcomplete.quests.sport

import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.sortedBy
import de.westnordost.streetcomplete.view.Item

class AddSportForm : AImageListQuestAnswerFragment<String, List<String>>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_sport_answer_multi) { applyMultiAnswer() }
    )

    // sorted by ~worldwide usages, minus country specific ones
    override val items get() = listOf(
        // 250k - 10k
        Item("soccer",             R.drawable.ic_sport_soccer,          R.string.quest_sport_soccer),
        Item("tennis",             R.drawable.ic_sport_tennis,          R.string.quest_sport_tennis),
        Item("basketball",         R.drawable.ic_sport_basketball,      R.string.quest_sport_basketball),
        Item("golf",               R.drawable.ic_sport_golf,            R.string.quest_sport_golf),
        Item("volleyball",         R.drawable.ic_sport_volleyball,      R.string.quest_sport_volleyball),
        Item("beachvolleyball",    R.drawable.ic_sport_beachvolleyball, R.string.quest_sport_beachvolleyball),
        Item("skateboard",         R.drawable.ic_sport_skateboard,      R.string.quest_sport_skateboard),
        Item("shooting",           R.drawable.ic_sport_shooting,        R.string.quest_sport_shooting),
        // 7k - 5k
        Item("baseball",           R.drawable.ic_sport_baseball,        R.string.quest_sport_baseball),
        Item("athletics",          R.drawable.ic_sport_athletics,       R.string.quest_sport_athletics),
        Item("table_tennis",       R.drawable.ic_sport_table_tennis,    R.string.quest_sport_table_tennis),
        Item("gymnastics",         R.drawable.ic_sport_gymnastics,      R.string.quest_sport_gymnastics),
        // 4k - 2k
        Item("boules",             R.drawable.ic_sport_boules,          R.string.quest_sport_boules),
        Item("handball",           R.drawable.ic_sport_handball,        R.string.quest_sport_handball),
        Item("field_hockey",       R.drawable.ic_sport_field_hockey,    R.string.quest_sport_field_hockey),
        Item("ice_hockey",         R.drawable.ic_sport_ice_hockey,      R.string.quest_sport_ice_hockey),
        Item("american_football",  R.drawable.ic_sport_american_football, R.string.quest_sport_american_football),
        Item("equestrian",         R.drawable.ic_sport_equestrian,      R.string.quest_sport_equestrian),
        Item("archery",            R.drawable.ic_sport_archery,         R.string.quest_sport_archery),
        Item("roller_skating",     R.drawable.ic_sport_roller_skating,  R.string.quest_sport_roller_skating),
        // 1k - 0k
        Item("badminton",          R.drawable.ic_sport_badminton,       R.string.quest_sport_badminton),
        Item("cricket",            R.drawable.ic_sport_cricket,         R.string.quest_sport_cricket),
        Item("rugby",              R.drawable.ic_sport_rugby,           R.string.quest_sport_rugby),
        Item("bowls",              R.drawable.ic_sport_bowls,           R.string.quest_sport_bowls),
        Item("softball",           R.drawable.ic_sport_softball,        R.string.quest_sport_softball),
        Item("racquet",            R.drawable.ic_sport_racquet,         R.string.quest_sport_racquet),
        Item("ice_skating",        R.drawable.ic_sport_ice_skating,     R.string.quest_sport_ice_skating),
        Item("paddle_tennis",      R.drawable.ic_sport_paddle_tennis,   R.string.quest_sport_paddle_tennis),
        Item("australian_football", R.drawable.ic_sport_australian_football, R.string.quest_sport_australian_football),
        Item("canadian_football",  R.drawable.ic_sport_canadian_football, R.string.quest_sport_canadian_football),
        Item("netball",            R.drawable.ic_sport_netball,         R.string.quest_sport_netball),
        Item("gaelic_games",       R.drawable.ic_sport_gaelic_games,    R.string.quest_sport_gaelic_games),
        Item("sepak_takraw",       R.drawable.ic_sport_sepak_takraw,    R.string.quest_sport_sepak_takraw)
    ).sortedBy(countryInfo.popularSports)

    override val maxSelectableItems = -1
    override val maxNumberOfInitiallyShownItems = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below

    }

    override fun onClickOk(selectedItems: List<String>) {
        if (selectedItems.size > 3) {
            AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_sport_manySports_confirmation_title)
                .setMessage(R.string.quest_sport_manySports_confirmation_description)
                .setPositiveButton(R.string.quest_manySports_confirmation_specific) { _, _ -> applyAnswer(selectedItems) }
                .setNegativeButton(R.string.quest_manySports_confirmation_generic) { _, _ -> applyMultiAnswer() }
                .show()
        } else {
            applyAnswer(selectedItems)
        }
    }

    private fun applyMultiAnswer() {
        applyAnswer(listOf("multi"))
    }
}
