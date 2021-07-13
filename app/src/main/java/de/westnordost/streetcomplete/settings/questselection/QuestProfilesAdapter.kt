package de.westnordost.streetcomplete.settings.questselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestProfile
import de.westnordost.streetcomplete.data.visiblequests.QuestProfilesController
import de.westnordost.streetcomplete.data.visiblequests.QuestProfilesSource
import kotlinx.android.synthetic.main.row_quest_profile.view.*
import kotlinx.coroutines.*
import javax.inject.Inject

class QuestProfilesAdapter @Inject constructor(
    private val context: Context,
    private val questProfilesController: QuestProfilesController
) : RecyclerView.Adapter<QuestProfilesAdapter.QuestProfileViewHolder>(), LifecycleObserver {

    private val profiles: MutableList<QuestProfile> = mutableListOf()

    private val lifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val questProfilesListener = object : QuestProfilesSource.Listener {
        override fun onSelectedQuestProfileChanged() { lifecycleScope.launch {
            notifyDataSetChanged()
        }}

        override fun onAddedQuestProfile(profile: QuestProfile) { lifecycleScope.launch {
            profiles.add(profile)
            notifyItemInserted(profiles.size - 1)
        }}

        override fun onDeletedQuestProfile(profileId: Long) { lifecycleScope.launch {
            val deleteIndex = profiles.indexOfFirst { it.id == profileId }
            profiles.removeAt(deleteIndex)
            notifyItemRemoved(deleteIndex)
        }}
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        profiles.add(QuestProfile(0, context.getString(R.string.quest_profiles_default_name)))
        profiles.addAll(questProfilesController.getAllQuestProfiles())

        questProfilesController.addListener(questProfilesListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        questProfilesController.removeListener(questProfilesListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        lifecycleScope.cancel()
    }

    override fun getItemCount(): Int = profiles.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestProfileViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.row_quest_profile, parent, false)
        return QuestProfileViewHolder(layout)
    }

    override fun onBindViewHolder(holder: QuestProfileViewHolder, position: Int) {
        holder.onBind(profiles[position])
    }

    inner class QuestProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onBind(with: QuestProfile) {
            itemView.selectionRadioButton.text = with.name
            itemView.selectionRadioButton.setOnCheckedChangeListener(null)
            itemView.selectionRadioButton.isChecked = questProfilesController.selectedQuestProfileId == with.id
            itemView.selectionRadioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onSelectQuestProfile(with.id)
            }
            itemView.deleteButton.isEnabled = true
            itemView.deleteButton.isInvisible = with.id == 0L
            itemView.deleteButton.setOnClickListener { onClickDeleteQuestProfile(with.id) }
        }

        fun onSelectQuestProfile(profileId: Long) {
            lifecycleScope.launch(Dispatchers.IO) {
                questProfilesController.selectedQuestProfileId = profileId
            }
        }

        fun onClickDeleteQuestProfile(profileId: Long) {
            AlertDialog.Builder(itemView.context)
                .setMessage(R.string.quest_profiles_delete_message)
                .setPositiveButton(R.string.delete_confirmation) { _,_ -> deleteQuestProfile(profileId) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        fun deleteQuestProfile(profileId: Long) {
            itemView.deleteButton.isEnabled = false
            lifecycleScope.launch(Dispatchers.IO) {
                questProfilesController.deleteQuestProfile(profileId)
            }
        }
    }
}
