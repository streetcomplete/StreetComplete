package de.westnordost.streetcomplete.data.sync

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.westnordost.streetcomplete.ApplicationConstants.NAME
import de.westnordost.streetcomplete.ApplicationConstants.NOTIFICATIONS_CHANNEL_SYNC
import de.westnordost.streetcomplete.MainActivity
import de.westnordost.streetcomplete.R

/** Creates the notification for syncing in the Android notifications area. Used both by the upload
 *  and by the download service. */
fun createSyncNotification(context: Context): Notification {
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
    val manager = NotificationManagerCompat.from(context)
    if (manager.getNotificationChannelCompat(NOTIFICATIONS_CHANNEL_SYNC) == null) {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(NOTIFICATIONS_CHANNEL_SYNC, IMPORTANCE_LOW)
                .setName(context.getString(R.string.notification_channel_sync))
                .build()
        )
    }

    return NotificationCompat.Builder(context, NOTIFICATIONS_CHANNEL_SYNC)
        .setSmallIcon(R.mipmap.ic_notification)
        .setContentTitle(NAME)
        .setContentText(context.resources.getString(R.string.notification_syncing))
        .setContentIntent(pendingIntent)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .build()
}
