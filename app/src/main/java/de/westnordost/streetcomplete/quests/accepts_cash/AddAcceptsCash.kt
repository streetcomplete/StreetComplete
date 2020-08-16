package de.westnordost.streetcomplete.quests.accepts_cash

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddAcceptsCash(o: OverpassMapDataAndGeometryApi) : SimpleOverpassQuestType<Boolean>(o) {
    private val amenity = listOf(
        "bar", "cafe", "fast_food", "food_court", "ice_cream", "pub", "biergarten",
        "restaurant", "cinema", "nightclub", "planetarium", "theatre", "marketplace",
        "internet_cafe", "car_wash", "fuel", "pharmacy", "telephone", "vending_machine"
    )
    private val tourismWithImpliedFees = listOf(
        "zoo", "aquarium", "theme_park", "hotel", "hostel", "motel", "guest_house",
        "apartment", "camp_site"
    )
    private val tourismWithoutImpliedFees = listOf(
        "attraction", "museum", "gallery" 
    )
    private val leisure = listOf(
        "adult_gaming_centre", "amusement_arcade", "bowling_alley", "escape_game", "miniature_golf",
        "sauna", "trampoline_park", "tanning_salon"
    )
    private val craft = listOf(
        "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
        "electronics_repair", "key_cutter", "stonemason"
    )

    override val tagFilters = """
        nodes, ways, relations with
        (
          (shop and shop !~ no|vacant|mall)
          or amenity ~ ${amenity.joinToString("|")}
          or leisure ~ ${leisure.joinToString("|")}
          or craft ~ ${craft.joinToString("|")}
          or tourism ~ ${tourismWithImpliedFees.joinToString("|")}
          or tourism ~ ${tourismWithoutImpliedFees.joinToString("|")} and fee = yes
        )
        and name and !payment:cash and !payment:coins and !payment:notes
    """
    override val commitMessage = "Add whether this place accepts cash as payment"
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
    override val wikiLink = "Key:payment"
    override val icon = R.drawable.ic_quest_cash

    override val enabledInCountries = NoCountriesExcept(
            // Europe
            "SE"
    )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_accepts_cash_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("payment:cash", if(answer) "yes" else "no")
    }
}
