package de.westnordost.streetcomplete.quests.address

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import javax.inject.Inject

class AddAddressStreetForm : AbstractQuestFormAnswerFragment<AddressStreetAnswer>() {
    private var textField: EditText? = null
    private var isPlacename = false

    private val serializer: Serializer

    init {
        val fields = InjectedFields()
        Injector.instance.applicationComponent.inject(fields)
        serializer = fields.serializer
    }

    override fun onClickOk() {
        onClickOk(textField!!.text.toString())
    }

    fun onClickOk(name: String) {
        if(isPlacename) {
            applyAnswer(PlaceName(name))
        } else {
            applyAnswer(StreetName(name))
        }
    }

    override val contentLayoutResId = R.layout.quest_streetname

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttonNameSuggestions : View = view.findViewById(R.id.nameSuggestionsButton)
        val nameSuggestionsMap = getRoadNameSuggestions()
        val nameSuggestionsList = mutableListOf<String>()
        for (NameSuggestion in nameSuggestionsMap) {
            val name = NameSuggestion[""] ?: continue // just default language names
            nameSuggestionsList += name
        }
        textField = view.findViewById(R.id.name)
        buttonNameSuggestions.setOnClickListener { v ->
            showNameSuggestionsMenu(v, nameSuggestionsList) { selected ->
                textField!!.setText(selected)
            }
        }
        textField!!.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val serializedName = serializer.toBytes(textField!!.text.toString())
        outState.putByteArray(NAMES_DATA, serializedName)
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
        isPlacename = true
        setLayout(R.layout.quest_streetname_place)
    }

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)
        val buttonNameSuggestions : View = view.findViewById(R.id.nameSuggestionsButton)
        textField = view.findViewById(R.id.name)
        buttonNameSuggestions.setOnClickListener { v ->
            showNameSuggestionsMenu(v, listOf("dummy", "trololol")) { selected -> ;
                textField!!.setText(selected)
            }
        }
        textField!!.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    protected fun showKeyboardInfo() {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_streetName_cantType_title)
                .setMessage(R.string.quest_streetName_cantType_description)
                .setPositiveButton(R.string.quest_streetName_cantType_open_settings) { _, _ ->
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
                .setNeutralButton(R.string.quest_streetName_cantType_open_store) { _, _ ->
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_APP_MARKET)
                    startActivity(intent)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    // all added name rows are not empty
    override fun isFormComplete() = textField!!.text.toString().trim() != ""


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
