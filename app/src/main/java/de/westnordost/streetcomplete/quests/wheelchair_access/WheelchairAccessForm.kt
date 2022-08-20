package de.westnordost.streetcomplete.quests.wheelchair_access

import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.LIMITED
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.NO
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.YES
import de.westnordost.streetcomplete.util.ktx.toast

open class WheelchairAccessForm : AbstractOsmQuestForm<WheelchairAccess>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO.apply { updatedDescriptions = descriptions }) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES.apply { updatedDescriptions = descriptions }) },
        AnswerItem(R.string.quest_wheelchairAccess_limited) { applyAnswer(LIMITED.apply { updatedDescriptions = descriptions }) },
    )

    private val descriptions = mutableMapOf<String, String>()

    override fun isRejectingClose(): Boolean = descriptions.isNotEmpty()

    override val otherAnswers: List<AnswerItem> = listOf(
        AnswerItem(R.string.quest_wheelchair_description_answer) {
            val languages = (countryInfo.officialLanguages.map { ":$it" } + ":en" + "").toMutableSet()
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(30,10,30,10)
            }
            val fields = languages.associateWith {
                val e = EditText(context).apply {
                    hint = it.substringAfter(':').ifEmpty { context.getString(R.string.quest_wheelchair_description_no_language) }
                    element.tags["wheelchair:description$it"]?.let { setText(it) }
                    addTextChangedListener {
                        if (text.toString().length > 254) {
                            context.toast(R.string.quest_wheelchair_description_too_long)
                            setText(text.toString().substring(0, 254))
                        }
                    }
                }
                layout.addView(e)
                e
            }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_wheelchair_description_title)
                .setView(layout)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok) { _,_ ->
                    fields.forEach { (s, editText) ->
                        if (editText.text.toString().trim() != (element.tags["wheelchair:description$s"] ?: ""))
                            descriptions[s] = editText.text.toString()
                    }
                }
                .show()
        }
    )
}
