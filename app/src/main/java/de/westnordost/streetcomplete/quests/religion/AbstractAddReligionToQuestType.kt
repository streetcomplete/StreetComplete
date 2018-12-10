package de.westnordost.streetcomplete.quests.religion

import android.os.Bundle

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

abstract class AbstractAddReligionToQuestType(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override fun createForm() = AddReligionToPlaceOfWorshipForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val values = answer.getStringArrayList(AddReligionToPlaceOfWorshipForm.OSM_VALUES)
        if (values != null && !values.isEmpty()) {
            val religionValueStr = values[0]
            changes.add("religion", religionValueStr)
        }
    }
}
