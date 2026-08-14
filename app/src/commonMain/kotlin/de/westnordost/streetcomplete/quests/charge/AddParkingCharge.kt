package de.westnordost.streetcomplete.quests.charge

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.resources.*

/**
 * Quest that asks for the parking fee of locations where a fee is required, but the amount
 * is unknown or hasn't been verified for a long time.
 */
class AddParkingCharge : OsmFilterQuestType<ChargeAnswer>() {
    override val elementFilter = """
        nodes, ways, relations with amenity = parking
        and access ~ yes|customers|public
        and fee = yes
        and (
            !charge and !charge:conditional
            or charge older today -18 months
        )
    """
    override val changesetComment = "Add parking charges"
    override val wikiLink = "Key:charge"
    override val icon = Res.drawable.quest_parking_charge
    override val achievements = listOf(CAR)
    override val hint = Res.string.quest_parking_charge_hint
    override val title = Res.string.quest_parking_charge_title

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("""
             nodes, ways with amenity = parking
         """.toElementFilterExpression())

    @Composable
    override fun Form(on: (QuestAction<ChargeAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        AddChargeForm(on, countryInfo)
    }

    override fun applyAnswerTo(answer: ChargeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is SimpleCharge -> {
                // Format: "1.50 EUR/hour"
                tags["charge"] = "${answer.amount} ${answer.currency}/${answer.timeUnit.toOsmValue(false)}"
                tags.updateCheckDateForKey("charge")
            }
        }
    }
}
