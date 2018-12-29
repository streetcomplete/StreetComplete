package de.westnordost.streetcomplete.data.download;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Provider;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.VisibleQuestListener;
import de.westnordost.streetcomplete.data.VisibleQuestRelay;

import static de.westnordost.streetcomplete.ApplicationConstants.NOTIFICATIONS_CHANNEL_DOWNLOAD;

/** Downloads all quests in a given area asynchronously. To use, start the service with the
 * appropriate parameters. (see #onStartCommand)
 *
 *  Generally, starting a new download cancels the old one. This is a feature; Consideration:
 *  If the user requests a new area to be downloaded, he'll generally be more interested in his last
 *  request than any request he made earlier and he wants that as fast as possible. (Downloading
 *  in-parallel is not possible with Overpass, only one request a time is allowed on the public
 *  instance)
 *
 *  The service can be bound to snoop into the state of the downloading process:
 *  * To receive progress callbacks
 *  * To receive callbacks when new quests are created or old ones removed
 *  * To query for the state of the service and/or current download task, i.e. if the current
 *    download job was started by the user
 *  */
public class QuestDownloadService extends Service
{
	private static final String TAG = "QuestDownload";

	public static final	String
			ARG_TILES_RECT = "tilesRect",
			ARG_MAX_QUEST_TYPES = "maxQuestTypes",
			ARG_IS_PRIORITY = "isPriority";

	/* Not using IntentService here even though we are doing practically the same because the
	   Runnables that are executed in the queue have to be set up with the cancel flag when
	   onStartCommand is called, not when they are finally executed
	 */

	// listeners
	private QuestDownloadProgressRelay progressListenerRelay;
	private VisibleQuestRelay visibleQuestRelay;

	// service and worker thread
	@Inject Provider<QuestDownload> questDownloadProvider;
	private final IBinder binder = new Interface();

	private QuestDownload currentDownload;
	private final Object downloadLock = new Object();

	private volatile Looper serviceLooper;
	private volatile ServiceHandler serviceHandler;
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			createNotificationChannel();
		}
		progressListenerRelay = new QuestDownloadProgressRelay(this, NOTIFICATIONS_CHANNEL_DOWNLOAD, 1);
		visibleQuestRelay = new VisibleQuestRelay();
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void createNotificationChannel()
	{
		NotificationManager service = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
		assert service != null;
		service.createNotificationChannel(new NotificationChannel(
			NOTIFICATIONS_CHANNEL_DOWNLOAD, getString(R.string.notification_channel_download), NotificationManager.IMPORTANCE_LOW));
	}

	@Override public int onStartCommand(@Nullable Intent intent, int flags, int startId)
	{
		cancel();

		if(intent != null)
		{
			Rect tiles = intent.getParcelableExtra(ARG_TILES_RECT);

			Integer maxQuestTypes = null;
			if (intent.hasExtra(ARG_MAX_QUEST_TYPES))
			{
				maxQuestTypes = intent.getIntExtra(ARG_MAX_QUEST_TYPES, 0);
			}

			boolean isPriority = intent.hasExtra(ARG_IS_PRIORITY);

			Message msg = serviceHandler.obtainMessage();
			QuestDownload dl = questDownloadProvider.get();
			dl.init(tiles, maxQuestTypes, isPriority, cancelState);
			msg.obj = dl;
			msg.arg1 = startId;
			serviceHandler.sendMessage(msg);
		}
		return START_NOT_STICKY;
	}

	@Override public IBinder onBind(Intent intent)
	{
		return binder;
	}

	@Override public void onDestroy()
	{
		cancel();
		serviceHandler.removeCallbacksAndMessages(null);
		serviceLooper.quit();
	}

	private void cancel()
	{
		cancelState.set(true);
		cancelState = new AtomicBoolean(false);
	}

	/** Public interface to classes that are bound to this service */
	public class Interface extends Binder
	{
		public void setProgressListener(QuestDownloadProgressListener listener)
		{
			progressListenerRelay.setListener(listener);
		}

		public void setQuestListener(VisibleQuestListener listener)
		{
			visibleQuestRelay.setListener(listener);
		}

		public boolean isDownloading()
		{
			synchronized (downloadLock)
			{
				return currentDownload != null && !currentDownload.isFinished();
			}
		}

		public boolean currentDownloadHasPriority()
		{
			synchronized (downloadLock)
			{
				return currentDownload.isPriority();
			}
		}

		public void startForeground()
		{
			progressListenerRelay.startForeground();
		}

		public void stopForeground()
		{
			progressListenerRelay.stopForeground();
		}
	}

	private class ServiceHandler extends Handler
	{
		ServiceHandler(Looper looper) { super(looper); }

		@Override public void handleMessage(Message msg)
		{
			synchronized (downloadLock)
			{
				currentDownload = (QuestDownload) msg.obj;
			}
			try
			{
				currentDownload.setProgressListener(progressListenerRelay);
				currentDownload.setVisibleQuestListener(visibleQuestRelay);
				currentDownload.download();
			}
			catch(Exception e)
			{
				Log.e(TAG, "Unable to download quests", e);
				progressListenerRelay.onError(e);
			}

			stopSelf(msg.arg1);
		}
	}
}
