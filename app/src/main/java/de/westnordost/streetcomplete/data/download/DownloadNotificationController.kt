package de.westnordost.streetcomplete.data.download

import android.app.*
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.MainActivity
import de.westnordost.streetcomplete.R

/** Shows the download progress in the Android notifications area */
class DownloadNotificationController(
    private val service: Service,
    notificationChannelId: String,
    private val notificationId: Int
) {
    private val notificationBuilder = createNotificationBuilder(notificationChannelId)

    fun show() {
        service.startForeground(notificationId, notificationBuilder.build())
    }

    fun hide() {
        service.stopForeground(true)
    }

    private fun createNotificationBuilder(notificationChannelId: String): NotificationCompat.Builder {
        val pendingIntent = PendingIntent.getActivity(service, 0, Intent(service, MainActivity::class.java), 0)
        val manager = NotificationManagerCompat.from(service)
        var channel = manager.getNotificationChannelCompat(notificationChannelId)
        if (channel == null) {
            channel = NotificationChannelCompat.Builder(notificationChannelId,
                NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(service.getString(R.string.notification_channel_download))
                .build()
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(service, notificationChannelId)
            .setSmallIcon(R.mipmap.ic_dl_notification)
            .setContentTitle(ApplicationConstants.NAME)
            .setContentText(service.resources.getString(R.string.notification_downloading))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    }
}
