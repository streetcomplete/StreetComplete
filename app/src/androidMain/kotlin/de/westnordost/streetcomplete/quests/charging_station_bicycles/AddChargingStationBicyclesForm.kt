package de.westnordost.streetcomplete.quests.charging_station_bicycles

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.charging_station_bicycles.ChargingStationBicycles.*

class AddChargingStationBicyclesForm : AbstractOsmQuestForm<ChargingStationBicycles>() {
    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES) },
        AnswerItem(R.string.quest_hasFeature_only) { applyAnswer(ONLY) },
    )
}
