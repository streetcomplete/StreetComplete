package de.westnordost.streetcomplete.quests.aerialway

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.*
import de.westnordost.streetcomplete.resources.*

class AddAerialwayBicycleAccess : OsmFilterQuestType<AerialwayBicycleAccessAnswer>() {

    override val elementFilter = """
        ways with
          aerialway ~ cable_car|gondola|chair_lift
          and !aerialway:bicycle and !bicycle
    """

    override val changesetComment = "Specify whether bicycles can be taken on aerialway"
    override val wikiLink = "Tag:aerialway"
    override val icon = R.drawable.quest_aerialway_bicycle
    override val title = Res.string.quest_aerialway_bicycle_title
    override val achievements = listOf(RARE, BICYCLIST)

    @Composable
    override fun Form(onAnswer: (AerialwayBicycleAccessAnswer) -> Unit) {
        AddAerialwayBicycleAccessForm(onAnswer)
    }

    override fun applyAnswerTo(answer: AerialwayBicycleAccessAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            YES -> tags["aerialway:bicycle"] = "yes"
            SUMMER -> tags["aerialway:bicycle"] = "summer"
            NO -> tags["aerialway:bicycle"] = "no"
            NO_SIGN -> tags["aerialway:bicycle:signed"] = "no"
        }
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
            nodes, ways with aerialway
        """.toElementFilterExpression())
    }
