package de.westnordost.streetcomplete.util.dialogs

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.util.ktx.dpToPx

// using setView fills the entire AlertDialog, while setMessage or set*Items add some padding
// this adds same/similar padding to setView
fun AlertDialog.Builder.setViewWithDefaultPadding(v: View): AlertDialog.Builder {
    v.setDefaultDialogPadding()
    return setView(v)
}

fun View.setDefaultDialogPadding() {
    val padding = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        getDimensionFromAttribute(context, android.R.attr.dialogPreferredPadding)
    } else {
        context.resources.dpToPx(20).toInt()
    }
    // no source for /3, but it looks ok
    setPadding(padding, padding / 3, padding, padding / 3)
}

private fun getDimensionFromAttribute(context: Context, attr: Int): Int {
    val typedValue = TypedValue()
    return if (context.theme.resolveAttribute(attr, typedValue, true))
        TypedValue.complexToDimensionPixelSize(typedValue.data, context.resources.displayMetrics)
    else 0
}
