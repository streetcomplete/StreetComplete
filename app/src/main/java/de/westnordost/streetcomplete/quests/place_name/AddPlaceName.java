package de.westnordost.streetcomplete.quests.place_name;

import android.os.Bundle;
import android.text.TextUtils;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.OverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;

public class AddPlaceName extends OverpassQuestType
{
	// not all amenities have names. Rather have an inclusive list here than an exclusive one. This
	// list goes down to a few hundred (documented) elements in taginfo (Mar 2017)
	private static final String[] AMENITIES_WITH_NAMES = {
			"restaurant","cafe","ice_cream","fast_food","bar","pub","biergarten","food_court","nightclub",		// eat & drink
			"cinema","theatre","planetarium","arts_centre","studio",											// culture
			"events_venue","conference_centre","exhibition_centre","music_venue",								// events
			"townhall","prison","courthouse","embassy","police","fire_station","ranger_station",				// civic
			"bank","atm","bureau_de_change","money_transfer", "post_office","library","marketplace",			// commercial
			"community_centre","social_facility","nursing_home","childcare","retirement_home","social_centre",	// social
			"car_wash","car_rental","boat_rental","fuel",														// car stuff
			"dentist","doctors","clinic","pharmacy","hospital",													// health care
			"place_of_worship","monastery",																		// religious
			"kindergarten","school","college","university","research_institute",								// education
			"driving_school","dive_centre","language_school","music_school",									// learning
			"casino","brothel","gambling","love_hotel","stripclub",												// bad stuff
			"animal_boarding","animal_shelter","animal_breeding","veterinary",									// animals
	};

	private static final String[] TOURISMS_WITH_NAMES = {
			"attraction","zoo","aquarium","theme_park","gallery","museum",											// attractions
			"hotel","guest_house","motel","hostel","alpine_hut","apartment","resort","camp_site", "caravan_site",	// accomodations
			// and tourism=info, see below
	};

	private static final String[] LEISURES_WITH_NAMES = {
			"park","nature_reserve", "sports_centre","fitness_centre","dance","golf_course",
			"water_park","miniature_golf", "stadium","marina","bowling_alley", "amusement_arcade",
			"adult_gaming_centre", "tanning_salon","horse_riding"
	};

	@Override protected String getTagFilters()
	{
		return " nodes, ways, relations with !name and !noname and (" +
			   " shop and shop !~ no|vacant or" +
			   " amenity ~ " + TextUtils.join("|",AMENITIES_WITH_NAMES) + " or" +
 			   " tourism ~ " + TextUtils.join("|",TOURISMS_WITH_NAMES) + " or" +
			   " tourism = information and information = office or" +
			   " leisure ~ " + TextUtils.join("|",LEISURES_WITH_NAMES) + " )";
	}

	@Override public int importance()
	{
		return QuestImportance.MAJOR;
	}

	@Override public AbstractQuestAnswerFragment createForm()
	{
		return new AddPlaceNameForm();
	}

	@Override public Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(AddPlaceNameForm.NO_NAME))
		{
			changes.add("noname", "yes");
			return R.string.quest_placeName_commitMessage_noname;
		}

		String name = answer.getString(AddPlaceNameForm.NAME);
		if(name != null) changes.add("name", name);
		return R.string.quest_placeName_commitMessage;
	}

	@Override public String getIconName() {	return "label"; }
}
