package de.westnordost.streetcomplete.data;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.osm.upload.OsmQuestChangesUpload;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteUpload;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestChangesUpload;
import de.westnordost.streetcomplete.oauth.OAuth;

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 *  notes and quests he answered */
public class QuestChangesUploadService extends IntentService
{
	public static final String
			ACTION_ERROR = "de.westnordost.QuestChangesUploadService.ERROR",
			IS_AUTH_FAILED = "authFailed";

	private static final String TAG = "QuestChangesUpload";

	@Inject Provider<OsmNoteQuestChangesUpload> noteQuestUploadProvider;
	@Inject Provider<OsmQuestChangesUpload> questUploadProvider;
	@Inject Provider<CreateNoteUpload> createNoteUploadProvider;
	@Inject SharedPreferences prefs;

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
		if(cancelState.get()) return;

		// let's fail early in case of no authorization
		if(!OAuth.isAuthorized(prefs))
		{
			Log.i(TAG, "User is not authorized");
			Intent errorIntent = new Intent(ACTION_ERROR);
			errorIntent.putExtra(IS_AUTH_FAILED, true);
			sendError(errorIntent);
			return;
		}

		Log.i(TAG, "Starting upload changes");

		try
		{
			OsmNoteQuestChangesUpload noteQuestUpload = noteQuestUploadProvider.get();
			noteQuestUpload.upload(cancelState);

			if (cancelState.get()) return;

			OsmQuestChangesUpload osmQuestUpload = questUploadProvider.get();
			osmQuestUpload.upload(cancelState);

			if (cancelState.get()) return;

			CreateNoteUpload createNoteUpload = createNoteUploadProvider.get();
			createNoteUpload.upload(cancelState);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Unable to upload changes", e);
			sendError(new Intent(ACTION_ERROR));
		}

		Log.i(TAG, "Finished upload changes");
	}

	private void sendError(Intent errorIntent)
	{
		LocalBroadcastManager.getInstance(this).sendBroadcast(errorIntent);
	}
}
