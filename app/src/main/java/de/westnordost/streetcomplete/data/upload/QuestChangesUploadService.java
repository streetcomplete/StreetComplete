package de.westnordost.streetcomplete.data.upload;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmapi.common.errors.OsmAuthorizationException;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.VisibleQuestRelay;
import de.westnordost.streetcomplete.data.osm.upload.OsmQuestChangesUpload;
import de.westnordost.streetcomplete.data.osm.upload.UndoOsmQuestChangesUpload;
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteUpload;
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestChangesUpload;
import de.westnordost.streetcomplete.oauth.OAuthPrefs;

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 *  notes and quests he answered */
public class QuestChangesUploadService extends IntentService
{
	private static final String TAG = "QuestChangesUpload";

	private static Boolean banned = null;
	private static String banReason = null;

	@Inject Provider<OsmNoteQuestChangesUpload> noteQuestUploadProvider;
	@Inject Provider<OsmQuestChangesUpload> questUploadProvider;
	@Inject Provider<UndoOsmQuestChangesUpload> undoQuestUploadProvider;
	@Inject Provider<CreateNoteUpload> createNoteUploadProvider;
	@Inject OAuthPrefs oAuth;

	private final IBinder binder = new Interface();

	// listeners
	private final VisibleQuestRelay visibleQuestRelay  = new VisibleQuestRelay();
	private QuestChangesUploadProgressListener progressListener;
	private final OnUploadedChangeListener uploadedChangeRelay = new OnUploadedChangeListener()
	{
		@Override public void onUploaded()
		{
			if(progressListener != null) progressListener.onProgress(true);
		}

		@Override public void onDiscarded()
		{
			if(progressListener != null) progressListener.onProgress(false);
		}
	};

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

	@Override public IBinder onBind(Intent intent)
	{
		return binder;
	}

	@Override public void onDestroy()
	{
		cancelState.set(true);
		super.onDestroy();
	}

	@Override protected void onHandleIntent(Intent intent)
	{
		if(cancelState.get()) return;

		if(progressListener != null)
		{
			progressListener.onStarted();
		}

		try
		{
			if(isBanned())
			{
				throw new VersionBannedException(banReason);
			}

			// let's fail early in case of no authorization
			if(!oAuth.isAuthorized())
			{
				throw new OsmAuthorizationException(401, "Unauthorized", "User is not authorized");
			}

			Log.i(TAG, "Starting upload changes");

			OsmNoteQuestChangesUpload noteQuestUpload = noteQuestUploadProvider.get();
			noteQuestUpload.setProgressListener(uploadedChangeRelay);
			noteQuestUpload.upload(cancelState);

			if (cancelState.get()) return;

			UndoOsmQuestChangesUpload undoOsmQuestUpload = undoQuestUploadProvider.get();
			undoOsmQuestUpload.setProgressListener(uploadedChangeRelay);
			undoOsmQuestUpload.setVisibleQuestListener(visibleQuestRelay);
			undoOsmQuestUpload.upload(cancelState);

			if (cancelState.get()) return;

			OsmQuestChangesUpload osmQuestUpload = questUploadProvider.get();
			osmQuestUpload.setProgressListener(uploadedChangeRelay);
			osmQuestUpload.setVisibleQuestListener(visibleQuestRelay);
			osmQuestUpload.upload(cancelState);

			if (cancelState.get()) return;

			CreateNoteUpload createNoteUpload = createNoteUploadProvider.get();
			createNoteUpload.setProgressListener(uploadedChangeRelay);
			createNoteUpload.upload(cancelState);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Unable to upload changes", e);
			if(progressListener != null)
			{
				progressListener.onError(e);
			}
		}

		if(progressListener != null)
		{
			progressListener.onFinished();
		}

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
			URL url = new URL("https://www.westnordost.de/streetcomplete/banned_versions.txt");
			connection = (HttpURLConnection) url.openConnection();
			try (InputStream is = connection.getInputStream())
			{
				try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						String[] text = line.split("\t");
						String userAgent = text[0];

						if (userAgent.equals(ApplicationConstants.USER_AGENT))
						{
							banned = true;
							banReason = text.length > 1 ? text[1] : null;
							return;
						}
					}
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

	/** Public interface to classes that are bound to this service */
	public class Interface extends Binder
	{
		public void setProgressListener(QuestChangesUploadProgressListener listener)
		{
			progressListener = listener;
		}

		public void setQuestListener(VisibleQuestListener listener)
		{
			visibleQuestRelay.setListener(listener);
		}
	}
}
