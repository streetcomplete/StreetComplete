package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle
import android.text.TextUtils

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment

class AddRoadSurface @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = " ways with highway ~ " + TextUtils.join("|", ROADS_WITH_SURFACES) + " and" +
                " !surface and (access !~ private|no or (foot and foot !~ private|no))"

    override val commitMessage: String
        get() = "Add road surfaces"
    override val icon: Int
        get() = R.drawable.ic_quest_street_surface

    override fun createForm(): AbstractQuestAnswerFragment {
        return AddRoadSurfaceForm()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("surface", answer.getString(GroupedImageListQuestAnswerFragment.OSM_VALUE)!!)
    }

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        val isSquare = tags.containsKey("area") && tags["area"] == "yes"
        return if (hasName) {
            if (isSquare)
                R.string.quest_streetSurface_square_name_title
            else
                R.string.quest_streetSurface_name_title
        } else {
            if (isSquare)
                R.string.quest_streetSurface_square_title
            else
                R.string.quest_streetSurface_title
        }
    }

    companion object {
        // well, all roads have surfaces, what I mean is that not all ways with highway key are
        // "something with a surface"
        private val ROADS_WITH_SURFACES = arrayOf(
            // "trunk","trunk_link","motorway","motorway_link", // too much, motorways are almost by definition asphalt (or concrete)
            "primary",
            "primary_link",
            "secondary",
            "secondary_link",
            "tertiary",
            "tertiary_link",
            "unclassified",
            "residential",
            "living_street",
            "pedestrian",
            "track",
            "road"
        )/*"service", */// this is too much, and the information value is very low
    }
}
