package de.westnordost.streetcomplete.quests.baby_changing_table;

import android.os.Bundle;

import javax.inject.Inject;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.QuestImportance;
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao;
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment;
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment;

public class AddBabyChangingTable extends SimpleOverpassQuestType
{
	@Inject public AddBabyChangingTable(OverpassMapDataDao overpassServer)
	{
		super(overpassServer);
	}

	@Override
	protected String getTagFilters()
	{
		return "nodes, ways with (((amenity ~ restaurant|pub|cafe|ice_cream|fuel or shop=mall or building=retail) and toilets=yes) or amenity=toilets) and !diaper";
	}

	@Override
	public int importance()
	{
		return QuestImportance.MINOR;
	}

	public AbstractQuestAnswerFragment createForm()
	{
		AbstractQuestAnswerFragment form =  new YesNoQuestAnswerFragment();
		form.setTitle(R.string.quest_baby_changing_table_title);
		return form;
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		String yesno = answer.getBoolean(YesNoQuestAnswerFragment.ANSWER) ? "yes" : "no";
		changes.add("diaper", yesno);
	}

	@Override public String getCommitMessage()
	{
		return "Add baby changing table";
	}

	@Override public String getIconName() {	return "baby_changing_table"; }
}
