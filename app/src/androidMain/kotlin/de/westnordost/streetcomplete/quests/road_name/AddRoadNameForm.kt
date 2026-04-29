package de.westnordost.streetcomplete.quests.road_name

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalizedNameQuestForm
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

class AddRoadNameForm : AbstractOsmQuestForm<RoadNameAnswer>() {

    private val prefs: Preferences by inject()
    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    private val initialLocalizedNames = mutableStateOf<List<LocalizedName>?>(null)

    @Composable
    override fun Content() {
        var confirmNoStreetName by remember { mutableStateOf(false) }

        LocalizedNameQuestForm(
            prefs = prefs,
            countryInfo = countryInfo,
            initialLocalizedNames = initialLocalizedNames.value,
            onClickOk = { applyAnswer(RoadName(it)) },
            onNoNameSign = { confirmNoStreetName = true },
            hint = {
                Text(stringResource(Res.string.quest_streetName_abbreviation_instruction))
            }
        )

        if (confirmNoStreetName) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoStreetName = false },
                onConfirmed = { applyAnswer(RoadNameAnswer.NoName) },
                titleText = stringResource(Res.string.quest_name_answer_noName_confirmation_title),
                text = { Text(stringResource(Res.string.quest_streetName_answer_noName_confirmation_description)) },
                confirmButtonText = stringResource(Res.string.quest_name_noName_confirmation_positive),
            )
        }
    }

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        nameSuggestionsSource.getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.let { initialLocalizedNames.value = it }
        return true
    }
}
