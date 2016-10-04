package de.westnordost.osmagent.quests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import de.westnordost.osmagent.quests.osm.upload.OsmQuestChangesUpload;
import de.westnordost.osmagent.quests.osmnotes.CreateNoteUpload;
import de.westnordost.osmagent.quests.osmnotes.OsmNoteQuestChangesUpload;

public class QuestChangesUploader
{
	private ExecutorService executorService;
	private AtomicBoolean cancelState;

	public QuestChangesUploader()
	{
		executorService = Executors.newSingleThreadExecutor();
	}

	public void shutdown()
	{
		executorService.shutdown();
	}

	public void cancel()
	{
		if(cancelState != null)
		{
			cancelState.set(true);
		}
	}

	public void upload()
	{
		cancel();
		cancelState = new AtomicBoolean(false);

		executorService.submit(
				new Runnable()
				{
					@Override public void run()
					{
						if(cancelState.get()) return;
						OsmNoteQuestChangesUpload noteQuestUpload = null; // TODO get with dagger
						noteQuestUpload.upload(cancelState);

						if(cancelState.get()) return;
						OsmQuestChangesUpload osmQuestUpload = null;// TODO get with dagger
						osmQuestUpload.upload(cancelState);

						if(cancelState.get()) return;
						CreateNoteUpload createNoteUpload = null; // TODO get with dagger
						createNoteUpload.upload(cancelState);
					}
				}
		);
	}

}
