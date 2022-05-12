package de.westnordost.streetcomplete.quests.external

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.databinding.QuestOsmoseExternalBinding
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class ExternalForm(private val externalList: ExternalList) : AbstractQuestAnswerFragment<Boolean>() {

    override val contentLayoutResId = R.layout.quest_osmose_external
    private val binding by contentViewBinding(QuestOsmoseExternalBinding::bind)

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_external_remove) {
            val element = osmElement ?: return@AnswerItem
            val key = ElementKey(element.type, element.id)
            externalList.remove(key)
            tempSkipQuest()
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val element = osmElement ?: return
        val key = ElementKey(element.type, element.id)
        val text = externalList.questsMap[key]
        binding.description.text =
            if (text == null) resources.getString(R.string.quest_external_osmose_not_found)
            else resources.getString(R.string.quest_external_message_for_element, text)
    }

}
