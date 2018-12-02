package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
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
				"amusement_arcade", "adult_gaming_centre", "tanning_salon"
		};

		String[] amenities = {
				"restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub",
				"cinema", "library", "theatre", "arts_centre", "casino", "conference_centre",
				"bank", "bureau_de_change", "money_transfer", "post_office", "internet_cafe", "marketplace",
				"police", "ranger_station", "courthouse", "embassy", "townhall", "community_centre", "youth_centre",
				"car_wash", "car_rental", "fuel", "driving_school",
				"doctors", "clinic", "pharmacy", "veterinary", "dentist",
				"place_of_worship",
		};

		String[] tourism = {
				"zoo", "aquarium", "theme_park", "gallery", "attraction", "viewpoint",
				"museum", "hotel", "guest_house", "hostel", "motel", "apartment", "chalet",
		};

		String[] offices = {
			"insurance", "government", "lawyer", "estate_agent", "political_party", "travel_agent",
			"tax_advisor", "therapist",
		};

		return " nodes, ways, relations with ( shop and shop !~ no|vacant or" +
				" amenity ~ " + TextUtils.join("|", amenities) + " or" +
				" amenity = parking and parking = multi-storey or" +
				" amenity = recycling and recycling_type = centre or" +
				" tourism ~ " + TextUtils.join("|", tourism) + " or" +
				" tourism = information and information = office or" +
				" leisure ~ " + TextUtils.join("|",leisures) + " or" +
				" office ~ " + TextUtils.join("|",offices) +
				" ) and !wheelchair and name";
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

	@Override public String getCommitMessage() { return "Add wheelchair access"; }
	@Override public int getIcon() { return R.drawable.ic_quest_wheelchair_shop; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_wheelchairAccess_name_title;
	}

	@Override public int getDefaultDisabledMessage()
	{
		return R.string.default_disabled_msg_go_inside;
	}
}
