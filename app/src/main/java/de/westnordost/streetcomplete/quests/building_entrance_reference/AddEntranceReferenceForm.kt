package de.westnordost.streetcomplete.quests.building_entrance_reference

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull

class AddEntranceReferenceForm : AbstractQuestFormAnswerFragment<EntranceAnswer>() {

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_entrance_reference_nothing_signed) { onNothingSigned() },
    )

    private var referenceCodeInput: EditText? = null
    private var flatRangeStartInput: EditText? = null
    private var flatRangeEndInput: EditText? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContentView(R.layout.quest_entrance_reference)
        referenceCodeInput = view.findViewById(R.id.referenceCodeInput)
        flatRangeStartInput = view.findViewById(R.id.flatRangeStartInput)
        flatRangeEndInput = view.findViewById(R.id.flatRangeEndInput)
        listOfNotNull(
            referenceCodeInput, flatRangeStartInput, flatRangeEndInput,
        ).forEach { it.doAfterTextChanged { checkIsFormComplete() } }
    }

    /* ------------------------------------- Other answers -------------------------------------- */

    private fun onNothingSigned() {
        applyAnswer(Unsigned)
    }


    /* ----------------------------------- Commit answer ---------------------------------------- */

    override fun onClickOk() {
        val answer = createAnswer()
        applyAnswer(answer)
    }

    override fun isFormComplete(): Boolean {
        val referenceCode = referenceCodeInput?.nonBlankTextOrNull
        val flatRangeStart = flatRangeStartInput?.nonBlankTextOrNull
        val flatRangeEnd = flatRangeEndInput?.nonBlankTextOrNull
        return (referenceCode != null && flatRangeStart == null && flatRangeEnd == null)
            || (flatRangeStart != null && flatRangeEnd != null)
    }

    override fun isRejectingClose(): Boolean =
        referenceCodeInput?.nonBlankTextOrNull != null
            || flatRangeStartInput?.nonBlankTextOrNull != null
            || flatRangeEndInput?.nonBlankTextOrNull != null

    private fun createAnswer(): EntranceAnswer {
        val referenceCode = referenceCodeInput?.nonBlankTextOrNull
        val flatRangeStart = flatRangeStartInput?.nonBlankTextOrNull
        val flatRangeEnd = flatRangeEndInput?.nonBlankTextOrNull
        val flatRange = if(flatRangeStart != null && flatRangeEnd != null) {
            if(flatRangeStart == flatRangeEnd) {
                flatRangeStart
            } else {
                "$flatRangeStart-$flatRangeEnd"
            }
        } else {
            null
        }

        return when {
            referenceCode != null && flatRange != null -> ReferenceCodeAndFlatRange(referenceCode, flatRange)
            referenceCode != null                      -> ReferenceCode(referenceCode)
            flatRange != null                          -> FlatRange(flatRange)
            else                                       -> throw UnsupportedOperationException()
        }
    }
}
