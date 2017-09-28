package de.westnordost.streetcomplete.data.osm.upload;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.streetcomplete.Injector;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ChangesetAutoCloserJobService extends JobService
{
	@Inject OsmQuestChangesUpload osmQuestChangesUpload;

	public ChangesetAutoCloserJobService()
	{
		super();
		Injector.instance.getApplicationComponent().inject(this);
	}

	@Override public boolean onStartJob(JobParameters jobParameters)
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
		return true;
	}

	@Override public boolean onStopJob(JobParameters jobParameters)
	{
		return true;
	}
}
