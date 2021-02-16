package de.westnordost.streetcomplete.controls

import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.ktx.toast
import kotlinx.android.synthetic.main.fragment_main_menu_button.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/** Fragment that shows the main menu button and manages its logic */
class MainMenuButtonFragment : Fragment(R.layout.fragment_main_menu_button),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var downloadController: DownloadController

    interface Listener {
        fun getDownloadArea(): BoundingBox?
    }

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.mainMenuButton.setOnClickListener { onClickMainMenu() }
    }

    /* ------------------------------------------------------------------------------------------ */

    internal fun onClickMainMenu() {
        context?.let { MainMenuDialog(it, this::onClickDownload).show() }
    }

    /* ------------------------------------ Download Button  ------------------------------------ */

    private fun onClickDownload() {
        if (isConnected()) downloadDisplayedArea()
        else context?.toast(R.string.offline)
    }

    private fun isConnected(): Boolean {
        val connectivityManager = context?.getSystemService<ConnectivityManager>()
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun downloadDisplayedArea() {
        val downloadArea = listener?.getDownloadArea() ?: return

        if (downloadController.isPriorityDownloadInProgress) {
            context?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.confirmation_cancel_prev_download_title)
                    .setPositiveButton(R.string.confirmation_cancel_prev_download_confirmed) { _, _ ->
                        downloadController.download(downloadArea, true)
                    }
                    .setNegativeButton(R.string.confirmation_cancel_prev_download_cancel, null)
                    .show()
            }
        } else {
            downloadController.download(downloadArea, true)
        }
    }
}
