package de.westnordost.streetcomplete.screens.user.profile

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.graphics.withRotation
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
import de.westnordost.streetcomplete.ktx.flipHorizontally
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.tryStartActivity
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.io.File
import java.lang.Math.max
import java.lang.Math.min
import java.util.Locale
import kotlin.random.Random

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
            updatePlaceholderRanksTexts()
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

    /*
    Drawable providing decoration, suitable for a circular background
    100 and more: fully grown wreath with all pretty elements
    99 to 10: may be losing elements as it gets smaller
    below: no decorative styling at all
     */
    class LaurelWreath(val resources: Resources, private val percentageOfGrowth: Int) : Drawable() {
        val laurelLeafOnStalk = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_rotated)
        val laurelStalk = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_stalk)
        val horizontalEndingLeaf = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_ending)

        private val niceSubtleGreen: Paint = Paint().apply { setARGB(255, 152, 184, 126) }

        private val antiAliasPaint: Paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        override fun draw(canvas: Canvas) {
            // Get the drawable's bounds
            val width: Int = bounds.width()
            val height: Int = bounds.height()
            val radius: Float = Math.min(width, height).toFloat() / 2f

            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, niceSubtleGreen)

            if (percentageOfGrowth < 10) {
                return
            }

            val imageWidth = laurelLeafOnStalk.intrinsicWidth // width is the same as intrinsicWidth
            val imageHeight = laurelLeafOnStalk.intrinsicHeight // width is the same as intrinsicWidth

            val n = 11f

            val offset = width / 2f
            val endingImageInternalOffset = imageWidth / 2f // drawBitmap takes lower-upper corner of bitmap, we care about bitmap center
            val reach = ((n - 1) * percentageOfGrowth / 100).toInt()

            for (i in 0..reach) {
                // https://developer.android.com/reference/kotlin/androidx/core/graphics/package-summary#(android.graphics.Canvas).withRotation(kotlin.Float,kotlin.Float,kotlin.Float,kotlin.Function1)

                var bitmap = laurelLeafOnStalk.bitmap
                if (i == reach) {
                    bitmap = horizontalEndingLeaf.bitmap
                } else if (i == 0 ) {
                    bitmap = laurelStalk.bitmap
                }

                val flippedBitmap = bitmap.flipHorizontally()
                // left side
                canvas.withRotation(i * 180.0f / n, width / 2f, height / 2f) {
                    canvas.drawBitmap(bitmap, offset - endingImageInternalOffset, height * 0.78f, antiAliasPaint)
                }

                // right side
                canvas.withRotation(-i * 180.0f / n, width / 2f, height / 2f) {
                    canvas.drawBitmap(flippedBitmap, offset - endingImageInternalOffset, height * 0.78f, antiAliasPaint)
                }
            }
        }

        override fun setAlpha(alpha: Int) {
            // This method is required
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            // This method is required
        }

        override fun getOpacity(): Int =
            // Must be PixelFormat.UNKNOWN, TRANSLUCENT, TRANSPARENT, or OPAQUE
            PixelFormat.OPAQUE
    }

    private fun updateGlobalRankText() {
        val rank = statisticsSource.rank
        binding.globalRankContainer.isGone = rank <= 0 || statisticsSource.getEditCount() <= 100
        binding.globalRankText.text = "#$rank"
        val scMapperCountIn2022 = 45000
        val rankEnoughForFullMarks = 1000
        val rankEnoughToStartGrowingReward = 8000 // TODO: tweak this based on actual numbers!
        val ranksAboveThreshold = max(rankEnoughToStartGrowingReward - rank, 0)
        val scaledRank = (ranksAboveThreshold * 100.0 / (rankEnoughToStartGrowingReward - rankEnoughForFullMarks)).toInt()
        binding.globalRankText.background = LaurelWreath(resources, min(scaledRank, 100))
    }

    private fun updatePlaceholderRanksTexts() {
        var percent: Int
        percent = 100
        binding.placeholder1Text.text = "$percent%"
        binding.placeholder1Text.background = LaurelWreath(resources, percent)

        percent = 90 + Random.nextInt(10)
        binding.placeholder2Text.text = "$percent%"
        binding.placeholder2Text.background = LaurelWreath(resources, percent)

        percent = 80 + Random.nextInt(10)
        binding.placeholder3Text.text = "$percent%"
        binding.placeholder3Text.background = LaurelWreath(resources, percent)

        percent = 70 + Random.nextInt(10)
        binding.placeholder4Text.text = "$percent%"
        binding.placeholder4Text.background = LaurelWreath(resources, percent)

        percent = 60 + Random.nextInt(10)
        binding.placeholder5Text.text = "$percent%"
        binding.placeholder5Text.background = LaurelWreath(resources, percent)

        percent = 50 + Random.nextInt(10)
        binding.placeholder6Text.text = "$percent%"
        binding.placeholder6Text.background = LaurelWreath(resources, percent)

        percent = 40 + Random.nextInt(10)
        binding.placeholder7Text.text = "$percent%"
        binding.placeholder7Text.background = LaurelWreath(resources, percent)

        percent = 30 + Random.nextInt(10)
        binding.placeholder8Text.text = "$percent%"
        binding.placeholder8Text.background = LaurelWreath(resources, percent)

        percent = 20 + Random.nextInt(10)
        binding.placeholder9Text.text = "$percent%"
        binding.placeholder9Text.background = LaurelWreath(resources, percent)

        percent = 10 + Random.nextInt(10)
        binding.placeholder10Text.text = "$percent%"
        binding.placeholder10Text.background = LaurelWreath(resources, percent)

        percent = 0 + Random.nextInt(10)
        binding.placeholder11Text.text = "$percent%"
        binding.placeholder11Text.background = LaurelWreath(resources, percent)
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
