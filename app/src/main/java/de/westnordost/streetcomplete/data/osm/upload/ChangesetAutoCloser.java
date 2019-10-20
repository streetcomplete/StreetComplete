package de.westnordost.streetcomplete.data.osm.upload;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import static androidx.work.ExistingWorkPolicy.REPLACE;
import static de.westnordost.streetcomplete.data.changesets.OpenChangesetsDao.CLOSE_CHANGESETS_AFTER_INACTIVITY_OF;

public class ChangesetAutoCloser
{
	@Inject public ChangesetAutoCloser() {}

	public void enqueue()
	{
		// changesets are closed delayed after X minutes of inactivity
		WorkManager.getInstance().enqueueUniqueWork("AutoCloseChangesets", REPLACE,
			new OneTimeWorkRequest.Builder(ChangesetAutoCloserWorker.class)
				.setInitialDelay(CLOSE_CHANGESETS_AFTER_INACTIVITY_OF, TimeUnit.MILLISECONDS)
				.setConstraints(new Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.build())
				.build());
	}
}
