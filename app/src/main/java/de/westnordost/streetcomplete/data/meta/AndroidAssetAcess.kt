package de.westnordost.streetcomplete.data.meta

import android.content.res.AssetManager

class AndroidAssetAcess(private val assetManager: AssetManager) : AssetAccess {
    override fun list(basepath: String): Array<out String>? {
        return assetManager.list(basepath)
    }

    override fun open(s: String): java.io.InputStream {
        return assetManager.open(s)
    }
}
