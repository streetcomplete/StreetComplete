package de.westnordost.streetcomplete.quests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.FragmentQuestAnswerBinding
import de.westnordost.streetcomplete.ktx.popIn
import de.westnordost.streetcomplete.ktx.popOut
import de.westnordost.streetcomplete.ktx.toast

/** Abstract base class for dialogs in which the user answers a quest with a form he has to fill
 * out  */
abstract class AbstractQuestFormAnswerFragment<T> : AbstractQuestAnswerFragment<T>() {

    private var _binding: FragmentQuestAnswerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        /* the binding must be initialized already in onCreateView because checkIsFormComplete() may
           be called by a subclass already in onCreateView, which in turn accesses the binding,
           see #3590 */
        _binding = FragmentQuestAnswerBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.okButton.setOnClickListener {
            if (!isFormComplete()) {
                activity?.toast(R.string.no_changes)
            } else {
                onClickOk()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected fun checkIsFormComplete() {
        if (isFormComplete()) {
            binding.okButton.popIn()
        } else {
            binding.okButton.popOut()
        }
    }

    protected abstract fun onClickOk()

    abstract fun isFormComplete(): Boolean

    override fun isRejectingClose() = isFormComplete()
}
