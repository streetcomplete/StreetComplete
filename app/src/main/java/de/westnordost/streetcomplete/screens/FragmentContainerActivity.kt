package de.westnordost.streetcomplete.screens

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R

/** An activity that contains one full-screen ("main") fragment */
open class FragmentContainerActivity(
    @LayoutRes contentLayoutId: Int = R.layout.activity_fragment_container,
) : BaseActivity(contentLayoutId) {

    protected fun replaceMainFragment(
        f: Fragment,
        customOptions: (FragmentTransaction.() -> Unit)? = null,
    ) {
        supportFragmentManager.popBackStack("main", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.commit {
            customOptions?.invoke(this)
            replace(R.id.fragment_container, f)
            setPrimaryNavigationFragment(f)
        }
    }
}
