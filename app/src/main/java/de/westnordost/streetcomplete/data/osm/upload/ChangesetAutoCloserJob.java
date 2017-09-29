package de.westnordost.streetcomplete.data.osm.upload;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import javax.inject.Inject;

import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao;

public class ChangesetAutoCloserJob extends Job
{
	public static final String TAG = "ChangesetAutoCloserJob";

	public static void scheduleJob() {
		long delay = OpenChangesetsDao.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF;

		new JobRequest.Builder(TAG)
				.setUpdateCurrent(true)
				.setExecutionWindow(delay, delay*2)
				.setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
				.build()
				.schedule();
	}

	private final OsmQuestChangesUpload osmQuestChangesUpload;

	@Inject public ChangesetAutoCloserJob(OsmQuestChangesUpload osmQuestChangesUpload)
	{
		super();
		this.osmQuestChangesUpload = osmQuestChangesUpload;
	}

	@Override @NonNull protected Result onRunJob(Params params)
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
		return Result.SUCCESS;
	}


}
