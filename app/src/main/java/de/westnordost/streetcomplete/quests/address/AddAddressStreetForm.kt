package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.quests.localized_name.AAddLocalizedNameForm
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.localized_name.AddLocalizedNameAdapter
import de.westnordost.streetcomplete.quests.localized_name.LocalizedName
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import java.util.*
import javax.inject.Inject

class AddAddressStreetForm : AAddLocalizedNameForm<AddressStreetAnswer>() {
    private var textField: EditText? = null
    private var isPlaceName = false

    private val serializer: Serializer

    init {
        val fields = InjectedFields()
        Injector.instance.applicationComponent.inject(fields)
        serializer = fields.serializer
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

    override val otherAnswers = listOf(
            OtherAnswer(R.string.quest_address_street_no_named_streets) { switchToPlaceName() }
    )

    @Inject
    internal lateinit var abbreviationsByLocale: AbbreviationsByLocale
    @Inject
    internal lateinit var roadNameSuggestionsDao: RoadNameSuggestionsDao

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val serializedName = serializer.toBytes(textField!!.text.toString())
        outState.putByteArray(NAMES_DATA, serializedName)
    }

    override fun setupNameAdapter(data: List<LocalizedName>, addLanguageButton: View): AddLocalizedNameAdapter {
        return AddLocalizedNameAdapter(
                data, activity!!, getPossibleStreetsignLanguages(),
                abbreviationsByLocale, getRoadNameSuggestions(), addLanguageButton
        )
    }

    private fun getRoadNameSuggestions(): List<MutableMap<String, String>> {
        return roadNameSuggestionsDao.getNames(
                geometryToMajorPoints(elementGeometry),
                AddAddressStreet.MAX_DIST_FOR_ROAD_NAME_SUGGESTION_IN_METERS
        )
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

    private fun switchToPlaceName() {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_address_street_noStreet_confirmation_title)
                .setPositiveButton(R.string.quest_address_street_noStreet_confirmation_positive) {_, _ -> switchToPlaceNameLayout() } //
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
    }

    private fun switchToPlaceNameLayout() {
        isPlaceName = true
        setLayout(R.layout.quest_streetname_place)
    }

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)
        val buttonNameSuggestions : View = view.findViewById(R.id.nameSuggestionsButton)
        textField = view.findViewById(R.id.name)

        //TODO - use actual place names
        val nameSuggestionsMap = getRoadNameSuggestions()
        val nameSuggestionsList = mutableListOf<String>()
        for (NameSuggestion in nameSuggestionsMap) {
            val name = NameSuggestion[""] ?: continue // just default language names
            nameSuggestionsList += name
        }

        buttonNameSuggestions.setOnClickListener { v ->
            showNameSuggestionsMenu(v, nameSuggestionsList) { selected -> ;
                textField!!.setText(selected)
            }
        }
        textField!!.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    internal class InjectedFields {
        @Inject internal lateinit var serializer: Serializer
    }

    companion object {
        private const val NAMES_DATA = "names_data"
    }

    /** Show a context menu above the given [view] where the user can select one key from the
     * [nameSuggestionList]. The value of the selected key will be passed to the
     * [callback] */
    private fun showNameSuggestionsMenu(
            view: View,
            nameSuggestionList: List<String>,
            callback: (String) -> Unit
    ) {
        val popup = PopupMenu(activity!!, view)

        for ((i, key) in nameSuggestionList.withIndex()) {
            popup.menu.add(Menu.NONE, i, Menu.NONE, key)
        }

        popup.setOnMenuItemClickListener { item ->
            callback(item.title.toString())
            true
        }
        popup.show()
    }

}
