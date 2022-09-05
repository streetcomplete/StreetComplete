package de.westnordost.streetcomplete.quests.address

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.DialogQuestAddressNoHousenumberBinding
import de.westnordost.streetcomplete.osm.housenumber.AddressNumber
import de.westnordost.streetcomplete.osm.housenumber.ConscriptionNumber
import de.westnordost.streetcomplete.osm.housenumber.HouseAndBlockNumber
import de.westnordost.streetcomplete.osm.housenumber.HouseNumber
import de.westnordost.streetcomplete.osm.housenumber.addToHouseNumber
import de.westnordost.streetcomplete.osm.housenumber.looksInvalid
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.building_type.BuildingType
import de.westnordost.streetcomplete.quests.building_type.asItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.showKeyboard
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

class AddHousenumberForm : AbstractOsmQuestForm<HouseNumberAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_address_answer_no_housenumber) { onNoHouseNumber() },
        AnswerItem(R.string.quest_address_answer_house_name_and_housenumber) { switchToHouseNameAndHouseNumber() },
        AnswerItem(R.string.quest_address_answer_house_name) { switchToHouseName() },
        AnswerItem(R.string.quest_housenumber_multiple_numbers) { showMultipleNumbersHint() }
    )

    private var houseNumberInput: EditText? = null
    private var houseNameInput: EditText? = null
    private var conscriptionNumberInput: EditText? = null
    private var streetNumberInput: EditText? = null
    private var blockNumberInput: EditText? = null

    private var toggleKeyboardButton: Button? = null

    private var addButton: View? = null
    private var subtractButton: View? = null

    enum class InterfaceMode {
        HOUSENUMBER, HOUSENAME, HOUSENUMBER_AND_HOUSENAME
    }
    private var interfaceMode: InterfaceMode = InterfaceMode.HOUSENUMBER

    private var houseNumberInputTextColors: ColorStateList? = null

    // because the hint is implemented as a hack: it is actually the text proper but colored in light-gray
    private val isShowingHouseNumberHint: Boolean get() = houseNumberInputTextColors != null

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
        toggleKeyboardButton = view.findViewById(R.id.toggleKeyboardButton)
        houseNumberInput = view.findViewById(R.id.houseNumberInput)
        houseNameInput = view.findViewById(R.id.houseNameInput)
        conscriptionNumberInput = view.findViewById(R.id.conscriptionNumberInput)
        streetNumberInput = view.findViewById(R.id.streetNumberInput)
        blockNumberInput = view.findViewById(R.id.blockNumberInput)
        addButton = view.findViewById(R.id.addButton)
        subtractButton = view.findViewById(R.id.subtractButton)

        addButton?.setOnClickListener { addToHouseNumberInput(+1) }
        subtractButton?.setOnClickListener { addToHouseNumberInput(-1) }

        // must be called before registering the text changed watchers because it changes the text
        prefillBlockNumber()

        initKeyboardButton()
        // must be after initKeyboardButton because it re-sets the onFocusListener
        showHouseNumberHint()

        listOfNotNull(
            houseNumberInput, houseNameInput, conscriptionNumberInput,
            streetNumberInput, blockNumberInput
        ).forEach { it.doAfterTextChanged { checkIsFormComplete() } }

        checkIsFormComplete()
    }

    private fun prefillBlockNumber() {
        /* the block number likely does not change from one input to the other, so let's prefill it
           with the last selected value */
        lastBlockNumber?.let { blockNumberInput?.setText(it) }
    }

    private fun showHouseNumberHint() {
        val input = houseNumberInput ?: return
        val prev = lastHouseNumber ?: return

        /* The Auto fit layout does not work with hints, so we workaround this by setting the "real"
        *  text instead and make it look like it is a hint. This little hack is much less effort
        *  than to fork and fix the external dependency. We need to revert back the color both on
        *  focus and on text changed (tapping on +/- button) */
        houseNumberInputTextColors = input.textColors
        input.setTextColor(input.hintTextColors)
        input.setText(prev)
        input.doAfterTextChanged {
            val colors = houseNumberInputTextColors
            if (colors != null) input.setTextColor(colors)
            houseNumberInputTextColors = null
        }
        input.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            updateKeyboardButtonVisibility()
            if (hasFocus) input.showKeyboard()
            val colors = houseNumberInputTextColors
            if (hasFocus && colors != null) {
                input.text = null
                input.setTextColor(colors)
                houseNumberInputTextColors = null
            }
        }
    }

    private fun addToHouseNumberInput(add: Int) {
        val input = houseNumberInput ?: return
        val prev = input.text.toString().ifBlank { lastHouseNumber } ?: return
        val newHouseNumber = addToHouseNumber(prev, add) ?: return
        input.setText(newHouseNumber)
        input.setSelection(newHouseNumber.length)
    }

    private fun initKeyboardButton() {
        toggleKeyboardButton?.text = "abc"
        toggleKeyboardButton?.setOnClickListener {
            val focus = requireActivity().currentFocus
            if (focus != null && focus is EditText) {
                val start = focus.selectionStart
                val end = focus.selectionEnd
                if (focus.inputType and InputType.TYPE_CLASS_NUMBER != 0) {
                    focus.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    toggleKeyboardButton?.text = "123"
                } else {
                    focus.inputType = InputType.TYPE_CLASS_NUMBER
                    focus.keyListener = DigitsKeyListener.getInstance("0123456789.,- /")
                    toggleKeyboardButton?.text = "abc"
                }
                // for some reason, the cursor position gets lost first time the input type is set (#1093)
                focus.setSelection(start, end)
                focus.showKeyboard()
            }
        }
        updateKeyboardButtonVisibility()

        val onFocusChange = View.OnFocusChangeListener { v, hasFocus ->
            updateKeyboardButtonVisibility()
            if (hasFocus) v.showKeyboard()
        }
        houseNumberInput?.onFocusChangeListener = onFocusChange
        streetNumberInput?.onFocusChangeListener = onFocusChange
        blockNumberInput?.onFocusChangeListener = onFocusChange
    }

    private fun updateKeyboardButtonVisibility() {
        toggleKeyboardButton?.isInvisible = !(
            houseNumberInput?.hasFocus() == true
            || streetNumberInput?.hasFocus() == true
            || blockNumberInput?.hasFocus() == true
        )
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
        || houseNumberInput?.nonBlankTextOrNull?.takeIf { !isShowingHouseNumberHint } != null
        || conscriptionNumberInput?.nonBlankTextOrNull != null
        || streetNumberInput?.nonBlankTextOrNull != null
        || blockNumberInput?.nonBlankTextOrNull != null

    private fun createAnswer(): HouseNumberAndHouseName {
        val houseName = houseNameInput?.nonBlankTextOrNull
        val houseNumber = houseNumberInput?.nonBlankTextOrNull?.takeIf { !isShowingHouseNumberHint }
        val conscriptionNumber = conscriptionNumberInput?.nonBlankTextOrNull
        val streetNumber = streetNumberInput?.nonBlankTextOrNull
        val blockNumber = blockNumberInput?.nonBlankTextOrNull

        val addressNumber = when {
            conscriptionNumber != null                 -> ConscriptionNumber(conscriptionNumber, streetNumber) // streetNumber is optional
            blockNumber != null && houseNumber != null -> HouseAndBlockNumber(houseNumber, blockNumber)
            houseNumber != null                        -> HouseNumber(houseNumber)
            else                                       -> null
        }
        return HouseNumberAndHouseName(addressNumber, houseName)
    }

    private val InterfaceMode.layout get() = when (this) {
        InterfaceMode.HOUSENUMBER -> when (countryInfo.countryCode) {
            "JP" -> R.layout.quest_housenumber_japan
            "CZ" -> R.layout.quest_housenumber_czechia
            "SK" -> R.layout.quest_housenumber_slovakia
            else -> R.layout.quest_housenumber
        }
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
