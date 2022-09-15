package de.westnordost.streetcomplete.screens.main.overlays

import android.os.Bundle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.FragmentContainerActivity

class OverlaySelectionActivity :
    FragmentContainerActivity(R.layout.activity_user) { // TODO how this even works? This layout gets passed down to AppCompatActivity - does it have any effect on anything?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            mainFragment = OverlayFragment()
        }
    }
}
