package de.westnordost.osmagent.quests.types;

import android.app.DialogFragment;
import android.os.Bundle;

import de.westnordost.osmagent.quests.QuestImportance;
import de.westnordost.osmagent.quests.dialogs.StreetNameDialog;
import de.westnordost.osmapi.map.data.Element;

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

	public void applyAnswerTo(Bundle answer, Element element)
	{
		if(answer.getBoolean(StreetNameDialog.NO_NAME))
		{
			element.getTags().put("no_name", "yes");
		}
		else
		{
			String name = answer.getString(StreetNameDialog.NAME);
			element.getTags().put("name", name);
		}
	}

}
