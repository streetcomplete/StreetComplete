package de.westnordost.streetcomplete.quests.address

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.MapDataWithGeometryHandler
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.tql.getQuestPrintStatement
import de.westnordost.streetcomplete.data.osm.tql.toGlobalOverpassBBox

class AddAddressStreet(private val overpassMapDataDao: OverpassMapDataDao) : OsmElementQuestType<AddressStreetAnswer> {
    override val commitMessage = "Add address"
    override val icon = R.drawable.ic_quest_label

    override fun getTitle(tags: Map<String, String>) = R.string.quest_address_street_title

    override fun createForm() = AddAddressStreetForm()

    override fun download(bbox: BoundingBox, handler: MapDataWithGeometryHandler): Boolean {
        return overpassMapDataDao.getAndHandleQuota(getOverpassQuery(bbox), handler);
    }

    override fun isApplicableTo(element: Element): Boolean? = null

    override fun applyAnswerTo(answer: AddressStreetAnswer, changes: StringMapChangesBuilder) {
        when(answer){
            is StreetName -> {changes.add("addr:street", answer.name)}
            is PlaceName -> {changes.add("addr:place", answer.name)}
        }
    }

    private fun getOverpassQuery(bbox: BoundingBox) =
            bbox.toGlobalOverpassBBox() + """
            relation["type"="associatedStreet"];
            > -> .inStreetRelation;

            nwr["addr:street"!~".*"]["addr:housenumber"]["addr:place"!~".*"] -> .missing_data;

            (.missing_data; - .inStreetRelation;);""" +
                    getQuestPrintStatement()
}
