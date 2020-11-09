package de.westnordost.streetcomplete.quests.housenumber

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.core.content.getSystemService
import androidx.core.view.isInvisible

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.building_type.BuildingType
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder


class AddHousenumberForm : AbstractQuestFormAnswerFragment<HousenumberAnswer>() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_address_answer_no_housenumber) { onNoHouseNumber() },
        OtherAnswer(R.string.quest_address_answer_house_name) { switchToHouseName() },
        OtherAnswer(R.string.quest_housenumber_multiple_numbers) { showMultipleNumbersHint() }
    )

    private var houseNumberInput: EditText? = null
    private var houseNameInput: EditText? = null
    private var conscriptionNumberInput: EditText? = null
    private var streetNumberInput: EditText? = null
    private var blockNumberInput: EditText? = null

    private var toggleKeyboardButton: Button? = null

    private var addButton: View? = null
    private var subtractButton: View? = null

    private var isHousename: Boolean = false

    private var houseNumberInputTextColors: ColorStateList? = null

    private val isShowingHouseNumberHint: Boolean get() = houseNumberInputTextColors != null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        isHousename = savedInstanceState?.getBoolean(IS_HOUSENAME) ?: false
        setLayout(if(isHousename) R.layout.quest_housename else R.layout.quest_housenumber)

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_HOUSENAME, isHousename)
    }

    override fun onClickOk() {
        createAnswer()?.let { answer ->
            confirmHousenumber(answer.looksInvalid(countryInfo.additionalValidHousenumberRegex)) {
                applyAnswer(answer)
                if (answer.isRealHouseNumberAnswer) lastRealHousenumberAnswer = answer
            }
        }
    }

    override fun isFormComplete() = (!isShowingHouseNumberHint || isHousename) && createAnswer() != null

    /* ------------------------------------- Other answers -------------------------------------- */

    private fun switchToHouseName() {
        isHousename = true
        setLayout(R.layout.quest_housename)
    }

    private fun showMultipleNumbersHint() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_housenumber_multiple_numbers_description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
        }
    }

    private fun onNoHouseNumber() {
        val buildingValue = osmElement!!.tags["building"]!!
        val buildingType = BuildingType.getByTag("building", buildingValue)
        if (buildingType != null) {
            val inflater = LayoutInflater.from(requireContext())
            val inner = inflater.inflate(R.layout.dialog_quest_address_no_housenumber, null, false)
            ItemViewHolder(inner.findViewById(R.id.item_view)).bind(buildingType.item)

            AlertDialog.Builder(requireContext())
                .setView(inner)
                .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyAnswer(NoHouseNumber) }
                .setNegativeButton(R.string.quest_generic_hasFeature_no_leave_note) { _, _ -> composeNote() }
                .show()
        } else {
            // fallback in case the type of building is known by Housenumber quest but not by
            // building type quest
            onClickCantSay()
        }
    }

    /* -------------------------- Set (different) housenumber layout  --------------------------- */

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)

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

        val onChanged = TextChangedWatcher { checkIsFormComplete() }
        houseNumberInput?.addTextChangedListener(onChanged)
        houseNameInput?.addTextChangedListener(onChanged)
        conscriptionNumberInput?.addTextChangedListener(onChanged)
        streetNumberInput?.addTextChangedListener(onChanged)
        blockNumberInput?.addTextChangedListener(onChanged)
    }

    private fun prefillBlockNumber() {
        /* the block number likely does not change from one input to the other, so let's prefill it
           with the last selected value */
        val input = blockNumberInput ?: return
        val blockNumberAnswer = lastRealHousenumberAnswer as? HouseAndBlockNumber ?: return
        input.setText(blockNumberAnswer.blockNumber)
    }

    private fun showHouseNumberHint() {
        val input = houseNumberInput ?: return
        val prev = lastRealHousenumberAnswer?.realHouseNumber ?: return

        /* The Auto fit layout does not work with hints, so we workaround this by setting the "real"
        *  text instead and make it look like it is a hint. This little hack is much less effort
        *  than to fork and fix the external dependency. We need to revert back the color both on
        *  focus and on text changed (tapping on +/- button) */
        houseNumberInputTextColors = input.textColors
        input.setTextColor(input.hintTextColors)
        input.setText(prev)
        input.addTextChangedListener(TextChangedWatcher {
            val colors = houseNumberInputTextColors
            if (colors != null) input.setTextColor(colors)
            houseNumberInputTextColors = null
        })
        input.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            updateKeyboardButtonVisibility()
            if (hasFocus) showKeyboard(input)
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
        val prev = if (input.text.isEmpty()) {
            lastRealHousenumberAnswer?.realHouseNumber
        } else {
            input.text.toString()
        } ?: return
        val newHouseNumber = prev.addToHouseNumber(add) ?: return
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
                showKeyboard(focus)
            }
        }
        updateKeyboardButtonVisibility()

        val onFocusChange = View.OnFocusChangeListener { v, hasFocus ->
            updateKeyboardButtonVisibility()
            if (hasFocus) showKeyboard(v)
        }
        houseNumberInput?.onFocusChangeListener = onFocusChange
        streetNumberInput?.onFocusChangeListener = onFocusChange
        blockNumberInput?.onFocusChangeListener = onFocusChange
    }

    private fun showKeyboard(focus: View) {
        val imm = activity?.getSystemService<InputMethodManager>()
        imm?.showSoftInput(focus, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateKeyboardButtonVisibility() {
        toggleKeyboardButton?.isInvisible = !(
            houseNumberInput?.hasFocus() == true ||
            streetNumberInput?.hasFocus() == true ||
            blockNumberInput?.hasFocus() == true
        )
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

    private fun createAnswer(): HousenumberAnswer? =
        if (houseNameInput != null) {
            houseNameInput?.nonEmptyInput?.let { HouseName(it) }
        }
        else if (conscriptionNumberInput != null && streetNumberInput != null) {
            conscriptionNumberInput?.nonEmptyInput?.let { conscriptionNumber ->
                val streetNumber = streetNumberInput?.nonEmptyInput // streetNumber is optional
                ConscriptionNumber(conscriptionNumber, streetNumber)
            }
        }
        else if (blockNumberInput != null && houseNumberInput != null) {
            blockNumberInput?.nonEmptyInput?.let { blockNumber ->
                houseNumberInput?.nonEmptyInput?.let { houseNumber ->
                    HouseAndBlockNumber(houseNumber, blockNumber)
                }
            }
        }
        else if (houseNumberInput != null) {
            houseNumberInput?.nonEmptyInput?.let { HouseNumber(it) }
        }
        else null

    private val EditText.nonEmptyInput:String? get() {
        val input = text.toString().trim()
        return if(input.isNotEmpty()) input else null
    }

    companion object {
        private var lastRealHousenumberAnswer: HousenumberAnswer? = null

        private const val IS_HOUSENAME = "is_housename"
    }
}

private val HousenumberAnswer.isRealHouseNumberAnswer: Boolean get() = when(this) {
    is HouseNumber -> true
    is HouseAndBlockNumber -> true
    else -> false
}

private val HousenumberAnswer.realHouseNumber: String? get() = when(this) {
    is HouseNumber -> number
    is HouseAndBlockNumber -> houseNumber
    else -> null
}

private fun String.addToHouseNumber(add: Int): String? {
    val parsed = parseHouseNumber(this) ?: return null
    when {
        add == 0 -> return this
        add > 0  -> {
            val last = when (val it = parsed.list.last()) {
                is HouseNumbersPartsRange -> it.end
                is SingleHouseNumbersPart -> it.single
            }
            return (last.number + add).toString()
        }
        add < 0  -> {
            val first = when (val it = parsed.list.first()) {
                is HouseNumbersPartsRange -> it.start
                is SingleHouseNumbersPart -> it.single
            }
            val result = first.number + add
            return if (result < 1) null else result.toString()
        }
        else -> return null
    }
}
