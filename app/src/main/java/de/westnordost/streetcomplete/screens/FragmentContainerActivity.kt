package de.westnordost.streetcomplete.screens

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R

/** An activity that contains one full-screen ("main") fragment */
open class FragmentContainerActivity(
    @LayoutRes contentLayoutId: Int = R.layout.activity_fragment_container,
) : BaseActivity(contentLayoutId) {

    var mainFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)
        set(value) {
            supportFragmentManager.popBackStack("main", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            if (value != null) {
                supportFragmentManager.commit { replace(R.id.fragment_container, value) }
            }
        }
}
