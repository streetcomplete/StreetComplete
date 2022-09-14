package de.westnordost.streetcomplete.overlays.address

import android.os.Bundle
import android.view.View
import android.widget.EditText
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.AbbreviationsByLocale
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.databinding.FragmentOverlayAddressBinding
import de.westnordost.streetcomplete.osm.address.AddressNumber
import de.westnordost.streetcomplete.osm.address.applyTo
import de.westnordost.streetcomplete.osm.address.createAddressNumber
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.osm.address.AddressNumberAndNameInputViewController
import de.westnordost.streetcomplete.osm.address.HouseAndBlockNumber
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameViewController
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.quests.road_name.RoadNameSuggestionsSource
import de.westnordost.streetcomplete.util.ShowHouseNumber
import de.westnordost.streetcomplete.util.getNameAndLocationLabelString
import org.koin.android.ext.android.inject

class AddressOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_address
    private val binding by contentViewBinding(FragmentOverlayAddressBinding::bind)

    private val abbreviationsByLocale: AbbreviationsByLocale by inject()
    private val roadNameSuggestionsSource: RoadNameSuggestionsSource by inject()

    private lateinit var numberOrNameInputCtrl: AddressNumberAndNameInputViewController
    private lateinit var streetOrPlaceCtrl: StreetOrPlaceNameViewController

    private var addressNumber: AddressNumber? = null
    private var houseName: String? = null
    private var streetOrPlaceName: StreetOrPlaceName? = null

    private var isPlaceName = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isPlaceName = savedInstanceState?.getBoolean(IS_PLACE_NAME) ?: false

        addressNumber = createAddressNumber(element.tags)
        houseName = element.tags["addr:housename"]
        streetOrPlaceName =
            if (isPlaceName) element.tags["addr:place"]?.let { PlaceName(it) }
            else             element.tags["addr:street"]?.let { StreetName(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitleHintLabel(getNameAndLocationLabelString(
            element.tags, resources, featureDictionary,
            showHouseNumber = ShowHouseNumber.NEVER
        ))

        streetOrPlaceCtrl = StreetOrPlaceNameViewController(
            select = binding.streetOrPlaceSelect,
            placeNameInputContainer = binding.placeNameInputContainer,
            placeNameInput = binding.placeNameInput.apply { hint = lastPlaceName },
            streetNameInputContainer = binding.streetNameInputContainer,
            streetNameInput = binding.streetNameInput,
            roadNameSuggestionsSource = roadNameSuggestionsSource,
            abbreviationsByLocale = abbreviationsByLocale,
            countryLocale = countryInfo.locale
        )
        streetOrPlaceCtrl.streetOrPlaceName = streetOrPlaceName
        streetOrPlaceCtrl.onInputChanged = { checkIsFormComplete() }

        val numberOrNameBinding = binding.addressNumberOrNameContainer
        val numberView = layoutInflater.inflate(
            getAddressNumberLayoutResId(countryInfo.countryCode),
            numberOrNameBinding.countrySpecificContainer
        )
        numberOrNameInputCtrl = AddressNumberAndNameInputViewController(
            toggleHouseNameButton = numberOrNameBinding.toggleHouseNameButton,
            houseNameInput = numberOrNameBinding.houseNameInput,
            toggleAddressNumberButton = numberOrNameBinding.toggleAddressNumberButton,
            addressNumberContainer = numberOrNameBinding.addressNumberContainer,
            activity = requireActivity(),
            houseNumberInput = numberView.findViewById<EditText?>(R.id.houseNumberInput)?.apply { hint = lastHouseNumber },
            blockNumberInput = numberView.findViewById<EditText?>(R.id.blockNumberInput)?.apply { hint = lastBlockNumber },
            conscriptionNumberInput = numberView.findViewById(R.id.conscriptionNumberInput),
            streetNumberInput = numberView.findViewById(R.id.streetNumberInput),
            toggleKeyboardButton = numberOrNameBinding.toggleKeyboardButton,
            addButton = numberView.findViewById(R.id.addButton),
            subtractButton = numberView.findViewById(R.id.subtractButton),
        )
        numberOrNameInputCtrl.addressNumber = addressNumber
        numberOrNameInputCtrl.houseName = houseName
        numberOrNameInputCtrl.onInputChanged = {
            if (lastPlaceName != null) {
                streetOrPlaceCtrl.applyPlaceNameHint()
            }
            checkIsFormComplete()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PLACE_NAME, isPlaceName)
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        return streetOrPlaceCtrl.selectStreetAt(position, clickAreaSizeInMeters)
    }

    override fun hasChanges(): Boolean =
        numberOrNameInputCtrl.addressNumber != addressNumber
        || numberOrNameInputCtrl.houseName != houseName
        || streetOrPlaceCtrl.streetOrPlaceName != streetOrPlaceName

    override fun isFormComplete(): Boolean =
        numberOrNameInputCtrl.isComplete && streetOrPlaceCtrl.streetOrPlaceName != null
        // can also be empty to delete the address tagging
        || numberOrNameInputCtrl.isEmpty && streetOrPlaceCtrl.streetOrPlaceName == null

    override fun onClickOk() {
        val number = numberOrNameInputCtrl.addressNumber
        val houseName = numberOrNameInputCtrl.houseName
        val streetOrPlaceName = streetOrPlaceCtrl.streetOrPlaceName

        if (number is HouseAndBlockNumber) { number.blockNumber.let { lastBlockNumber = it } }
        number?.streetHouseNumber?.let { lastHouseNumber = it }
        lastPlaceName = if (streetOrPlaceName is PlaceName) streetOrPlaceName.name else null

        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also { tags ->
            number?.applyTo(tags)
            houseName?.let { tags["addr:housename"] = it }
            streetOrPlaceName?.applyTo(tags)
        }.create()))
    }

    companion object {
        private var lastBlockNumber: String? = null
        private var lastHouseNumber: String? = null
        private var lastPlaceName: String? = null

        private const val IS_PLACE_NAME = "is_place_name"
    }
}

private fun getAddressNumberLayoutResId(countryCode: String): Int = when (countryCode) {
    "JP" -> R.layout.view_house_number_japan
    "CZ" -> R.layout.view_house_number_czechia
    "SK" -> R.layout.view_house_number_slovakia
    else -> R.layout.view_house_number
}
