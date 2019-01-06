package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.TextInputQuestAnswerFragment
import kotlinx.android.synthetic.main.quest_motorcycle_parking_capacity.*

class AddMotorcycleParkingCapacityForm : TextInputQuestAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_motorcycle_parking_capacity

    override val editText = capacityInput!!
}
