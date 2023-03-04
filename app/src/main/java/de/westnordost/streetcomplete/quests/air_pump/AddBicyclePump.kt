package de.westnordost.streetcomplete.quests.air_pump

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddBicyclePump : OsmFilterQuestType<Boolean>() {

    /* if service:bicycle:pump is undefined, nothing has been said about its existence;
     * see https://wiki.openstreetmap.org/wiki/Tag:shop=bicycle#Additional_keys
     *
     * Also, "access=customers" + "service:bicycle:pump=yes" is an invalid combination, as the wiki states that
     * "yes" means "a feature has a bicycle pump which can be used by anybody, not only customers"
     */
    override val elementFilter = """
        nodes, ways with
        (amenity = bicycle_repair_station or shop = bicycle)
        and (
            !compressed_air and !service:bicycle:pump
            or service:bicycle:pump older today -6 years
        )
        and access !~ private|no|customers
    """
    override val changesetComment = "Survey whether bicycle pumps are available"
    override val wikiLink = "Key:service:bicycle:pump"
    override val icon = R.drawable.ic_quest_bicycle_pump
    override val achievements = listOf(BICYCLIST)

    override fun getTitle(tags: Map<String, String>) =
        if (tags["shop"] == "bicycle") R.string.quest_air_pump_bicycle_shop_title
        else R.string.quest_air_pump_bicycle_repair_station_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes, ways with
            compressed_air = yes
            or service:bicycle:pump = yes
            or amenity ~ compressed_air|bicycle_repair_station
            or shop = bicycle
        """)

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("service:bicycle:pump", answer.toYesNo())
    }
}
