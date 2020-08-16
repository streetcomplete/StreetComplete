package de.westnordost.streetcomplete.data.download

import android.app.*
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.MainActivity
import de.westnordost.streetcomplete.R

/** Shows the download progress in the Android notifications area */
class QuestDownloadNotificationController(
    private val service: Service,
    private val notificationChannelId: String,
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        return NotificationCompat.Builder(service, notificationChannelId)
            .setSmallIcon(R.mipmap.ic_dl_notification)
            .setContentTitle(ApplicationConstants.NAME)
            .setContentText(service.resources.getString(R.string.notification_downloading))
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val mgr = service.application.getSystemService<NotificationManager>()!!
        mgr.createNotificationChannel(
            NotificationChannel(
                notificationChannelId,
                service.getString(R.string.notification_channel_download),
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }
}
