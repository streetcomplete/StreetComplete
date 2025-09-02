package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.ferry.wayIdsInFerryRoutes
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSign.MAX_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSign.MAX_GROSS_VEHICLE_MASS
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSign.MAX_TANDEM_AXLE_LOAD
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightSign.MAX_WEIGHT

class AddMaxWeight : OsmElementQuestType<MaxWeightAnswer>, AndroidQuest {

    // We ask for the maximum weight of bridges and ferries.
    // The general filter is used for both:
    private val generalFilter by lazy {
        """ways, relations with
         !maxweight and maxweight:signed != no
         and !maxaxleload
         and !maxbogieweight
         and !maxweight:hgv and !maxweight:bus and !maxweight:hgv_articulated and !maxweight:tourist_bus and !maxweight:coach
         and !maxweightrating and !maxweightrating:hgv and !maxweightrating:bus and !hgv
         and !maxunladenweight and !maxunladenweight:hgv and !maxunladenweight:bus
         and vehicle !~ private|no
         and (access !~ private|no or (foot and foot !~ private|no))
         and area != yes
    """.toElementFilterExpression() }

    private val highwayFilter by lazy {
        """ways with
         highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|living_street|service|busway
         and bridge and bridge != no
         and service != driveway
         and motor_vehicle !~ private|no
    """.toElementFilterExpression() }

    private val ferryFilter by lazy {
        """ways, relations with
         route = ferry
         and motor_vehicle = yes
        """.toElementFilterExpression() }

    override val changesetComment = "Specify maximum allowed weights"
    override val wikiLink = "Key:maxweight"
    override val icon = R.drawable.ic_quest_max_weight
    override val hasMarkersAtEnds = true
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_maxweight_title

    override fun createForm() = AddMaxWeightForm()

    override fun applyAnswerTo(answer: MaxWeightAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is MaxWeight -> {
                tags[answer.sign.osmKey] = answer.weight.toString()
            }
            is NoMaxWeightSign -> {
                tags["maxweight:signed"] = "no"
            }
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // copied from AddFerryAccessMotorVehicle - see comment there why this filtering is necessary
        val wayIdsInFerryRoutes = wayIdsInFerryRoutes(mapData.relations)
        return mapData
            .filter(generalFilter)
            .filter { ferryFilter.matches(it) || highwayFilter.matches(it) }
            .filter { it !is Way || it.id !in wayIdsInFerryRoutes }
            .asIterable()
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!generalFilter.matches(element)) return false
        if (element is Way) {
            if (highwayFilter.matches(element)) return true
            // This way may be part of a ferry relation, in which case we will not ask
            if (ferryFilter.matches(element)) return null
            return false
        }
        if (element is Relation) {
            // Highway relations are not matched
            return ferryFilter.matches(element)
        }
        return false
    }
}

private val MaxWeightSign.osmKey get() = when (this) {
    MAX_WEIGHT             -> "maxweight"
    MAX_GROSS_VEHICLE_MASS -> "maxweightrating"
    MAX_AXLE_LOAD          -> "maxaxleload"
    MAX_TANDEM_AXLE_LOAD   -> "maxbogieweight"
}
