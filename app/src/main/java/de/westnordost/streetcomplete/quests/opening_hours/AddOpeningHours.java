package de.westnordost.streetcomplete.quests.opening_hours;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;

public class AddOpeningHours extends SimpleOverpassQuestType
{
	@Inject public AddOpeningHours(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

    @Override protected String getTagFilters()
	{
		String[] leisures = {
				"golf_course", "water_park", "miniature_golf", "dance",
				"bowling_alley", "horse_riding", "sports_centre", "fitness_centre",
				"amusement_arcade", "adult_gaming_centre", "tanning_salon" };

		String[] amenities = {
				"restaurant", "cafe", "ice_cream", "fast_food",
				"bar", "pub", "biergarten", "food_court",
				"cinema", "nightclub", "bank", "atm",
				"bureau_de_change",	"money_transfer", "post_office", "library",
				"courthouse", "embassy", "car_wash", "car_rental",
				"marketplace", "fuel", "driving_school", "dentist",
				"doctors", "clinic", "pharmacy", "veterinary" };

		String[] tourism = {
				"zoo", "aquarium", "theme_park", "gallery",
				"museum"
		};

		return " nodes, ways, relations with ( shop and shop !~ no|vacant or" +
				" amenity ~ " + TextUtils.join("|", amenities) + " or" +
				" amenity = bicycle_parking and bicycle_parking = building or" +
				" amenity = parking and parking = multi-storey or" +
				" amenity = recycling and recycling_type = centre or" +
				" tourism ~ " + TextUtils.join("|", tourism) + " or" +
				" tourism = information and information = office or" +
				" leisure ~ " + TextUtils.join("|",leisures) + ")" +
				" and !opening_hours and name";
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddOpeningHoursForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String openingHours = answer.getString(AddOpeningHoursForm.OPENING_HOURS);
		if(openingHours != null)
		{
			changes.add("opening_hours", openingHours);
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add opening hours";
	}

	@Override public String getIconName() {	return "opening_hours"; }
}
