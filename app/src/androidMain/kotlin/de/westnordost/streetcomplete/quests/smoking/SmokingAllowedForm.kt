package de.westnordost.streetcomplete.quests.smoking

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.ARadioGroupQuestForm
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.NO
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.OUTSIDE
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.SEPARATED
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.YES
import org.jetbrains.compose.resources.stringResource

class SmokingAllowedForm : ARadioGroupQuestForm<SmokingAllowed, SmokingAllowed>() {

    override val items: List<SmokingAllowed> get() {
        val tags = element.tags
        val isAlreadyOutdoor =
            tags["leisure"] == "outdoor_seating" || tags["amenity"] == "biergarten" ||
            (tags["outdoor_seating"] == "yes" && tags["indoor_seating"] == "no")
        val noOutdoorSmoking =
            tags["outdoor_seating"] == "no" &&
            tags["amenity"] != "nightclub" && tags["amenity"] != "stripclub" && tags["amenity"] != "pub"
            // nightclubs etc. might have outside smoking areas even when there is no seating outside

        return listOfNotNull(
            NO,
            if (isAlreadyOutdoor || noOutdoorSmoking) null else OUTSIDE,
            SEPARATED,
            YES,
        )
    }

    @Composable override fun BoxScope.ItemContent(item: SmokingAllowed) {
        Text(stringResource(item.text))
    }
}
