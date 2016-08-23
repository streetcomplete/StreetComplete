package de.westnordost.osmagent.quests.create;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.westnordost.osmagent.quests.OsmQuest;
import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;

/** Downloads data from overpass and creates quests for the given quest types. */
class OverpassQuestDownloader implements Runnable
{
	private final OverpassMapDataDao dao;

	private final int initialQueueSize;

	private final Queue<QuestType> downloadQueue;

	private final BoundingBox bbox;
	private final Handler<OsmQuest> handler;

	private volatile boolean pause;

	private ProgressListener progressListener;

	public interface ProgressListener
	{
		void onDone();
		void onProgress(float progress, float total);
	}

	public OverpassQuestDownloader(OverpassMapDataDao dao, List<QuestType> questTypes,
								   BoundingBox bbox, Handler<OsmQuest> handler)
	{
		this.dao = dao;
		initialQueueSize = questTypes.size();
		downloadQueue = new LinkedList<>(questTypes);
		this.bbox = bbox;
		this.handler = handler;
	}

	/** Clears the download queue and thus stops it. (The runnable will finish it's current download
	 *  though, if any) */
	public synchronized void stop()
	{
		downloadQueue.clear();
	}

	public synchronized void setProgressListener(ProgressListener progressListener)
	{
		this.progressListener = progressListener;
	}

	/** Pause downloading. (The runnable will finish it's current download though, if any) */
	public void pause()
	{
		pause = true;
	}

	/** Resume downloading just where it left off before calling pause. */
	public void resume()
	{
		pause = false;
		synchronized (this)
		{
			notify();
		}
	}

	public void run()
	{
		while (!Thread.interrupted())
		{
			dispatchProgress();

			try
			{
				while (pause)
				{
					synchronized (this) { wait(); }
				}
			}
			catch (InterruptedException e)
			{
				// ok, finish thread
				break;
			}
			QuestType questType;
			synchronized (this)
			{
				questType = downloadQueue.poll();
			}

			// done
			if(questType == null) break;

			/* creating a new handler for each query because this handler keeps around data
			   for the requested area which is not needed beyond the quests that are being
			   associated with it */
			CreateQuestMapDataHandler mapDataHandler = new CreateQuestMapDataHandler(
					questType, handler);

			String oql = questType.getOverpassQuery(bbox);
			dao.get(oql, mapDataHandler);
		}

		dispatchDone();
	}

	private synchronized void dispatchProgress()
	{
		if (progressListener == null) return;
		progressListener.onProgress(initialQueueSize - downloadQueue.size(), initialQueueSize);
	}

	private synchronized void dispatchDone()
	{
		if (progressListener == null) return;
		progressListener.onDone();
	}
}
