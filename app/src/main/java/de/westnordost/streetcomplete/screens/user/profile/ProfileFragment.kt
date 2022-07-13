package de.westnordost.streetcomplete.screens.user.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.databinding.FragmentProfileBinding
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.tryStartActivity
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.LaurelWreath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.io.File
import java.lang.Math.max
import java.lang.Math.min
import java.util.Locale

/** Shows the user profile: username, avatar, star count and a hint regarding unpublished changes */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val userDataSource: UserDataSource by inject()
    private val userLoginStatusController: UserLoginStatusController by inject()
    private val userUpdater: UserUpdater by inject()
    private val statisticsSource: StatisticsSource by inject()
    private val achievementsSource: AchievementsSource by inject()
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource by inject()
    private val avatarsCacheDirectory: File by inject(named("AvatarsCacheDirectory"))

    private lateinit var anonAvatar: Bitmap

    private val binding by viewBinding(FragmentProfileBinding::bind)

    private val unsyncedChangesCountListener = object : UnsyncedChangesCountSource.Listener {
        override fun onIncreased() { viewLifecycleScope.launch { updateUnpublishedEditsText() } }
        override fun onDecreased() { viewLifecycleScope.launch { updateUnpublishedEditsText() } }
    }
    private val statisticsListener = object : StatisticsSource.Listener {
        override fun onAddedOne(type: String) {
            viewLifecycleScope.launch { updateEditCountText() }
        }
        override fun onSubtractedOne(type: String) {
            viewLifecycleScope.launch { updateEditCountText() }
        }
        override fun onUpdatedAll() {
            viewLifecycleScope.launch { updateStatisticsTexts() }
        }
        override fun onCleared() {
            viewLifecycleScope.launch { updateStatisticsTexts() }
        }
        override fun onUpdatedDaysActive() {
            viewLifecycleScope.launch { updateDaysActiveText() }
        }
    }
    private val userListener = object : UserDataSource.Listener {
        override fun onUpdated() { viewLifecycleScope.launch { updateUserName() } }
    }
    private val userAvatarListener = object : UserUpdater.Listener {
        override fun onUserAvatarUpdated() { viewLifecycleScope.launch { updateAvatar() } }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        anonAvatar = context.getDrawable(R.drawable.ic_osm_anon_avatar)!!.createBitmap()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutButton.setOnClickListener {
            userLoginStatusController.logOut()
        }
        binding.profileButton.setOnClickListener {
            openUrl("https://www.openstreetmap.org/user/" + userDataSource.userName)
        }
    }

    override fun onStart() {
        super.onStart()

        viewLifecycleScope.launch {
            userDataSource.addListener(userListener)
            userUpdater.addUserAvatarListener(userAvatarListener)
            statisticsSource.addListener(statisticsListener)
            unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)

            updateUserName()
            updateAvatar()
            updateEditCountText()
            updateUnpublishedEditsText()
            updateDaysActiveText()
            updateGlobalRankText()
            updateLocalRankText()
            updateAchievementLevelsText()
        }
    }

    override fun onStop() {
        super.onStop()
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
        statisticsSource.removeListener(statisticsListener)
        userDataSource.removeListener(userListener)
        userUpdater.removeUserAvatarListener(userAvatarListener)
    }

    private fun updateUserName() {
        binding.userNameTextView.text = userDataSource.userName
    }

    private fun updateAvatar() {
        val avatarFile = File(avatarsCacheDirectory.toString() + File.separator + userDataSource.userId)
        val avatar = if (avatarFile.exists()) BitmapFactory.decodeFile(avatarFile.path) else anonAvatar
        binding.userAvatarImageView.setImageBitmap(avatar)
    }

    private suspend fun updateStatisticsTexts() {
        updateEditCountText()
        updateDaysActiveText()
        updateGlobalRankText()
        updateLocalRankText()
    }

    private suspend fun updateEditCountText() {
        binding.editCountText.text = withContext(Dispatchers.IO) { statisticsSource.getEditCount().toString() }
    }

    private suspend fun updateUnpublishedEditsText() {
        val unsyncedChanges = unsyncedChangesCountSource.getCount()
        binding.unpublishedEditCountText.text = getString(R.string.unsynced_quests_description, unsyncedChanges)
        binding.unpublishedEditCountText.isGone = unsyncedChanges <= 0
    }

    private fun updateDaysActiveText() {
        val daysActive = statisticsSource.daysActive
        binding.daysActiveContainer.isGone = daysActive <= 0
        binding.daysActiveText.text = daysActive.toString()
        binding.daysActiveText.background = LaurelWreath(resources, min(daysActive + 20, 100))
    }

    private fun updateGlobalRankText() {
        // note that global rank merges multiple people with the same score
        // in case that 1000 people made 11 edits all will have the same rank (say, 3814)
        // in case that 1000 people made 10 edits all will have the same rank (in this case - 3815)
        val rank = statisticsSource.rank
        binding.globalRankContainer.isGone = rank <= 0 || statisticsSource.getEditCount() <= 100
        binding.globalRankText.text = "#$rank"
        val rankEnoughForFullMarks = 1000
        val rankEnoughToStartGrowingReward = 3800
        val ranksAboveThreshold = max(rankEnoughToStartGrowingReward - rank, 0)
        val scaledRank = (ranksAboveThreshold * 100.0 / (rankEnoughToStartGrowingReward - rankEnoughForFullMarks)).toInt()
        binding.globalRankText.background = LaurelWreath(resources, min(scaledRank, 100))
    }

    private suspend fun updateLocalRankText() {
        val statistics = withContext(Dispatchers.IO) {
            statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount()
        }
        if (statistics == null) binding.localRankContainer.isGone = true
        else {
            val shouldShow = statistics.rank != null && statistics.rank > 0 && statistics.count > 50
            val countryLocale = Locale("", statistics.countryCode)
            binding.localRankContainer.isGone = !shouldShow
            if (shouldShow) {
                binding.localRankText.text = "#${statistics.rank}"
                binding.localRankLabel.text = getString(R.string.user_profile_local_rank, countryLocale.displayCountry)
                binding.localRankText.background = LaurelWreath(resources, min(100 - statistics.rank!!, 100))
            }
        }
    }

    private suspend fun updateAchievementLevelsText() {
        val levels = withContext(Dispatchers.IO) { achievementsSource.getAchievements().sumOf { it.second } }
        binding.achievementLevelsContainer.isGone = levels <= 0
        binding.achievementLevelsText.text = "$levels"
        binding.achievementLevelsText.background = LaurelWreath(resources, min(levels / 2, 100))
    }

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        return tryStartActivity(intent)
    }
}
