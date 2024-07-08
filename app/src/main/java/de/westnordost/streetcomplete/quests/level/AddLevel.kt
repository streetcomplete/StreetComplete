package de.westnordost.streetcomplete.quests.level

import androidx.appcompat.app.AlertDialog
import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestController
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.quests.questPrefix
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isInMultipolygon

class AddLevel : OsmElementQuestType<String> {

    /* including any kind of public transport station because even really large bus stations feel
     * like small airport terminals, like Mo Chit 2 in Bangkok*/
    private val mallFilter by lazy { """
        ways, relations with
         shop = mall
         or aeroway = terminal
         or railway = station
         or amenity = bus_station
         or public_transport = station
         ${if (prefs.getBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, false)) "or (building and building:levels != 1 and building !~ roof|house|detached|carport)" else ""}
    """.toElementFilterExpression() }

    private val thingsWithLevelOrDoctorsFilter by lazy { """
        nodes, ways, relations with level
        ${if (prefs.getBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, false)) """
            or (
              amenity ~ doctors|dentist
              or healthcare ~ doctor|dentist|psychotherapist|physiotherapist
            ) """
        else ""}
    """.toElementFilterExpression() }

    /* only nodes because ways/relations are not likely to be floating around freely in a mall
     * outline */
    private val shopsAndMoreFilter by lazy { """
        nodes with
         (
           (shop and shop !~ no|vacant|mall)
           or craft
           or (amenity and amenity !~ parking|parking_entrance)
           or leisure
           or office
           or tourism
           or healthcare
           or (man_made = surveillance and surveillance:type = camera)
         )
         and !level
    """.toElementFilterExpression()}

    private val shopFilter by lazy { """
        nodes with
          !level
          and (name or brand or noname = yes or name:signed = no)
    """.toElementFilterExpression() }

    override val changesetComment = "Determine on which level elements are in a building"
    override val wikiLink = "Key:level"
    override val icon = R.drawable.ic_quest_level
    /* disabled because in a mall with multiple levels, if there are nodes with no level defined,
     * it really makes no sense to tag something as vacant if the level is not known. Instead, if
     * the user cannot find the place on any level in the mall, delete the element completely. */
    override val isReplacePlaceEnabled = false
    override val isDeleteElementEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_level_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val moreLevels = prefs.getBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, false)
        // get all shops that have no level tagged
        val shopsWithoutLevel = if (moreLevels) mapData.filter { shopsAndMoreFilter.matches(it) }.toMutableList()
            else mapData.filter { shopFilter.matches(it) && it.isPlace() }.toMutableList()
        if (shopsWithoutLevel.isEmpty()) return emptyList()

        val result = mutableListOf<Element>()
        if (moreLevels) {
            // add doctors, independent of the building they're in
            // and remove them from shops without level
            shopsWithoutLevel.removeAll {
                if (it.isDoctor()) result.add(it)
                else false
            }
        }
        if (shopsWithoutLevel.isEmpty()) return emptyList()

        // get geometry of all malls (or buildings) in the area
        val mallGeometries = mapData
            .filter { mallFilter.matches(it) }
            .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }
            .toMutableList()
        if (mallGeometries.isEmpty()) return result

        // get all shops that have level tagged or are doctors
        val thingsWithLevel = mapData.filter { thingsWithLevelOrDoctorsFilter.matches(it) }
        if (thingsWithLevel.isEmpty()) return result

        // with this, find malls that contain shops that have different levels tagged
        mallGeometries.retainAll { mallGeometry ->
            var level: String? = null
            for (shop in thingsWithLevel) {
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.getBounds().contains(pos)) continue // crude filter first for performance reasons
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                if (shop.tags.containsKey("level")) {
                    if (level != null) {
                        if (level != shop.tags["level"]) return@retainAll true
                    } else {
                        level = shop.tags["level"]
                    }
                }
            }
            return@retainAll false
        }
        if (mallGeometries.isEmpty()) return result

        // find places inside remaining mallGeometries, but not on outline
        for (mallGeometry in mallGeometries) {
            val it = shopsWithoutLevel.iterator()
            val mallNodePositions = mallGeometry.polygons.flatten().toHashSet()
            while (it.hasNext()) {
                val shop = it.next()
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.getBounds().contains(pos)) continue
                if (mallNodePositions.contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(shop)
                it.remove() // shop can only be in one mall
            }
        }
        return result
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (prefs.getBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, false)) {
            if (!shopsAndMoreFilter.matches(element)) return false
        } else {
            if (!shopFilter.matches(element) || !element.isPlace()) return false
        }
        // doctors are frequently at non-ground level
        if (element.isDoctor() && prefs.getBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, false) && !element.tags.containsKey("level")) return true
        // for shops with no level, we actually need to look at geometry in order to find if it is
        // contained within any multi-level mall
        return null
    }

    private fun Element.isDoctor() = tags["amenity"] in doctorAmenity || tags["healthcare"] in doctorHealthcare

    override fun createForm() = AddLevelForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["level"] = answer
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        AlertDialog.Builder(context)
            .setTitle(R.string.quest_settings_level_title)
            .setNegativeButton(android.R.string.cancel, null)
            .setItems(
                arrayOf(context.getString(R.string.quest_settings_level_default), context.getString(R.string.quest_settings_level_more))
            ) { _, i ->
                if (i == 1) prefs.edit().putBoolean(questPrefix(prefs) + PREF_MORE_LEVELS, true).apply()
                    else prefs.edit().remove(questPrefix(prefs) + PREF_MORE_LEVELS).apply()
                OsmQuestController.reloadQuestTypes()
            }
            .create()

}

private val doctorAmenity = hashSetOf("doctors", "dentist")
private val doctorHealthcare = hashSetOf("doctor", "dentist", "psychotherapist", "physiotherapist")

const val PREF_MORE_LEVELS = "qs_AddLevel_more_levels"
