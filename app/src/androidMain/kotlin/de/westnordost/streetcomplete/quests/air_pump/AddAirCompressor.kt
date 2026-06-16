package de.westnordost.streetcomplete.quests.air_pump

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddAirCompressor : OsmFilterQuestType<Boolean>(), AndroidQuest {

    override val elementFilter = """
        nodes, ways with
        amenity = fuel
        and (
            !compressed_air
            or compressed_air older today -6 years
        )
    """

    override val changesetComment = "Survey availability of air compressors"
    override val wikiLink = "Key:compressed_air"
    override val icon = R.drawable.quest_car_air_compressor
    override val title = Res.string.quest_air_pump_compressor_title
    override val achievements = listOf(CAR, BICYCLIST)
    override val isReplacePlaceEnabled = true

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes, ways with
            compressed_air = yes
            or amenity ~ compressed_air|fuel
        """)

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("compressed_air", answer.toYesNo())
    }
}
