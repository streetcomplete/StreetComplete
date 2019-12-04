package de.westnordost.streetcomplete.quests.address

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.View
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
        onClickOk(adapter.name)
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

    fun setupNameAdapter(data: List<Name>): AddNameSuggestionAdapter {
        return AddNameSuggestionAdapter(
                data, activity!!, getRoadNameSuggestions()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLocalizedNameAdapter(savedInstanceState)
    }

    private fun initLocalizedNameAdapter(savedInstanceState: Bundle?) {
        val data: ArrayList<Name> = if (savedInstanceState != null) {
            serializer.toObject(savedInstanceState.getByteArray(NAMES_DATA)!!)
        } else {
            ArrayList()
        }

        adapter = setupNameAdapter(data)
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
        val serializedName = serializer.toBytes(adapter.name)
        outState.putByteArray(NAMES_DATA, serializedName)
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
    override fun isFormComplete() = adapter.name.trim() != ""


    internal class InjectedFields {
        @Inject internal lateinit var serializer: Serializer
    }

    companion object {
        private const val NAMES_DATA = "names_data"
    }

}
