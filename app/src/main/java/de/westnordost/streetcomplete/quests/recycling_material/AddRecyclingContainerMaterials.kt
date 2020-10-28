package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.deleteCheckDatesForKey
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.util.LatLonRaster
import de.westnordost.streetcomplete.util.distanceTo
import de.westnordost.streetcomplete.util.enclosingBoundingBox

class AddRecyclingContainerMaterials : OsmElementQuestType<RecyclingContainerMaterialsAnswer> {

    private val filter by lazy { """
        nodes with 
          amenity = recycling and recycling_type = container
    """.toElementFilterExpression() }

    override val commitMessage = "Add recycled materials to container"
    override val wikiLink = "Key:recycling"
    override val icon = R.drawable.ic_quest_recycling_container

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val bbox = mapData.boundingBox ?: return emptyList()

        val olderThan2Years = TagOlderThan("recycling", RelativeDate(-(365 * 2).toFloat()))

        val containers = mapData.nodes.filter { filter.matches(it) }

        /* Only recycling containers that do either not have any recycling:* tag yet or
         * haven't been touched for 2 years and are exclusively recycling types selectable in
         * StreetComplete. */
        val eligibleContainers = containers.filter {
            !it.hasAnyRecyclingMaterials() ||
            (olderThan2Years.matches(it) && !it.hasUnknownRecyclingMaterials())
        }
        /* Also, exclude recycling containers right next to another because the user can't know if
         * certain materials are already recycled in that other container */
        val containerPositions = LatLonRaster(bbox, 0.0005)
        for (container in containers) {
            containerPositions.insert(container.position)
        }

        val minDistance = 20.0
        return eligibleContainers.filter { container ->
            val nearbyBounds = container.position.enclosingBoundingBox(minDistance)
            val nearbyContainerPositions = containerPositions.getAll(nearbyBounds)
            // only finds one position = only found self -> no other container is near
            nearbyContainerPositions.count { container.position.distanceTo(it) <= minDistance } == 1
        }
    }

    // can't determine by tags alone because we need info about geometry surroundings
    override fun isApplicableTo(element: Element): Boolean? = null

    override fun getTitle(tags: Map<String, String>) = R.string.quest_recycling_materials_title

    override fun createForm() = AddRecyclingContainerMaterialsForm()

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
        val anyPlastic = listOf("recycling:plastic", "recycling:plastic_packaging", "recycling:plastic_bottles")
        when {
            selectedMaterials.contains("recycling:plastic_bottles") -> {
                changes.addOrModify("recycling:plastic_packaging", "no")
                changes.addOrModify("recycling:plastic", "no")
            }
            selectedMaterials.contains("recycling:plastic_packaging") -> {
                changes.addOrModify("recycling:plastic", "no")
                changes.deleteIfExists("recycling:plastic_bottles")
            }
            selectedMaterials.contains("recycling:plastic") -> {
                changes.deleteIfExists("recycling:plastic_packaging")
                changes.deleteIfExists("recycling:plastic_bottles")
            }
            else -> {
                changes.deleteIfExists("recycling:plastic")
                changes.deleteIfExists("recycling:plastic_packaging")
                changes.deleteIfExists("recycling:plastic_bottles")
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
        if (isNotActuallyChangingAnything) {
            changes.updateCheckDateForKey("recycling")
        } else {
            changes.deleteCheckDatesForKey("recycling")
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

private val allKnownMaterials = RecyclingMaterial.values().map { "recycling:" + it.value }

private fun Element.hasAnyRecyclingMaterials(): Boolean =
    tags?.any { it.key.startsWith("recycling:") && it.value == "yes" } ?: false

private fun Element.hasUnknownRecyclingMaterials(): Boolean =
    tags?.any {
        it.key.startsWith("recycling:") &&
        it.key !in allKnownMaterials &&
        it.value == "yes"
    } ?: true