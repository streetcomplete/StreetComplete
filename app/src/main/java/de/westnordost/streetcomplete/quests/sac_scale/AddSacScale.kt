package de.westnordost.streetcomplete.quests.sac_scale

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.getPrefixedFullElementSelectionPref

private const val PREF_SAC_SCALE_WITHOUT_RELATION = "quest_sac_scale_without_relation"

class AddSacScale : OsmElementQuestType<SacScale> {

    private val elementFilter = """
        ways with
          highway ~ path
          and !sac_scale
          and access !~ no|private
          and foot !~ no|private
          and (!lit or lit = no)
          and surface ~ "grass|sand|dirt|soil|fine_gravel|compacted|wood|gravel|pebblestone|rock|ground|earth|mud|woodchips|snow|ice|salt|stone"
    """
    val filter by lazy {
        prefs.getString(getPrefixedFullElementSelectionPref(prefs), elementFilter)!!
            .toElementFilterExpression()
    }

    override val changesetComment = "Specify SAC Scale"
    override val wikiLink = "Key:sac_scale"
    override val icon = R.drawable.ic_quest_sac_scale
    override val defaultDisabledMessage = R.string.default_disabled_msg_sacScale

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sacScale_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        if (isSacScaleWithoutRelation) {
            mapData.filter(filter).asIterable()
        } else {
            mapData.relations.filter {
                it.tags["route"] == "hiking"
            }.map {
                mapData.getAllWayInRelation(it.id).filter { way ->
                    filter.matches(way)
                }
            }.flatten()
        }


    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element)

    override fun getHighlightedElements(
        element: Element,
        getMapData: () -> MapDataWithGeometry
    ) = getMapData().filter("ways with highway and sac_scale")

    override fun createForm() = AddSacScaleForm()

    override fun applyAnswerTo(
        answer: SacScale,
        tags: Tags,
        geometry: ElementGeometry,
        timestampEdited: Long
    ) {
        tags["sac_scale"] = answer.osmValue
    }

    override val hasQuestSettings: Boolean = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setMessage(R.string.pref_quest_sac_scale_without_relation)
            .setPositiveButton(R.string.quest_generic_hasFeature_yes) { _, _ ->
                prefs.edit().putBoolean(PREF_SAC_SCALE_WITHOUT_RELATION, true).apply()
            }
            .setNegativeButton(R.string.quest_generic_hasFeature_no) { _, _ ->
                prefs.edit().putBoolean(PREF_SAC_SCALE_WITHOUT_RELATION, false).apply()
            }
            .setNeutralButton(R.string.quest_settings_reset) { _, _ ->
                prefs.edit { remove(PREF_SAC_SCALE_WITHOUT_RELATION) }
            }
            .create()

    private val isSacScaleWithoutRelation
        get() = prefs.getBoolean(PREF_SAC_SCALE_WITHOUT_RELATION, false)


    private fun MapData.getAllWayInRelation(id: Long): List<Way> {
        val mutableList = mutableListOf<Way>()

        getRelation(id)?.members?.forEach { member ->
            when (member.type) {
                ElementType.NODE -> Unit
                ElementType.WAY -> getWay(member.ref)?.let { mutableList.add(it) }

                ElementType.RELATION -> mutableList.addAll(getAllWayInRelation(member.ref))
            }
        }
        return mutableList
    }
}
