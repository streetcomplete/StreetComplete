package de.westnordost.streetcomplete.user

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.graphics.withRotation
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.data.user.UserUpdater
import de.westnordost.streetcomplete.data.user.achievements.AchievementsSource
import de.westnordost.streetcomplete.data.user.statistics.StatisticsSource
import de.westnordost.streetcomplete.databinding.FragmentProfileBinding
import de.westnordost.streetcomplete.ktx.createBitmap
import de.westnordost.streetcomplete.ktx.flipHorizontally
import de.westnordost.streetcomplete.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.ktx.tryStartActivity
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import java.io.File
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
        override fun onIncreased() { viewLifecycleScope.launch { updateUnpublishedQuestsText() } }
        override fun onDecreased() { viewLifecycleScope.launch { updateUnpublishedQuestsText() } }
    }
    private val questStatisticsDaoListener = object : StatisticsSource.Listener {
        override fun onAddedOne(questType: QuestType<*>) {
            viewLifecycleScope.launch { updateSolvedQuestsText() }
        }
        override fun onSubtractedOne(questType: QuestType<*>) {
            viewLifecycleScope.launch { updateSolvedQuestsText() }
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
            statisticsSource.addListener(questStatisticsDaoListener)
            unsyncedChangesCountSource.addListener(unsyncedChangesCountListener)

            updateUserName()
            updateAvatar()
            updateSolvedQuestsText()
            updateUnpublishedQuestsText()
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
        statisticsSource.removeListener(questStatisticsDaoListener)
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
        updateSolvedQuestsText()
        updateDaysActiveText()
        updateGlobalRankText()
        updateLocalRankText()
    }

    private suspend fun updateSolvedQuestsText() {
        binding.solvedQuestsText.text = withContext(Dispatchers.IO) { statisticsSource.getSolvedCount().toString() }
    }

    private suspend fun updateUnpublishedQuestsText() {
        val unsyncedChanges = unsyncedChangesCountSource.getCount()
        binding.unpublishedQuestsText.text = getString(R.string.unsynced_quests_description, unsyncedChanges)
        binding.unpublishedQuestsText.isGone = unsyncedChanges <= 0
    }

    private fun updateDaysActiveText() {
        val daysActive = statisticsSource.daysActive
        binding.daysActiveContainer.isGone = daysActive <= 0
        binding.daysActiveText.text = daysActive.toString()
    }

    /*
    100 and more: fully grown wreath with all pretty elements
    99 to 1: may be losing elements as it gets smaller
    0 and lower: no decorative styling at all
     */
    class LaurelWreath(val resources: Resources, private val percentageOfGrowth: Int) : Drawable() {
        val laurelLeafOnStalk = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_rotated)
        val laurelStalk = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_stalk)
        val horizontalEndingLeaf = resources.getBitmapDrawable(R.drawable.ic_laurel_leaf_ending)

        private val redPaint: Paint = Paint().apply { setARGB(255, 255, 0, 0) }
        private val niceSubtleGreen: Paint = Paint().apply { setARGB(255, 186, 209, 154) }
        private val yellowPaint: Paint = Paint().apply { setARGB(255, 255, 255, 0) }
        private val orangePaint: Paint = Paint().apply { setARGB(255, 255, 155, 0) }
        private val bluePaint: Paint = Paint().apply { setARGB(255, 0, 0, 255) }

        private val antiAliasPaint: Paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }

        override fun draw(canvas: Canvas) {
            // Get the drawable's bounds
            val width: Int = bounds.width()
            val height: Int = bounds.height()
            val radius: Float = Math.min(width, height).toFloat() / 2f

            // Draw a red circle in the center
            canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, niceSubtleGreen)

            val imageWith = laurelLeafOnStalk.intrinsicWidth //width is the same as intrinsicWidth
            val imageHeight = laurelLeafOnStalk.intrinsicHeight //width is the same as intrinsicWidth

            val imageWithEnding = horizontalEndingLeaf.intrinsicWidth //width is the same as intrinsicWidth
            val imageHeightEnding = horizontalEndingLeaf.intrinsicHeight //width is the same as intrinsicWidth
            //TODO - ending image should have the same size as a regular one

            val n = 11f

            val offset = width / 2f
            val imageInternalOffset = imageWith/2f // drawBitmap takes lower-upper corner of bitmap, we care about bitmap center
            val endingImageInternalOffset = imageWith/2f // drawBitmap takes lower-upper corner of bitmap, we care about bitmap center
            val reach = ((n-1)*percentageOfGrowth/100).toInt()

            for(i in 0..reach) {
                // https://web.archive.org/web/20210201203811/https://stackoverflow.com/questions/36493977/flip-a-bitmap-image-horizontally-or-vertically
                val offsetFromBorder = height/12f
                // https://developer.android.com/reference/kotlin/androidx/core/graphics/package-summary#(android.graphics.Canvas).withRotation(kotlin.Float,kotlin.Float,kotlin.Float,kotlin.Function1)

                var bitmap = laurelLeafOnStalk.bitmap
                if(i == reach) {
                    bitmap = horizontalEndingLeaf.bitmap
                } else if (i == 0 ) {
                    bitmap = laurelStalk.bitmap
                }

                val flippedBitmap = bitmap.flipHorizontally()
                // left side
                canvas.withRotation(i * 180.0f/n, width / 2f, height / 2f) {
                    canvas.drawBitmap(bitmap, offset - endingImageInternalOffset, height*0.78f, antiAliasPaint)
                }

                canvas.withRotation(-i * 180.0f/n, width / 2f, height / 2f) {
                    canvas.drawBitmap(flippedBitmap, offset - endingImageInternalOffset, height*0.78f, antiAliasPaint)
                }
            }
            //val smallCircleRadius = width /10f
            //canvas.drawCircle(smallCircleRadius * 2, height - smallCircleRadius * 2, smallCircleRadius, bluePaint)
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
        binding.globalRankContainer.isGone = rank <= 0 || statisticsSource.getSolvedCount() <= 100
        binding.globalRankText.text = "#$rank"
        binding.globalRankText.background = LaurelWreath(resources,1001 - rank)
    }

    private fun updatePlaceholderRanksTexts() {
        binding.placeholder1Text.text = "100%"
        binding.placeholder1Text.background = LaurelWreath(resources, 100)

        binding.placeholder2Text.text = "90%"
        binding.placeholder2Text.background = LaurelWreath(resources, 90)

        binding.placeholder3Text.text = "50%"
        binding.placeholder3Text.background = LaurelWreath(resources, 50)

        binding.placeholder4Text.text = "30%"
        binding.placeholder4Text.background = LaurelWreath(resources, 30)

        binding.placeholder5Text.text = "10%"
        binding.placeholder5Text.background = LaurelWreath(resources, 10)

        binding.placeholder6Text.text = "0%"
        binding.placeholder6Text.background = LaurelWreath(resources, 0)
    }

    private suspend fun updateLocalRankText() {
        val statistics = withContext(Dispatchers.IO) {
            statisticsSource.getCountryStatisticsOfCountryWithBiggestSolvedCount()
        }
        if (statistics == null) binding.localRankContainer.isGone = true
        else {
            val shouldShow = statistics.rank != null && statistics.rank > 0 && statistics.solvedCount > 50
            val countryLocale = Locale("", statistics.countryCode)
            binding.localRankContainer.isGone = !shouldShow
            binding.localRankText.text = "#${statistics.rank}"
            binding.localRankLabel.text = getString(R.string.user_profile_local_rank, countryLocale.displayCountry)
        }
    }

    private suspend fun updateAchievementLevelsText() {
        val levels = withContext(Dispatchers.IO) { achievementsSource.getAchievements().sumOf { it.second } }
        binding.achievementLevelsContainer.isGone = levels <= 0
        binding.achievementLevelsText.text = "$levels"
    }

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        return tryStartActivity(intent)
    }
}
