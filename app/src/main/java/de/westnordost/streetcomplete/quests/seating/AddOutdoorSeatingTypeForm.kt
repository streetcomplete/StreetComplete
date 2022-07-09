package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AListQuestForm
import de.westnordost.streetcomplete.quests.TextItem

class AddOutdoorSeatingTypeForm : AListQuestForm<String>() {
    override val items = listOf(
        TextItem("parklet", R.string.quest_seating_parklet),
        TextItem("pedestrian_zone", R.string.quest_seating_pedestrian_zone),
        TextItem("street", R.string.quest_seating_street),
        TextItem("sidewalk", R.string.quest_seating_sidewalk),
        TextItem("patio", R.string.quest_seating_patio),
        TextItem("terrace", R.string.quest_seating_terrace),
        TextItem("balcony", R.string.quest_seating_balcony),
        TextItem("veranda", R.string.quest_seating_veranda),
        TextItem("roof", R.string.quest_seating_roof),
        TextItem("garden", R.string.quest_seating_garden),
        TextItem("beach", R.string.quest_seating_beach),
    )
}
