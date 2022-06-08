package de.westnordost.streetcomplete.quests.bus_stop_shelter

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.COVERED
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.NO_SHELTER
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.SHELTER

class AddBusStopShelterForm : AbstractOsmQuestForm<BusStopShelterAnswer>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO_SHELTER) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(SHELTER) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_busStopShelter_covered) { applyAnswer(COVERED) }
    )
}
