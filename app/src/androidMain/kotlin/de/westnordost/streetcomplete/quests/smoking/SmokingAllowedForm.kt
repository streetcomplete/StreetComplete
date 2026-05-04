package de.westnordost.streetcomplete.quests.smoking

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.*
import de.westnordost.streetcomplete.ui.common.quest.RadioGroupQuestForm
import org.jetbrains.compose.resources.stringResource

class SmokingAllowedForm : AbstractOsmQuestForm<SmokingAllowed>() {

    @Composable
    override fun Content() {
        val items = remember {
            val tags = element.tags
            val isAlreadyOutdoor =
                tags["leisure"] == "outdoor_seating" ||
                tags["amenity"] == "biergarten" ||
                (tags["outdoor_seating"] == "yes" && tags["indoor_seating"] == "no")
            val noOutdoorSmoking =
                tags["outdoor_seating"] == "no" &&
                tags["amenity"] != "nightclub" &&
                tags["amenity"] != "stripclub" &&
                tags["amenity"] != "pub"
            // nightclubs etc. might have outside smoking areas even when there is no seating outside

            listOfNotNull(
                NO,
                if (isAlreadyOutdoor || noOutdoorSmoking) null else OUTSIDE,
                SEPARATED,
                YES,
            )
        }

        RadioGroupQuestForm(
            items = items,
            itemContent = { Text(stringResource(it.text)) },
            onClickOk = { applyAnswer(it) }
        )
    }
}
