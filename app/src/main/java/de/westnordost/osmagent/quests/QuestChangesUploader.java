package de.westnordost.osmagent.quests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmagent.quests.osm.upload.OsmQuestChangesUpload;
import de.westnordost.osmagent.quests.osmnotes.CreateNoteUpload;
import de.westnordost.osmagent.quests.osmnotes.OsmNoteQuestChangesUpload;

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 *  notes and quests he answered.
 *
 *  A single thread executor makes sure that it is done in a separate thread and that calling upload
 *  several times (from different threads) does not create competing upload threads that may run
 *  into race conditions. */
public class QuestChangesUploader
{
	private final Provider<OsmNoteQuestChangesUpload> noteQuestUploadProvider;
	private final Provider<OsmQuestChangesUpload> questUploadProvider;
	private final Provider<CreateNoteUpload> createNoteUploadProvider;

	private ExecutorService executorService;
	private AtomicBoolean cancelState;

	@Inject public QuestChangesUploader(
			Provider<OsmNoteQuestChangesUpload> noteQuestUploadProvider,
			Provider<OsmQuestChangesUpload> questUploadProvider,
			Provider<CreateNoteUpload> createNoteUploadProvider)
	{
		this.noteQuestUploadProvider = noteQuestUploadProvider;
		this.questUploadProvider = questUploadProvider;
		this.createNoteUploadProvider = createNoteUploadProvider;
		executorService = Executors.newSingleThreadExecutor();
	}

	public void shutdown()
	{
		cancel();
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
		if(cancelState == null || cancelState.get())
		{
			cancelState = new AtomicBoolean(false);
		}

		executorService.submit(
				new Runnable()
				{
					@Override public void run()
					{
						if(cancelState.get()) return;
						OsmNoteQuestChangesUpload noteQuestUpload = noteQuestUploadProvider.get();
						noteQuestUpload.upload(cancelState);

						if(cancelState.get()) return;
						OsmQuestChangesUpload osmQuestUpload = questUploadProvider.get();
						osmQuestUpload.upload(cancelState);

						if(cancelState.get()) return;
						CreateNoteUpload createNoteUpload = createNoteUploadProvider.get();
						createNoteUpload.upload(cancelState);
					}
				}
		);
	}

}
