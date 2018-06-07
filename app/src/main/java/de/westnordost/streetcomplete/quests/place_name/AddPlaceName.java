package de.westnordost.streetcomplete.quests.place_name;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Hashtable;
import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddPlaceName extends SimpleOverpassQuestType
{

	// not all amenities have names. Rather have an inclusive list here than an exclusive one. This
	// list goes down to a few hundred (documented) elements in taginfo (Mar 2017)

	private static final Hashtable<String, String[]> OBJECTS_WITH_NAMES = new Hashtable<String,String[]>() {{
		put("amenity", new String[]{
			"restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub",        // eat & drink
			"cinema", "theatre", "planetarium", "arts_centre", "studio",                                            // culture
			"events_venue", "conference_centre", "exhibition_centre", "music_venue",                                // events
			"townhall", "prison", "courthouse", "embassy", "police", "fire_station", "ranger_station",                // civic
			"bank", "bureau_de_change", "money_transfer", "post_office", "library", "marketplace", "internet_cafe", // commercial
			"community_centre", "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre",    // social
			"car_wash", "car_rental", "boat_rental", "fuel",                                                        // car stuff
			"dentist", "doctors", "clinic", "pharmacy", "hospital",                                                    // health care
			"place_of_worship", "monastery",                                                                        // religious
			"kindergarten", "school", "college", "university", "research_institute",                                // education
			"driving_school", "dive_centre", "language_school", "music_school",                                    // learning
			"casino", "brothel", "gambling", "love_hotel", "stripclub",                                                // bad stuff
			"animal_boarding", "animal_shelter", "animal_breeding", "veterinary",                                    // animals
		});
		put("tourism", new String[]{
			"attraction", "zoo", "aquarium", "theme_park", "gallery", "museum",                                            // attractions
			"hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site",    // accomodations
			// and tourism=information, see below
		});
		put("leisure", new String[]{
			"park","nature_reserve", "sports_centre","fitness_centre","dance","golf_course",
			"water_park","miniature_golf", "stadium","marina","bowling_alley", "amusement_arcade",
			"adult_gaming_centre", "tanning_salon","horse_riding"
		});
		put("office", new String[]{
			"insurance"
		});
	}};

	@Inject public AddPlaceName(OverpassMapDataDao overpassServer) { super(overpassServer); }

	@Override protected String getTagFilters()
	{
		String query = " nodes, ways, relations with !name and noname != yes and (" +
			   " shop and shop !~ no|vacant" +
			   " or tourism = information and information = office";
		for(String key: OBJECTS_WITH_NAMES.keySet()){
			query += " or (" +key + "~ " + TextUtils.join("|",OBJECTS_WITH_NAMES.get(key)) + " )";
		}
		query += ")";
		return query;
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddPlaceNameForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddPlaceNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return;
		}

		String name = answer.getString(AddPlaceNameForm.NAME);
		if(name != null) changes.add("name", name);
	}

	@Override public String getCommitMessage() { return "Determine place names"; }
	@Override public int getIcon() { return R.drawable.ic_quest_label; }
	@Override public int getTitle(@NonNull Map<String, String> tags)
	{
		return R.string.quest_placeName_title;
	}
}
