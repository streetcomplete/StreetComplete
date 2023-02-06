package de.westnordost.streetcomplete.quests.healthcare_speciality

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.Tags

class AddHealthcareSpeciality : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
         amenity = doctors
         and name and !healthcare:speciality
    """
    override val changesetComment = "Add healthcare specialities"
    override val wikiLink = "Key:healthcare:speciality"
    override val icon = R.drawable.ic_quest_healthcare_speciality
    override val defaultDisabledMessage = R.string.quest_healthcare_speciality_disabled_message

    override fun getTitle(tags: Map<String, String>) = R.string.quest_healthcare_speciality_title

    override fun createForm() = MedicalSpecialityTypeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["healthcare:speciality"] = answer
    }
}
