package de.westnordost.streetcomplete.quests.osmose

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.databinding.QuestOsmoseExternalBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.questPrefix
import org.koin.android.ext.android.inject

class OsmoseForm(private val db: OsmoseDao) : AbstractOsmQuestForm<OsmoseAnswer>() {

    var issue: OsmoseIssue? = null

    private val osmQuestController: OsmQuestController by inject()

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override val contentLayoutResId = R.layout.quest_osmose_external
    private val binding by contentViewBinding(QuestOsmoseExternalBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        issue = db.get(ElementKey(element.type, element.id))
        val text = issue?.let {
            if (it.subtitle.isBlank())
                it.title
            else
                it.title + ": \n" + it.subtitle
        }
        binding.description.text =
            if (text == null) resources.getString(R.string.quest_external_osmose_not_found)
            else resources.getString(R.string.quest_osmose_message_for_element, issue?.item, text)
        if (issue?.subtitle?.startsWith("Concerns tag:") == true) {
            val tag = issue?.subtitle?.substringAfter("Concerns tag: `")?.substringBefore("`") ?: return
            buttonPanelAnswers.add(
                AnswerItem(R.string.quest_osmose_modify_tag) {
                    activity?.let {
                        val inputEditTextField = EditText(it)
                        inputEditTextField.setText(tag.substringAfter("="))
                        AlertDialog.Builder(it)
                            .setTitle(R.string.quest_osmose_set_value)
                            .setMessage(tag.substringBefore("=") + " =")
                            .setView(inputEditTextField)
                            .setPositiveButton(android.R.string.ok) {_,_ ->
                                val newValue = inputEditTextField.text.toString()
                                if (newValue.isNotBlank())
                                    applyAnswer(AdjustTagAnswer(
                                        issue?.uuid ?: "",
                                        tag.substringBefore("="),
                                        newValue.trim()
                                    ))
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                    }
                }
            )
        }
        if (issue != null)
            buttonPanelAnswers.add(AnswerItem(R.string.quest_osmose_false_positive) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.quest_osmose_false_positive)
                    .setMessage(R.string.quest_osmose_no_undo)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _,_ ->
                        db.setAsFalsePositive(issue?.uuid ?: "", questKey)
                        // remove quest from db
                        osmQuestController.delete(questKey as OsmQuestKey)
                    }
            } )
        updateButtonPanel()
    }

    override val otherAnswers: List<AnswerItem> by lazy { listOf(
        AnswerItem(R.string.quest_osmose_hide_type) {
            val types = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_ITEMS, "")!!.split(",").filterNot { it.isBlank() }.toMutableSet()
            issue?.let {
                types.add(it.item)
                prefs.edit().putString(questPrefix(prefs) + PREF_OSMOSE_ITEMS,types.joinToString(",")).apply()
            }
            tempHideQuest()
        }
    ) }

}

sealed interface OsmoseAnswer
class AdjustTagAnswer(val uuid: String, val tag: String, val newValue: String) : OsmoseAnswer
