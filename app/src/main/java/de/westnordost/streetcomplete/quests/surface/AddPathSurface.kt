package de.westnordost.streetcomplete.quests.surface

import android.os.Bundle

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.GroupedImageListQuestAnswerFragment

class AddPathSurface @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = " ways with highway ~ path|footway|cycleway|bridleway|steps and" + " !surface and access !~ private|no"

    override val commitMessage: String
        get() = "Add path surfaces"
    override val icon: Int
        get() = R.drawable.ic_quest_way_surface

    override fun createForm(): AbstractQuestAnswerFragment {
        return AddPathSurfaceForm()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("surface", answer.getString(GroupedImageListQuestAnswerFragment.OSM_VALUE)!!)
    }

    override fun getTitle(tags: Map<String, String>): Int {
        val isSquare = tags.containsKey("area") && tags["area"] == "yes"
        val pathType = tags["highway"]
        if (isSquare) return R.string.quest_streetSurface_square_title
        if ("bridleway" == pathType) return R.string.quest_pathSurface_title_bridleway
        return if ("steps" == pathType) R.string.quest_pathSurface_title_steps else R.string.quest_pathSurface_title
        // rest is rather similar, can be called simply "path"
    }
}
