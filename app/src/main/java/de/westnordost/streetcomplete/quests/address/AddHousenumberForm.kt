package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogQuestAddressNoHousenumberBinding
import de.westnordost.streetcomplete.databinding.ViewAddressNumberOrNameInputBinding
import de.westnordost.streetcomplete.osm.address.AddressNumberAndNameInputViewController
import de.westnordost.streetcomplete.osm.address.HouseAndBlockNumber
import de.westnordost.streetcomplete.osm.address.looksInvalid
import de.westnordost.streetcomplete.osm.address.streetHouseNumber
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.building_type.BuildingType
import de.westnordost.streetcomplete.quests.building_type.asItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class AddHousenumberForm : AbstractOsmQuestForm<HouseNumberAnswer>() {

    override val contentLayoutResId = R.layout.view_address_number_or_name_input
    private val binding by contentViewBinding(ViewAddressNumberOrNameInputBinding::bind)

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_address_answer_no_housenumber) { onNoHouseNumber() },
        AnswerItem(R.string.quest_address_answer_house_name2) { showHouseName() },
        AnswerItem(R.string.quest_housenumber_multiple_numbers) { showMultipleNumbersHint() }
    )

    private var isShowingHouseName: Boolean = false
    private lateinit var numberOrNameInputCtrl: AddressNumberAndNameInputViewController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numberView = layoutInflater.inflate(
            getAddressNumberLayoutResId(countryInfo.countryCode),
            binding.countrySpecificContainer
        )
        numberOrNameInputCtrl = AddressNumberAndNameInputViewController(
            toggleHouseNameButton = binding.toggleHouseNameButton,
            houseNameInput = binding.houseNameInput,
            toggleAddressNumberButton = binding.toggleAddressNumberButton,
            addressNumberContainer = binding.addressNumberContainer,
            activity = requireActivity(),
            houseNumberInput = numberView.findViewById<EditText?>(R.id.houseNumberInput)?.apply { hint = lastHouseNumber },
            blockNumberInput = numberView.findViewById<EditText?>(R.id.blockNumberInput)?.apply { hint = lastBlockNumber },
            conscriptionNumberInput = numberView.findViewById(R.id.conscriptionNumberInput),
            streetNumberInput = numberView.findViewById(R.id.streetNumberInput),
            toggleKeyboardButton = binding.toggleKeyboardButton,
            addButton = numberView.findViewById(R.id.addButton),
            subtractButton = numberView.findViewById(R.id.subtractButton),
        )
        numberOrNameInputCtrl.onInputChanged = { checkIsFormComplete() }

        // initially do not show any house number / house name UI
        isShowingHouseName = savedInstanceState?.getBoolean(SHOW_HOUSE_NAME) == true
        if (!isShowingHouseName) {
            binding.toggleAddressNumberButton.isGone = true
            binding.toggleHouseNameButton.isGone = true
        }

        checkIsFormComplete()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_HOUSE_NAME, isShowingHouseName)
    }

    /* ------------------------------------- Other answers -------------------------------------- */

    private fun showMultipleNumbersHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_housenumber_multiple_numbers_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    private fun onNoHouseNumber() {
        val buildingValue = element.tags["building"]!!
        val buildingType = BuildingType.getByTag("building", buildingValue)?.asItem()
        if (buildingType != null) {
            showNoHouseNumberDialog(buildingType)
        } else {
            // fallback in case the type of building is known by Housenumber quest but not by
            // building type quest
            onClickCantSay()
        }
    }

    private fun showNoHouseNumberDialog(buildingType: DisplayItem<BuildingType>) {
        val dialogBinding = DialogQuestAddressNoHousenumberBinding.inflate(layoutInflater)
        ItemViewHolder(dialogBinding.root).bind(buildingType)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(AddressNumberOrName(null, null)) }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(WrongBuildingType) }
            .show()
    }

    /* ----------------------------------- Show house name -------------------------------------- */

    private fun showHouseName() {
        isShowingHouseName = true
        binding.toggleAddressNumberButton.isGone = false
        binding.toggleHouseNameButton.isGone = false
        numberOrNameInputCtrl.setHouseNameViewExpanded(true)
        binding.houseNameInput.requestFocus()
    }

    /* ----------------------------------- Commit answer ---------------------------------------- */

    override fun onClickOk() {
        val number = numberOrNameInputCtrl.addressNumber
        val isUnusual = number?.looksInvalid(countryInfo.additionalValidHousenumberRegex) == true
        confirmHouseNumber(isUnusual) {
            applyAnswer(AddressNumberOrName(number, numberOrNameInputCtrl.houseName))
            if (number is HouseAndBlockNumber) { number.blockNumber.let { lastBlockNumber = it } }
            number?.streetHouseNumber?.let { lastHouseNumber = it }
        }
    }

    private fun confirmHouseNumber(isUnusual: Boolean, onConfirmed: () -> Unit) {
        if (isUnusual) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_address_unusualHousenumber_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        } else {
            onConfirmed()
        }
    }
    override fun isFormComplete(): Boolean = numberOrNameInputCtrl.isComplete

    override fun isRejectingClose(): Boolean = !numberOrNameInputCtrl.isEmpty

    companion object {
        private var lastBlockNumber: String? = null
        private var lastHouseNumber: String? = null

        private const val SHOW_HOUSE_NAME = "show_house_name"
    }
}

private fun getAddressNumberLayoutResId(countryCode: String): Int = when (countryCode) {
    "JP" -> R.layout.view_house_number_japan
    "CZ" -> R.layout.view_house_number_czechia
    "SK" -> R.layout.view_house_number_slovakia
    else -> R.layout.view_house_number
}
