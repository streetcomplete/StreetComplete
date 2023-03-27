package de.westnordost.streetcomplete.quests.railway_platform_ref

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.util.math.distanceTo

class AddRailwayPlatformRef : OsmElementQuestType<String> {

    // inspired by https://github.com/streetcomplete/StreetComplete/pull/4315 + comments
    private val platformFilter = """
        ways with
          public_transport = platform
          and railway ~ platform|platform_edge
          and !ref
          and !~ "ref:.*"
          and !local_ref
          and !name
          and noref != yes
          and tram != yes
          and subway != yes
          and light_rail != yes
          and monorail != yes
    """.toElementFilterExpression()

    private val railwayFilter = "ways with railway = rail".toElementFilterExpression()

    // this is somewhat slow if there are matching platforms and a lot of railways, but that's rather
    // uncommon
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val railwayLineGeometries = mapData.ways.mapNotNull {
            // only geometries that are "normal" railways
            if (railwayFilter.matches(it))
                (mapData.getWayGeometry(it.id) as? ElementPolylinesGeometry)?.polylines?.singleOrNull()
            else null
        }
        if (railwayLineGeometries.isEmpty()) return emptyList()

        // this also finds lines on non-matching levels / layers, but that's ok for now
        return mapData.ways.filter { platformFilter.matches(it) }.mapNotNull { platform ->
            val platformLineGeometry = mapData.getWayGeometry(platform.id)?.let {
                // only normal ways or areas
                if (it is ElementPolylinesGeometry)
                    it.polylines.singleOrNull()
                else (it as? ElementPolygonsGeometry)?.polygons?.singleOrNull()
            } ?: return@mapNotNull null
            var nearbyRailways = 0
            // we want at least two nearby railways, because for a single track there is no need for asking ref
            railwayLineGeometries.forEach {
                if (it.distanceTo(platformLineGeometry) < 10.0) {
                    nearbyRailways++
                    if (nearbyRailways > 1)
                        return@mapNotNull platform
                }
            }
            null
        }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (platformFilter.matches(element))
            null
        else
            false

    override val changesetComment = "Specify railway platform refs"
    override val wikiLink = "Tag:railway=platform"
    override val icon = R.drawable.ic_quest_railway_platform_ref
    override val achievements = listOf(EditTypeAchievement.CITIZEN)
    override val enabledInCountries = NoCountriesExcept("DE", "FR", "CH", "AT")
    override val defaultDisabledMessage = R.string.quest_disabled_msg_railway_platform_ref

    override fun getTitle(tags: Map<String, String>) = R.string.quest_railwayPlatformRef_title

    override fun createForm() = AddRailwayPlatformRefForm()

    // don't allow sth like not signed, because railway platforms should always be signed...
    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags[prefs.getString(PREF_KEY, "ref")!!] = answer
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context) =
        AlertDialog.Builder(context)
            .setMessage(R.string.quest_railwayPlatformRef_message)
            .setPositiveButton("local_ref") { _, _ -> prefs.edit { putString(PREF_KEY, "local_ref") }}
            .setNegativeButton("ref") { _, _ -> prefs.edit { remove(PREF_KEY) }}
            .create()
}

private const val PREF_KEY = "qs_AddRailwayPlatformRef_key"
