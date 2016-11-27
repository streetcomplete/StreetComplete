package de.westnordost.streetcomplete.data;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.osm.upload.OsmQuestChangesUpload;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteUpload;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestChangesUpload;

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 *  notes and quests he answered */
public class QuestChangesUploadService extends IntentService
{
	public static final String ACTION_ERROR = "de.westnordost.QuestChangesUploadService.ERROR";

	private static final String TAG = "QuestChangesUpload";

	@Inject Provider<OsmNoteQuestChangesUpload> noteQuestUploadProvider;
	@Inject Provider<OsmQuestChangesUpload> questUploadProvider;
	@Inject Provider<CreateNoteUpload> createNoteUploadProvider;

	private AtomicBoolean cancelState;

	public QuestChangesUploadService()
	{
		super(TAG);
		Injector.instance.getApplicationComponent().inject(this);
	}

	@Override public void onCreate()
	{
		super.onCreate();
		cancelState = new AtomicBoolean(false);
	}

	@Override public void onDestroy()
	{
		cancelState.set(true);
		super.onDestroy();
	}

	@Override protected void onHandleIntent(Intent intent)
	{
		boolean errorHappened = false;

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

		if(errorHappened)
		{
			Intent errorIntent = new Intent(ACTION_ERROR);
			LocalBroadcastManager.getInstance(this).sendBroadcast(errorIntent);
		}
	}
}
