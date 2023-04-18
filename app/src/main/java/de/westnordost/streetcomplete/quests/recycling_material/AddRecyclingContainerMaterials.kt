package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.BEVERAGE_CARTONS
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PET
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC_BOTTLES
import de.westnordost.streetcomplete.quests.recycling_material.RecyclingMaterial.PLASTIC_PACKAGING

class AddRecyclingContainerMaterials : OsmElementQuestType<RecyclingContainerMaterialsAnswer> {

    private val filter by lazy { """
        nodes, ways with
          amenity = recycling
          and recycling_type = container
          and access !~ private|no
          and !note and !recycling:note and !note:recycling
    """.toElementFilterExpression() }

    override val changesetComment = "Specify what can be recycled in recycling containers"
    override val wikiLink = "Key:recycling"
    override val icon = R.drawable.ic_quest_recycling_container
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_materials_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.nodes.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        /* Only recycling containers that do either not have any recycling:* tag yet or
         * haven't been touched for 2 years and are exclusively recycling types selectable in
         * StreetComplete. */
        filter.matches(element) && (
            !element.hasAnyRecyclingMaterials()
            || recyclingOlderThan2Years.matches(element) && !element.hasUnknownRecyclingMaterials()
        )

    override fun createForm() = AddRecyclingContainerMaterialsForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

    override fun applyAnswerTo(answer: RecyclingContainerMaterialsAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer is RecyclingMaterials) {
            applyRecyclingMaterialsAnswer(answer.materials, tags)
        } else if (answer is IsWasteContainer) {
            applyWasteContainerAnswer(tags)
        }
    }

    private fun applyRecyclingMaterialsAnswer(materials: List<RecyclingMaterial>, tags: Tags) {
        // first clear recycling:* taggings previously "yes"
        for ((key, value) in tags.entries) {
            if (key.startsWith("recycling:") && value == "yes") {
                tags.remove(key)
            }
        }

        // if the user chose deliberately not "all plastic", also tag it explicitly
        if (materials.any { it in RecyclingMaterial.plastics }) {
            for (plastic in RecyclingMaterial.plastics) {
                tags.remove("recycling:${plastic.value}")
            }
            when {
                PLASTIC_PACKAGING in materials -> {
                    tags["recycling:plastic"] = "no"
                }
                BEVERAGE_CARTONS in materials && PLASTIC_BOTTLES in materials -> {
                    tags["recycling:plastic_packaging"] = "no"
                    tags["recycling:plastic"] = "no"
                }
                BEVERAGE_CARTONS in materials -> {
                    tags["recycling:plastic_bottles"] = "no"
                    tags["recycling:plastic_packaging"] = "no"
                    tags["recycling:plastic"] = "no"
                }
                PLASTIC_BOTTLES in materials -> {
                    tags["recycling:beverage_cartons"] = "no"
                    tags["recycling:plastic_packaging"] = "no"
                    tags["recycling:plastic"] = "no"
                }
                PET in materials -> {
                    tags["recycling:plastic_bottles"] = "no"
                    tags["recycling:beverage_cartons"] = "no"
                    tags["recycling:plastic_packaging"] = "no"
                    tags["recycling:plastic"] = "no"
                }
            }
        }

        // set selected recycling:* taggings to "yes"
        val selectedMaterials = materials.map { "recycling:${it.value}" }
        for (material in selectedMaterials) {
            tags[material] = "yes"
        }

        // only set the check date if nothing was changed
        if (!tags.hasChanges || tags.hasCheckDateForKey("recycling")) {
            tags.updateCheckDateForKey("recycling")
        }
    }

    private fun applyWasteContainerAnswer(tags: Tags) {
        tags["amenity"] = "waste_disposal"
        tags.remove("recycling_type")

        val recyclingKeys = tags.keys.filter { it.startsWith("recycling:") }
        for (key in recyclingKeys) {
            tags.remove(key)
        }
        tags.removeCheckDatesForKey("recycling")
    }
}

private val recyclingOlderThan2Years =
    TagOlderThan("recycling", RelativeDate(-(365 * 2).toFloat()))

private val allKnownMaterials = RecyclingMaterial.values().map { "recycling:" + it.value }

private fun Element.hasAnyRecyclingMaterials(): Boolean =
    tags.any { it.key.startsWith("recycling:") && it.value == "yes" }

private fun Element.hasUnknownRecyclingMaterials(): Boolean =
    tags.any {
        it.key.startsWith("recycling:")
        && it.key !in allKnownMaterials
        && it.value == "yes"
    }
