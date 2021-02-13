package de.westnordost.streetcomplete.controls

import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.DownloadController
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.map.MainFragment
import de.westnordost.streetcomplete.util.area
import de.westnordost.streetcomplete.util.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import kotlinx.android.synthetic.main.fragment_main_menu_button.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.sqrt

/** Fragment that shows the main menu button and manages its logic */
class MainMenuButtonFragment : Fragment(R.layout.fragment_main_menu_button),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var downloadController: DownloadController

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
        val displayArea = (requireParentFragment() as MainFragment).mapFragment?.getDisplayedArea()
        if (displayArea == null) {
            context?.toast(R.string.cannot_find_bbox_or_reduce_tilt, Toast.LENGTH_LONG)
        } else {
            val enclosingBBox = displayArea.asBoundingBoxOfEnclosingTiles(ApplicationConstants.QUEST_TILE_ZOOM)
            val areaInSqKm = enclosingBBox.area() / 1000000
            if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM) {
                context?.toast(R.string.download_area_too_big, Toast.LENGTH_LONG)
            } else {
                if (downloadController.isPriorityDownloadInProgress) {
                    context?.let {
                        AlertDialog.Builder(it)
                            .setMessage(R.string.confirmation_cancel_prev_download_title)
                            .setPositiveButton(R.string.confirmation_cancel_prev_download_confirmed) { _, _ ->
                                downloadAreaConfirmed(enclosingBBox)
                            }
                            .setNegativeButton(R.string.confirmation_cancel_prev_download_cancel, null)
                            .show()
                    }
                } else {
                    downloadAreaConfirmed(enclosingBBox)
                }
            }
        }
    }

    private fun downloadAreaConfirmed(bbox: BoundingBox) {
        var bbox = bbox
        val areaInSqKm = bbox.area() / 1000000
        // below a certain threshold, it does not make sense to download, so let's enlarge it
        if (areaInSqKm < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM) {
            val cameraPosition = (requireParentFragment() as MainFragment).mapFragment?.cameraPosition
            if (cameraPosition != null) {
                val radius = sqrt( 1000000 * ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM / PI)
                bbox = cameraPosition.position.enclosingBoundingBox(radius)
            }
        }
        downloadController.download(bbox, true)
    }
}
