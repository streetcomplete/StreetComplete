package de.westnordost.osmagent.quests.osm.types;

import android.app.DialogFragment;
import android.os.Bundle;

import de.westnordost.osmagent.quests.QuestImportance;
import de.westnordost.osmagent.quests.dialogs.StreetNameDialog;
import de.westnordost.osmagent.quests.osm.changes.StringMapChangesBuilder;

import de.westnordost.osmagent.R;

public class AddRoadName extends OverpassQuestType
{
	@Override
	protected String getTagFilters()
	{
		return " ways with (" +
		       " highway ~ living_street|bicycle_road|residential|pedestrian|primary|secondary|tertiary|unclassified|road or " +
		       " highway = service and service = alley) and !name and noname != yes ";
	}

	@Override
	public int importance()
	{
		return QuestImportance.WARNING;
	}

	public DialogFragment getDialog()
	{
		return new StreetNameDialog();
	}

	public void applyAnswerTo(Bundle answer, StringMapChangesBuilder changes)
	{
		if(answer.getBoolean(StreetNameDialog.NO_NAME))
		{
			changes.add("no_name", "yes");
		}
		else
		{
			String name = answer.getString(StreetNameDialog.NAME);
			changes.add("name", name);
		}
	}

	@Override public int getCommitMessageResourceId()
	{
		return R.string.quest_openingHours_commitMessage;
	}

	@Override public String getIconName() {	return "signpost"; }
}
