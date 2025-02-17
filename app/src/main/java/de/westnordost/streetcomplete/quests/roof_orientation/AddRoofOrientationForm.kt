package de.westnordost.streetcomplete.quests.roof_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddRoofOrientationForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("along", R.string.quest_roofOrientation_along),
        TextItem("across", R.string.quest_roofOrientation_across),
    )
}
