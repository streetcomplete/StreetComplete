package de.westnordost.streetcomplete.quests.accepts_cash

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import de.westnordost.streetcomplete.util.ktx.toYesNo

class AddAcceptsCash : OsmFilterQuestType<Boolean>() {

    override val elementFilter: String get() {
        val amenities = listOf(
            "bar", "cafe", "fast_food", "food_court", "ice_cream", "pub", "biergarten",
            "restaurant", "cinema", "nightclub", "planetarium", "theatre", "marketplace",
            "internet_cafe", "car_wash", "fuel", "pharmacy", "telephone", "vending_machine"
        )
        val tourismsWithImpliedFees = listOf(
            "zoo", "aquarium", "theme_park", "hotel", "hostel", "motel", "guest_house",
            "apartment", "camp_site"
        )
        val tourismsWithoutImpliedFees = listOf(
            "attraction", "museum", "gallery"
        )
        val leisures = listOf(
            "adult_gaming_centre", "amusement_arcade", "bowling_alley", "escape_game", "miniature_golf",
            "sauna", "trampoline_park", "tanning_salon"
        )
        val crafts = listOf(
            "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
            "electronics_repair", "key_cutter", "stonemason"
        )
        return """
            nodes, ways with
            (
              (shop and shop !~ no|vacant|mall)
              or amenity ~ ${amenities.joinToString("|")}
              or leisure ~ ${leisures.joinToString("|")}
              or craft ~ ${crafts.joinToString("|")}
              or tourism ~ ${tourismsWithImpliedFees.joinToString("|")}
              or tourism ~ ${tourismsWithoutImpliedFees.joinToString("|")} and fee = yes
            )
            and !payment:cash and !payment:coins and !payment:notes and payment:others != no
            and (name or brand or noname = yes or name:signed = no)
        """
    }

    override val changesetComment = "Survey whether payment with cash is accepted"
    override val wikiLink = "Key:payment"
    override val icon = R.drawable.ic_quest_cash
    override val isReplacePlaceEnabled = true
    override val enabledInCountries = NoCountriesExcept(
        "FI", // https://github.com/streetcomplete/StreetComplete/issues/5500
        "GB", // https://github.com/streetcomplete/StreetComplete/issues/4517
        "SE",
        "NL", // https://github.com/streetcomplete/StreetComplete/issues/4826
    )
    override val achievements = listOf(CITIZEN)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_accepts_cash_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["payment:cash"] = answer.toYesNo()
    }
}
