package de.westnordost.streetcomplete.controls

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.DownloadProgressListener
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.ktx.toPx
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fragment that takes care of showing the download progress */
class DownloadProgressFragment : Fragment(R.layout.fragment_download_progress) {

    @Inject internal lateinit var downloadProgressSource: DownloadProgressSource

    private val progressView get() = view as IconsDownloadProgressView

    private val downloadProgressListener = object : DownloadProgressListener {
        override fun onStarted() { lifecycleScope.launch { animateInProgressView() }}
        override fun onFinished() { lifecycleScope.launch { animateOutProgressView() }}
    }

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressView.setIcon(resources.getDrawable(R.drawable.ic_search_black_128dp))
    }

    override fun onStart() {
        super.onStart()
        updateDownloadProgress()
        downloadProgressSource.addDownloadProgressListener(downloadProgressListener)
    }

    override fun onStop() {
        super.onStop()
        downloadProgressSource.removeDownloadProgressListener(downloadProgressListener)
    }

    private fun animateInProgressView() {
        progressView.animate()
            .setStartDelay(1000)
            .withStartAction { view?.visibility = View.VISIBLE }
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f).scaleY(1f)
            .setDuration(IN_OUT_DURATION)
            .withEndAction(null)
            .start()
    }

    private fun animateOutProgressView() {
        progressView.animate()
            .setStartDelay(0)
            .withStartAction(null)
            .translationY(INITIAL_Y_OFFSET.toPx(requireContext()))
            .alpha(INITIAL_ALPHA)
            .scaleX(INITIAL_SCALE).scaleY(INITIAL_SCALE)
            .setDuration(IN_OUT_DURATION)
            .withEndAction { view?.visibility = View.GONE }
            .start()
    }

    private fun updateDownloadProgress() {
        if (downloadProgressSource.isDownloadInProgress) {
            showProgressView()
        } else {
            hideProgressView()
        }
    }

    private fun showProgressView() {
        view?.visibility = View.VISIBLE
        progressView.scaleX = 1f
        progressView.scaleY = 1f
        progressView.alpha = 1f
        progressView.translationY = 0f
    }

    private fun hideProgressView() {
        view?.visibility = View.GONE
        progressView.scaleX = INITIAL_SCALE
        progressView.scaleY = INITIAL_SCALE
        progressView.alpha = INITIAL_ALPHA
        progressView.translationY = INITIAL_Y_OFFSET.toPx(requireContext())
    }

    companion object {
        const val INITIAL_SCALE = 0.1f
        const val INITIAL_ALPHA = 0.4f
        const val INITIAL_Y_OFFSET = 140f
        const val IN_OUT_DURATION = 300L
    }
}
