package de.westnordost.streetcomplete.ktx

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

fun Context.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

fun Context.showHint(@StringRes messageId: Int) {
    AlertDialog.Builder(this)
            .setMessage(messageId)
            .setPositiveButton(android.R.string.ok, null)
            .show()
}
