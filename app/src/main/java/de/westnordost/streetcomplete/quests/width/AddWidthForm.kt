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
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import kotlinx.android.synthetic.main.quest_width.*
import kotlinx.android.synthetic.main.quest_width.manualInputField
import kotlin.math.roundToInt

class AddWidthForm : AbstractQuestFormAnswerFragment<String>() {
    override val contentLayoutResId = R.layout.quest_width

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        checkIsFormComplete()
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

    override fun onClickOk() {
        applyAnswer("test") // TODO sst: use real value
    }
}
