package de.westnordost.streetcomplete.quests.piste_ref

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddPisteRef : OsmFilterQuestType<PisteRefAnswer>() {

    override val elementFilter = """
        ways, relations with
          piste:type = downhill
          and !ref
          and !piste:ref
    """
    override val changesetComment = "Survey piste ref"
    override val wikiLink = "Key:piste:ref"
    override val icon = R.drawable.ic_quest_piste_ref
    override val defaultDisabledMessage = R.string.default_disabled_msg_ee


    override fun getTitle(tags: Map<String, String>) = R.string.quest_piste_ref_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("ways, relations with piste:type = downhill")

    override fun createForm() = AddPisteRefForm()

    override fun applyAnswerTo(answer: PisteRefAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is PisteRef ->          tags["piste:ref"] = answer.ref
            is PisteConnection ->   tags["piste:type"] = "connection"
        }
    }
}
