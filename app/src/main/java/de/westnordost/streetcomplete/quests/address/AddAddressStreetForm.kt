package de.westnordost.streetcomplete.quests.address

import android.view.Menu
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
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
import de.westnordost.streetcomplete.util.TextChangedWatcher
import java.util.*
import javax.inject.Inject

class AddAddressStreetForm : AAddLocalizedNameForm<AddressStreetAnswer>() {
    private var isPlaceName = false
    private var defaultName = ""

    @Inject
    internal lateinit var abbreviationsByLocale: AbbreviationsByLocale
    @Inject
    internal lateinit var roadNameSuggestionsDao: RoadNameSuggestionsDao

    @Inject internal lateinit var favs: LastPickedValuesStore<String>

    init {
        Injector.instance.applicationComponent.inject(this)
        val lastPickedNames = favs.get(javaClass.simpleName)
        defaultName = if (lastPickedNames.isEmpty()) {""} else {lastPickedNames.first}
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

        favs.add(javaClass.simpleName,
                names.first().name, max = 1)

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

    override fun setupNameAdapter(data: List<LocalizedName>, addLanguageButton: View): AddLocalizedNameAdapter {

        return AddLocalizedNameAdapter(
                data, activity!!, getPossibleStreetsignLanguages(),
                abbreviationsByLocale, getNameSuggestions(), addLanguageButton,
                defaultName
        )
    }

    private fun getNameSuggestions(): List<MutableMap<String, String>> {
        return if (isPlaceName) {
            emptyList<MutableMap<String, String>>()
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
        changeDescriptionLabel(resources.getString(R.string.quest_address_street_place_name_label))
        defaultName = ""
        initLocalizedNameAdapter(null)
    }

}
