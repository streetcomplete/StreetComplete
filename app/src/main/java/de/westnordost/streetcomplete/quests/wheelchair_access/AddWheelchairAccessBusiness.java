package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.text.TextUtils;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;


public class AddWheelchairAccessBusiness extends SimpleOverpassQuestType
{
	@Inject public AddWheelchairAccessBusiness(OverpassMapDataDao overpassServer)
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
				"doctors", "clinic", "pharmacy", "veterinary",
				"place_of_worship", "townhall"};

		String[] tourism = {
				"zoo", "aquarium", "theme_park", "gallery",
				"museum", "hotel", "guest_house", "hostel",
				"motel", "viewpoint"
		};


		return " nodes, ways, relations with ( shop and shop !~ no|vacant or" +
				" amenity ~ " + TextUtils.join("|", amenities) + " or" +
				" amenity = bicycle_parking and bicycle_parking = building or" +
				" amenity = parking and parking = multi-storey or" +
				" amenity = recycling and recycling_type = centre or" +
				" tourism ~ " + TextUtils.join("|", tourism) + " or" +
				" tourism = information and information = office or" +
				" leisure ~ " + TextUtils.join("|",leisures)  + " or"+
				" historic = memorial)" +
				" and !wheelchair and name";
	}

	@Override public int importance()
	{
		return QuestImportance.MINOR;
	}

	@Override public WheelchairAccessAnswerFragment createForm()
	{
		return new AddWheelchairAccessBusinessForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String wheelchair = answer.getString(AddWheelchairAccessBusinessForm.ANSWER);
		if(wheelchair != null)
		{
			changes.add("wheelchair", wheelchair);
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add wheelchair access to businesses";
	}

	@Override public String getIconName() {	return "wheelchair_shop"; }
}
