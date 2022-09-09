package de.westnordost.streetcomplete.overlays.address

import android.os.Bundle
import android.view.View
import android.widget.EditText
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.edits.update_tags.UpdateElementTagsAction
import de.westnordost.streetcomplete.databinding.FragmentOverlayAddressBinding
import de.westnordost.streetcomplete.osm.address.AddressNumber
import de.westnordost.streetcomplete.osm.address.applyTo
import de.westnordost.streetcomplete.osm.address.createAddressNumber
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.osm.address.AddressNumberInputViewController
import de.westnordost.streetcomplete.osm.address.AddressNumberOrNameInputViewController
import de.westnordost.streetcomplete.osm.address.PlaceName
import de.westnordost.streetcomplete.osm.address.StreetName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceName
import de.westnordost.streetcomplete.osm.address.StreetOrPlaceNameViewController

class AddressOverlayForm : AbstractOverlayForm() {

    override val contentLayoutResId = R.layout.fragment_overlay_address
    private val binding by contentViewBinding(FragmentOverlayAddressBinding::bind)

    private lateinit var addressOrNameInputCtrl: AddressNumberOrNameInputViewController
    private lateinit var streetOrPlaceNameViewCtrl: StreetOrPlaceNameViewController

    private var addressNumber: AddressNumber? = null
    private var houseName: String? = null
    private var streetOrPlaceName: StreetOrPlaceName? = null

    private var isPlaceName = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isPlaceName = savedInstanceState?.getBoolean(IS_PLACENAME) ?: false

        addressNumber = createAddressNumber(element.tags)
        houseName = element.tags["addr:housename"]
        streetOrPlaceName =
            if (isPlaceName) element.tags["addr:place"]?.let { PlaceName(it) }
            else             element.tags["addr:street"]?.let { StreetName(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        streetOrPlaceNameViewCtrl = StreetOrPlaceNameViewController(
            binding.streetOrPlaceSelect,
            binding.streetNameInput,
            binding.placeNameInput
        )
        streetOrPlaceNameViewCtrl.onInputChanged = { checkIsFormComplete() }
        streetOrPlaceNameViewCtrl.streetOrPlaceName = streetOrPlaceName

        val numberOrNameBinding = binding.addressNumberOrNameContainer
        val numberView = layoutInflater.inflate(getAddressNumberLayoutResId(countryInfo.countryCode), numberOrNameBinding.countrySpecificContainer)

        val addressNumberCtrl = AddressNumberInputViewController(
            requireActivity(),
            numberView.findViewById<EditText?>(R.id.houseNumberInput)?.apply { hint = lastHouseNumber },
            numberView.findViewById(R.id.blockNumberInput),
            numberView.findViewById(R.id.conscriptionNumberInput),
            numberView.findViewById(R.id.streetNumberInput),
            numberOrNameBinding.toggleKeyboardButton,
            numberView.findViewById(R.id.addButton),
            numberView.findViewById(R.id.subtractButton),
        )

        addressOrNameInputCtrl = AddressNumberOrNameInputViewController(
            numberOrNameBinding.toggleHouseNameButton,
            numberOrNameBinding.houseNameInput,
            numberOrNameBinding.toggleAddressNumberButton,
            numberOrNameBinding.addressNumberContainer,
            addressNumberCtrl
        )
        addressOrNameInputCtrl.onInputChanged = { checkIsFormComplete() }
        addressOrNameInputCtrl.addressNumber = addressNumber
        addressOrNameInputCtrl.houseName = houseName
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_PLACENAME, isPlaceName)
    }

    override fun hasChanges(): Boolean =
        addressOrNameInputCtrl.addressNumber != addressNumber
        || addressOrNameInputCtrl.houseName != houseName
        || streetOrPlaceNameViewCtrl.streetOrPlaceName != streetOrPlaceName

    override fun isFormComplete(): Boolean =
        addressOrNameInputCtrl.isComplete && streetOrPlaceName != null

    override fun onClickOk() {
        applyEdit(UpdateElementTagsAction(StringMapChangesBuilder(element.tags).also { tags ->
            addressOrNameInputCtrl.addressNumber?.applyTo(tags)
            addressOrNameInputCtrl.houseName?.let { tags["addr:housename"] = it }
            streetOrPlaceNameViewCtrl.streetOrPlaceName?.applyTo(tags)
        }.create()))
    }

    companion object {
        private var lastHouseNumber: String? = null

        private const val IS_PLACENAME = "is_placename"
    }
}

private fun getAddressNumberLayoutResId(countryCode: String): Int = when (countryCode) {
    "JP" -> R.layout.view_house_number_japan
    "CZ" -> R.layout.view_house_number_czechia
    "SK" -> R.layout.view_house_number_slovakia
    else -> R.layout.view_house_number
}
