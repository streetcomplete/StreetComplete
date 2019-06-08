package de.westnordost.streetcomplete.quests.bus_stop_shelter

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.*

class AddBusStopShelterForm : AYesNoQuestAnswerFragment<BusStopShelterAnswer>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_busStopShelter_covered) { applyAnswer(COVERED) }
    )

    override fun onClick(answer: Boolean) { applyAnswer(if(answer) SHELTER else NO_SHELTER) }
}
