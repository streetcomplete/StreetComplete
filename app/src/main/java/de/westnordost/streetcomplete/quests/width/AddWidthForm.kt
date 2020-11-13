package de.westnordost.streetcomplete.quests.width

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.measurement.ARCoreMeasurementActivity
import de.westnordost.streetcomplete.measurement.ARCoreMeasurementActivity.Companion.REQUEST_CODE_MEASURE_DISTANCE
import de.westnordost.streetcomplete.measurement.ARCoreMeasurementActivity.Companion.RESULT_ATTRIBUTE_DISTANCE
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment.Listener.SidewalkSide
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerWithSidewalkFragment
import kotlinx.android.synthetic.main.quest_width.*
import kotlin.math.roundToInt

class AddWidthForm : AbstractQuestFormAnswerWithSidewalkFragment<WidthAnswer>() {
    override val contentLayoutResId = R.layout.quest_width

    private var answer: WidthAnswer? = null

    override fun shouldHandleSidewalks(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initInputFields()
        checkIsFormComplete()
    }

    private fun initInputFields() {
        measureButton.setOnClickListener {
            val intent = Intent(activity?.application, ARCoreMeasurementActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_MEASURE_DISTANCE)
        }

        manualInputField.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // NOP
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // TODO sst: format value here?
            }

            override fun afterTextChanged(s: Editable?) {
                checkIsFormComplete()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_MEASURE_DISTANCE) {
            if (resultCode == RESULT_OK) {
                val distance = data!!.getFloatExtra((RESULT_ATTRIBUTE_DISTANCE), -1f)
                if (distance >= 0) {
                    manualInputField.setText((distance * 100).roundToInt().toString())
                }
            }
        }
    }

    override fun isFormComplete(): Boolean {
        return manualInputField.text.isNotEmpty()
    }

    override fun resetInputs() {
        manualInputField.text = null
    }

    override fun onClickOk() {
        val width = manualInputField.text.toString()
        if (elementHasSidewalk) {
                if (answer is SidewalkWidthAnswer) {
                    if (currentSidewalkSide == SidewalkSide.LEFT) {
                        (answer as SidewalkWidthAnswer).leftSidewalkValue = width
                    } else {
                        (answer as SidewalkWidthAnswer).rightSidewalkValue = width
                    }
                    applyAnswer(answer!!)
                } else {
                    answer =
                        if (currentSidewalkSide == SidewalkSide.LEFT)
                            SidewalkWidthAnswer(width, null)
                        else
                            SidewalkWidthAnswer(null, width)
                    if (sidewalkOnBothSides) {
                        switchToOppositeSidewalkSide()
                    } else {
                        applyAnswer(answer!!)
                    }
                }
        } else {
            answer = SimpleWidthAnswer(width)
            applyAnswer(answer!!)
        }
    }
}
