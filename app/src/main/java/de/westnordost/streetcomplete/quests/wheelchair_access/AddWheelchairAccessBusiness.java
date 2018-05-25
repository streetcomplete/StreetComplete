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
				"amusement_arcade", "adult_gaming_centre", "tanning_salon",
				"playground", "stadium", "recreation_ground", "sauna",
				"fishing", "resort", "beach_resort", "bird_hide",
				"outdoor_seating", "bandstand", "club", "hackerspace",
				"swimming_area", "summer_camp", "social_club", "wildlife_hide",
				"pitch" };

		String[] amenities = {
				"restaurant", "cafe", "ice_cream", "fast_food",
				"bar", "pub", "biergarten", "food_court",
				"cinema", "nightclub", "bank",
				"bureau_de_change",	"money_transfer", "post_office", "library",
				"courthouse", "embassy", "car_wash", "car_rental",
				"marketplace", "fuel", "driving_school", "dentist",
				"doctors", "clinic", "pharmacy", "veterinary",
				"place_of_worship", "townhall", "school", "kindergarten",
				"shelter", "hospital", "police", "community_centre",
				"social_facility", "public_building", "university", "college",
				"theatre", "arts_centre", "bbq", "nursing_home",
				"ferry_terminal", "childcare", "shower", "prison", "studio",
				"casino", "internet_cafe", "public_bookcase", "brothel",
				"dojo", "gambling", "payment_terminal", "events_venue",
				"public_bath", "training", "social_centre", "animal_breeding",
				"animal_shelter", "retirement_home", "monastery", "sauna",
				"lavoir", "music_school", "animal_boarding", "ranger_station",
				"love_hotel", "crematorium", "gym", "stables", "stripclub",
				"dancing_school", "language_school", "photo_booth", "mortuary",
				"coworking_space", "customs", "conference_centre",
				"smoking_area", "payment_centre", "research_institute" };

		String[] tourism = {
				"zoo", "aquarium", "theme_park", "gallery", "museum", "hotel",
				"guest_house", "hostel", "motel", "viewpoint", "attraction",
				"chalet", "apartment", "resort" };


		return " nodes, ways, relations with ( shop and shop !~ no|vacant or" +
				" amenity ~ " + TextUtils.join("|", amenities) + " or" +
				" amenity = parking and parking = multi-storey or" +
				" amenity = recycling and recycling_type = centre or" +
				" tourism ~ " + TextUtils.join("|", tourism) + " or" +
				" tourism = information and information = office or" +
				" leisure ~ " + TextUtils.join("|",leisures)  +
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

	@Override public String getCommitMessage() { return "Add wheelchair access to businesses"; }
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
