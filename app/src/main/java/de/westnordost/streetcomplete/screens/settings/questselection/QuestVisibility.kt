package de.westnordost.streetcomplete.screens.settings.questselection

import android.content.SharedPreferences
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.othersource.OtherSourceQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quests.bench_material.AddBenchMaterial
import de.westnordost.streetcomplete.quests.contact.AddContactPhone
import de.westnordost.streetcomplete.quests.contact.AddContactWebsite
import de.westnordost.streetcomplete.quests.cuisine.AddCuisine
import de.westnordost.streetcomplete.quests.healthcare_speciality.AddHealthcareSpeciality
import de.westnordost.streetcomplete.quests.seating.AddOutdoorSeatingType
import de.westnordost.streetcomplete.quests.service_building.AddServiceBuildingOperator
import de.westnordost.streetcomplete.quests.service_building.AddServiceBuildingType
import de.westnordost.streetcomplete.quests.tree.AddTreeGenus

data class QuestVisibility(val questType: QuestType, var visible: Boolean, val prefs: SharedPreferences) {
    val isInteractionEnabled = prefs.getBoolean(Prefs.EXPERT_MODE, false)
        || (questType !is OsmNoteQuestType
            && questType !is OtherSourceQuestType
            && questType !is AddBenchMaterial
            && questType !is AddContactPhone
            && questType !is AddContactWebsite
            && questType !is AddCuisine
            && questType !is AddHealthcareSpeciality
            && questType !is AddOutdoorSeatingType
            && questType !is AddServiceBuildingOperator
            && questType !is AddServiceBuildingType
            && questType !is AddTreeGenus
            && questType.dotColor == "no"
        )
}
