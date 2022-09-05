package de.westnordost.streetcomplete.quests.address

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogQuestAddressNoHousenumberBinding
import de.westnordost.streetcomplete.osm.housenumber.AddressNumber
import de.westnordost.streetcomplete.osm.housenumber.HouseAndBlockNumber
import de.westnordost.streetcomplete.osm.housenumber.HouseNumber
import de.westnordost.streetcomplete.osm.housenumber.looksInvalid
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.building_type.BuildingType
import de.westnordost.streetcomplete.quests.building_type.asItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.view.controller.ConscriptionNumberInputViewController
import de.westnordost.streetcomplete.view.controller.HouseNumberInputViewController
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class AddHousenumberForm : AbstractOsmQuestForm<HouseNumberAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_address_answer_no_housenumber) { onNoHouseNumber() },
        AnswerItem(R.string.quest_address_answer_house_name_and_housenumber) { switchToHouseNameAndHouseNumber() },
        AnswerItem(R.string.quest_address_answer_house_name) { switchToHouseName() },
        AnswerItem(R.string.quest_housenumber_multiple_numbers) { showMultipleNumbersHint() }
    )

    private var houseNameInput: EditText? = null

    private var houseNumberInputViewController: HouseNumberInputViewController? = null
    private var conscriptionNumberInputViewController: ConscriptionNumberInputViewController? = null

    enum class InterfaceMode {
        HOUSENUMBER, HOUSENAME, HOUSENUMBER_AND_HOUSENAME
    }
    private var interfaceMode: InterfaceMode = InterfaceMode.HOUSENUMBER

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prevMode = savedInstanceState?.getString(INTERFACE_MODE)?.let { InterfaceMode.valueOf(it) }
        setInterfaceMode(prevMode ?: InterfaceMode.HOUSENUMBER)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTERFACE_MODE, interfaceMode.name)
    }

    /* ------------------------------------- Other answers -------------------------------------- */

    private fun switchToHouseName() {
        setInterfaceMode(InterfaceMode.HOUSENAME)
        houseNameInput?.requestFocus()
    }

    private fun switchToHouseNameAndHouseNumber() {
        setInterfaceMode(InterfaceMode.HOUSENUMBER_AND_HOUSENAME)
        houseNameInput?.requestFocus()
    }

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
            showNoHousenumberDialog(buildingType)
        } else {
            // fallback in case the type of building is known by Housenumber quest but not by
            // building type quest
            onClickCantSay()
        }
    }

    private fun showNoHousenumberDialog(buildingType: DisplayItem<BuildingType>) {
        val dialogBinding = DialogQuestAddressNoHousenumberBinding.inflate(layoutInflater)
        ItemViewHolder(dialogBinding.root).bind(buildingType)

        AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(HouseNumberAndHouseName(null, null)) }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ -> applyAnswer(WrongBuildingType) }
            .show()
    }

    /* -------------------------- Set (different) housenumber layout  --------------------------- */

    private fun setInterfaceMode(interfaceMode: InterfaceMode) {
        this.interfaceMode = interfaceMode
        val view = setContentView(interfaceMode.layout)
        onContentViewCreated(view)
    }

    private fun onContentViewCreated(view: View) {
        houseNameInput = view.findViewById(R.id.houseNameInput)
        val houseNumberInput: EditText? = view.findViewById(R.id.houseNumberInput)
        val toggleKeyboardButton: Button? = view.findViewById(R.id.toggleKeyboardButton)
        val conscriptionNumberInput: EditText? = view.findViewById(R.id.conscriptionNumberInput)
        val streetNumberInput: EditText? = view.findViewById(R.id.streetNumberInput)

        houseNumberInputViewController = if (houseNumberInput != null && toggleKeyboardButton != null) {
            HouseNumberInputViewController(
                requireActivity(),
                houseNumberInput,
                view.findViewById(R.id.blockNumberInput),
                toggleKeyboardButton,
                view.findViewById(R.id.addButton),
                view.findViewById(R.id.subtractButton),
                lastBlockNumber,
                lastHouseNumber
            )
        } else null

        conscriptionNumberInputViewController = if (conscriptionNumberInput != null && toggleKeyboardButton != null && streetNumberInput != null) {
            ConscriptionNumberInputViewController(
                requireActivity(),
                conscriptionNumberInput,
                streetNumberInput,
                toggleKeyboardButton
            )
        } else null

        houseNumberInputViewController?.onInputChanged = { checkIsFormComplete() }
        conscriptionNumberInputViewController?.onInputChanged = { checkIsFormComplete() }
        houseNameInput?.doAfterTextChanged { checkIsFormComplete() }

        checkIsFormComplete()
    }

    /* ----------------------------------- Commit answer ---------------------------------------- */

    override fun onClickOk() {
        val answer = createAnswer()
        val isUnusual = answer.number?.looksInvalid(countryInfo.additionalValidHousenumberRegex) == true
        confirmHousenumber(isUnusual) {
            applyAnswer(answer)
            (answer.number as? HouseAndBlockNumber)?.blockNumber?.let { lastBlockNumber = it }
            answer.number?.houseNumber?.let { lastHouseNumber = it }
        }
    }

    override fun isFormComplete(): Boolean {
        val answer = createAnswer()
        return when (interfaceMode) {
            InterfaceMode.HOUSENUMBER -> answer.number != null
            InterfaceMode.HOUSENAME -> answer.name != null
            InterfaceMode.HOUSENUMBER_AND_HOUSENAME -> answer.name != null && answer.number != null
        }
    }

    private fun confirmHousenumber(isUnusual: Boolean, onConfirmed: () -> Unit) {
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

    override fun isRejectingClose(): Boolean =
        houseNameInput?.nonBlankTextOrNull != null
        || houseNumberInputViewController?.isEmpty == false
        || conscriptionNumberInputViewController?.isEmpty == false

    private fun createAnswer(): HouseNumberAndHouseName {
        val houseName = houseNameInput?.nonBlankTextOrNull
        val addressNumber =
            houseNumberInputViewController?.addressNumber
            ?: conscriptionNumberInputViewController?.conscriptionNumber
        return HouseNumberAndHouseName(addressNumber, houseName)
    }

    private val InterfaceMode.layout get() = when (this) {
        InterfaceMode.HOUSENUMBER -> R.layout.quest_housenumber
        InterfaceMode.HOUSENAME -> R.layout.quest_housename
        InterfaceMode.HOUSENUMBER_AND_HOUSENAME -> R.layout.quest_housename_and_housenumber
    }

    companion object {
        private var lastBlockNumber: String? = null
        private var lastHouseNumber: String? = null

        private const val INTERFACE_MODE = "interface_mode"
    }
}

private val AddressNumber.houseNumber: String? get() = when (this) {
    is HouseNumber -> houseNumber
    is HouseAndBlockNumber -> houseNumber
    // not conscription number because there is no logical succession
    else -> null
}
