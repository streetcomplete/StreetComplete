package de.westnordost.streetcomplete.quests.road_name

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.NameSuggestionsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.view.localized_name.showKeyboardInfo
import org.koin.android.ext.android.inject

class AddRoadNameForm : AAddLocalizedNameForm<RoadNameAnswer>() {

    private val nameSuggestionsSource: NameSuggestionsSource by inject()

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_name_answer_noName) { confirmNoStreetName() },
        AnswerItem(R.string.quest_streetName_answer_cantType) { showKeyboardInfo(requireContext()) }
    )

    private val roadsWithNamesFilter =
        "ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} and name"
            .toElementFilterExpression()

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        nameSuggestionsSource.getNames(position, clickAreaSizeInMeters, roadsWithNamesFilter)
            .firstOrNull()
            ?.let { localizedNames.value = it }

        return true
    }

    override fun showAbbreviationsHint(): Boolean = true

    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(RoadName(names))
    }

    private fun confirmNoStreetName() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setMessage(R.string.quest_streetName_answer_noName_confirmation_description)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(RoadNameAnswer.NoName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
