package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.LastPickedValuesStore
import de.westnordost.streetcomplete.quests.localized_name.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.localized_name.AddLocalizedNameAdapter
import de.westnordost.streetcomplete.quests.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import java.util.*
import javax.inject.Inject

class AddAddressStreetForm : AAddLocalizedNameForm<AddressStreetAnswer>() {
    private var isPlaceName = false
    private var defaultName = ""

    @Inject internal lateinit var abbreviationsByLocale: AbbreviationsByLocale
    @Inject internal lateinit var roadNameSuggestionsDao: RoadNameSuggestionsDao

    @Inject internal lateinit var favs: LastPickedValuesStore<String>

    init {
        Injector.instance.applicationComponent.inject(this)
        val lastPickedNames = favs.get(javaClass.simpleName)
        defaultName = if (lastPickedNames.isEmpty()) "" else lastPickedNames.first
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isPlaceName = savedInstanceState?.getBoolean(IS_PLACENAME) ?: false
        contentLayoutResId = if (isPlaceName) R.layout.quest_localized_name_place else R.layout.quest_localizedname
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PLACENAME, isPlaceName)
    }

    override fun onClickOk(names: List<LocalizedName>) {
        val possibleAbbreviations = LinkedList<String>()
        for ((languageCode, name) in adapter.localizedNames) {
            val locale = if(languageCode.isEmpty()) countryInfo.locale else Locale(languageCode)
            val abbr = abbreviationsByLocale.get(locale)
            val containsAbbreviations = abbr?.containsAbbreviations(name) == true

            if (name.contains(".") || containsAbbreviations) {
                possibleAbbreviations.add(name)
            }
        }

        if (!isPlaceName) {
            favs.add(javaClass.simpleName, names.first().name, max = 1)
        }

        confirmPossibleAbbreviationsIfAny(possibleAbbreviations) {
            if(isPlaceName) {
                applyAnswer(PlaceName(names))
            } else {
                applyAnswer(StreetName(names))
            }
        }
    }

    override val otherAnswers = listOf(
            OtherAnswer(R.string.quest_address_street_no_named_streets) { switchToPlaceNameLayout() }
    )

    override fun createLocalizedNameAdapter(data: List<LocalizedName>, addLanguageButton: View) =
        AddLocalizedNameAdapter(
            data, activity!!, getPossibleStreetsignLanguages(),
            abbreviationsByLocale, getNameSuggestions(), addLanguageButton,
            if (isPlaceName) R.layout.quest_localized_name_place_row else R.layout.quest_localizedname_row,
            defaultName
        )

    private fun getNameSuggestions(): List<MutableMap<String, String>> {
        return if (isPlaceName) {
            emptyList()
        } else {
            roadNameSuggestionsDao.getNames(
                geometryToMajorPoints(elementGeometry),
                AddAddressStreet.MAX_DIST_FOR_ROAD_NAME_SUGGESTION
            )
        }
    }

    private fun geometryToMajorPoints(geometry: ElementGeometry): List<LatLon> {
        return when(geometry) {
            is ElementPolylinesGeometry -> {
                val polyline = geometry.polylines.first()
                listOf(polyline.first(), polyline.last())
            }
            is ElementPolygonsGeometry -> {
                // return center and one of nodes from the way constructing area
                listOf(geometry.center, geometry.polygons.first().last())
            }
            is ElementPointGeometry -> {
                listOf(geometry.center)
            }
        }
    }

    override fun isRejectingClose() : Boolean {
        // if the form is complete, we will reject close, unless it is still has only the default name
        return isFormComplete() && !(adapter.localizedNames.size == 1 && adapter.localizedNames.first().name == defaultName)
    }

    private fun switchToPlaceNameLayout() {
        isPlaceName = true
        defaultName = ""
        setLayout(R.layout.quest_localized_name_place)
    }

    companion object {
        private const val IS_PLACENAME = "is_placename"
    }
}
