package de.westnordost.streetcomplete.quests.tactile_paving;

import android.os.Bundle;

import java.util.Map;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.Countries;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddTactilePavingBusStop extends SimpleOverpassQuestType
{
	@Inject public AddTactilePavingBusStop(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override protected String getTagFilters()
	{
		return "nodes with (public_transport=platform or (highway=bus_stop and public_transport!=stop_position)) and !tactile_paving";
	}

	public AbstractQuestAnswerFragment createForm()
	{
		return new TactilePavingBusStopForm();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("tactile_paving", yesno);
	}

	@Override public String getCommitMessage() { return "Add tactile pavings on bus stops"; }
	@Override public int getIcon() { return R.drawable.ic_quest_blind_bus; }
	@Override public int getTitle(Map<String,String> tags)
	{
		boolean hasName = tags.containsKey("name");
		if(hasName) return R.string.quest_tactilePaving_title_name_bus;
		else        return R.string.quest_tactilePaving_title_bus;
	}

	@Override public Countries getEnabledForCountries()
	{
		return Countries.noneExcept(new String[]
			{
				// areas based on research
				"CN-91", "SG", "AU", "NZ",
				// generated from OSM data
				"DE", "CH", "GB", "IE", "JP", "ES", "FR", "NL", "US-GA",
			});
	}
}
