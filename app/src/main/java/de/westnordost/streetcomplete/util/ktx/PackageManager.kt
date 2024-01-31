package de.westnordost.streetcomplete.util.ktx

import android.content.pm.PackageManager

fun PackageManager.isPackageInstalled(packageName: String): Boolean =
    try {
        getPackageInfo(packageName, 0) != null
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
