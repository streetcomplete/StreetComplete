package de.westnordost.streetcomplete.quests.housenumber

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.quests.building_type.BuildingType
import de.westnordost.streetcomplete.util.TextChangedWatcher
import de.westnordost.streetcomplete.view.ItemViewHolder


class AddHousenumberForm : AbstractQuestFormAnswerFragment() {

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_address_answer_no_housenumber) { onNoHouseNumber() },
        OtherAnswer(R.string.quest_address_answer_house_name) { switchToHouseName() },
        OtherAnswer(R.string.quest_housenumber_multiple_numbers) { showMultipleNumbersHint() }
    )

    private var houseNumberInput: EditText? = null
    private var houseNameInput: EditText? = null
    private var conscriptionNumberInput: EditText? = null
    private var streetNumberInput: EditText? = null

    private var isHousename: Boolean = false

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
        if (houseNameInput != null) {
            applyHouseNameAnswer(houseNameInput!!.trimmedInput)
        } else if (conscriptionNumberInput != null && streetNumberInput != null) {
            applyConscriptionNumberAnswer(
                conscriptionNumberInput!!.trimmedInput,
                streetNumberInput!!.trimmedInput
            )
        } else if (houseNumberInput != null) {
            applyHouseNumberAnswer(houseNumberInput!!.trimmedInput)
        }
    }

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
        val item = BuildingType.getByTag("building", buildingValue)
        if (item != null) {
            val inflater = LayoutInflater.from(activity)
            val inner = inflater.inflate(R.layout.dialog_quest_address_no_housenumber, null, false)
            ItemViewHolder(inner.findViewById(R.id.item_view)).bind(item)

            AlertDialog.Builder(activity!!)
                .setView(inner)
                .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ -> applyNoHouseNumberAnswer() }
                .setNegativeButton(R.string.quest_generic_hasFeature_no_leave_note) { _, _ -> onClickCantSay() }
                .show()
        } else {
            // fallback in case the type of building is known by Housenumber quest but not by
            // building type quest
            onClickCantSay()
        }
    }

    private fun applyNoHouseNumberAnswer() {
        val answer = Bundle()
        answer.putBoolean(NO_ADDRESS, true)
        applyAnswer(answer)
    }

    private fun applyHouseNameAnswer(houseName: String) {
        val answer = Bundle()
        answer.putString(HOUSENAME, houseName)
        applyAnswer(answer)
    }

    private fun applyHouseNumberAnswer(houseNumber: String) {
        val answer = Bundle()
        val looksInvalid = !houseNumber.matches(getValidHousenumberRegex())

        confirmHousenumber(looksInvalid) {
            answer.putString(HOUSENUMBER, houseNumber)
            applyAnswer(answer)
        }
    }

    private fun applyConscriptionNumberAnswer(conscriptionNumber: String, streetNumber: String) {
        val answer = Bundle()
        var looksInvalid = !conscriptionNumber.matches(VALID_CONSCRIPTIONNUMBER_REGEX.toRegex())
        if (streetNumber.isNotEmpty()) {
            looksInvalid = looksInvalid || !streetNumber.matches(getValidHousenumberRegex())
        }

        confirmHousenumber(looksInvalid) {
            answer.putString(CONSCRIPTIONNUMBER, conscriptionNumber)
            // streetNumber is optional
            if (streetNumber.isNotEmpty()) {
                answer.putString(STREETNUMBER, streetNumber)
            }
            applyAnswer(answer)
        }
    }

    // i.e. "95-98" or "5,5a,6" etc. (but not: "1, 3" or "3 - 5" or "5,6-7")
    private fun getValidHousenumberRegex(): Regex {
        var regex = VALID_HOUSENUMBER_REGEX
        val additionalRegex = countryInfo.additionalValidHousenumberRegex
        if (additionalRegex != null) {
            regex = "(($regex)|($additionalRegex))"
        }
        return "^$regex((-$regex)|(,$regex)+)?".toRegex()
    }

    override fun isFormComplete(): Boolean {
        // streetNumber is always optional
        val input = getFirstNonNull(houseNumberInput, houseNameInput, conscriptionNumberInput)
        return input?.trimmedInput?.isNotEmpty() ?: false
    }

    private fun setLayout(layoutResourceId: Int) {
        val view = setContentView(layoutResourceId)

        houseNumberInput = view.findViewById(R.id.houseNumberInput)
        houseNameInput = view.findViewById(R.id.houseNameInput)
        conscriptionNumberInput = view.findViewById(R.id.conscriptionNumberInput)
        streetNumberInput = view.findViewById(R.id.streetNumberInput)

        val onChanged = TextChangedWatcher { checkIsFormComplete() }
        houseNumberInput?.addTextChangedListener(onChanged)
        houseNameInput?.addTextChangedListener(onChanged)
        conscriptionNumberInput?.addTextChangedListener(onChanged)
        streetNumberInput?.addTextChangedListener(onChanged)

        // streetNumber is always optional
        val input = getFirstNonNull(houseNumberInput, houseNameInput, conscriptionNumberInput)
        input?.requestFocus()

        initKeyboardButton(view)
    }

    private fun initKeyboardButton(view: View) {
        val toggleKeyboardButton = view.findViewById<Button>(R.id.toggleKeyboardButton)
        if (toggleKeyboardButton != null) {
            toggleKeyboardButton.text = "abc"
            toggleKeyboardButton.setOnClickListener {
                val focus = activity!!.currentFocus
                if (focus != null && focus is EditText) {
                    val start = focus.selectionStart
                    val end = focus.selectionEnd
                    if (focus.inputType and InputType.TYPE_CLASS_NUMBER != 0) {
                        focus.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                        toggleKeyboardButton.text = "123"
                    } else {
                        focus.inputType = InputType.TYPE_CLASS_NUMBER
                        focus.keyListener = DigitsKeyListener.getInstance("0123456789.,- /")
                        toggleKeyboardButton.text = "abc"
                    }
                    // for some reason, the cursor position gets lost first time the input type is set (#1093)
                    focus.setSelection(start, end)

                    val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(focus, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }

    private fun confirmHousenumber(isUnusual: Boolean, onConfirmed: () -> Unit) {
        if (isUnusual) {
            AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_generic_confirmation_title)
                .setMessage(R.string.quest_address_unusualHousenumber_confirmation_description)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> onConfirmed() }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
        } else {
            onConfirmed()
        }
    }

    private val EditText.trimmedInput get() = text.toString().trim()

    companion object {
        const val NO_ADDRESS = "noaddress"
        const val HOUSENUMBER = "housenumber"
        const val HOUSENAME = "housename"
        const val CONSCRIPTIONNUMBER = "conscriptionnumber"
        const val STREETNUMBER = "streetnumber"

        private val IS_HOUSENAME = "is_housename"
        // i.e. 9999/a, 9/a, 99/9, 99a, 99 a, 9 / a
        const val VALID_HOUSENUMBER_REGEX = "\\p{N}{1,4}((\\s?/\\s?\\p{N})|(\\s?/?\\s?\\p{L}))?"

        const val VALID_CONSCRIPTIONNUMBER_REGEX = "\\p{N}{1,6}"

        private fun getFirstNonNull(vararg view: EditText?): EditText? {
            for (editText in view) {
                if (editText != null) return editText
            }
            return null
        }
    }
}
