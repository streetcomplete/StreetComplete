package de.westnordost.streetcomplete.quests.tourism_information

import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.BOARD
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.GUIDEPOST
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.MAP
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.OFFICE
import de.westnordost.streetcomplete.quests.tourism_information.TourismInformation.TERMINAL
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_tourism_information_board
import de.westnordost.streetcomplete.resources.quest_tourism_information_guidepost
import de.westnordost.streetcomplete.resources.quest_tourism_information_map
import de.westnordost.streetcomplete.resources.quest_tourism_information_office
import de.westnordost.streetcomplete.resources.quest_tourism_information_terminal
import de.westnordost.streetcomplete.resources.tourism_information_board
import de.westnordost.streetcomplete.resources.tourism_information_guidepost
import de.westnordost.streetcomplete.resources.tourism_information_map
import de.westnordost.streetcomplete.resources.tourism_information_office
import de.westnordost.streetcomplete.resources.tourism_information_terminal
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val TourismInformation.title: StringResource get() = when (this) {
    OFFICE ->    Res.string.quest_tourism_information_office
    BOARD ->     Res.string.quest_tourism_information_board
    TERMINAL ->  Res.string.quest_tourism_information_terminal
    MAP ->       Res.string.quest_tourism_information_map
    GUIDEPOST -> Res.string.quest_tourism_information_guidepost
}

val TourismInformation.icon: DrawableResource get() = when (this) {
    OFFICE ->    Res.drawable.tourism_information_office
    BOARD ->     Res.drawable.tourism_information_board
    TERMINAL ->  Res.drawable.tourism_information_terminal
    MAP ->       Res.drawable.tourism_information_map
    GUIDEPOST -> Res.drawable.tourism_information_guidepost
}
