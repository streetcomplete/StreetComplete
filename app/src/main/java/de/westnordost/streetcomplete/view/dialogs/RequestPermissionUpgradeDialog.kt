package de.westnordost.streetcomplete.view.dialogs

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.user.UserLoginStatusController
import de.westnordost.streetcomplete.screens.user.UserActivity

/** Shows a dialog that asks the user to login */
class RequestPermissionUpgradeDialog(
    context: Context,
    private val userLoginStatusController: UserLoginStatusController,
) : AlertDialog(context, R.style.Theme_Bubble_Dialog) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_permission_upgrade, null, false)
        setView(view)
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok)) { _, _ ->
            if (userLoginStatusController.isLoggedIn) {
                userLoginStatusController.logOut()
            }
            val intent = Intent(context, UserActivity::class.java)
            intent.putExtra(UserActivity.EXTRA_LAUNCH_AUTH, true)
            context.startActivity(intent)
        }
        setButton(BUTTON_NEGATIVE, context.getString(R.string.later)) { _, _ -> }
    }
}
