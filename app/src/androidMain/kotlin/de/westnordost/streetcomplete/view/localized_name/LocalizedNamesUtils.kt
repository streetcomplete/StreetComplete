package de.westnordost.streetcomplete.view.localized_name

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.R

fun confirmNoName(context: Context, onConfirmed: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle(R.string.quest_name_answer_noName_confirmation_title)
        .setPositiveButton(R.string.quest_name_noName_confirmation_positive) { _, _ -> onConfirmed() }
        .setNegativeButton(R.string.quest_generic_confirmation_no, null)
        .show()
}

fun showKeyboardInfo(context: Context) {
    AlertDialog.Builder(context)
        .setTitle(R.string.quest_streetName_cantType_title)
        .setMessage(R.string.quest_streetName_cantType_description)
        .setPositiveButton(R.string.quest_streetName_cantType_open_settings) { _, _ ->
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        .setNeutralButton(R.string.quest_streetName_cantType_open_store) { _, _ ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MARKET)
            context.startActivity(intent)
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}
