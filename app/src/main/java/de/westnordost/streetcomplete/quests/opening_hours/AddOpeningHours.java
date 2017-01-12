package de.westnordost.streetcomplete.quests.opening_hours;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;

import de.westnordost.streetcomplete.R;


public class AddOpeningHours extends OverpassQuestType
{
    @Override protected String getTagFilters()
	{
		return " elements with ( shop and shop !~ no|vacant or" +
               " amenity ~ restaurant|cafe|ice_cream|fast_food|bar|pub|biergarten|cinema|nightclub|" +
                          "bank|atm|post_office|library|courthouse|embassy|" +
                          "car_wash|car_rental|marketplace|fuel|" +
		                  "dentist|doctors|clinic|pharmacy|veterinary or" +
               " amenity = bicycle_parking and bicycle_parking = building or" +
               " amenity = parking and parking = multi-storey or" +
               " amenity = recycling and recycling_type = centre or" +
               " tourism ~ zoo|theme_park|gallery|museum or" +
               " tourism = information and information = office or" +
               " leisure ~ golf_course|water_park|miniature_golf )" +
               " and !opening_hours";
	}

    @Override public int importance()
    {
        return QuestImportance.MINOR;
    }

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddOpeningHoursForm();
	}

	@Override public Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String openingHours = answer.getString(AddOpeningHoursForm.OPENING_HOURS);
		if(openingHours != null)
		{
			changes.add("opening_hours", openingHours);
			return R.string.quest_openingHours_commitMessage;
		}
		return null;
	}

	@Override public String getIconName() {	return "opening_hours"; }
}
