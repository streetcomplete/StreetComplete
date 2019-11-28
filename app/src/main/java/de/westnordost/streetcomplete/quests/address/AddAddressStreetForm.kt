package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.localized_name.*
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.quest_placename.*
import java.util.*
import javax.inject.Inject

class AddAddressStreetForm : AAddLocalizedNameForm<AddressStreetAnswer>() {
    override fun onClickOk(names: List<LocalizedName>) {
        assert(names.size == 1)
        val name = names[0].name
        val possibleAbbreviations = LinkedList<String>()
        val locale = countryInfo.locale
        val abbr = abbreviationsByLocale.get(locale)
        val containsAbbreviations = abbr?.containsAbbreviations(name) == true
        if (name.contains(".") || containsAbbreviations) {
            possibleAbbreviations.add(name)
        }
        confirmPossibleAbbreviationsIfAny(possibleAbbreviations) {
            applyAnswer(StreetName(name))
        }
    }

    override val contentLayoutResId = R.layout.quest_streetname

    override val otherAnswers = listOf(
            OtherAnswer(R.string.quest_address_street_no_named_streets) { confirmNoName() }
    )

    @Inject
    internal lateinit var abbreviationsByLocale: AbbreviationsByLocale
    @Inject
    internal lateinit var roadNameSuggestionsDao: RoadNameSuggestionsDao

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun setupNameAdapter(data: List<LocalizedName>, addLanguageButton: Button): AddLocalizedNameAdapter {
        return AddLocalizedNameAdapter(
                data, activity!!, listOf("dummy"), //FIX this horrific hack
                abbreviationsByLocale, getRoadNameSuggestions(), addLanguageButton
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    private fun getRoadNameSuggestions(): List<MutableMap<String, String>> {
        return roadNameSuggestionsDao.getNames(
                geometryToMajorPoints(elementGeometry),
                AddRoadName.MAX_DIST_FOR_ROAD_NAME_SUGGESTION
        )
    }

    private fun geometryToMajorPoints(geometry: ElementGeometry): List<LatLon> {
        when(geometry) {
            is ElementPolylinesGeometry -> {
                val polyline = geometry.polylines.first()
                return listOf(polyline.first(), polyline.last())
            }
            is ElementPolygonsGeometry -> {
                // return center and one of nodes from the way constructing area
                return listOf(geometry.center, geometry.polygons.first().last())
            }
            is ElementPointGeometry -> {
                return listOf(geometry.center)
            }
        }


    }

    private fun confirmNoName() {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_name_answer_noName_confirmation_title)
                .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(PlaceName("TODO")) } //TODO!
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
    }
}
