package de.westnordost.streetcomplete.controls

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.notifications.*
import de.westnordost.streetcomplete.ktx.popIn
import de.westnordost.streetcomplete.ktx.popOut
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Handles showing a button with a little counter that shows how many unread notifications there are */
class NotificationButtonFragment : Fragment(R.layout.fragment_notification_button) {

    @Inject lateinit var notificationsSource: NotificationsSource

    interface Listener {
        fun onClickShowNotification(notification: Notification)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val notificationButton get() = view as NotificationButton

    private var notificationsSourceUpdateListener = object : NotificationsSource.UpdateListener {
        override fun onNumberOfNotificationsUpdated(numberOfNotifications: Int) {
            viewLifecycleScope.launch { updateButtonStateAnimated(numberOfNotifications) }
        }
    }

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notificationButton.setOnClickListener { viewLifecycleScope.launch { onClickButton() } }
    }

    override fun onStart() {
        super.onStart()
        notificationsSource.addListener(notificationsSourceUpdateListener)
        viewLifecycleScope.launch { initializeButtonState() }
    }

    override fun onStop() {
        super.onStop()
        notificationsSource.removeListener(notificationsSourceUpdateListener)
    }

    private suspend fun initializeButtonState() {
        val numberOfNotifications = withContext(Dispatchers.IO) { notificationsSource.getNumberOfNotifications() }
        notificationButton.notificationsCount = numberOfNotifications
        notificationButton.isGone = numberOfNotifications <= 0
    }

    private fun updateButtonStateAnimated(numberOfNotifications: Int) {
        notificationButton.notificationsCount = numberOfNotifications
        if (notificationButton.isVisible && numberOfNotifications == 0) {
            notificationButton.popOut()
        } else if(!notificationButton.isVisible && numberOfNotifications > 0) {
            notificationButton.popIn()
        }
    }

    private suspend fun onClickButton() {
        val notification = withContext(Dispatchers.IO) { notificationsSource.popNextNotification() }
        if (notification != null) {
            listener?.onClickShowNotification(notification)
        }
    }
}
