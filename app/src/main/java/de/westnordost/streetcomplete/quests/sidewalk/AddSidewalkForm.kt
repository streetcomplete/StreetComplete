package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem


class AddSidewalkForm : AStreetSideSelectFragment<Sidewalk, SidewalkSides>() {

    override val items: List<StreetSideDisplayItem<Sidewalk>>
        get() = Sidewalk.values().map { it.asStreetSideDisplayItem() }

    override fun onClickOk(leftSide: Sidewalk, rightSide: Sidewalk) {
        applyAnswer(SidewalkSides(leftSide, rightSide))
    }

}
