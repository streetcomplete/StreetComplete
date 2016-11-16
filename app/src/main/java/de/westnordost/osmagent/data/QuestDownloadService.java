package de.westnordost.osmagent.data;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.osmagent.Injector;
import de.westnordost.osmagent.Prefs;
import de.westnordost.osmagent.data.osm.download.OsmQuestDownload;
import de.westnordost.osmagent.data.osmnotes.OsmNotesDownload;
import de.westnordost.osmapi.map.data.BoundingBox;
import de.westnordost.osmapi.map.data.LatLon;

/** Downloads all quests in a given area. To use, bind to the service and then call download(...)
 *  to download the quests. If you start the service before binding to it, the service will run
 *  until all downloads finished. */
public class QuestDownloadService extends Service
{
	/* Not using IntentService here even though we are doing practically the same because the
	   Runnables that are executed in the queue have to be set up with the cancel flag when
	   onStartCommand is called, not when they are finally executed
	 */

	public static final	String
			ARG_MINLON = "minLon",
			ARG_MINLAT = "minLat",
			ARG_MAXLON = "maxLon",
			ARG_MAXLAT = "maxLat",
			ARG_MAX_VISIBLE_QUESTS = "maxVisibleQuests",
			ARG_PROGRESS = "progress",
			ARG_QUEST_TYPE = "questType";

	public static final String
			ACTION_ERROR = "de.westnordost.osmagent.data.ERROR",
			ACTION_STARTED = "de.westnordost.osmagent.data.STARTED",
			ACTION_PROGRESS = "de.westnordost.osmagent.data.PROGRESS",
			ACTION_FINISHED = "de.westnordost.osmagent.data.FINISHED",
			ACTION_JUST_IN = "de.westnordost.osmagent.data.JUST_IN",
			ACTION_JUST_IN_NOTES = "de.westnordost.osmagent.data.JUST_IN_NOTES";

	private static final String TAG = "QuestDownload";

	@Inject SharedPreferences prefs;
	@Inject Provider<OsmNotesDownload> notesDownloadProvider;
	@Inject Provider<OsmQuestDownload> questDownloadProvider;

	private volatile Looper serviceLooper;
	private volatile ServiceHandler serviceHandler;
	private class ServiceHandler extends Handler
	{
		ServiceHandler(Looper looper) { super(looper); }

		@Override public void handleMessage(Message msg)
		{
			((Runnable) msg.obj).run();
			stopSelf(msg.arg1);
		}
	}

	private AtomicBoolean cancelState;

	public QuestDownloadService()
	{
		super();
		Injector.instance.getApplicationComponent().inject(this);
		cancelState = new AtomicBoolean(false);
	}

	@Override public void onCreate()
	{
		super.onCreate();

		HandlerThread thread = new HandlerThread(TAG);
		thread.start();

		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}

	@Override public int onStartCommand(@Nullable Intent intent, int flags, int startId)
	{
		cancel();

		if(intent != null)
		{
			Integer maxVisibleQuests = null;
			if (intent.hasExtra(ARG_MAX_VISIBLE_QUESTS))
				maxVisibleQuests = intent.getIntExtra(ARG_MAX_VISIBLE_QUESTS,0);

			BoundingBox bbox = getBBox(intent);

			Message msg = serviceHandler.obtainMessage();
			msg.obj = new DownloadRunnable(bbox, maxVisibleQuests, cancelState);
			msg.arg1 = startId;
			serviceHandler.sendMessage(msg);
		}
		return START_NOT_STICKY;
	}

	@Override public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override public void onDestroy()
	{
		cancel();
		serviceLooper.quit();
	}

	private void cancel()
	{
		cancelState.set(true);
		cancelState = new AtomicBoolean(false);
	}

	private class DownloadRunnable implements Runnable, OsmQuestDownload.ProgressListener
	{
		final BoundingBox bbox;
		final Integer maxVisibleQuests;
		final AtomicBoolean cancel;

		DownloadRunnable(BoundingBox bbox, Integer maxVisibleQuests, AtomicBoolean cancel)
		{
			this.bbox = bbox;
			this.maxVisibleQuests = maxVisibleQuests;
			this.cancel = cancel;

		}

		@Override public void run()
		{
			if(cancel.get()) return;

			boolean errorHappened = false;

			sendBroadcast(new Intent(ACTION_STARTED));

			Set<LatLon> notesPositions = null;
			OsmNotesDownload notesDownload = notesDownloadProvider.get();

			Long userId = prefs.getLong(Prefs.OSM_USER_ID, -1);
			if(userId == -1) userId = null;

			try
			{
				int maxNotes = maxVisibleQuests != null ? maxVisibleQuests : 10000;
				notesPositions = notesDownload.download(bbox, userId, maxNotes);
			}
			catch(Exception e)
			{
				Log.e(TAG, "Unable to download notes", e);
				errorHappened = true;
			}

			sendFinishedNotes();

			OsmQuestDownload questDownload = questDownloadProvider.get();
			questDownload.setProgressListener(this);

			try
			{
				questDownload.download(bbox, notesPositions, maxVisibleQuests, cancel);
			}
			catch(Exception e)
			{
				Log.e(TAG, "Unable to download quests", e);
				errorHappened = true;
			}

			if(errorHappened)
			{
				sendBroadcast(new Intent(ACTION_ERROR));
			}
			sendBroadcast(new Intent(ACTION_FINISHED));
		}

		@Override public void onProgress(float progress, QuestType questType)
		{
			Intent progressIntent = new Intent(ACTION_PROGRESS);
			progressIntent.putExtra(ARG_PROGRESS, progress);
			sendBroadcast(progressIntent);

			Intent questTypeFinishedIntent = new Intent(ACTION_JUST_IN);
			questTypeFinishedIntent.putExtra(ARG_QUEST_TYPE, questType.getClass().getSimpleName());
			putBBox(bbox, questTypeFinishedIntent);
			sendBroadcast(questTypeFinishedIntent);
		}

		private void sendFinishedNotes()
		{
			Intent intent = new Intent(ACTION_JUST_IN_NOTES);
			putBBox(bbox, intent);
			sendBroadcast(intent);
		}

		private void sendBroadcast(Intent intent)
		{
			LocalBroadcastManager.getInstance(QuestDownloadService.this).sendBroadcast(intent);
		}
	}

	public static void putBBox(BoundingBox bbox, Intent intent)
	{
		intent.putExtra(ARG_MINLAT, bbox.getMinLatitude());
		intent.putExtra(ARG_MINLON, bbox.getMinLongitude());
		intent.putExtra(ARG_MAXLAT, bbox.getMaxLatitude());
		intent.putExtra(ARG_MAXLON, bbox.getMaxLongitude());
	}

	public static BoundingBox getBBox(Intent intent)
	{
		Bundle args = intent.getExtras();
		return new BoundingBox(
				args.getDouble(ARG_MINLAT), args.getDouble(ARG_MINLON),
				args.getDouble(ARG_MAXLAT), args.getDouble(ARG_MAXLON)
		);
	}
}
