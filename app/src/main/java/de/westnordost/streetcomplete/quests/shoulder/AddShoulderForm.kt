package de.westnordost.streetcomplete.quests.shoulder

import android.os.Bundle
import android.view.View
import android.widget.TextView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideItem

class AddShoulderForm : AStreetSideSelectFragment<Boolean, ShoulderSides>() {

    override val items = listOf(
        StreetSideItem(
            false,
            R.drawable.ic_shoulder_illustration_no,
            R.string.quest_shoulder_value_no,
            R.drawable.ic_shoulder_no
        ),
        StreetSideItem(
            true,
            R.drawable.ic_shoulder_illustration_yes,
            R.string.quest_shoulder_value_yes,
            R.drawable.ic_shoulder_yes
        )
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.descriptionLabel).setText(R.string.quest_shoulder_explanation)
    }

    override fun onClickOk(leftSide: Boolean, rightSide: Boolean) {
        applyAnswer(ShoulderSides(leftSide, rightSide))
    }
}
