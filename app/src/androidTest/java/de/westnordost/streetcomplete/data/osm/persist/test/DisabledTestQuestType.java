package de.westnordost.streetcomplete.data.osm.persist.test;

import de.westnordost.streetcomplete.R;

public class DisabledTestQuestType extends TestQuestType
{
	@Override public int getDefaultDisabledMessage()
	{
		return R.string.default_disabled_msg_go_inside;
	}
}