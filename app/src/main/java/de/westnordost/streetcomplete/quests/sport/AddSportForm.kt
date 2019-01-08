package de.westnordost.streetcomplete.quests.sport

import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import java.util.ArrayList

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.ImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.sortedBy

import de.westnordost.streetcomplete.quests.sport.Sport.*

class AddSportForm : ImageListQuestAnswerFragment() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_sport_answer_multi) { applyMultiAnswer() }
    )

    // sorted by ~worldwide usages, minus country specific ones
    override val items get() = listOf(
        // 250k - 10k
        SOCCER, TENNIS, BASKETBALL, GOLF,
        VOLLEYBALL, BEACHVOLLEYBALL, SKATEBOARD, SHOOTING,
        // 7k - 5k
        BASEBALL, ATHLETICS, TABLE_TENNIS, GYMNASTICS,
        // 4k - 2k
        BOULES, HANDBALL, FIELD_HOCKEY, ICE_HOCKEY,
        AMERICAN_FOOTBALL, EQUESTRIAN, ARCHERY, ROLLER_SKATING,
        // 1k - 0k
        BADMINTON, CRICKET, RUGBY, BOWLS,
        SOFTBALL, RACQUET, ICE_SKATING, PADDLE_TENNIS,
        AUSTRALIAN_FOOTBALL, CANADIAN_FOOTBALL, NETBALL, GAELIC_GAMES,
        SEPAK_TAKRAW
    // show only first 24 items (6 rows)
    ).sortedBy(countryInfo.popularSports).subList(0, 24)

    override val maxSelectableItems = -1
    override val maxNumberOfInitiallyShownItems = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector.cellLayoutId = R.layout.cell_icon_select_with_label_below

    }

    override fun onClickOk() {
        if (imageSelector.selectedIndices.size > 3) {
            AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_sport_manySports_confirmation_title)
                .setMessage(R.string.quest_sport_manySports_confirmation_description)
                .setPositiveButton(R.string.quest_manySports_confirmation_specific) { _, _ -> applyAnswer() }
                .setNegativeButton(R.string.quest_manySports_confirmation_generic) { _, _ -> applyMultiAnswer() }
                .show()
        } else {
            applyAnswer()
        }
    }

    private fun applyMultiAnswer() {
        val answer = Bundle()
        val strings = ArrayList<String>(1)
        strings.add("multi")
        answer.putStringArrayList(ImageListQuestAnswerFragment.OSM_VALUES, strings)
        applyAnswer(answer)
    }
}
