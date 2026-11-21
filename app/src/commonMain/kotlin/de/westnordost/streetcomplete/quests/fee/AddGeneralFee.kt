package de.westnordost.streetcomplete.quests.fee

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.fee.Fee
import de.westnordost.streetcomplete.quests.parking_fee.ParkingFeeAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.common.quest.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo
import org.jetbrains.compose.resources.stringResource

class AddGeneralFee : OsmFilterQuestType<FeeAnswer>() {

    // Bicycle charging station are amenity=charging_station with bicycle=yes/designated.
    // We further exclude dual use charging stations as payment may be waived only for cyclists increasing risk of incorrect answers
    override val elementFilter = """
        nodes, ways, relations with
          (
            (
              amenity = charging_station
              and bicycle ~ yes|designated
              and (!motorcar or motorcar = no)
              and (!motorcycle or motorcycle = no)
              and (!truck or truck = no)
            )
            or (
              tourism = camp_site
              and (
                camp_site = basic
                or backcountry = yes
                or shower = no
                or toilets = no
              )
            )
            or tourism ~ museum|gallery|caravan_site|zoo|aquarium|wilderness_hut
            or leisure ~ beach_resort|disc_golf_course
            or amenity ~ sanitary_dump_station|shower|water_point|public_bath|bicycle_wash|binoculars|device_charging_station|vacuum_cleaner
            or natural = cave_entrance and access=yes
            or man_made = tower and tower:type = observation and access=yes
            or historic = castle and access = yes
            or waterway = water_point
          )
          and access !~ private|no
          and !fee
          and !fee:conditional
    """
    override val changesetComment = "Specify whether places take fees to visit"
    override val wikiLink = "Key:fee"
    override val icon = Res.drawable.quest_fee
    override val title = Res.string.quest_generalFee_title
    override val achievements = listOf(CITIZEN)

    @Composable
    override fun Form(on: (QuestAction<FeeAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        QuestForm(
            on = on,
            title = stringResource(
                if (element.tags["amenity"] != null || element.tags["waterway"] != null) {
                    // Things you must pay to "use"
                    Res.string.quest_generalFee_title
                } else if (element.tags["tourism"] == "wilderness_hut" || element.tags["tourism"] == "camp_site" || element.tags["tourism"] == "caravan_site") {
                    // Places you must pay to "stay at"
                    Res.string.quest_generalFee_title3
                } else {
                    // Places you must pay to "enter"
                    Res.string.quest_generalFee_title2
                }
            ),
            answers = listOf(
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(
                    FeeAnswer.NO)) },
                AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(FeeAnswer.YES)) },
            ),
            otherAnswers = {
                if (element.tags["amenity"] != null) {
                    listOf(
                        AnswerItem(stringResource(Res.string.quest_toiletsFee_fee_for_non_customers)) {
                            on(Answer(FeeAnswer.ONLY_FOR_NON_CUSTOMERS))
                        },
                    )
                }
                else
                    listOf()
            }
        )
    }

    override fun applyAnswerTo(answer: FeeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long)  {
        tags.remove("fee:conditional")
        when (answer) {
            FeeAnswer.ONLY_FOR_NON_CUSTOMERS -> {
                tags["access"] = "yes"
                tags["fee"] = "yes"
                tags["fee:conditional"] = "no @ customers"
            }
            FeeAnswer.YES -> {
                tags["fee"] = "yes"
            }
            FeeAnswer.NO -> {
                tags["fee"] = "no"
            }
        }
    }
}
