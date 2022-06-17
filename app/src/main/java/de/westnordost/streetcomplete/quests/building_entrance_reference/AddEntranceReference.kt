package de.westnordost.streetcomplete.quests.crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.quests.building_entrance_reference.AddEntranceReferenceForm
import de.westnordost.streetcomplete.quests.building_entrance_reference.EntranceAnswer
import de.westnordost.streetcomplete.quests.building_entrance_reference.FlatRange
import de.westnordost.streetcomplete.quests.building_entrance_reference.ReferenceCode
import de.westnordost.streetcomplete.quests.building_entrance_reference.ReferenceCodeAndFlatRange
import de.westnordost.streetcomplete.quests.building_entrance_reference.Unsigned

class AddEntranceReference : OsmElementQuestType<EntranceAnswer> {

    private val buildingFilter by lazy { """
        ways with
          building = apartments
          and ruins != yes and disused != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Add entrance identification"
    override val wikiLink = "Key:ref"
    override val icon = R.drawable.ic_quest_door_address
    override val questTypeAchievements = listOf(CITIZEN, BLIND)
    override val enabledInCountries = NoCountriesExcept(
        "PL", // Poland - own knowledge of Mateusz Konieczny https://github.com/streetcomplete/StreetComplete/issues/3064#issuecomment-879447168
        "RU", // Russia - https://github.com/streetcomplete/StreetComplete/issues/3064#issuecomment-880231076
        "MK", // North Macedonia https://github.com/streetcomplete/StreetComplete/issues/3064#issuecomment-1158016740
        "BG", // Bulgaria - https://github.com/streetcomplete/StreetComplete/issues/3064#issuecomment-879318531
        "UA", // Ukraine - https://t.me/osmUA/123011
        "BY", // Belarus - https://t.me/byosm/59969
        "EE", // Estonia - https://t.me/byosm/59976
        "HU", // Hungary https://github.com/streetcomplete/StreetComplete/issues/3064#issuecomment-1159088130
        "RO", // Romania https://t.me/OSMRomania/7363

        // not applicable (no matching address system)
        // Kosovo (XK) -  https://t.me/osmkosovo/19219
        // Serbia (RS) - https://t.me/osm_sr/6877
        // Lithuania (LT) - https://lists.openstreetmap.org/pipermail/talk-lt/2022-June/thread.html#2723 https://github.com/streetcomplete/StreetComplete/issues/3064#issuecomment-1147833339
        // Slovakia (SK) https://groups.google.com/g/osm_sk/c/IqZz0-iRK5I
        // Czech Republic (CZ) https://t.me/openstreetmapcz/1242
        // Croatia https://github.com/streetcomplete/StreetComplete/issues/3064#issuecomment-1157858833

        // was unable to reach communities or get replies from Kazakhstan, Moldova, Bosnia and Herzegovina,
        // Slovenia, Albania, Montenegro, Georgia (using what was on https://openstreetmap.community/ )
        // if you are sure that this quest should be enabled there or elsewhere
        // please create an issue at https://github.com/streetcomplete/StreetComplete/issues
    )
    override fun getTitle(tags: Map<String, String>) = R.string.quest_entrance_reference

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter { it.tags.containsKey("entrance") }.asSequence()

    override fun isApplicableTo(element: Element): Boolean? =
        if (element !is Node || (element.tags["entrance"] != "main" && element.tags["entrance"] != "staircase") ) false else null

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // note: it does not support multipolygon buildings
        val buildingWays = mapData.ways.asSequence()
            .filter { buildingFilter.matches(it) }
        val returned = mutableListOf<Node>()

        for (way in buildingWays) {
            val entrancesForApartments = mutableListOf<Node>()
            way.nodeIds.forEach { nodeId ->
                val node = mapData.getNode(nodeId)
                if (node != null) {
                    if (node.tags["entrance"] == "main" || node.tags["entrance"] == "staircase") {
                        entrancesForApartments.add(node)
                    }
                }
            }
            if (entrancesForApartments.count() >= 2) {
                for (entrance in entrancesForApartments) {
                    if (entrance.tags["noref"] == "yes") {
                        continue
                    }
                    if (entrance.tags["ref:signed"] == "no") {
                        continue
                    }
                    if (entrance.tags.containsKey("addr:flats")) {
                        continue
                    }
                    if (entrance.tags.containsKey("name")) {
                        continue
                    }
                    if (entrance.tags.containsKey("ref")) {
                        continue
                    }
                    returned.add(entrance)
                }
            }
        }
        return returned
    }

    override fun createForm() = AddEntranceReferenceForm()

    override fun applyAnswerTo(answer: EntranceAnswer, tags: Tags, timestampEdited: Long) {
        when (answer) {
            is FlatRange -> {
                tags["addr:flats"] = answer.flatRange
            }
            is ReferenceCode -> {
                tags["ref"] = answer.referenceCode
            }
            is ReferenceCodeAndFlatRange -> {
                tags["addr:flats"] = answer.flatRange
                tags["ref"] = answer.referenceCode
            }
            Unsigned -> {
                tags["ref:signed"] = "no"
            }
        }
    }
}
