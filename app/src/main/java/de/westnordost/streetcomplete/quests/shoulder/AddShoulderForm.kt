package de.westnordost.streetcomplete.quests.shoulder

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem

class AddShoulderForm : AStreetSideSelectFragment<Boolean, ShoulderSides>() {

    override val contentLayoutResId = R.layout.quest_shoulder_explanation

    override val items: List<StreetSideDisplayItem<Boolean>>
        get() = TODO()

    override fun onClickOk(leftSide: Boolean, rightSide: Boolean) {
        applyAnswer(ShoulderSides(leftSide, rightSide))
    }
}
