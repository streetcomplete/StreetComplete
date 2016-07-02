package de.westnordost.osmagent.quests;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.overpass.OverpassMapDataDao;

/** Downloads data from overpass and creates quests */
public class OverpassQuestDownloader
{
	private final List<QuestType> questTypeList;
	private final OverpassMapDataDao dao;
	private final Handler<Quest> handler;

	private final QuestDownloadThread thread;

	private final LinkedBlockingQueue<QuestDownloadTask> downloadQueue;

	@Inject
	public OverpassQuestDownloader(OverpassMapDataDao dao, List<QuestType> questTypeList,
								   Handler<Quest> handler )
	{
		this.dao = dao;
		this.handler = handler;
		this.questTypeList = questTypeList;

		downloadQueue = new LinkedBlockingQueue<>();

		/* there may only ever be one thread because Overpass does not permit parallel queries from
		   the same ip. The thread is started right away, but will only do work when the queue is
		   filled */
		thread = new QuestDownloadThread();
		thread.start();
	}

	/** Start downloading the data to create the quests in the given bounding box. Dismisses
	 *  the download of previous bounding boxes */
	public void start(BoundingBox bbox)
	{
		if(!thread.isAlive())
		{
			throw new IllegalStateException("Download thread has already been shutdown!");
		}
		stop();
		for(QuestType questType : questTypeList)
		{
			QuestDownloadTask task = new QuestDownloadTask();
			task.questType = questType;
			task.bbox = bbox;
			downloadQueue.add(task);
		}
	}

	public void stop()
	{
		downloadQueue.clear();
	}

	public void shutdown()
	{
		thread.interrupt();
	}

	private class QuestDownloadTask
	{
		QuestType questType;
		BoundingBox bbox;
	}

	private class QuestDownloadThread extends Thread
	{
		public void run()
		{
			while( !isInterrupted() )
			{
				QuestDownloadTask dl;
				try
				{
					dl = downloadQueue.take();
				}
				catch (InterruptedException e)
				{
					// ok, finish thread
					return;
				}

				String oql = dl.questType.getOverpassQuery(dl.bbox);

				/* creating a new handler for each query because this handler keeps around data
				   for the requested area which is not needed beyond the quests that are being
				   associated with it */
				CreateQuestMapDataHandler mapDataHandler = new CreateQuestMapDataHandler(dl.questType, handler);
				dao.get(oql, mapDataHandler);
			}
		}
	}
}
