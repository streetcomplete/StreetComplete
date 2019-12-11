package de.westnordost.streetcomplete.data.download

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.MainActivity
import de.westnordost.streetcomplete.R

/** Shows a download progress notification */
class QuestDownloadNotification(
    private val service: Service,
    notificationChannelId: String,
    private val notificationId: Int
) {
    private val notificationBuilder = createNotificationBuilder(notificationChannelId)

    fun showProgress(progress: Float) {
        val progress1000 = (progress * 1000).toInt()
        val n = notificationBuilder.setProgress(1000, progress1000, false).build()
        service.startForeground(notificationId, n)
    }

    fun hide() {
        service.stopForeground(true)
    }

    private fun createNotificationBuilder(notificationChannelId: String): Notification.Builder {
        val pendingIntent = PendingIntent.getActivity(service, 0, Intent(service, MainActivity::class.java), 0)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            Notification.Builder(service, notificationChannelId)
        } else {
            Notification.Builder(service)
        }
        builder
            .setSmallIcon(R.mipmap.ic_dl_notification)
            .setContentTitle(ApplicationConstants.NAME)
            .setContentText(service.resources.getString(R.string.notification_downloading))
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_PROGRESS)
        }
        return builder
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val mgr = (service.application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        mgr.createNotificationChannel(
            NotificationChannel(
                ApplicationConstants.NOTIFICATIONS_CHANNEL_DOWNLOAD,
                service.getString(R.string.notification_channel_download),
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }
}
