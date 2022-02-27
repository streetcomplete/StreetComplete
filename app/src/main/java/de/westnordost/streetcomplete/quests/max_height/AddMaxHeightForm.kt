package de.westnordost.streetcomplete.quests.max_height

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.ktx.allowOnlyNumbers
import de.westnordost.streetcomplete.ktx.intOrNull
import de.westnordost.streetcomplete.ktx.numberOrNull
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.TextChangedWatcher

class AddMaxHeightForm : AbstractQuestFormAnswerFragment<MaxHeightAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_maxheight_answer_noSign) { confirmNoSign() }
    )

    private var meterInput: EditText? = null
    private var feetInput: EditText? = null
    private var inchInput: EditText? = null
    private var heightUnitSelect: Spinner? = null
    private var meterInputSign: View? = null
    private var feetInputSign: View? = null

    private val lengthUnits get() = countryInfo.lengthUnits

    override fun isFormComplete() = getHeightFromInput() != null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMaxHeightSignLayout(R.layout.quest_maxheight, lengthUnits.first())
    }

    private fun setMaxHeightSignLayout(resourceId: Int, unit: LengthUnit) {
        val contentView = setContentView(resourceId)

        val splitWayHint = contentView.findViewById<TextView>(R.id.splitWayHint)
        splitWayHint?.text = getString(R.string.quest_maxheight_split_way_hint, getString(R.string.quest_generic_answer_differs_along_the_way))
        splitWayHint?.isGone = osmElement!!.type == ElementType.NODE

        meterInput = contentView.findViewById(R.id.meterInput)
        feetInput = contentView.findViewById(R.id.feetInput)
        inchInput = contentView.findViewById(R.id.inchInput)

        val onTextChangedListener = TextChangedWatcher { checkIsFormComplete() }
        meterInput?.addTextChangedListener(onTextChangedListener)
        feetInput?.addTextChangedListener(onTextChangedListener)
        inchInput?.addTextChangedListener(onTextChangedListener)

        meterInputSign = contentView.findViewById(R.id.meterInputSign)
        feetInputSign = contentView.findViewById(R.id.feetInputSign)

        heightUnitSelect = contentView.findViewById(R.id.heightUnitSelect)
        heightUnitSelect?.isGone = lengthUnits.size == 1
        heightUnitSelect?.adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_centered, lengthUnits)
        heightUnitSelect?.setSelection(0)
        heightUnitSelect?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                switchLayout(heightUnitSelect?.selectedItem as LengthUnit)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        inchInput?.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            val destStr = dest.toString()
            val input = destStr.substring(0, dstart) + source.toString() + destStr.substring(dend, destStr.length)

            if (input.isEmpty() || input.toIntOrNull() != null && input.toInt() <= 12) null else ""
        })
        meterInput?.allowOnlyNumbers()
        switchLayout(unit)
    }

    private fun switchLayout(unit: LengthUnit) {
        val isMetric = unit == LengthUnit.METER
        val isImperial = unit == LengthUnit.FOOT_AND_INCH

        meterInputSign?.isGone = !isMetric
        feetInputSign?.isGone = !isImperial

        if (isMetric) meterInput?.requestFocus()
        if (isImperial) feetInput?.requestFocus()
    }

    override fun onClickOk() {
        if (userSelectedUnrealisticHeight()) {
            confirmUnusualInput { applyMaxHeightFormAnswer() }
        } else {
            applyMaxHeightFormAnswer()
        }
    }

    private fun userSelectedUnrealisticHeight(): Boolean {
        val height = getHeightFromInput() ?: return false
        val m = height.toMeters()
        return m > 6 || m < 1.9
    }

    private fun applyMaxHeightFormAnswer() {
        applyAnswer(MaxHeight(getHeightFromInput()!!))
    }

    private fun getHeightFromInput(): Length? {
        when (heightUnitSelect?.selectedItem as LengthUnit? ?: lengthUnits.first()) {
            LengthUnit.METER -> {
                return meterInput?.numberOrNull?.let { LengthInMeters(it) }
            }
            LengthUnit.FOOT_AND_INCH -> {
                val feet = feetInput?.intOrNull
                val inches = inchInput?.intOrNull

                if (feet != null && inches != null) {
                    return LengthInFeetAndInches(feet, inches)
                }
            }
        }
        return null
    }

    private fun confirmNoSign() {
        activity?.let { AlertDialog.Builder(it)
            .setMessage(R.string.quest_maxheight_answer_noSign_question)
            .setPositiveButton(R.string.quest_maxheight_answer_noSign_question_yes) { _, _ -> applyAnswer(NoMaxHeightSign(true)) }
            .setNegativeButton(R.string.quest_maxheight_answer_noSign_question_no) { _, _ -> applyAnswer(NoMaxHeightSign(false)) }
            .show()
        }
    }

    private fun confirmUnusualInput(callback: () -> (Unit)) {
        activity?.let { AlertDialog.Builder(it)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setMessage(R.string.quest_maxheight_unusualInput_confirmation_description)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> callback() }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
        }
    }
}
