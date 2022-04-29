package de.westnordost.streetcomplete.quests.shoulder

import android.os.Bundle
import android.view.View
import android.widget.TextView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AStreetSideSelectFragment
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem
import de.westnordost.streetcomplete.quests.StreetSideItem
import de.westnordost.streetcomplete.util.ktx.shoulderLineStyleResId

class AddShoulderForm : AStreetSideSelectFragment<Boolean, ShoulderSides>() {

    override val items = listOf(false, true)

    override fun getDisplayItem(value: Boolean): StreetSideDisplayItem<Boolean> =
        if (value) {
            StreetSideItem(
                value,
                countryInfo.shoulderLineStyleResId ?: R.drawable.ic_shoulder_white_line,
                R.string.quest_shoulder_value_yes
            )
        } else {
            StreetSideItem(value, R.drawable.ic_shoulder_no, R.string.quest_shoulder_value_no)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.descriptionLabel).setText(R.string.quest_shoulder_explanation2)
    }

    override fun onClickOk(leftSide: Boolean?, rightSide: Boolean?) {
        applyAnswer(ShoulderSides(leftSide!!, rightSide!!))
    }
}
