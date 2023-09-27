package de.westnordost.streetcomplete.screens.user.profile

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Prefs
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
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.LaurelWreathDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    private val prefs: SharedPreferences by inject()

    private lateinit var anonAvatar: Bitmap

    private val animations = ArrayList<Animator>()

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

        binding.localRankText.background = LaurelWreathDrawable(resources)
        binding.globalRankText.background = LaurelWreathDrawable(resources)
        binding.daysActiveText.background = LaurelWreathDrawable(resources)
        binding.achievementLevelsText.background = LaurelWreathDrawable(resources)
        binding.currentWeekLocalRankText.background = LaurelWreathDrawable(resources)
        binding.currentWeekGlobalRankText.background = LaurelWreathDrawable(resources)

        binding.logoutButton.setOnClickListener {
            userLoginStatusController.logOut()
        }
        binding.profileButton.setOnClickListener {
            openUri("https://www.openstreetmap.org/user/" + userDataSource.userName)
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
            updateDatesActiveView()
        }
    }

    override fun onPause() {
        super.onPause()
        animations.forEach { it.pause() }
    }

    override fun onResume() {
        super.onResume()
        animations.forEach { it.resume() }
    }

    override fun onStop() {
        super.onStop()
        unsyncedChangesCountSource.removeListener(unsyncedChangesCountListener)
        statisticsSource.removeListener(statisticsListener)
        userDataSource.removeListener(userListener)
        userUpdater.removeUserAvatarListener(userAvatarListener)
        achievementsSource.removeListener(achievementsListener)

        animations.forEach { it.end() }
        animations.clear()
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
        updateDatesActiveView()
    }

    private suspend fun updateDatesActiveView() {
        val context = context ?: return

        val datesActive = withContext(Dispatchers.IO) { statisticsSource.getActiveDates() }.toSet()
        binding.datesActiveView.setImageDrawable(DatesActiveDrawable(
            datesActive,
            statisticsSource.activeDatesRange,
            context.dpToPx(18),
            context.dpToPx(2),
            context.dpToPx(4),
            context.resources.getColor(R.color.hint_text)
        ))
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
        binding.daysActiveText.background.level = min(daysActive + 20, 100) * 100
    }

    private fun updateGlobalRankTexts() {
        val rank = statisticsSource.rank
        updateGlobalRankText(
            rank,
            prefs.getInt(Prefs.LAST_SHOWN_USER_GLOBAL_RANK, -1),
            binding.globalRankContainer,
            binding.globalRankText
        )
        prefs.edit { putInt(Prefs.LAST_SHOWN_USER_GLOBAL_RANK, rank) }

        val rankCurrentWeek = statisticsSource.currentWeekRank
        updateGlobalRankText(
            rankCurrentWeek,
            prefs.getInt(Prefs.LAST_SHOWN_USER_GLOBAL_RANK_CURRENT_WEEK, -1),
            binding.currentWeekGlobalRankContainer,
            binding.currentWeekGlobalRankText
        )
        prefs.edit { putInt(Prefs.LAST_SHOWN_USER_GLOBAL_RANK_CURRENT_WEEK, rankCurrentWeek) }
    }

    private fun updateGlobalRankText(rank: Int, previousRank: Int, container: View, circle: TextView ) {
        val shouldHide = rank <= 0 || statisticsSource.getEditCount() <= 100
        container.isGone = shouldHide
        if (shouldHide) return

        val updateRank = { r: Int ->
            circle.text = "#$r"
            circle.background.level = getScaledGlobalRank(r)
        }

        if (previousRank <= 0 || previousRank < rank) {
            updateRank(rank)
        } else {
            animate(previousRank, rank, container, updateRank)
        }
    }

    private fun getScaledGlobalRank(rank: Int): Int {
        // note that global rank merges multiple people with the same score
        // in case that 1000 people made 11 edits all will have the same rank (say, 3814)
        // in case that 1000 people made 10 edits all will have the same rank (in this case - 3815)
        return getScaledRank(rank, 1000, 3800)
    }

    private fun getScaledLocalRank(rank: Int): Int {
        // very tricky as area may have thousands of users or just few
        // lets say that being one of two active people in a given area is also praiseworthy
        return getScaledRank(rank, 10, 100)
    }

    /** Translate the user's actual rank to a value from 0 (bad) to 10000 (the best) */
    private fun getScaledRank(rank: Int, rankEnoughForFullMarks: Int, rankEnoughToStartGrowingReward: Int): Int {
        val ranksAboveThreshold = max(rankEnoughToStartGrowingReward - rank, 0)
        return min(10000, (ranksAboveThreshold * 10000.0 / (rankEnoughToStartGrowingReward - rankEnoughForFullMarks)).toInt())
    }

    private suspend fun updateLocalRankTexts() {
        val localRank = withContext(Dispatchers.IO) { statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount() }
        updateLocalRankText(
            localRank,
            prefs.getString(Prefs.LAST_SHOWN_USER_LOCAL_RANK, null)?.let { Json.decodeFromString(it) },
            50,
            binding.localRankContainer,
            binding.localRankLabel,
            binding.localRankText
        )
        prefs.edit { putString(Prefs.LAST_SHOWN_USER_LOCAL_RANK, Json.encodeToString(localRank)) }

        val localRankCurrentWeek = withContext(Dispatchers.IO) { statisticsSource.getCurrentWeekCountryStatisticsOfCountryWithBiggestSolvedCount() }
        updateLocalRankText(
            localRankCurrentWeek,
            prefs.getString(Prefs.LAST_SHOWN_USER_LOCAL_RANK_CURRENT_WEEK, null)?.let { Json.decodeFromString(it) },
            5,
            binding.currentWeekLocalRankContainer,
            binding.currentWeekLocalRankLabel,
            binding.currentWeekLocalRankText
        )
        prefs.edit { putString(Prefs.LAST_SHOWN_USER_LOCAL_RANK_CURRENT_WEEK, Json.encodeToString(localRankCurrentWeek)) }
    }

    private fun updateLocalRankText(
        statistics: CountryStatistics?,
        previousStatistics: CountryStatistics?,
        min: Int,
        container: View,
        label: TextView,
        circle: TextView
    ) {
        val rank = statistics?.rank ?: 0
        val shouldShow = statistics != null && rank > 0 && statistics.count > min
        container.isGone = !shouldShow
        if (!shouldShow) return

        val countryLocale = Locale("", statistics?.countryCode ?: "")
        label.text = getString(R.string.user_profile_local_rank, countryLocale.displayCountry)

        val updateRank = { r: Int ->
            circle.text = "#$r"
            circle.background.level = getScaledLocalRank(r)
        }
        if (statistics?.countryCode != previousStatistics?.countryCode ||
            previousStatistics?.rank == null || rank > previousStatistics.rank) {
            updateRank(rank)
        } else {
            animate(previousStatistics.rank, rank, container, updateRank)
        }
    }

    private suspend fun updateAchievementLevelsText() {
        val levels = withContext(Dispatchers.IO) { achievementsSource.getAchievements().sumOf { it.second } }
        binding.achievementLevelsContainer.isGone = levels <= 0
        binding.achievementLevelsText.text = levels.toString()
        binding.achievementLevelsText.background.level = min(levels / 2, 100) * 100
    }

    private fun animate(previous: Int, now: Int, view: View, block: (value: Int) -> Unit) {
        block(previous)
        val anim = ValueAnimator.ofInt(previous, now)
        anim.duration = 3000
        anim.addUpdateListener { block(it.animatedValue as Int) }
        val p = view.getLocationInWindow()
        anim.startDelay = max(0, view.context.pxToDp(p.y).toLong() * 12 - 2000)
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()
        animations.add(anim)
    }
}
