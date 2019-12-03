package de.westnordost.streetcomplete.quests.address

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.data.osm.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.ktx.toObject
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNameSuggestionsDao
import de.westnordost.streetcomplete.util.AdapterDataChangedWatcher
import de.westnordost.streetcomplete.util.Serializer
import kotlinx.android.synthetic.main.quest_streetname.addLanguageButton
import kotlinx.android.synthetic.main.quest_streetname.namesList
import java.util.*
import javax.inject.Inject

class AddAddressStreetForm : AbstractQuestFormAnswerFragment<AddressStreetAnswer>() {
    private var isPlacename = false

    private val serializer: Serializer

    protected lateinit var adapter: AddNameSuggestionAdapter

    init {
        val fields = InjectedFields()
        Injector.instance.applicationComponent.inject(fields)
        serializer = fields.serializer
    }

    override fun onClickOk() {
        onClickOk(createOsmModel())
    }

    fun onClickOk(names: List<Name>) {
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
            if(isPlacename) {
                applyAnswer(PlaceName(name))
            } else {
                applyAnswer(StreetName(name))
            }
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

    fun setupNameAdapter(data: List<Name>, addLanguageButton: Button): AddNameSuggestionAdapter {
        return AddNameSuggestionAdapter(
                data, activity!!, listOf("dummy"), //FIX this horrific hack
                abbreviationsByLocale, getRoadNameSuggestions(), addLanguageButton
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLocalizedNameAdapter(savedInstanceState)
    }

    private fun initLocalizedNameAdapter(savedInstanceState: Bundle?) {
        val data: ArrayList<Name> = if (savedInstanceState != null) {
            serializer.toObject(savedInstanceState.getByteArray(LOCALIZED_NAMES_DATA)!!)
        } else {
            ArrayList()
        }

        adapter = setupNameAdapter(data, addLanguageButton)
        adapter.addOnNameChangedListener { checkIsFormComplete() }
        adapter.registerAdapterDataObserver(AdapterDataChangedWatcher { checkIsFormComplete() })
        namesList.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        namesList.adapter = adapter
        namesList.isNestedScrollingEnabled = false
        checkIsFormComplete()
    }

    protected fun getPossibleStreetsignLanguages(): List<String> {
        val result = mutableListOf<String>()
        result.addAll(countryInfo.officialLanguages)
        result.addAll(countryInfo.additionalStreetsignLanguages)
        return result.distinct()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val serializedNames = serializer.toBytes(ArrayList(adapter.localizedNames))
        outState.putByteArray(LOCALIZED_NAMES_DATA, serializedNames)
    }

    private fun createOsmModel(): List<Name> {
        val data = adapter.localizedNames.toMutableList()
        // language is only specified explicitly in OSM (usually) if there is more than one name specified
        if(data.size == 1) {
            data[0].languageCode = ""
        }
        // but if there is more than one language, ensure that a "main" name is also specified
        else {
            val mainLanguageIsSpecified = data.indexOfFirst { it.languageCode == "" } >= 0
            // use the name specified in the top row for that
            if(!mainLanguageIsSpecified) {
                data.add(Name("", data[0].name))
            }
        }
        return data
    }

    private fun getRoadNameSuggestions(): List<MutableMap<String, String>> {
        return roadNameSuggestionsDao.getNames(
                geometryToMajorPoints(elementGeometry),
                AddAddressStreet.MAX_DIST_FOR_ROAD_NAME_SUGGESTION_IN_METERS
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

        /*
        houseNumberInput = view.findViewById(R.id.houseNumberInput)
        houseNameInput = view.findViewById(R.id.houseNameInput)
        conscriptionNumberInput = view.findViewById(R.id.conscriptionNumberInput)
        streetNumberInput = view.findViewById(R.id.streetNumberInput)
        blockNumberInput = view.findViewById(R.id.blockNumberInput)

        val onChanged = TextChangedWatcher { checkIsFormComplete() }
        houseNumberInput?.addTextChangedListener(onChanged)
        houseNameInput?.addTextChangedListener(onChanged)
        conscriptionNumberInput?.addTextChangedListener(onChanged)
        streetNumberInput?.addTextChangedListener(onChanged)
        blockNumberInput?.addTextChangedListener(onChanged)

        // streetNumber is always optional
        val input = AddHousenumberForm.getFirstNonNull(blockNumberInput, houseNumberInput, houseNameInput, conscriptionNumberInput)
        input?.requestFocus()

        initKeyboardButton(view)
         */
    }

    protected fun confirmPossibleAbbreviationsIfAny(names: Queue<String>, onConfirmedAll: () -> Unit) {
        if (names.isEmpty()) {
            onConfirmedAll()
        } else {
            /* recursively call self on confirm until the list of not-abbreviations to confirm is
               through */
            val name = names.remove()
            confirmPossibleAbbreviation(name) { confirmPossibleAbbreviationsIfAny(names, onConfirmedAll) }
        }
    }

    protected fun confirmPossibleAbbreviation(name: String, onConfirmed: () -> Unit) {
        val title = Html.fromHtml(
                resources.getString(
                        R.string.quest_streetName_nameWithAbbreviations_confirmation_title_name,
                        "<i>" + Html.escapeHtml(name) + "</i>"
                )
        )

        AlertDialog.Builder(activity!!)
                .setTitle(title)
                .setMessage(R.string.quest_streetName_nameWithAbbreviations_confirmation_description)
                .setPositiveButton(R.string.quest_streetName_nameWithAbbreviations_confirmation_positive) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
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
    override fun isFormComplete() = adapter.localizedNames.isNotEmpty()
            && adapter.localizedNames.all { it.name.trim().isNotEmpty() }


    internal class InjectedFields {
        @Inject internal lateinit var serializer: Serializer
    }

    companion object {
        private const val LOCALIZED_NAMES_DATA = "localized_names_data"
    }

}
