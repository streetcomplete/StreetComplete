package de.westnordost.osmagent.quests;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.westnordost.osmagent.Prefs;
import de.westnordost.osmagent.quests.osm.download.OsmQuestDownload;
import de.westnordost.osmagent.quests.osmnotes.OsmNotesDownload;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

public class QuestDownloader
{
	private static final String TAG = "QuestDownload";

	private final SharedPreferences prefs;

	private ExecutorService executorService;
	private QuestDispatcher questDispatcher;

	private AtomicBoolean cancelState;

	@Inject public QuestDownloader(SharedPreferences prefs)
	{
		this.prefs = prefs;

		executorService = Executors.newSingleThreadExecutor();
		questDispatcher = new QuestDispatcher();
	}

	public void cancel()
	{
		if(cancelState != null)
		{
			cancelState.set(true);
		}
	}

	public void addQuestListener(QuestListener listener)
	{
		questDispatcher.addListener(listener);
	}

	public void removeQuestListener(QuestListener listener)
	{
		questDispatcher.removeListener(listener);
	}

	public void download(final BoundingBox bbox, final int maxVisibleQuests)
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
						OsmNotesDownload notesDownload = null; // TODO init with dagger
						notesDownload.setQuestListener(questDispatcher);

						Long userId = prefs.getLong(Prefs.OSM_USER_ID, -1);
						if(userId == -1) userId = null;

						try
						{
							notesPositions = notesDownload.download(bbox, userId, maxVisibleQuests);
						}
						catch(Exception e)
						{
							Log.e(TAG, "Unable to download notes", e);
						}

						int maxOsmQuestsToRetrieve = maxVisibleQuests;
						maxOsmQuestsToRetrieve -= notesDownload.getVisibleQuestsRetrieved();

						OsmQuestDownload questDownload = null; // TODO init with dagger
						questDownload.setQuestListener(questDispatcher);

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

	private class QuestDispatcher implements QuestListener
	{
		List<QuestListener> questListeners = new LinkedList<>();

		public void addListener(QuestListener listener)
		{
			questListeners.add(listener);
		}

		public void removeListener(QuestListener listener)
		{
			questListeners.remove(listener);
		}

		@Override public void onQuestCreated(Quest quest)
		{
			for(QuestListener listener : questListeners)
			{
				listener.onQuestCreated(quest);
			}
		}

		@Override public void onQuestRemoved(Quest quest)
		{
			for(QuestListener listener : questListeners)
			{
				listener.onQuestRemoved(quest);
			}
		}
	}

}
