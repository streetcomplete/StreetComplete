package de.westnordost.streetcomplete.data.osm.upload;


import android.app.IntentService;
import android.content.Intent;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
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
		try
		{
			osmQuestChangesUpload.closeOpenChangesets();
		}
		catch(OsmConnectionException e)
		{
			// wasn't able to connect to the server (i.e. connection timeout). Oh well, then,
			// never mind.
		}
		catch(OsmAuthorizationException e)
		{
			// the user may not be authorized yet (or not be authorized anymore) #283
			// nothing we can do about here. He will have to reauthenticate when he next opens the app
		}
		ChangesetAutoCloserReceiver.completeWakefulIntent(intent);
	}
}