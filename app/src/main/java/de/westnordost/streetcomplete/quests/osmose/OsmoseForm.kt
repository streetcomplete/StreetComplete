package de.westnordost.streetcomplete.quests.osmose

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractExternalSourceQuestForm
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.databinding.QuestOsmoseCustomQuestBinding
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.util.dialogs.setViewWithDefaultPadding
import de.westnordost.streetcomplete.util.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

@SuppressLint("SetTextI18n") // android studio complains, but that's element type and id and probably should not be translated
class OsmoseForm : AbstractExternalSourceQuestForm() {

    private val osmoseDao: OsmoseDao by inject()

    private val issue: OsmoseIssue? by lazy {
        val key = questKey as ExternalSourceQuestKey
        osmoseDao.getIssue(key.id)
    }

    private val questController: ExternalSourceQuestController by inject()

    override val buttonPanelAnswers by lazy {
        val issue = issue
        if (issue == null) emptyList()
        else
        listOfNotNull(
            if (issue.elements.isEmpty()) null
            else if (issue.elements.size == 1) {
                val e = mapDataSource.get(issue.elements.single().type, issue.elements.single().id)
                if (e == null) null
                else
                    AnswerItem(R.string.quest_generic_answer_show_edit_tags) { editTags(e) }
            } else {
                val elements = issue.elements.mapNotNull { mapDataSource.get(it.type, it.id) }
                if (elements.isEmpty()) null
                else
                    AnswerItem(R.string.quest_generic_answer_show_edit_tags) {
                        val l = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
                        var d: AlertDialog? = null
                        elements.forEach { e ->
                            l.addView(Button(requireContext()).apply {
                                text = "${e.type} ${e.id}"
                                setOnClickListener {
                                    editTags(e)
                                    d?.dismiss()
                                }
                            })
                        }
                        d = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.quest_osmose_select_element)
                            .setViewWithDefaultPadding(l)
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                        d?.show()
                    }
            },
            AnswerItem(R.string.quest_osmose_false_positive) {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.quest_osmose_false_positive)
                    .setMessage(R.string.quest_osmose_no_undo)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.quest_generic_confirmation_yes) { _,_ ->
                        osmoseDao.setAsFalsePositive(issue.uuid)
                        tempHideQuest() // will still not be shown again, as osmoseDao doesn't create a quest from that any more
                        // todo: do some kind of edit, so it can be undone? the edit could be deleted on upload (see also ExternalSourceModule commented stuff)
                    }
                    .show()
            }
        )
    }

    override val contentLayoutResId = R.layout.quest_osmose_custom_quest
    private val binding by contentViewBinding(QuestOsmoseCustomQuestBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val issue = issue
        if (issue == null) {
            context?.toast(R.string.quest_custom_quest_osmose_not_found)
            questController.delete(questKey as ExternalSourceQuestKey)
            return
        }
        setTitle(resources.getString(R.string.quest_osmose_title, issue.title))
        binding.description.text = resources.getString(R.string.quest_osmose_message_for_element, "${issue.item}/${issue.itemClass}", issue.subtitle)

        if (issue.elements.size > 1) viewLifecycleScope.launch { highlightElements() }
        updateButtonPanel()
    }

    private fun highlightElements() {
        val issue = issue ?: return
        val elementsAndGeometry = issue.elements.mapNotNull { mapDataSource.get(it.type, it.id) }.mapNotNull { e -> mapDataSource.getGeometry(e.type, e.id)?.let { e to it } }

        if (prefs.getBoolean(Prefs.SHOW_WAY_DIRECTION, false) && elementsAndGeometry.any { it.second is ElementPolylinesGeometry }) {
            // show geometry containing way direction together with normal one. not nice looking, but:
            //  normal one contains way labels, which are necessary for editing
            //  this here contains the arrows
            // and adding arrows to "normal" highlighted ways in special cases only is maybe work for later
            val mapFragment = (parentFragment as? MainFragment)?.childFragmentManager?.fragments?.filterIsInstance<MainMapFragment>()?.singleOrNull()
            mapFragment?.highlightGeometries(elementsAndGeometry.map { it.second })
        }

        val showsGeometryMarkersListener = parentFragment as? ShowsGeometryMarkers ?: activity as? ShowsGeometryMarkers ?: return
        elementsAndGeometry.forEach {
            showsGeometryMarkersListener.putMarkerForCurrentHighlighting(it.second, null, "${it.first.type} ${it.first.id}")
        }
    }

    override val otherAnswers: List<AnswerItem> by lazy { listOfNotNull(
        AnswerItem(R.string.quest_osmose_hide_type) { showIgnoreDialog() },
        AnswerItem(R.string.quest_osmose_delete_this_issue) {
            questController.delete(questKey as ExternalSourceQuestKey)
        },
    )
    }

    private fun showIgnoreDialog() {
        val issue = issue ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.quest_osmose_hide_type)
            .setItems(arrayOfNotNull("item: ${issue.item}", "item/class: ${issue.item}/${issue.itemClass}", "subtitle: ${issue.subtitle}".takeIf { issue.subtitle.isNotBlank() })) { _, i ->
                when (i) {
                    0 -> issue.item.toString()
                    1 -> "${issue.item}/${issue.itemClass}"
                    2 -> issue.subtitle
                    else -> null
                }?.let { addToIgnoreList(it) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun addToIgnoreList(item: String) {
        val types = prefs.getString(questPrefix(prefs) + PREF_OSMOSE_ITEMS, OSMOSE_DEFAULT_IGNORED_ITEMS)
            .split("§§")
            .mapNotNull { if (it.isNotBlank()) it.trim() else null }
            .toMutableSet()
        types.add(item)
        prefs.putString(questPrefix(prefs) + PREF_OSMOSE_ITEMS,types.sorted().joinToString("§§"))
        osmoseDao.reloadIgnoredItems()
        questController.invalidate()
    }
}
