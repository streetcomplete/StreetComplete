package de.westnordost.streetcomplete.quests.sport

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.sport.Sport.AMERICAN_FOOTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.ARCHERY
import de.westnordost.streetcomplete.quests.sport.Sport.ATHLETICS
import de.westnordost.streetcomplete.quests.sport.Sport.AUSTRALIAN_FOOTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BADMINTON
import de.westnordost.streetcomplete.quests.sport.Sport.BASEBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BASKETBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BEACHVOLLEYBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BOULES
import de.westnordost.streetcomplete.quests.sport.Sport.BOWLS
import de.westnordost.streetcomplete.quests.sport.Sport.CANADIAN_FOOTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.CRICKET
import de.westnordost.streetcomplete.quests.sport.Sport.EQUESTRIAN
import de.westnordost.streetcomplete.quests.sport.Sport.FIELD_HOCKEY
import de.westnordost.streetcomplete.quests.sport.Sport.GAELIC_GAMES
import de.westnordost.streetcomplete.quests.sport.Sport.GOLF
import de.westnordost.streetcomplete.quests.sport.Sport.GYMNASTICS
import de.westnordost.streetcomplete.quests.sport.Sport.HANDBALL
import de.westnordost.streetcomplete.quests.sport.Sport.ICE_HOCKEY
import de.westnordost.streetcomplete.quests.sport.Sport.ICE_SKATING
import de.westnordost.streetcomplete.quests.sport.Sport.MULTI
import de.westnordost.streetcomplete.quests.sport.Sport.NETBALL
import de.westnordost.streetcomplete.quests.sport.Sport.PADDLE_TENNIS
import de.westnordost.streetcomplete.quests.sport.Sport.RACQUET
import de.westnordost.streetcomplete.quests.sport.Sport.ROLLER_SKATING
import de.westnordost.streetcomplete.quests.sport.Sport.RUGBY
import de.westnordost.streetcomplete.quests.sport.Sport.SEPAK_TAKRAW
import de.westnordost.streetcomplete.quests.sport.Sport.SHOOTING
import de.westnordost.streetcomplete.quests.sport.Sport.SKATEBOARD
import de.westnordost.streetcomplete.quests.sport.Sport.SOCCER
import de.westnordost.streetcomplete.quests.sport.Sport.SOFTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.TABLE_TENNIS
import de.westnordost.streetcomplete.quests.sport.Sport.TENNIS
import de.westnordost.streetcomplete.quests.sport.Sport.VOLLEYBALL
import de.westnordost.streetcomplete.view.image_select.Item

class AddSportForm : AImageListQuestForm<Sport, List<Sport>>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_sport_answer_multi) { applyMultiAnswer() }
    )

    override val items get() = listOf(
        // sorted by ~worldwide usages, minus country specific ones
        // 250k - 10k
        Item(SOCCER,              R.drawable.ic_sport_soccer,              R.string.quest_sport_soccer),
        Item(TENNIS,              R.drawable.ic_sport_tennis,              R.string.quest_sport_tennis),
        Item(BASKETBALL,          R.drawable.ic_sport_basketball,          R.string.quest_sport_basketball),
        Item(GOLF,                R.drawable.ic_sport_golf,                R.string.quest_sport_golf),
        Item(VOLLEYBALL,          R.drawable.ic_sport_volleyball,          R.string.quest_sport_volleyball),
        Item(BEACHVOLLEYBALL,     R.drawable.ic_sport_beachvolleyball,     R.string.quest_sport_beachvolleyball),
        Item(SKATEBOARD,          R.drawable.ic_sport_skateboard,          R.string.quest_sport_skateboard),
        Item(SHOOTING,            R.drawable.ic_sport_shooting,            R.string.quest_sport_shooting),
        // 7k - 5k
        Item(BASEBALL,            R.drawable.ic_sport_baseball,            R.string.quest_sport_baseball),
        Item(ATHLETICS,           R.drawable.ic_sport_athletics,           R.string.quest_sport_athletics),
        Item(TABLE_TENNIS,        R.drawable.ic_sport_table_tennis,        R.string.quest_sport_table_tennis),
        Item(GYMNASTICS,          R.drawable.ic_sport_gymnastics,          R.string.quest_sport_gymnastics),
        // 4k - 2k
        Item(BOULES,              R.drawable.ic_sport_boules,              R.string.quest_sport_boules),
        Item(HANDBALL,            R.drawable.ic_sport_handball,            R.string.quest_sport_handball),
        Item(FIELD_HOCKEY,        R.drawable.ic_sport_field_hockey,        R.string.quest_sport_field_hockey),
        Item(ICE_HOCKEY,          R.drawable.ic_sport_ice_hockey,          R.string.quest_sport_ice_hockey),
        Item(AMERICAN_FOOTBALL,   R.drawable.ic_sport_american_football,   R.string.quest_sport_american_football),
        Item(EQUESTRIAN,          R.drawable.ic_sport_equestrian,          R.string.quest_sport_equestrian),
        Item(ARCHERY,             R.drawable.ic_sport_archery,             R.string.quest_sport_archery),
        Item(ROLLER_SKATING,      R.drawable.ic_sport_roller_skating,      R.string.quest_sport_roller_skating),
        // 1k - 0k
        Item(BADMINTON,           R.drawable.ic_sport_badminton,           R.string.quest_sport_badminton),
        Item(CRICKET,             R.drawable.ic_sport_cricket,             R.string.quest_sport_cricket),
        Item(RUGBY,               R.drawable.ic_sport_rugby,               R.string.quest_sport_rugby),
        Item(BOWLS,               R.drawable.ic_sport_bowls,               R.string.quest_sport_bowls),
        Item(SOFTBALL,            R.drawable.ic_sport_softball,            R.string.quest_sport_softball),
        Item(RACQUET,             R.drawable.ic_sport_racquet,             R.string.quest_sport_racquet),
        Item(ICE_SKATING,         R.drawable.ic_sport_ice_skating,         R.string.quest_sport_ice_skating),
        Item(PADDLE_TENNIS,       R.drawable.ic_sport_paddle_tennis,       R.string.quest_sport_paddle_tennis),
        Item(AUSTRALIAN_FOOTBALL, R.drawable.ic_sport_australian_football, R.string.quest_sport_australian_football),
        Item(CANADIAN_FOOTBALL,   R.drawable.ic_sport_canadian_football,   R.string.quest_sport_canadian_football),
        Item(NETBALL,             R.drawable.ic_sport_netball,             R.string.quest_sport_netball),
        Item(GAELIC_GAMES,        R.drawable.ic_sport_gaelic_games,        R.string.quest_sport_gaelic_games),
        Item(SEPAK_TAKRAW,        R.drawable.ic_sport_sepak_takraw,        R.string.quest_sport_sepak_takraw)
    ).sortedBy { countryInfo.popularSports.indexOf(it.value!!.osmValue) }

    override val maxSelectableItems = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below
    }

    override fun onClickOk(selectedItems: List<Sport>) {
        if (selectedItems.size > 3) {
            AlertDialog.Builder(requireContext())
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
        applyAnswer(listOf(MULTI))
    }
}
