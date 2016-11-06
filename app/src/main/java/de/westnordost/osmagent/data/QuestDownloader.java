package de.westnordost.osmagent.data;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmagent.Prefs;
import de.westnordost.osmagent.data.osm.download.OsmQuestDownload;
import de.westnordost.osmagent.data.osmnotes.OsmNotesDownload;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

public class QuestDownloader
{
	private static final String TAG = "QuestDownload";

	private final SharedPreferences prefs;
	private final Provider<OsmNotesDownload> notesDownloadProvider;
	private final Provider<OsmQuestDownload> questDownloadProvider;

	private ExecutorService executorService;

	private AtomicBoolean cancelState;

	private VisibleQuestListener listener;

	@Inject public QuestDownloader(SharedPreferences prefs,
								   Provider<OsmNotesDownload> notesDownloadProvider,
								   Provider<OsmQuestDownload> questDownloadProvider)
	{
		this.prefs = prefs;
		this.notesDownloadProvider = notesDownloadProvider;
		this.questDownloadProvider = questDownloadProvider;

		executorService = Executors.newSingleThreadExecutor();
	}

	public void cancel()
	{
		if(cancelState != null)
		{
			cancelState.set(true);
		}
	}

	public void setQuestListener(VisibleQuestListener listener)
	{
		this.listener = listener;
	}

	public void download(final BoundingBox bbox, final Integer maxVisibleQuests)
	{
		cancel();
		cancelState = new AtomicBoolean(false);

		executorService.submit(
				new Runnable()
				{
					@Override public void run()
					{
						if(cancelState.get()) return;

						Set<LatLon> notesPositions = null;
						OsmNotesDownload notesDownload = notesDownloadProvider.get();
						notesDownload.setQuestListener(listener);

						Long userId = prefs.getLong(Prefs.OSM_USER_ID, -1);
						if(userId == -1) userId = null;

						try
						{
							int maxNotes = maxVisibleQuests != null ? maxVisibleQuests : 10000;
							notesPositions = notesDownload.download(bbox, userId, maxNotes);
						}
						catch(Exception e)
						{
							Log.e(TAG, "Unable to download notes", e);
						}

						Integer maxOsmQuestsToRetrieve = maxVisibleQuests;
						if(maxOsmQuestsToRetrieve != null)
						{
							maxOsmQuestsToRetrieve -= notesDownload.getVisibleQuestsRetrieved();
						}

						OsmQuestDownload questDownload = questDownloadProvider.get();
						questDownload.setQuestListener(listener);

						try
						{
							questDownload.download(bbox, notesPositions, maxOsmQuestsToRetrieve, cancelState);
						}
						catch(Exception e)
						{
							Log.e(TAG, "Unable to download osm quests", e);
						}
					}
				}
		);
	}

	public void shutdown()
	{
		cancel();
		executorService.shutdown();
	}

}
