package de.westnordost.streetcomplete.data.sync

import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import de.westnordost.streetcomplete.ApplicationConstants.NAME
import de.westnordost.streetcomplete.ApplicationConstants.NOTIFICATIONS_CHANNEL_SYNC
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.MainActivity

/** Creates the notification for syncing in the Android notifications area. Used both by the upload
 *  and by the download service. */
fun createSyncNotification(context: Context): Notification {
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE)
    } else {
        PendingIntent.getActivity(context, 0, intent, 0)
    }
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
        .setContentTitle(context.resources.getString(R.string.app_name))
        .setContentText(context.resources.getString(R.string.notification_syncing))
        .setContentIntent(pendingIntent)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .build()
}
