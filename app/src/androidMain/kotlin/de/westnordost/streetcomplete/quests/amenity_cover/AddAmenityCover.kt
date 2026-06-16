package de.westnordost.streetcomplete.quests.amenity_cover

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.containsAll
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddAmenityCover(
    private val getFeature: (Element) -> Feature?
) : OsmElementQuestType<Boolean>, AndroidQuest {

    private val nodesFilter by lazy { """
        nodes with
          (leisure = picnic_table or amenity = bbq)
          and access !~ private|no
          and !covered
          and (!seasonal or seasonal = no)
    """.toElementFilterExpression() }
    override val changesetComment = "Specify whether various amenities are covered"
    override val wikiLink = "Key:covered"
    override val icon = R.drawable.quest_picnic_table_cover
    override val title = Res.string.quest_amenityCover_title
    override val isDeleteElementEnabled = true
    override val achievements = listOf(OUTDOORS)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) =
        nodesFilter.matches(element) && getFeature(element) != null

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry): Sequence<Element> {
        /* put markers for objects that are exactly the same as for which this quest is asking for
           e.g. it's a ticket validator? -> display other ticket validators. Etc. */
        val feature = getFeature(element) ?: return emptySequence()
        return mapData.asSequence().filter { it.tags.containsAll(feature.tags) }
    }

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["covered"] = answer.toYesNo()
    }
}
