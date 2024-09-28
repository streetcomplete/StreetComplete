package de.westnordost.streetcomplete.quests.custom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestController
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.quests.AbstractExternalSourceQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.CreatePoiFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.toTags
import de.westnordost.streetcomplete.util.ktx.toast
import org.koin.android.ext.android.inject

class CustomQuestForm : AbstractExternalSourceQuestForm() {

// switch back to this form if some sort of longer text field should be added
//    override val contentLayoutResId = R.layout.quest_osmose_custom_quest
//    private val binding by contentViewBinding(QuestOsmoseCustomQuestBinding::bind)
    private lateinit var entryId: String
    private var tagsText: String? = null
    private var pos: LatLon? = null

    private val questController: ExternalSourceQuestController by inject()
    private val customQuestList: CustomQuestList by inject()

    override val buttonPanelAnswers by lazy {
        val t = tagsText
        val p = pos
        listOfNotNull(
            AnswerItem(R.string.quest_custom_quest_remove) { questController.delete(questKey as ExternalSourceQuestKey) },
            if (t != null && p != null)
                AnswerItem(R.string.quest_custom_quest_add_node) {
                    val f = CreatePoiFragment.createWithPrefill(t, p, questKey)
                    parentFragmentManager.commit {
                        replace(id, f, "bottom_sheet")
                        addToBackStack("bottom_sheet")
                    }
                    (parentFragment as? MainFragment)?.offsetPos(p)
                }
            else null
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entryId = (questKey as ExternalSourceQuestKey).id
        val entry = customQuestList.getEntry(entryId)
        if (entry == null) {
            context?.toast(R.string.quest_custom_quest_osmose_not_found)
            questController.delete(questKey as ExternalSourceQuestKey)
            return
        }
        val text = entry.text

        if (text.contains("addNode")) {
            setTitle(resources.getString(R.string.quest_custom_quest_title) + " ${text.substringBefore("addNode")}")
            val tags = text.substringAfter("addNode").replace(",", "\n").toTags()
            tagsText = tags.map { "${it.key}=${it.value}" }.joinToString("\n")
            pos = entry.position ?: entry.elementKey?.let { mapDataSource.getGeometry(it.type, it.id)?.center }
            if (pos == null) {
                setTitleHintLabel(getString(R.string.quest_custom_quest_add_node_text, null)) // should never happen, because we can locate the quest
                return
            }
            setTitleHintLabel(getString(R.string.quest_custom_quest_add_node_text, "\n$tagsText"))
        } else
            setTitle(resources.getString(R.string.quest_custom_quest_title) + " $text")
    }
}
