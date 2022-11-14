package de.westnordost.streetcomplete.screens.main.overlays

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.FragmentContainerActivity

class OverlaySelectionActivity :
    FragmentContainerActivity(R.layout.activity_user) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mainFragment = OverlayFragment()
        }
    }
}
