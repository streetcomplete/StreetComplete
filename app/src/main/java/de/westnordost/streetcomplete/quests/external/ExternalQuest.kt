package de.westnordost.streetcomplete.quests.external

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags

class ExternalQuest(private val externalList: ExternalList) : OsmElementQuestType<Boolean> {

    override val changesetComment = "Edit user-defined list of elements"
    override val wikiLink = "Tags"
    override val icon = R.drawable.ic_quest_external
    override val defaultDisabledMessage = R.string.quest_external_message

    override fun isApplicableTo(element: Element): Boolean =
        externalList.questsMap.containsKey(ElementKey(element.type, element.id))

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val list = mutableListOf<Element>()
        mapData.forEach {
            if (externalList.questsMap.contains(ElementKey(it.type, it.id)))
                list.add(it)
        }
        return list
    }

    override fun getTitle(tags: Map<String, String>): Int = R.string.quest_external_title

    override fun getTitleArgs(tags: Map<String, String>): Array<String> =
        arrayOf(tags.toString())

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) { }

    override fun createForm() = ExternalForm(externalList)
}

/* ideally i could start some intent like
     val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
         .addCategory(Intent.CATEGORY_OPENABLE)
         .setType("application/octet-stream")
     startActivityForResult(intent, REQUEST_CODE)
   and get the resulting file using something like
     override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
         if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && resultData != null)
            copyThatListTo("external.csv")
     }
   but this only works in a fragment (or other), but not here
   -> need to put it to settings, which is much less nice than the quest settings...
 */
