package de.westnordost.streetcomplete.quests.custom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestController
import de.westnordost.streetcomplete.data.quest.OtherSourceQuestKey
import de.westnordost.streetcomplete.databinding.QuestOsmoseCustomQuestBinding
import de.westnordost.streetcomplete.quests.AbstractOtherQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.screens.main.MainFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.CreatePoiFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.toTags
import de.westnordost.streetcomplete.util.ktx.toast
import org.koin.android.ext.android.inject

class CustomQuestForm(private val customQuestList: CustomQuestList) : AbstractOtherQuestForm() {

    override val contentLayoutResId = R.layout.quest_osmose_custom_quest
    private val binding by contentViewBinding(QuestOsmoseCustomQuestBinding::bind)
    lateinit var entryId: String
    private var tagsText: String? = null
    private var pos: LatLon? = null

    private val questController: OtherSourceQuestController by inject()

    override val buttonPanelAnswers by lazy {
        val t = tagsText
        val p = pos
        listOfNotNull(
        AnswerItem(R.string.quest_custom_quest_remove) { questController.delete(questKey as OtherSourceQuestKey) },
        if (t != null && p != null)
            AnswerItem(R.string.quest_custom_quest_add_node) {
                val f = CreatePoiFragment.createWithPrefill(t, p, questKey)
                parentFragmentManager.commit {
                    add(id, f, null)
                    addToBackStack(null)
                }
                (parentFragment as? MainFragment)?.offsetPos(p)
            }
        else null
    ) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        entryId = (questKey as OtherSourceQuestKey).id
        val entry = customQuestList.getEntry(entryId)
        if (entry == null) {
            context?.toast(R.string.quest_custom_quest_osmose_not_found)
            questController.delete(questKey as OtherSourceQuestKey)
            return
        }
        val text = entry.text

        if (text.contains("addNode")) {
            setTitle(resources.getString(R.string.quest_custom_quest_title, text.substringBefore("addNode")))
            val tags = text.substringAfter("addNode").replace(",", "\n").toTags()
            tagsText = tags.map { "${it.key}=${it.value}" }.joinToString("\n")
            pos = entry.position ?: entry.elementKey?.let { mapDataSource.getGeometry(it.type, it.id)?.center }
            if (pos == null) {
                setTitleHintLabel(getString(R.string.quest_custom_quest_add_node_text, null)) // should never happen, because we can locate the quest
                return
            }
            setTitleHintLabel(getString(R.string.quest_custom_quest_add_node_text, "\n$tagsText"))
        } else
            setTitle(resources.getString(R.string.quest_custom_quest_title, text))
    }
}
