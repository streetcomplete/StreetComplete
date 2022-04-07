package de.westnordost.streetcomplete.quests.entrance_reference

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
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.building_type.BuildingType
import de.westnordost.streetcomplete.quests.building_type.asItem
import de.westnordost.streetcomplete.util.ktx.nonBlankTextOrNull
import de.westnordost.streetcomplete.util.ktx.showKeyboard
import de.westnordost.streetcomplete.view.image_select.ItemViewHolder

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
