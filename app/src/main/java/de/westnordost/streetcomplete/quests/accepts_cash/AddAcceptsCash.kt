package de.westnordost.streetcomplete.quests.accepts_cash

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import java.util.concurrent.FutureTask

class AddAcceptsCash(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmFilterQuestType<Boolean>() {

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
        nodes, ways, relations with
        (
          (shop and shop !~ no|vacant|mall)
          or amenity ~ ${amenities.joinToString("|")}
          or leisure ~ ${leisures.joinToString("|")}
          or craft ~ ${crafts.joinToString("|")}
          or tourism ~ ${tourismsWithImpliedFees.joinToString("|")}
          or tourism ~ ${tourismsWithoutImpliedFees.joinToString("|")} and fee = yes
        )
        and name and !payment:cash and !payment:coins and !payment:notes
    """}

    override val commitMessage = "Add whether this place accepts cash as payment"
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside
    override val wikiLink = "Key:payment"
    override val icon = R.drawable.ic_quest_cash

    override val enabledInCountries = NoCountriesExcept("SE")

    override fun getTitle(tags: Map<String, String>) = 
        if (hasFeatureName(tags) && !tags.containsKey("brand"))
            R.string.quest_accepts_cash_type_title
        else
            R.string.quest_accepts_cash_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        return if (name != null) arrayOf(name,featureName.value.toString()) else arrayOf()
    }
    
    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("payment:cash", answer.toYesNo())
    }
    
    private fun hasFeatureName(tags: Map<String, String>?): Boolean =
        tags?.let { featureDictionaryFuture.get().byTags(it).find().isNotEmpty() } ?: false
}
