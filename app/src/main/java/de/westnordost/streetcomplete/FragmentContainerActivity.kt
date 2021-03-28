package de.westnordost.streetcomplete

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit

/** An activity that contains one full-screen ("main") fragment */
open class FragmentContainerActivity(
    @LayoutRes contentLayoutId: Int = R.layout.activity_fragment_container
) : AppCompatActivity(contentLayoutId) {

    var mainFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)
        set(value) {
            supportFragmentManager.popBackStack("main", POP_BACK_STACK_INCLUSIVE)
            if (value != null) {
                supportFragmentManager.commit { replace(R.id.fragment_container, value) }
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
    }

    fun pushMainFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.enter_from_left, R.anim.exit_to_right
            )
            replace(R.id.fragment_container, fragment)
            addToBackStack("main")
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment.id == R.id.fragment_container) {
            if (fragment is HasTitle) {
                title = (fragment as HasTitle).title
            }
        }
    }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        (mainFragment as? IntentListener)?.onNewIntent(intent)
    }
}
