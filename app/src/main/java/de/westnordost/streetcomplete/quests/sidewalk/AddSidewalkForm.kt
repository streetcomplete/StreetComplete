package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment


class AddSidewalkForm : AStreetSideSelectFragment<Sidewalk, SidewalkSides>() {

    override val items = Sidewalk.values().map { it.asStreetSideItem() }

    override fun onClickOk(leftSide: Sidewalk, rightSide: Sidewalk) {
        applyAnswer(SidewalkSides(leftSide, rightSide))
    }
}
