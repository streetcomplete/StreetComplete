package de.westnordost.streetcomplete.ktx

import android.view.LayoutInflater
import android.view.View
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

    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        return getBinding()
    }

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
    noinline viewBinder: (View) -> T
) = FragmentViewBindingPropertyDelegate(this, viewBinder)

class FragmentViewBindingPropertyDelegate<T : ViewBinding>(
    private val fragment: Fragment,
    private val viewBinder: (View) -> T
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
            binding = viewBinder(thisRef.requireView())
            fragment.viewLifecycleOwner.lifecycle.addObserver(this)
        }
        return binding!!
    }

}
