package de.westnordost.streetcomplete.jobs;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.streetcomplete.data.osm.upload.ChangesetAutoCloserJob;

public class StreetCompleteJobCreator implements JobCreator
{
	private final Provider<ChangesetAutoCloserJob> changesetAutoCloserJobProvider;

	@Inject public StreetCompleteJobCreator(Provider<ChangesetAutoCloserJob> changesetAutoCloserJobProvider)
	{
		this.changesetAutoCloserJobProvider = changesetAutoCloserJobProvider;
	}

	@Override public Job create(String tag) {
		switch (tag) {
			case ChangesetAutoCloserJob.TAG:
				return changesetAutoCloserJobProvider.get();
			default:
				return null;
		}
	}
}
