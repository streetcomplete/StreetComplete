package de.westnordost.streetcomplete.screens.user.profile

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.statistics.CountryStatistics
import de.westnordost.streetcomplete.databinding.FragmentProfileBinding
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.openUri
import de.westnordost.streetcomplete.util.ktx.pxToDp
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.LaurelWreathDrawable
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import org.koin.androidx.viewmodel.ext.android.viewModel

/** Shows the user profile: username, avatar, star count and a hint regarding unpublished changes */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var anonAvatar: Bitmap

    private val animations = ArrayList<Animator>()

    private val viewModel by viewModel<ProfileViewModel>()
    private val binding by viewBinding(FragmentProfileBinding::bind)

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

        binding.logoutButton.setOnClickListener { viewModel.logOutUser() }
        binding.profileButton.setOnClickListener {
            openUri("https://www.openstreetmap.org/user/" + viewModel.userName.value)
        }

        observe(viewModel.userName) { name ->
            binding.userNameTextView.text = name
        }
        observe(viewModel.userAvatarFile) { file ->
            val avatar = if (file.exists()) BitmapFactory.decodeFile(file.path) else anonAvatar
            binding.userAvatarImageView.setImageBitmap(avatar)
        }
        observe(viewModel.editCount) { count ->
            binding.editCountText.text = count.toString()
        }
        observe(viewModel.editCountCurrentWeek) { count ->
            binding.currentWeekEditCountText.text = count.toString()
        }
        observe(viewModel.achievementLevels) { levels ->
            binding.achievementLevelsContainer.isGone = levels <= 0
            binding.achievementLevelsText.text = levels.toString()
            binding.achievementLevelsText.background.level = min(levels / 2, 100) * 100
        }
        observe(viewModel.unsyncedChangesCount) { count ->
            binding.unpublishedEditCountText.text = getString(R.string.unsynced_quests_description, count)
            binding.unpublishedEditCountText.isGone = count <= 0
        }
        observe(viewModel.datesActive) { (datesActive, range) ->
            val context = requireContext()
            binding.datesActiveView.setImageDrawable(DatesActiveDrawable(
                datesActive.toSet(),
                range,
                context.dpToPx(18),
                context.dpToPx(2),
                context.dpToPx(4),
                context.resources.getColor(R.color.hint_text)
            ))
        }
        observe(viewModel.daysActive) { daysActive ->
            binding.daysActiveContainer.isGone = daysActive <= 0
            binding.daysActiveText.text = daysActive.toString()
            binding.daysActiveText.background.level = min(daysActive + 20, 100) * 100
        }
        observe(viewModel.rank) { rank ->
            updateRank(rank, viewModel.editCount.value)
        }
        observe(viewModel.rankCurrentWeek) { rank ->
            updateRankCurrentWeek(rank, viewModel.editCountCurrentWeek.value)
        }
        observe(viewModel.biggestSolvedCountCountryStatistics) { statistics ->
            updateLocalRank(statistics)
        }
        observe(viewModel.biggestSolvedCountCurrentWeekCountryStatistics) { statistics ->
            updateLocalRankCurrentWeek(statistics)
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
        animations.forEach { it.end() }
        animations.clear()
    }


    private fun updateRank(rank: Int, editCount: Int) {
        val showRank = rank > 0 && editCount > 100
        binding.globalRankContainer.isGone = !showRank
        if (showRank) {
            updateRank(
                rank,
                viewModel.lastShownGlobalUserRank,
                ::getScaledGlobalRank,
                binding.globalRankText
            )
            viewModel.lastShownGlobalUserRank = rank
        }
    }

    private fun updateRankCurrentWeek(rank: Int, editCount: Int) {
        val showRank = rank > 0 && editCount > 100
        binding.currentWeekGlobalRankContainer.isGone = !showRank
        if (showRank) {
            updateRank(
                rank,
                viewModel.lastShownGlobalUserRankCurrentWeek,
                ::getScaledGlobalRank,
                binding.currentWeekGlobalRankText
            )
        }
        viewModel.lastShownGlobalUserRankCurrentWeek = rank
    }

    private fun updateLocalRank(statistics: CountryStatistics?) {
        val showRank = statistics?.rank != null && statistics.count > 50
        binding.localRankContainer.isGone = !showRank
        if (showRank) {
            updateRank(
                statistics?.rank ?: 0,
                viewModel.lastShownLocalUserRank?.rank,
                ::getScaledLocalRank,
                binding.localRankText
            )
            viewModel.lastShownLocalUserRank = statistics
            binding.localRankLabel.text = getLocalRankText(statistics?.countryCode)
        }
    }

    private fun updateLocalRankCurrentWeek(statistics: CountryStatistics?) {
        val showRank = statistics?.rank != null && statistics.count > 5
        binding.currentWeekLocalRankContainer.isGone = !showRank
        if (showRank) {
            updateRank(
                statistics?.rank ?: 0,
                viewModel.lastShownLocalUserRankCurrentWeek?.rank,
                ::getScaledLocalRank,
                binding.currentWeekLocalRankText
            )
            viewModel.lastShownLocalUserRankCurrentWeek = statistics
            binding.currentWeekLocalRankLabel.text = getLocalRankText(statistics?.countryCode)
        }
    }

    private fun getLocalRankText(countryCode: String?): String =
        getString(R.string.user_profile_local_rank, Locale("", countryCode ?: "").displayCountry)

    private fun updateRank(
        rank: Int,
        previousRank: Int?,
        getLevel: (rank: Int) -> Int,
        circle: TextView
    ) {
        val updateRank = { r: Int ->
            circle.text = "#$r"
            circle.background.level = getLevel(r)
        }

        if (previousRank == null || previousRank < rank) {
            updateRank(rank)
        } else {
            animate(previousRank, rank, circle, updateRank)
        }
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
