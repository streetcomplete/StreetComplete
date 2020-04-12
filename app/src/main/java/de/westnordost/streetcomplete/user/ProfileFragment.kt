package de.westnordost.streetcomplete.user

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesModule
import de.westnordost.streetcomplete.data.quest.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.QuestStatisticsDao
import de.westnordost.streetcomplete.data.user.UserController
import de.westnordost.streetcomplete.data.user.achievements.AchievementsModule
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsDao
import de.westnordost.streetcomplete.data.user.achievements.UserLinksDao
import de.westnordost.streetcomplete.util.BitmapUtil
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import javax.inject.Inject

/** Shows the user profile: username, avatar, star count and a hint regarding unpublished changes */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    @Inject internal lateinit var userController: UserController
    @Inject internal lateinit var questStatisticsDao: QuestStatisticsDao
    @Inject internal lateinit var userAchievementDao: UserAchievementsDao
    @Inject internal lateinit var userLinksDao: UserLinksDao
    @Inject internal lateinit var unsyncedChangesCountSource: UnsyncedChangesCountSource

    private lateinit var anonAvatar: Bitmap

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        anonAvatar = BitmapUtil.createBitmapFrom(resources.getDrawable(R.drawable.ic_osm_anon_avatar))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO remove before release
        testDataButton.setOnClickListener {
            for (achievement in AchievementsModule.achievements()) {
                userAchievementDao.put(achievement.id, 1)
            }
            for (link in AchievementsModule.links()) {
                userLinksDao.add(link.id)
            }
            questStatisticsDao.replaceAll(mapOf(
                "AddRoadName" to 12,
                "AddBusStopName" to 38,
                "MarkCompletedHighwayConstruction" to 1,
                "AddCycleway" to 21,
                "AddParkingFee" to 4,
                "AddTactilePavingCrosswalk" to 8,
                "AddRoofShape" to 17,
                "AddCyclewaySegregation" to 4,
                "AddSelfServiceLaundry" to 1,
                "AddWheelchairAccessBusiness" to 121
            ))
        }

        logoutButton.setOnClickListener {
            userController.logOut()
        }
        profileButton.setOnClickListener {
            openUrl("https://www.openstreetmap.org/user/" + userController.userName)
        }
    }

    override fun onStart() {
        super.onStart()
        userNameTextView.text = userController.userName
        val cacheDir = OsmNotesModule.getAvatarsCacheDirectory(requireContext())
        val avatarFile = File(cacheDir.toString() + File.separator + userController.userId)
        val avatar = if (avatarFile.exists()) BitmapFactory.decodeFile(avatarFile.path) else anonAvatar
        userAvatarImageView.setImageBitmap(avatar)

        solvedQuestsText.text = questStatisticsDao.getTotalAmount().toString()

        val unsyncedChanges = unsyncedChangesCountSource.count
        unpublishedQuestsText.text = getString(R.string.unsynced_quests_description, unsyncedChanges)
        unpublishedQuestsText.visibility = if (unsyncedChanges > 0) View.VISIBLE else View.GONE
    }

    private fun openUrl(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        return tryStartActivity(intent)
    }

    private fun tryStartActivity(intent: Intent): Boolean {
        return try {
            startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
