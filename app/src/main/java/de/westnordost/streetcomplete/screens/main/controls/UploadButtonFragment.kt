package de.westnordost.streetcomplete.screens.main.controls

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.view.dialogs.RequestLoginDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Fragment that shows the upload button, including upload progress etc. */
class UploadButtonFragment : Fragment(R.layout.fragment_upload_button) {

    private val viewModel by viewModel<UploadButtonViewModel>()
    private val uploadButton get() = view as UploadButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadButton.setOnClickListener {
            if (viewModel.isConnected) {
                if (viewModel.isLoggedIn) {
                    viewModel.upload()
                } else {
                    context?.let { RequestLoginDialog(it).show() }
                }
            } else {
                context?.toast(R.string.offline)
            }
        }

        observe(viewModel.isAutosync) { uploadButton.isGone = it }
        observe(viewModel.unsyncedChangesCount) { uploadButton.uploadableCount = it }
        observe(viewModel.isUploadInProgress) { uploadButton.isEnabled = !it }
    }
}
