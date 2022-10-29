package de.westnordost.streetcomplete.screens.user.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.Achievement
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.databinding.FragmentProfileBinding
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.tryStartActivity
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.LaurelWreathDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.io.File
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

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
            viewLifecycleScope.launch { updateEditCountTexts() }
        }
        override fun onSubtractedOne(type: String) {
            viewLifecycleScope.launch { updateEditCountTexts()  }
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
    private val achievementsListener = object : AchievementsSource.Listener {
        override fun onAchievementUnlocked(achievement: Achievement, level: Int) {
            viewLifecycleScope.launch { updateAchievementLevelsText() }
        }

        override fun onAllAchievementsUpdated() {
            viewLifecycleScope.launch { updateAchievementLevelsText() }
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
            achievementsSource.addListener(achievementsListener)

            updateUserName()
            updateAvatar()
            updateEditCountTexts()
            updateUnpublishedEditsText()
            updateDaysActiveText()
            updateGlobalRankTexts()
            updateLocalRankTexts()
            updateAchievementLevelsText()
        }
    }

    override fun onStop() {
        super.onStop()
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
        statisticsSource.removeListener(statisticsListener)
        userDataSource.removeListener(userListener)
        userUpdater.removeUserAvatarListener(userAvatarListener)
        achievementsSource.removeListener(achievementsListener)
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
        updateEditCountTexts()
        updateDaysActiveText()
        updateGlobalRankTexts()
        updateLocalRankTexts()
    }

    private suspend fun updateEditCountTexts() {
        binding.editCountText.text = withContext(Dispatchers.IO) { statisticsSource.getEditCount().toString() }
        binding.currentWeekEditCountText.text = withContext(Dispatchers.IO) { statisticsSource.getCurrentWeekEditCount().toString() }
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
        binding.daysActiveText.background = LaurelWreathDrawable(resources, min(daysActive + 20, 100))
    }

    private fun updateGlobalRankTexts() {
        updateGlobalRankText(
            statisticsSource.rank,
            binding.globalRankContainer,
            binding.globalRankText
        )

        updateGlobalRankText(
            statisticsSource.currentWeekRank,
            binding.currentWeekGlobalRankContainer,
            binding.currentWeekGlobalRankText
        )
    }

    private fun updateGlobalRankText(rank: Int, container: View, circle: TextView ) {
        container.isGone = rank <= 0 || statisticsSource.getEditCount() <= 100
        circle.text = "#$rank"
        circle.background = LaurelWreathDrawable(resources, getScaledGlobalRank(rank))
    }

    /** Translate the user's actual rank to a value from 0 (bad) to 100 (good) */
    private fun getScaledGlobalRank(rank: Int): Int {
        // note that global rank merges multiple people with the same score
        // in case that 1000 people made 11 edits all will have the same rank (say, 3814)
        // in case that 1000 people made 10 edits all will have the same rank (in this case - 3815)
        val rankEnoughForFullMarks = 1000
        val rankEnoughToStartGrowingReward = 3800
        val ranksAboveThreshold = max(rankEnoughToStartGrowingReward - rank, 0)
        return min(100, (ranksAboveThreshold * 100.0 / (rankEnoughToStartGrowingReward - rankEnoughForFullMarks)).toInt())
    }

    private suspend fun updateLocalRankTexts() {
        updateLocalRankText(
            withContext(Dispatchers.IO) { statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount() },
            50,
            binding.localRankContainer,
            binding.localRankLabel,
            binding.localRankText
        )

        updateLocalRankText(
            withContext(Dispatchers.IO) { statisticsSource.getCurrentWeekCountryStatisticsOfCountryWithBiggestSolvedCount() },
            5,
            binding.currentWeekLocalRankContainer,
            binding.currentWeekLocalRankLabel,
            binding.currentWeekLocalRankText
        )
    }

    private fun updateLocalRankText(statistics: CountryStatistics?, min: Int, container: View, label: TextView, circle: TextView) {
        val rank = statistics?.rank ?: 0
        container.isGone = statistics == null || rank <= 0 || statistics.count <= min
        circle.text = "#$rank"
        val countryLocale = Locale("", statistics?.countryCode ?: "")
        label.text = getString(R.string.user_profile_local_rank, countryLocale.displayCountry)
        circle.background = LaurelWreathDrawable(resources, min(100 - rank, 100))
    }

    private suspend fun updateAchievementLevelsText() {
        val levels = withContext(Dispatchers.IO) { achievementsSource.getAchievements().sumOf { it.second } }
        binding.achievementLevelsContainer.isGone = levels <= 0
        binding.achievementLevelsText.text = "$levels"
        binding.achievementLevelsText.background = LaurelWreathDrawable(resources, min(levels / 2, 100))
    }

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        return tryStartActivity(intent)
    }
}
