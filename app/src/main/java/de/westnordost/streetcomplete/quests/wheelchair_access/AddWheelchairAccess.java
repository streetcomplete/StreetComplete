package de.westnordost.streetcomplete.quests.wheelchair_access;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHoursForm;


public class AddWheelchairAccess extends SimpleOverpassQuestType
{
	@Inject public AddWheelchairAccess(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return " nodes, ways, relations with ( shop and shop !~ no|vacant or" +
				" amenity ~ restaurant|cafe|ice_cream|fast_food|bar|pub|biergarten|food_court|cinema|nightclub|" +
				"bank|atm|bureau_de_change|money_transfer|post_office|library|courthouse|embassy|" +
				"car_wash|car_rental|marketplace|fuel|driving_school|" +
				"dentist|doctors|clinic|pharmacy|veterinary or" +
				" amenity = bicycle_parking and bicycle_parking = building or" +
				" amenity = parking and parking = multi-storey or" +
				" amenity = recycling and recycling_type = centre or" +
				" tourism ~ zoo|aquarium|theme_park|gallery|museum or" +
				" tourism = information and information = office or" +
				" tourism ~ hotel|guest_house|hostel|motel or" +
				" leisure ~ golf_course|water_park|miniature_golf|dance|bowling_alley|horse_riding" +
				"sports_centre|fitness_centre|amusement_arcade|adult_gaming_centre|tanning_salon )" +
				" and !wheelchair and name";
	}

	@Override public int importance()
	{
		return QuestImportance.MINOR;
	}

	@Override public WheelchairAccessAnswerFragment createForm()
	{
		return new AddWheelchairAccessForm();
	}

	@Override public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String wheelchair = answer.getString(AddWheelchairAccessForm.ANSWER);
		if(wheelchair != null)
		{
			changes.add("wheelchair", wheelchair);
		}
	}

	@Override public String getCommitMessage()
	{
		return "Add wheelchair access";
	}

	@Override public String getIconName() {	return "wheelchair"; }
}
