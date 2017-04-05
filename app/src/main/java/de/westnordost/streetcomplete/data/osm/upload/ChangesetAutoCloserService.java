package de.westnordost.streetcomplete.data.osm.upload;


import android.app.IntentService;
import android.content.Intent;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;

public class ChangesetAutoCloserService extends IntentService
{
	@Inject OsmQuestChangesUpload osmQuestChangesUpload;

	public ChangesetAutoCloserService()
	{
		super("ChangesetAutoCloserService");
		Injector.instance.getApplicationComponent().inject(this);
	}

	@Override protected void onHandleIntent(Intent intent)
	{
		osmQuestChangesUpload.closeOpenChangesets();
		ChangesetAutoCloserReceiver.completeWakefulIntent(intent);
	}
}