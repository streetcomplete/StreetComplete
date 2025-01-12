package de.westnordost.streetcomplete.quests.bus_stop_name

import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.databinding.QuestLocalizednameBinding
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.quests.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.AnswerItem
import org.koin.android.ext.android.inject

class AddBusStopNameForm : AAddLocalizedNameForm<BusStopNameAnswer>() {

    override val contentLayoutResId = R.layout.quest_localizedname
    private val binding by contentViewBinding(QuestLocalizednameBinding::bind)

    override val addLanguageButton get() = binding.addLanguageButton
    override val namesList get() = binding.namesList

    override val adapterRowLayoutResId = R.layout.quest_localizedname_row

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_placeName_no_name_answer) { confirmNoName() },
        AnswerItem(R.string.quest_streetName_answer_cantType) { showKeyboardInfo() }
    )
    private val busStopNameSuggestionsSource: BusStopNameSuggestionsSource by inject()

    override fun getLocalizedNameSuggestions(): List<List<LocalizedName>> {
        val polyline = when (val geom = geometry) {
            is ElementPolylinesGeometry -> geom.polylines.first()
            is ElementPolygonsGeometry -> geom.polygons.first()
            is ElementPointGeometry -> listOf(geom.center)
        }
        return busStopNameSuggestionsSource.getNames(
            listOf(polyline.first(), polyline.last()),
            MAX_DIST_FOR_BUS_STOP_NAME_SUGGESTION
        )
    }
    override fun onClickOk(names: List<LocalizedName>) {
        applyAnswer(BusStopName(names))
    }

    private fun confirmNoName() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(NoBusStopName) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    companion object {
        const val MAX_DIST_FOR_BUS_STOP_NAME_SUGGESTION = 250.0 // m
    }
}
