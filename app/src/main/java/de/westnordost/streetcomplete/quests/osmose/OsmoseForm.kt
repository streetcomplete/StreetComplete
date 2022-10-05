package de.westnordost.streetcomplete.quests.osmose

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.quests.AbstractOtherQuestForm
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestController
import de.westnordost.streetcomplete.data.quest.OtherSourceQuestKey
import de.westnordost.streetcomplete.databinding.QuestOsmoseExternalBinding
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.onClickEditTags
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class OsmoseForm : AbstractOtherQuestForm() {

    private val osmoseDao: OsmoseDao by inject()

    private lateinit var issue: OsmoseIssue

    private val questController: OtherSourceQuestController by inject()
    private val mapDataSource: MapDataWithEditsSource by inject()

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override val contentLayoutResId = R.layout.quest_osmose_external
    private val binding by contentViewBinding(QuestOsmoseExternalBinding::bind)

    private val showsGeometryMarkersListener: ShowsGeometryMarkers? get() =
        parentFragment as? ShowsGeometryMarkers ?: activity as? ShowsGeometryMarkers

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val key = questKey as OtherSourceQuestKey
        val i = osmoseDao.getIssue(key.id)
        if (i == null) {
            context?.toast(R.string.quest_external_osmose_not_found)
            questController.delete(key)
            return
        }
        issue = i
        setTitle(resources.getString(R.string.quest_osmose_title, issue.title))
        binding.description.text = resources.getString(R.string.quest_osmose_message_for_element, "${issue.item}/${issue.itemClass}", issue.subtitle)
        buttonPanelAnswers.add(AnswerItem(R.string.quest_osmose_false_positive) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.quest_osmose_false_positive)
                .setMessage(R.string.quest_osmose_no_undo)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.quest_generic_confirmation_yes) { _,_ ->
                    osmoseDao.setAsFalsePositive(issue.uuid)
                    tempHideQuest() // will still not be shown again, as osmoseDao doesn't create a quest from that
                    // todo: do some kind of edit, so it can be undone? could be deleted on sync
                }
                .show()
        } )

        if (issue.elements.size == 1) element = mapDataSource.get(issue.elements.single().type, issue.elements.single().id)
        if (issue.elements.size > 1) viewLifecycleScope.launch { highlightElements() }
        updateButtonPanel()
    }

    private fun highlightElements() {
        issue.elements.mapNotNull { mapDataSource.get(it.type, it.id) }.mapNotNull { e -> mapDataSource.getGeometry(e.type, e.id)?.let { e to it } }.forEach {
            showsGeometryMarkersListener?.putMarkerForCurrentHighlighting(it.second, null, "${it.first.type} ${it.first.id}")
        }
    }

    override val otherAnswers: List<AnswerItem> by lazy { listOfNotNull(
        if (issue.elements.isEmpty()) null
        else if (issue.elements.size == 1) {
            val e = mapDataSource.get(issue.elements.single().type, issue.elements.single().id)
            if (e == null) null
            else
                AnswerItem(R.string.quest_generic_answer_show_edit_tags) { editTags(e) }
        }
        else {
            val elements = issue.elements.mapNotNull { mapDataSource.get(it.type, it.id) }
            if (elements.isEmpty()) null
            else
                AnswerItem(R.string.quest_generic_answer_show_edit_tags) {
                    val l = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(30, 10, 30, 10)
                    }
                    elements.forEach { e ->
                        l.addView(Button(requireContext()).apply {
                            text = "${e.type} ${e.id}"
                            setOnClickListener { editTags(e) }
                        })
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.quest_osmose_select_element)
                        .setView(l)
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
        },
        AnswerItem(R.string.quest_osmose_hide_type_specific) { addToIgnorelist("${issue.item}/${issue.itemClass}") },
        AnswerItem(R.string.quest_osmose_hide_type_generic) { addToIgnorelist(issue.item.toString()) },
        AnswerItem(R.string.quest_osmose_delete_this_issue) {
            questController.delete(questKey as OtherSourceQuestKey)
        },
    )
    }

    private fun editTags(e: Element) {
        onClickEditTags(e, context) { viewLifecycleScope.launch {
            osmoseDao.setDone(issue.uuid)
            tempHideQuest()
            editElement(e, it)
        } }
    }

    private fun addToIgnorelist(item: String) {
        val types = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_ITEMS, "")!!
            .split(",")
            .mapNotNull { if (it.isNotBlank()) it.trim() else null }
            .toMutableSet()
        types.add(item)
        prefs.edit().putString(questPrefix(prefs) + PREF_OSMOSE_ITEMS,types.sorted().joinToString(", ")).apply()
        osmoseDao.reloadIgnoredItems()
        questController.invalidate()
    }
}
