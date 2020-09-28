package de.westnordost.streetcomplete.quests.recycling_material

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.filters.RelativeDate
import de.westnordost.streetcomplete.data.elementfilter.filters.TagOlderThan
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.elementfilter.toGlobalOverpassBBox
import de.westnordost.streetcomplete.data.meta.deleteCheckDatesForKey
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryModify
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddRecyclingContainerMaterials(
    private val overpassApi: OverpassMapDataAndGeometryApi,
    private val r: ResurveyIntervalsStore
) : OsmElementQuestType<RecyclingContainerMaterialsAnswer> {

    override val commitMessage = "Add recycled materials to container"
    override val wikiLink = "Key:recycling"
    override val icon = R.drawable.ic_quest_recycling_container

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        return overpassApi.query(getOverpassQuery(bbox), handler)
    }

    private val allKnownMaterials = RecyclingMaterial.values().map { "recycling:" + it.value }

    /* Return recycling containers that do either not have any recycling:* tag yet, or if they do,
    *  haven't been touched for 2 years and are exclusively recycling types selectable in
    *  StreetComplete.
    *  Also, exclude recycling containers right next to another because the user can't know if
    *  certain materials are already recycled in that other container */
    private fun getOverpassQuery(bbox: BoundingBox) = """
        ${bbox.toGlobalOverpassBBox()}
        node[amenity = recycling][recycling_type = container] -> .all;
        
        node.all[~"^recycling:.*$" ~ "yes"] -> .with_recycling;
        (.all; - .with_recycling;) -> .without_recycling;
        
        node.with_recycling[~"^(${allKnownMaterials.joinToString("|")})$" ~ "yes" ] -> .with_known_recycling;
        (.with_recycling; - .with_known_recycling;) -> .with_unknown_recycling;
        
        node.all${olderThan(2).toOverpassQLString()} -> .old;
        
        (.without_recycling; (.old; - .with_unknown_recycling;);) -> .recyclings;
        
        foreach .recyclings (
            node[amenity = recycling][recycling_type = container](around: 20);
            node._(if:count(nodes) == 1);
            out meta geom;
        );
    """.trimIndent()

    // can't determine by tags alone because we need info about geometry surroundings
    override fun isApplicableTo(element: Element): Boolean? = null

    private fun olderThan(years: Int) =
        TagOlderThan("recycling", RelativeDate(-(r * 365 * years).toFloat()))

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
