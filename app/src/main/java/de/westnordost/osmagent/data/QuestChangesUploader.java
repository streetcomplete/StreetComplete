package de.westnordost.osmagent.data;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmagent.data.osm.upload.OsmQuestChangesUpload;
import de.westnordost.osmagent.data.osmnotes.CreateNoteUpload;
import de.westnordost.osmagent.data.osmnotes.OsmNoteQuestChangesUpload;

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 *  notes and quests he answered.
 *
 *  A single thread executor makes sure that it is done in a separate thread and that calling upload
 *  several times (from different threads) does not create competing upload threads that may run
 *  into race conditions. */
public class QuestChangesUploader
{
	private static final String TAG = "QuestChangesUpload";

	private final Provider<OsmNoteQuestChangesUpload> noteQuestUploadProvider;
	private final Provider<OsmQuestChangesUpload> questUploadProvider;
	private final Provider<CreateNoteUpload> createNoteUploadProvider;

	private ExecutorService executorService;
	private AtomicBoolean cancelState;

	private QuestDownloader.OnErrorListener errorListener;
	private boolean errorHappened;
	public interface OnErrorListener
	{
		void onError();
	}

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

	public void setOnErrorListener(QuestDownloader.OnErrorListener listener)
	{
		errorListener = listener;
		if(errorListener != null && errorHappened)
		{
			errorListener.onError();
			errorHappened = false;
		}
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

						Log.i(TAG, "Starting upload changes");

						OsmNoteQuestChangesUpload noteQuestUpload = noteQuestUploadProvider.get();
						try
						{
							noteQuestUpload.upload(cancelState);
						}
						catch (Exception e)
						{
							errorHappened = true;
							Log.e(TAG, "Unable to upload note quest changes", e);
						}

						if(cancelState.get()) return;
						OsmQuestChangesUpload osmQuestUpload = questUploadProvider.get();
						try
						{
							osmQuestUpload.upload(cancelState);
						}
						catch (Exception e)
						{
							errorHappened = true;
							Log.e(TAG, "Unable to upload osm quest changes", e);
						}

						if(cancelState.get()) return;
						CreateNoteUpload createNoteUpload = createNoteUploadProvider.get();
						try
						{
							createNoteUpload.upload(cancelState);
						}
						catch (Exception e)
						{
							errorHappened = true;
							Log.e(TAG, "Unable to upload new notes", e);
						}

						Log.i(TAG, "Finished upload changes");

						if(errorListener != null && errorHappened)
						{
							errorListener.onError();
							errorHappened = false;
						}
					}
				}
		);
	}

}
