package de.westnordost.streetcomplete.util.ktx

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

fun Fragment.openUri(uri: String) = context?.openUri(uri) ?: false

val Fragment.childFragmentManagerOrNull: FragmentManager? get() =
    if (host != null) childFragmentManager else null

val Fragment.viewLifecycleScope get() = viewLifecycleOwner.lifecycleScope

fun <T> Fragment.observe(flow: SharedFlow<T>, collector: FlowCollector<T>) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect {
                collector.emit(it)
            }
        }
    }
}
