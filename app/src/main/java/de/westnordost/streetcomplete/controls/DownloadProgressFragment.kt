package de.westnordost.streetcomplete.controls

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.DownloadProgressListener
import de.westnordost.streetcomplete.data.download.DownloadProgressSource
import de.westnordost.streetcomplete.ktx.toPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fragment that takes care of showing the download progress */
class DownloadProgressFragment : Fragment(R.layout.fragment_download_progress),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @Inject internal lateinit var downloadProgressSource: DownloadProgressSource

    private val mainHandler = Handler(Looper.getMainLooper())

    private val progressView get() = view as IconsDownloadProgressView

    private val animateOutRunnable = Runnable { animateOutProgressView() }

    private val downloadProgressListener = object : DownloadProgressListener {

        override fun onStarted() {
            launch(Dispatchers.Main) {
                animateInProgressView()
                progressView.enqueueIcon(resources.getDrawable(R.drawable.ic_search_black_128dp))
            }
        }

        override fun onFinished() {
            mainHandler.postDelayed(animateOutRunnable, 1000)
        }
    }

    init {
        Injector.applicationComponent.inject(this)
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

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        coroutineContext.cancel()
    }

    private fun animateInProgressView() {
        mainHandler.removeCallbacks(animateOutRunnable)
        view?.visibility = View.VISIBLE
        progressView.animate()
            .translationY(0f)
            .alpha(1f)
            .scaleX(1f).scaleY(1f)
            .setDuration(IN_OUT_DURATION)
            .start()
    }

    private fun animateOutProgressView() {
        progressView.animate()
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
