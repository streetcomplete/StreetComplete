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
import de.westnordost.streetcomplete.util.ktx.toast
import org.koin.android.ext.android.inject

class OsmoseForm(private val db: OsmoseDao) : AbstractOsmQuestForm<OsmoseAnswer>() {

    private lateinit var issue: OsmoseIssue

    private val osmQuestController: OsmQuestController by inject()

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override val contentLayoutResId = R.layout.quest_osmose_external
    private val binding by contentViewBinding(QuestOsmoseExternalBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val i = db.get(ElementKey(element.type, element.id))
        if (i == null) {
            context?.toast(R.string.quest_external_osmose_not_found)
            osmQuestController.delete(questKey as OsmQuestKey)
            return
        }
        issue = i
        setTitle(resources.getString(R.string.quest_osmose_title, issue.title))
        binding.description.text = resources.getString(R.string.quest_osmose_message_for_element, issue.item, issue.subtitle)
        if (issue.subtitle.startsWith("Concerns tag:")) {
            val tag = issue.subtitle.substringAfter("Concerns tag: `").substringBefore("`")
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
                                        issue.uuid,
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
        buttonPanelAnswers.add(AnswerItem(R.string.quest_osmose_false_positive) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_osmose_false_positive)
                .setMessage(R.string.quest_osmose_no_undo)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _,_ ->
                    db.setAsFalsePositive(issue.uuid)
                    // remove quest from db
                    osmQuestController.delete(questKey as OsmQuestKey)
                }
                .show()
        } )
        updateButtonPanel()
    }

    override val otherAnswers: List<AnswerItem> by lazy { listOf(
        AnswerItem(R.string.quest_osmose_hide_type) {
            val types = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_ITEMS, "")!!
                .split(",")
                .mapNotNull { if (it.isNotBlank()) it.trim() else null }
                .toMutableSet()
            types.add(issue.item)
            prefs.edit().putString(questPrefix(prefs) + PREF_OSMOSE_ITEMS,types.sorted().joinToString(", ")).apply()
            db.reloadIgnoredItems()
            osmQuestController.delete(questKey as OsmQuestKey)
        }
    ) }

}

sealed interface OsmoseAnswer
class AdjustTagAnswer(val uuid: String, val tag: String, val newValue: String) : OsmoseAnswer
