package de.westnordost.streetcomplete.quests.shop_name

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.OtherAnswer
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.quest_placename.*

class AddShopNameForm : AbstractQuestFormAnswerFragment<ShopNameAnswer>() {

    override val contentLayoutResId = R.layout.quest_shopname

    override val otherAnswers = listOf(
        OtherAnswer(R.string.quest_name_answer_noName) { confirmNoName() }
    )

    private val placeName get() = nameInput?.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })
    }

    override fun onClickOk() {
        applyAnswer(ShopName(placeName))
    }

    private fun confirmNoName() {
        AlertDialog.Builder(activity!!)
                .setTitle(R.string.quest_name_answer_noName_confirmation_title)
                .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> applyAnswer(NoShopNameSign) }
                .setNegativeButton(R.string.quest_generic_confirmation_no, null)
                .show()
    }

    override fun isFormComplete() = placeName.isNotEmpty()
}
