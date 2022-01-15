package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.deleteCheckDatesForKey
import de.westnordost.streetcomplete.data.meta.hasCheckDateForKey
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN

class AddRecyclingContainerMaterials : OsmElementQuestType<RecyclingContainerMaterialsAnswer> {

    private val filter by lazy { """
        nodes with
          amenity = recycling
          and recycling_type = container
          and access !~ private|no
    """.toElementFilterExpression() }

    override val commitMessage = "Add recycled materials to container"
    override val wikiLink = "Key:recycling"
    override val icon = R.drawable.ic_quest_recycling_container
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.nodes.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        /* Only recycling containers that do either not have any recycling:* tag yet or
         * haven't been touched for 2 years and are exclusively recycling types selectable in
         * StreetComplete. */
        filter.matches(element) &&
        (
            !element.hasAnyRecyclingMaterials() ||
            recyclingOlderThan2Years.matches(element) && !element.hasUnknownRecyclingMaterials()
        )

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_materials_title

    override fun createForm() = AddRecyclingContainerMaterialsForm()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with amenity = recycling")

    override fun applyAnswerTo(answer: RecyclingContainerMaterialsAnswer, changes: StringMapChangesBuilder) {
        if (answer is RecyclingMaterials) {
            applyRecyclingMaterialsAnswer(answer.materials, changes)
        } else if(answer is IsWasteContainer) {
            applyWasteContainerAnswer(changes)
        }
    }

    private fun applyRecyclingMaterialsAnswer(materials: List<RecyclingMaterial>, changes: StringMapChangesBuilder) {
        // set selected recycling:* taggings to "yes"
        val selectedMaterials = materials.map { "recycling:${it.value}" }
        for (acceptedMaterial in selectedMaterials) {
            changes.addOrModify(acceptedMaterial, "yes")
        }

        // if the user chose deliberately not "all plastic", also tag it explicitly
        val anyPlastic = listOf("recycling:plastic", "recycling:plastic_packaging", "recycling:plastic_bottles", "recycling:beverage_cartons")
        when {
            selectedMaterials.contains("recycling:plastic") -> {
                changes.deleteIfExists("recycling:plastic_packaging")
                changes.deleteIfExists("recycling:plastic_bottles")
                changes.deleteIfExists("recycling:beverage_cartons")
            }
            selectedMaterials.contains("recycling:plastic_packaging") -> {
                changes.addOrModify("recycling:plastic", "no")
                changes.deleteIfExists("recycling:plastic_bottles")
                changes.deleteIfExists("recycling:beverage_cartons")
            }
            selectedMaterials.contains("recycling:beverage_cartons") &&
            selectedMaterials.contains("recycling:plastic_bottles") -> {
                changes.addOrModify("recycling:plastic_packaging", "no")
                changes.addOrModify("recycling:plastic", "no")
            }
            selectedMaterials.contains("recycling:beverage_cartons") -> {
                changes.addOrModify("recycling:plastic_bottles", "no")
                changes.addOrModify("recycling:plastic_packaging", "no")
                changes.addOrModify("recycling:plastic", "no")
            }
            selectedMaterials.contains("recycling:plastic_bottles") -> {
                changes.addOrModify("recycling:beverage_cartons", "no")
                changes.addOrModify("recycling:plastic_packaging", "no")
                changes.addOrModify("recycling:plastic", "no")
            }
            else -> {
                changes.deleteIfExists("recycling:plastic")
                changes.deleteIfExists("recycling:plastic_packaging")
                changes.deleteIfExists("recycling:plastic_bottles")
                changes.deleteIfExists("recycling:beverage_cartons")
            }
        }

        // remove recycling:* taggings previously "yes" but now not any more
        val materialsNotSelectedAnymore = changes.getPreviousEntries().filter { (key, value) ->
            !selectedMaterials.contains(key)
            // don't touch any previous explicit recycling:*=no taggings
            && value == "yes"
            // leave plastic values alone because it is managed separately (see above)
            && !anyPlastic.contains(key)
        }.keys
        for (notAcceptedMaterial in materialsNotSelectedAnymore) {
            changes.delete(notAcceptedMaterial)
        }

        // only set the check date if nothing was changed
        val isNotActuallyChangingAnything = changes.getChanges().all { change ->
            change is StringMapEntryModify && change.value == change.valueBefore
        }
        if (isNotActuallyChangingAnything || changes.hasCheckDateForKey("recycling")) {
            changes.updateCheckDateForKey("recycling")
        }
    }

    private fun applyWasteContainerAnswer(changes: StringMapChangesBuilder) {
        changes.modify("amenity","waste_disposal")
        changes.delete("recycling_type")

        val previousRecyclingKeys = changes.getPreviousEntries().keys.filter { it.startsWith("recycling:") }
        for (previousRecyclingKey in previousRecyclingKeys) {
            changes.delete(previousRecyclingKey)
        }
        changes.deleteCheckDatesForKey("recycling")
    }
}

private val recyclingOlderThan2Years =
    TagOlderThan("recycling", RelativeDate(-(365 * 2).toFloat()))

private val allKnownMaterials = RecyclingMaterial.values().map { "recycling:" + it.value }

private fun Element.hasAnyRecyclingMaterials(): Boolean =
    tags.any { it.key.startsWith("recycling:") && it.value == "yes" }

private fun Element.hasUnknownRecyclingMaterials(): Boolean =
    tags.any {
        it.key.startsWith("recycling:") &&
        it.key !in allKnownMaterials &&
        it.value == "yes"
    }
