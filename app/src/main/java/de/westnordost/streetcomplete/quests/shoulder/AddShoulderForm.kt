package de.westnordost.streetcomplete.quests.shoulder

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText

class AddShoulderForm : AStreetSideSelectFragment<Boolean, ShoulderSides>() {

    override val contentLayoutResId = R.layout.quest_shoulder_explanation

    override val items = listOf(
        StreetSideDisplayItem(
            value = false,
            image = ResImage(R.drawable.ic_shoulder_no_illustration),
            icon = ResImage(R.drawable.ic_shoulder_no),
            title = ResText(R.string.quest_shoulder_value_no)
        ),
        StreetSideDisplayItem(
            value = true,
            image = ResImage(R.drawable.ic_shoulder_yes_illustration),
            icon = ResImage(R.drawable.ic_shoulder_yes),
            title = ResText(R.string.quest_shoulder_value_yes)
        )
    )

    override fun onClickOk(leftSide: Boolean, rightSide: Boolean) {
        applyAnswer(ShoulderSides(leftSide, rightSide))
    }
}
