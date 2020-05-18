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
import de.westnordost.streetcomplete.quests.localized_name.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.localized_name.AddLocalizedNameAdapter
import de.westnordost.streetcomplete.quests.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import java.util.*
import javax.inject.Inject

class AddAddressStreetForm : AAddLocalizedNameForm<AddressStreetAnswer>() {
    private var isPlaceName = false

    @Inject internal lateinit var abbreviationsByLocale: AbbreviationsByLocale
    @Inject internal lateinit var roadNameSuggestionsDao: RoadNameSuggestionsDao

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_address_street_no_named_streets) { switchToPlaceNameLayout() }
    )

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

        confirmPossibleAbbreviationsIfAny(possibleAbbreviations) {
            if(isPlaceName) {
                applyAnswer(PlaceName(names))
            } else {
                applyAnswer(StreetName(names))
            }
        }
    }

    override fun createLocalizedNameAdapter(data: List<LocalizedName>, addLanguageButton: View) =
        AddLocalizedNameAdapter(
            data, activity!!, getPossibleStreetsignLanguages(),
            abbreviationsByLocale, getNameSuggestions(), addLanguageButton,
            getRowLayoutResId()
        )

    private fun getNameSuggestions(): List<MutableMap<String, String>> {
        return if (isPlaceName) {
            emptyList()
        } else {
            roadNameSuggestionsDao.getNames(
                listOf(elementGeometry.center),
                AddAddressStreet.MAX_DIST_FOR_ROAD_NAME_SUGGESTION
            ).take(4)
            /* taking the four closest streets because in the worst case, the building is located
               on an island surrounded on every side by a street */
        }
    }

    private fun getRowLayoutResId(): Int =
        if (isPlaceName) R.layout.quest_localized_name_place_row else R.layout.quest_localizedname_row

    private fun switchToPlaceNameLayout() {
        isPlaceName = true
        setLayout(R.layout.quest_localized_name_place)
    }

    companion object {
        private const val IS_PLACENAME = "is_placename"
    }
}
