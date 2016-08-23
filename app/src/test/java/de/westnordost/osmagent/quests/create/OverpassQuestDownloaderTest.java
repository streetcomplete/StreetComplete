package de.westnordost.osmagent.quests.create;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

import de.westnordost.osmagent.quests.OsmQuest;
import de.westnordost.osmagent.quests.types.QuestType;
import de.westnordost.osmapi.common.Handler;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.OsmNode;
import de.westnordost.osmapi.map.handler.MapDataHandler;

public class OverpassQuestDownloaderTest extends TestCase
{
	private static final int SLEEPTIME = 500;

	Thread downloadThread;
	OverpassQuestDownloader downloader;
	Error error;

	public void testStop() throws InterruptedException
	{
		initDownloader(10, new NullHandler());
		downloadThread.start();

		Thread.sleep(2*SLEEPTIME);

		downloader.stop();

		Thread.sleep(SLEEPTIME);

		assertFalse(downloadThread.isAlive());
	}


	public void testDownload() throws InterruptedException
	{
		final int amount = 5;
		final CountHandler handler = new CountHandler();
		initDownloader(amount, handler);

		downloader.setProgressListener(new OverpassQuestDownloader.ProgressListener()
		{
			float count = 0f;

			@Override public void onDone()
			{
				assertEquals(amount, handler.count);
			}
			@Override public void onProgress(float progress, float total)
			{
				assertEquals(count++, progress);
				assertEquals((float)amount, total);
			}
		});
		downloadThread.start();
	}

	public void testStartPauseResume() throws InterruptedException
	{
		final int amount = 2;
		final ExpectSilenceHandler handler = new ExpectSilenceHandler();
		handler.expectSilenceAfterNext = true;

		initDownloader(amount, handler);
		downloader.setProgressListener(new OverpassQuestDownloader.ProgressListener()
		{
			@Override public void onDone()
			{
				assertEquals(amount, handler.count);
			}
			@Override public void onProgress(float progress, float total) { }
		});
		downloadThread.start();

		Thread.sleep(SLEEPTIME/2); // give the downloadThread a chance to start

		downloader.pause();
		// wait longer for nothing to happen than the downloadThread would take to return several quests
		Thread.sleep(5*SLEEPTIME);

		handler.expectSilence = false;
		downloader.resume();
	}

	@Override protected void tearDown() throws InterruptedException
	{
		downloadThread.join();

		if(error != null)
		{
			throw error;
		}
	}

	private void initDownloader(int amountOfQuestTypes, Handler<OsmQuest> handler)
	{
		List<QuestType> questTypes = createQuestTypeList(amountOfQuestTypes);
		downloader = new OverpassQuestDownloader(
				new SlowDao(), questTypes, new BoundingBox(0, 1, 2, 3), handler);
		downloadThread = new Thread(new Runnable() {
			@Override public void run()
			{
				// for joining assertion exceptions in that downloadThread into main JUnit downloadThread
				try
				{
					downloader.run();
				}
				catch(Error e)
				{
					error = e;
				}
			}
		});
	}

	private static List<QuestType> createQuestTypeList(int amount)
	{
		ArrayList<QuestType> result = new ArrayList<>();
		while(amount-- > 0) result.add(createApplyToAnythingQuestType());
		return result;
	}

	private static QuestType createApplyToAnythingQuestType()
	{
		QuestType questType = mock(QuestType.class);
		when(questType.appliesTo(any(Element.class))).thenReturn(true);
		return questType;
	}

	private class SlowDao extends OverpassMapDataDao
	{
		public SlowDao()
		{
			super(null,null);
		}

		public void get(String oql, MapDataHandler handler)
		{
			try
			{
				Thread.sleep(SLEEPTIME);
			}
			catch (InterruptedException e)
			{
				return;
			}

			handler.handle(new OsmNode(0,0,0d,0d,null,null));
		}
	}

	private class CountHandler implements Handler<OsmQuest>
	{
		int count;

		@Override public void handle(OsmQuest tea)
		{
			count++;
		}
	}

	private class ExpectSilenceHandler extends CountHandler
	{
		boolean expectSilence = false;
		boolean expectSilenceAfterNext = false;

		@Override public void handle(OsmQuest tea)
		{
			super.handle(tea);

			if(expectSilence) fail();
			if(expectSilenceAfterNext)
			{
				expectSilenceAfterNext = false;
				expectSilence = true;
			}
		}
	}

	private class NullHandler implements Handler<OsmQuest>
	{
		@Override public void handle(OsmQuest tea) { }
	}
}
