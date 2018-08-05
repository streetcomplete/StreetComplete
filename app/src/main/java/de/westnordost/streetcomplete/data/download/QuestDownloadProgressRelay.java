package de.westnordost.streetcomplete.data.download;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;

import de.westnordost.streetcomplete.MainActivity;
import de.westnordost.streetcomplete.ApplicationConstants;
import de.westnordost.streetcomplete.R;

/** Threadsafe relay for QuestDownloadProgressListener. Also, it can show a notification with
 *  progress. See startForeground/stopForeground
 *
 *  (setting the listener and calling the listener methods can safely be done from different threads) */
public class QuestDownloadProgressRelay implements QuestDownloadProgressListener
{
	private final int id;

	private final Notification.Builder notificationBuilder;
	private final Service service;

	private QuestDownloadProgressListener listener;
	private boolean showNotification;

	private Exception occuredError;
	private boolean isDownloading;
	private Float progress;

	public QuestDownloadProgressRelay(Service service, String notificationChannelId, int notificationId)
	{
		this.service = service;
		this.id = notificationId;
		showNotification = false;

		PendingIntent pendingIntent = PendingIntent.getActivity(
				service, 0, new Intent(service, MainActivity.class), 0);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			notificationBuilder = new Notification.Builder(service, notificationChannelId);
		}
		else
		{
			notificationBuilder = new Notification.Builder(service);
		}

		notificationBuilder
				.setSmallIcon(R.mipmap.ic_dl_notification)
				.setContentTitle(ApplicationConstants.NAME)
				.setContentText(service.getResources().getString(R.string.notification_downloading))
				.setContentIntent(pendingIntent);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			notificationBuilder.setCategory(Notification.CATEGORY_PROGRESS);
		}
	}

	@Override synchronized public void onStarted()
	{
		this.isDownloading = true;
		if(showNotification) showProgressNotification(0f);
		if(listener != null) listener.onStarted();
	}

	@Override synchronized public void onNotStarted()
	{
		if(listener != null) listener.onNotStarted();
	}

	@Override synchronized public void onProgress(float progress)
	{
		this.progress = progress;
		if(showNotification) showProgressNotification(progress);
		if(listener != null) listener.onProgress(progress);
	}

	private void showProgressNotification(float progress)
	{
		int progress1000 = (int) (progress*1000);
		Notification n = notificationBuilder.setProgress(1000, progress1000, false).build();
		service.startForeground(id,n);
	}

	@Override synchronized public void onError(Exception e)
	{
		this.occuredError = e;
		if(listener != null)
		{
			listener.onError(e);
			occuredError = null;
		}
	}

	@Override synchronized public void onSuccess()
	{
		if(listener != null) listener.onSuccess();
	}

	@Override synchronized public void onFinished()
	{
		this.isDownloading = false;
		this.progress = null;
		if(showNotification) hideProgressNotification();
		if(listener != null) listener.onFinished();
	}

	private void hideProgressNotification()
	{
		service.stopForeground(true);
	}

	public synchronized void startForeground()
	{
		showNotification = true;

		if (isDownloading)
		{
			showProgressNotification(progress != null ? progress : 0f);
		}
		else
		{
			hideProgressNotification();
		}
	}

	public synchronized void stopForeground()
	{
		showNotification = false;
		hideProgressNotification();
	}

	public synchronized void setListener(QuestDownloadProgressListener listener)
	{
		this.listener = listener;

		// bring listener up-to-date
		if (listener != null)
		{
			if (isDownloading)
			{
				listener.onStarted();
				if(progress != null) listener.onProgress(progress);
			}
			else
			{
				if (occuredError != null)
				{
					listener.onError(occuredError);
					occuredError = null;
				}
			}
		}
	}
}
