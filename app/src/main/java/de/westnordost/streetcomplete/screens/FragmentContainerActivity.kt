package de.westnordost.streetcomplete.screens

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.R

/** An activity that contains one full-screen ("main") fragment */
open class FragmentContainerActivity(
    @LayoutRes contentLayoutId: Int = R.layout.activity_fragment_container
) : BaseActivity(contentLayoutId), DisplaysTitle {

    var mainFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)
        set(value) {
            supportFragmentManager.popBackStack("main", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            if (value != null) {
                supportFragmentManager.commit { replace(R.id.fragment_container, value) }
            }
        }

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fragmentManager: FragmentManager, fragment: Fragment) {
            if (fragment.id == R.id.fragment_container && fragment is HasTitle) {
                updateTitle(fragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false)
    }

    fun pushMainFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.enter_from_end, R.anim.exit_to_start,
                R.anim.enter_from_start, R.anim.exit_to_end
            )
            replace(R.id.fragment_container, fragment)
            addToBackStack("main")
        }
    }

    override fun updateTitle(fragment: HasTitle) {
        title = fragment.title
        supportActionBar?.subtitle = fragment.subtitle
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if ((mainFragment as? BackPressedListener)?.onBackPressed() == true) {
            return
        }
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        } else super.onOptionsItemSelected(item)
    }
}
