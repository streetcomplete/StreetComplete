package de.westnordost.streetcomplete.data.osm.upload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;

public class ChangesetAutoCloser extends BroadcastReceiver
{
	@Inject OsmQuestChangesUpload osmQuestChangesUpload;

	public ChangesetAutoCloser()
	{
		Injector.instance.getApplicationComponent().inject(this);
	}

	@Override public void onReceive(Context context, Intent intent)
	{
		osmQuestChangesUpload.closeOpenChangesets();
	}
}
