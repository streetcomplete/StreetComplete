package de.westnordost.streetcomplete.quests.wheelchair_access

import android.content.Context
import android.text.InputFilter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.LIMITED
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.NO
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.YES
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding

open class WheelchairAccessForm : AbstractOsmQuestForm<WheelchairAccess>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(NO.apply { updatedDescriptions = descriptions }) },
        AnswerItem(R.string.quest_wheelchairAccess_limited) { applyAnswer(LIMITED.apply { updatedDescriptions = descriptions }) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(YES.apply { updatedDescriptions = descriptions }) },
    )

    private val descriptions = mutableMapOf<String, String>()

    override fun isRejectingClose(): Boolean = descriptions.isNotEmpty()

    override val otherAnswers: List<AnswerItem> get() = listOf(
        createAddDescriptionAnswer(element, descriptions, requireContext(), countryInfo)
    )
}

fun createAddDescriptionAnswer(element: Element, descriptions: MutableMap<String, String>, context: Context, countryInfo: CountryInfo) =
    AnswerItem(R.string.quest_wheelchair_description_answer) {
        val languages = (countryInfo.officialLanguages.map { ":$it" } + ":en" + "").toMutableSet()
        val layout = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val fields = languages.associateWith {
            val e = EditText(context).apply {
                hint = it.substringAfter(':').ifEmpty { context.getString(R.string.quest_wheelchair_description_no_language) }
                element.tags["wheelchair:description$it"]?.let { setText(it) }
                filters = arrayOf(InputFilter.LengthFilter(255))
            }
            layout.addView(e)
            e
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.quest_wheelchair_description_title)
            .setViewWithDefaultPadding(layout)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _,_ ->
                fields.forEach { (s, editText) ->
                    if (editText.text.toString().trim() != (element.tags["wheelchair:description$s"] ?: ""))
                        descriptions[s] = editText.text.toString()
                }
            }
            .show()
    }
