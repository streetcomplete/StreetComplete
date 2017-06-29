package de.westnordost.streetcomplete.data;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.osmapi.common.errors.OsmConnectionException;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.osm.upload.OsmQuestChangesUpload;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteUpload;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestChangesUpload;
import de.westnordost.streetcomplete.oauth.OAuthPrefs;

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 *  notes and quests he answered */
public class QuestChangesUploadService extends IntentService
{
	public static final String
			ACTION_ERROR = "de.westnordost.QuestChangesUploadService.ERROR",
			IS_AUTH_FAILED = "authFailed",
			IS_VERSION_BANNED = "banned",
			IS_CONNECTION_ERROR = "connectionError",
			EXCEPTION = "exception";

	public static final String
			ACTION_FINISHED = "de.westnordost.QuestChangesUploadService.FINISHED";

	private static final String TAG = "QuestChangesUpload";

	private static Boolean banned = null;
	private static String banReason = null;

	@Inject Provider<OsmNoteQuestChangesUpload> noteQuestUploadProvider;
	@Inject Provider<OsmQuestChangesUpload> questUploadProvider;
	@Inject Provider<CreateNoteUpload> createNoteUploadProvider;
	@Inject OAuthPrefs oAuth;

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

		if(isBanned())
		{
			Log.i(TAG, "This version is banned from making any changes!");
			Intent errorIntent = new Intent(ACTION_ERROR);
			errorIntent.putExtra(IS_VERSION_BANNED, true);
			send(errorIntent);
			return;
		}

		// let's fail early in case of no authorization
		if(!oAuth.isAuthorized())
		{
			Log.i(TAG, "User is not authorized");
			Intent errorIntent = new Intent(ACTION_ERROR);
			errorIntent.putExtra(IS_AUTH_FAILED, true);
			send(errorIntent);
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
		catch (OsmConnectionException e)
		{
			Log.i(TAG, "No connection");
			Intent errorIntent = new Intent(ACTION_ERROR);
			errorIntent.putExtra(IS_CONNECTION_ERROR, true);
			errorIntent.putExtra(EXCEPTION, e);
			send(errorIntent);
		}
		catch (OsmAuthorizationException e)
		{
			Log.i(TAG, "User is not authorized");
			Intent errorIntent = new Intent(ACTION_ERROR);
			errorIntent.putExtra(IS_AUTH_FAILED, true);
			errorIntent.putExtra(EXCEPTION, e);
			send(errorIntent);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Unable to upload changes", e);
			Intent errorIntent = new Intent(ACTION_ERROR);
			errorIntent.putExtra(EXCEPTION, e);
			send(errorIntent);
		}

		send(new Intent(ACTION_FINISHED));

		Log.i(TAG, "Finished upload changes");
	}

	private static boolean isBanned()
	{
		if(banned == null) checkBanned();
		return banned;
	}

	private static void checkBanned()
	{
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL("http://www.westnordost.de/streetcomplete/banned_versions.txt");
			connection = (HttpURLConnection) url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				if(line.startsWith(ApplicationConstants.USER_AGENT))
				{
					banned = true;
					return;
				}
			}
		}
		catch(IOException e)
		{
			// if there is an io exception, never mind then...! (The unreachability of the above
			// internet address should not lead to this app being unusable!)
		}
		finally
		{
			if (connection != null) connection.disconnect();
		}

		banned = false;
	}

	private void send(Intent intent)
	{
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
