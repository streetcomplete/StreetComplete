package de.westnordost.streetcomplete.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by sumanabhi
 * on 20,May,2021
 * at 15:49
 **/

inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(
    noinline viewInflater: (LayoutInflater) -> T
) = ActivityBindingPropertyDelegate(this, viewInflater)

class ActivityBindingPropertyDelegate<T : ViewBinding>(
    private val activity: AppCompatActivity,
    private val viewInflater: (LayoutInflater) -> T
) : ReadOnlyProperty<AppCompatActivity, T>, LifecycleEventObserver {

    private var binding: T? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T = getBinding()

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
        if (event == Event.ON_CREATE) {
            activity.setContentView(getBinding().root)
        } else if (event == Event.ON_DESTROY) {
            binding = null
            source.lifecycle.removeObserver(this)
        }
    }

    private fun getBinding(): T {
        if (binding == null) {
            binding = viewInflater(activity.layoutInflater)
        }
        return binding!!
    }
}

inline fun <reified T : ViewBinding> Fragment.viewBinding(
    noinline viewBinder: (View) -> T,
    rootViewId: Int? = null
) = FragmentViewBindingPropertyDelegate(this, viewBinder, rootViewId)

class FragmentViewBindingPropertyDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val viewBinder: (View) -> T,
    private val rootViewId: Int? = null
) : ReadOnlyProperty<Fragment, T>, LifecycleEventObserver {

    private var binding: T? = null

    override fun onStateChanged(source: LifecycleOwner, event: Event) {
        if (event == Event.ON_DESTROY) {
            binding = null
            source.lifecycle.removeObserver(this)
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (binding == null) {
            val rootView = if (rootViewId != null) {
                thisRef.requireView().findViewById<ViewGroup>(rootViewId)!!.getChildAt(0)
            } else {
                thisRef.requireView()
            }
            binding = viewBinder(rootView)
            fragment.viewLifecycleOwner.lifecycle.addObserver(this)
        }
        return binding!!
    }
}
