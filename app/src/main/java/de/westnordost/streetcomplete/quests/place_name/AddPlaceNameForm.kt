package de.westnordost.streetcomplete.quests.place_name

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.quest_placename.*


class AddPlaceNameForm : AbstractQuestFormAnswerFragment() {

    override val contentLayoutResId = R.layout.quest_placename

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_name_answer_noName) { confirmNoName() }
    )

    private val placeName get() = nameInput.text.toString().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun onClickOk() {
        val data = Bundle()
        data.putString(NAME, placeName)
        applyAnswer(data)
    }

    private fun confirmNoName() {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.quest_name_answer_noName_confirmation_title)
            .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ ->
                val data = Bundle()
                data.putBoolean(NO_NAME, true)
                applyAnswer(data)
            }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }

    override fun isFormComplete() = !placeName.isEmpty()

    companion object {
        const val NO_NAME = "no_name"
        const val NAME = "name"
    }
}
