package de.westnordost.streetcomplete.quests.tourism_information

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.BOARD
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.GUIDEPOST
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.MAP
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.OFFICE
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.TERMINAL
import de.westnordost.streetcomplete.view.image_select.Item

fun TourismInformation.asItem() = Item(this, iconResId, titleResId)

private val TourismInformation.titleResId: Int get() = when (this) {
    OFFICE ->    R.string.quest_tourism_information_office
    BOARD ->     R.string.quest_tourism_information_board
    TERMINAL ->  R.string.quest_tourism_information_terminal
    MAP ->       R.string.quest_tourism_information_map
    GUIDEPOST -> R.string.quest_tourism_information_guidepost
}

private val TourismInformation.iconResId: Int get() = when (this) {
    OFFICE ->    R.drawable.tourism_information_office
    BOARD ->     R.drawable.tourism_information_board
    TERMINAL ->  R.drawable.tourism_information_terminal
    MAP ->       R.drawable.tourism_information_map
    GUIDEPOST -> R.drawable.tourism_information_guidepost
}
